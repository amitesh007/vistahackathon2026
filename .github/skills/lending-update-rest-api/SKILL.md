---
name: lending-update-rest-api
description: >
  Skill for generating a full Update REST API stack (Entity, Model, Repository, Service)
  for any LoanIQ business object from a requirement spreadsheet. Derives all class structure
  from the `Update` tab of the spreadsheet and mirrors the coding patterns found in the
  LoanService reference implementation.
---

# LoanIQ Update REST API — Full Stack Generation Skill

> **Purpose:** Generate the complete Update API stack — **Entity**, **Model**, **Repository**, and **Service** — for any LoanIQ business object, driven by the `Update` tab of a requirement spreadsheet.
>
> This skill does **NOT** copy existing code verbatim. It derives structure from the spreadsheet and applies the patterns established by the LoanService reference implementation.

---

## When to Use This Skill

Use `lending-update-rest-api` when:
- Scaffolding a **new** business object Update stack from a requirement spreadsheet
- Generating all four layers (Entity / Model / Repository / Service) for an update operation
- The spreadsheet contains an `Update` tab with INPUT field definitions and UPDATABLE column markers

DO NOT use this skill for:
- Create operations (use `lending-create-rest-api`)
- Query/GetByID operations (use `lending-query-rest-api`)
- Delete operations (use `lending-delete-rest-api`)
- Modifying existing classes already generated

---

## Scripts

Two PowerShell scripts are provided under `.github/skills/lending-update-rest-api/scripts/`.

> **Note:** `extract-spreadsheet.ps1` is shared with `lending-create-rest-api`.
> Reference the version at `.github/skills/lending-create-rest-api/scripts/extract-spreadsheet.ps1`.

| Script | Purpose |
|---|---|
| `extract-spreadsheet.ps1` | Unzips the `.xlsx` into raw XML files for parsing *(shared from lending-create-rest-api)* |
| `find-update-workflow-sheet.ps1` | Locates the `Update` tab (guardrail: exits code 2 if missing), extracts metadata, derives `EntityName` |
| `read-update-workflow-attributes.ps1` | Reads all INPUT fields (with UPDATABLE classification) and OUTPUT confirmation fields from the Update tab |

### Invocation sequence

```powershell
$createDir = ".github\skills\lending-create-rest-api\scripts"
$updateDir = ".github\skills\lending-update-rest-api\scripts"

# 1. Resolve spreadsheet from requirements folder; EntityName = xlsx filename (no extension)
$reqDir   = "vistahackathon26\requirements"
$xlsx     = Get-ChildItem $reqDir -Filter "*.xlsx" | Select-Object -First 1
$EntityName = [System.IO.Path]::GetFileNameWithoutExtension($xlsx.Name)

# 2. Extract the xlsx (uses shared script)
$extracted = & "$createDir\extract-spreadsheet.ps1" -SpreadsheetPath $xlsx.FullName

# 2. Locate Update tab + extract metadata  (exits with code 2 if no Update tab)
$meta = & "$updateDir\find-update-workflow-sheet.ps1" -ExtractedPath $extracted
if ($LASTEXITCODE -eq 2) {
    Write-Error "No 'Update' tab found - skill execution halted."
    exit
}

# 3. Read INPUT fields (with UPDATABLE classification) and OUTPUT fields
$result = & "$updateDir\read-update-workflow-attributes.ps1" `
               -ExtractedPath $extracted `
               -SheetFile $meta.SheetFile `
               -OutputJson "C:\Auto\API\update_attributes.json"

# Key derived sets
$identifierFields = $result.InputFields | Where-Object { $_.Updatable -eq 'N' -and $_.Required -eq 'Y' }
$updatableFields  = $result.InputFields | Where-Object { $_.Updatable -eq 'Y' }
$allEntityFields  = $result.InputFields  # all input fields become entity columns

Write-Host "EntityName         : $($meta.EntityName)"
Write-Host "Identifier field(s): $($identifierFields.Count)"
Write-Host "Updatable fields   : $($updatableFields.Count)"
Write-Host "Total entity cols  : $($allEntityFields.Count)"
```

---

## Inputs

| Input | Description |
|---|---|
| `ExcelFilePath` | Absolute path to the requirement spreadsheet (`.xlsx`) |

---

## Spreadsheet Layout — Update Workflow Tab

The `Update` tab follows the same two-zone pattern with an extra `UPDATABLE` column.

### Metadata zone (rows 1–9)

| Row | Column A (key) | Column B (value) | Usage |
|---|---|---|---|
| 5 | `FILE_OP_PATH` | e.g. `C:\REST_AUTO_FILE_GEN\principal_payment` | Output path hint |
| 7 | **`INTEGRATION_CLASS`** | e.g. `UpdateLoanPrincipalPaymentIntegration` | **className** |
| 8 | `RESPONSE_CLASS` | e.g. `LiqAPILoanPrincipalPaymentIntegrationAsReturnValue` | Response class |
| 9 | `PACKAGE_NAME` | e.g. `com.misys.liq.api.rest.data.outstanding.principal` | LoanIQ package |

### Input zone (all fields submitted with an Update request)

| Row | Content |
|---|---|
| Section marker | Column A = `Input` |
| Column headers | Column A = `SL_NO` |
| Data rows | All field definitions (identifier + updatable fields) |
| Stop marker | Column A = `OUTPUT` |

### Output zone (response confirmation — all system-meta)

| Row | Content |
|---|---|
| Section marker | Column A = `OUTPUT` |
| Column headers | Column A = `SL_NO` |
| Data rows | `Success`, `Message`, `loanTransactionId`, `updateTimeStamp` |
| Stop marker | End of rows |

**Key columns:**

| Column | Header | Used for |
|---|---|---|
| A | SL_NO | Serial number |
| B | CLASS_NAME | className value |
| O | ATTRIBUTE_FIELD_NAME | Java field name (camelCase) |
| Q | DATA_TYPE | `String` / `Boolean` / `LocalDate` / `LocalDateTime` |
| R | REQUIRED | `Y` = mandatory, `N` = optional |
| **U** | **UPDATABLE** | **`Y` = field is modified on update; `N` = identifier or read-only** |
| X | MAX_SIZE | Column length for `@Column(length = ...)` on String fields |
| Y | Default Value | Field default |

### UPDATABLE column classification

| UPDATABLE | REQUIRED | Meaning | Code generation role |
|---|---|---|---|
| `N` | `Y` | **Identifier** — primary key used to look up the record | `findById()` argument; set on entity but never changed |
| `Y` | any | **Updatable field** — patched on the found entity | Mapped in `basicExecute()` from model to entity |
| `N` | `N` | **Read-only / informational** | Included as entity column; model field available but not patched |

---

## Step 0 — Guardrail: Verify `Update` Tab Exists

**Before doing any work**, run `find-update-workflow-sheet.ps1`.
If it exits with code 2, the spreadsheet has no `Update` tab — **stop immediately**.

```text
IF find-update-workflow-sheet.ps1 exits with code 2:
    STOP immediately.
    Output: "No 'Update' tab found in spreadsheet. Skill execution halted."
    Do not generate any files.
```

Only proceed to Step 1 when the script returns a valid `$meta` object.

---

## Step 1 — Derive the Entity Name

The entity name is derived by `find-update-workflow-sheet.ps1` automatically.
It reads **row 7, column B** (`INTEGRATION_CLASS`) and applies:

```text
EntityName = INTEGRATION_CLASS
    → Remove leading prefix  "Update"
    → Remove trailing suffix "Integration"

Example:
  UpdateLoanPrincipalPaymentIntegration  →  LoanPrincipalPayment
```

The result is available as `$meta.EntityName` after running the script.

---

## Step 2 — Read Update Tab Attributes

Run `read-update-workflow-attributes.ps1`. It returns an object with:

| Property | Contents | Used for |
|---|---|---|
| `InputFields` | All update input fields with `Updatable` classification | Entity, model, validation, service logic |
| `OutputFields` | Confirmation response fields (all `IsSystemMeta=true`) | Not mapped — documentation only |

**Field subsets from `InputFields`:**

```powershell
# Primary key — used in findById()
$identifierFields = $result.InputFields | Where-Object { $_.Updatable -eq 'N' -and $_.Required -eq 'Y' }

# Fields that are actually modified on the entity
$updatableFields  = $result.InputFields | Where-Object { $_.Updatable -eq 'Y' }

# All fields → entity @Column definitions and model fields
$allEntityFields  = $result.InputFields
```

Each field object has:

| Property | Source column | Description |
|---|---|---|
| `FieldName` | O | Java field name (camelCase, trimmed) |
| `ColumnName` | Derived | DB column name (SCREAMING_SNAKE_CASE) |
| `DataType` | Q | `String` / `Boolean` / `LocalDate` / `LocalDateTime` |
| `Required` | R | `Y` = mandatory, `N` = optional |
| `Updatable` | U | `Y` = modifiable; `N` = identifier or read-only |
| `IsYNBoolean` | Derived | `$true` when DataType is `Boolean` |
| `MaxSize` | X | Integer column length; `-1` if not specified |
| `DefaultValue` | Y | Default value string |

Treat `createTimeStamp` and `updateTimeStamp` as **system-managed** — always include them in the entity with `@PrePersist` / `@PreUpdate` lifecycle hooks. Never accept them from the request payload.

---

## Step 3 — Generate Entity Class

**Class name:** `{EntityName}`
**Full path:** `LoanService/src/main/java/com/loanservice/entity/{EntityName}.java`

### Rules

1. Annotate with `@Entity` and `@Table(name = "SCREAMING_SNAKE_CASE_OF_ENTITY_NAME")`.
2. The primary key (`@Id`) is the `InputField` where `Updatable=N` and `Required=Y`. Mark it `@Column(name = "...", length = 24)`.
3. Map **all** `InputFields` as `@Column` entries (both updatable and read-only fields become entity columns).
   - String fields: include `length` when `MaxSize > 0`.
   - Boolean Y/N fields (`IsYNBoolean = true`): add `@JsonSerialize(using = YNBooleanSerializer.class)` and default to `Boolean.FALSE`.
4. Add `createTimeStamp` (`@Column(name = "CREATE_TIMESTAMP", updatable = false)`) and `updateTimeStamp` (`@Column(name = "UPDATE_TIMESTAMP")`), both `LocalDateTime`.
5. Add `@PrePersist protected void onCreate()` and `@PreUpdate protected void onUpdate()`.
6. Generate standard getter/setter for every field (timestamps: getter only).

### Required Imports

```java
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.loanservice.model.YNBooleanSerializer;
```

### Reference Implementation

`LoanService/src/main/java/com/loanservice/entity/LoanPrincipalPayment.java`

---

## Step 4 — Generate Model Class

**Class name:** `{EntityName}Model`
**Full path:** `LoanService/src/main/java/com/loanservice/model/{EntityName}Model.java`

### Rules

1. Annotate with `@JsonInclude(JsonInclude.Include.NON_NULL)`.
2. First two fields are always `className` and `transaction`.
3. Map all `InputFields` — both the identifier and all updatable/optional fields.
4. Each field gets `@JsonProperty("fieldName")`.
5. Boolean Y/N fields get `@JsonDeserialize(using = YNBooleanDeserializer.class)`.
6. Do **not** include `createTimeStamp` / `updateTimeStamp` — those are entity-only system fields.
7. Do **not** include `Success`, `Message`, `updateTimeStamp` from the OUTPUT section.
8. Generate standard getter/setter for every field.

### Required Imports

```java
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
```

### Reference Implementation

`LoanService/src/main/java/com/loanservice/model/LoanRequest.java`

---

## Step 5 — Generate Repository Classes

Three files under `LoanService/src/main/java/com/loanservice/repository/`:

### 5a — Primary Repository Interface

**Class name:** `{EntityName}Repository`

```text
- @Repository
- extends JpaRepository<{EntityName}, String>
- extends {EntityName}RepositoryCustom
- declare: void deleteBy{PrimaryKeyField}(String id);
```

### 5b — Custom Repository Fragment Interface

**Class name:** `{EntityName}RepositoryCustom`

```text
- Plain interface (no annotations)
- declare: {EntityName} create({EntityName} entity);
```

### 5c — Custom Repository Implementation

**Class name:** `{EntityName}RepositoryImpl`

```text
- @Repository
- implements {EntityName}RepositoryCustom
- @PersistenceContext EntityManager entityManager
- @Transactional create(): entityManager.persist(entity); return entity;
```

### Reference Implementations

- `LoanService/src/main/java/com/loanservice/repository/LoanPrincipalPaymentRepository.java`
- `LoanService/src/main/java/com/loanservice/repository/LoanPrincipalPaymentRepositoryCustom.java`
- `LoanService/src/main/java/com/loanservice/repository/LoanPrincipalPaymentRepositoryImpl.java`

---

## Step 6 — Generate Service Class

**Class name:** `Update{EntityName}Integration`
**Full path:** `LoanService/src/main/java/com/loanservice/service/Update{EntityName}Integration.java`

### Rules

1. Annotate with `@Service("Update{EntityName}Integration")` — bean name must exactly match `INTEGRATION_CLASS`.
2. Extend `BaseIntegrationService`.
3. Inject `{EntityName}Repository` via `@Autowired`.
4. Override `basicValidation({EntityName}Model request)`:
   - `assertNotBlank` for every INPUT field where `Required = Y`.
   - For mandatory non-String fields (e.g. `LocalDate`): explicit null check with `IllegalArgumentException`.
   - `assertMaxLength` for every String field where `MaxSize > 0`.
5. Override `basicExecute({EntityName}Model request)`:
   - Call `repository.findById(request.get{PrimaryKeyField}())` to load the existing record.
   - If not found use `orElse(new {EntityName}())` — upsert pattern.
   - Apply the identifier field to the entity (`entity.set{PrimaryKeyField}(...)`).
   - Map **only the UPDATABLE fields** (`Updatable=Y`) from model to entity.
   - For Boolean Y/N fields apply null-safe default: `request.getField() != null ? request.getField() : Boolean.FALSE`.
   - If `existing.isPresent()`: call `repository.save(entity)`.
   - If new record: call `repository.create(entity)`.
   - Return the persisted entity.

### Required Imports

```java
import com.loanservice.entity.{EntityName};
import com.loanservice.model.{EntityName}Model;
import com.loanservice.repository.{EntityName}Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
```

### Reference Implementation

`LoanService/src/main/java/com/loanservice/service/UpdateLoanPrincipalPaymentIntegration.java`

---

## Step 7 — Generate Sample Request JSON

**File:** `update-{EntityName}.json`
**Path:** `vistahackathon26/LoanService/requirements/update-{EntityName}.json`

After all Java classes are generated, produce a sample JSON request payload that can be used directly in Postman or curl to test the Update endpoint.

### Rules

1. The JSON object must include `className` and `transaction` as the first two fields.
2. `className` value must match the `INTEGRATION_CLASS` from the spreadsheet (e.g. `"UpdateLoanPrincipalPaymentIntegration"`).
3. `transaction` value is a placeholder string: `"TXN-SAMPLE-001"`.
4. Include all INPUT fields from the spreadsheet, grouped by classification:
   - **IDENTIFIER fields** (`Updatable=N, Required=Y`) — include with a realistic lookup key value.
   - **UPDATABLE fields** (`Updatable=Y`) — include with realistic new values.
   - **READ-ONLY fields** (`Updatable=N, Required=N`) — may be omitted or included as `null`.
5. Field type formatting: `String` → realistic string; `Boolean` (Y/N) → `"Y"` or `"N"`; `LocalDate` → `"2025-01-15"`; `LocalDateTime` → `"2025-01-15T10:30:00"`.
6. Do NOT include `updateTimeStamp` — this is server-managed.
7. If the target file `update-{EntityName}.json` already exists → skip and report `⚠️ SKIPPED (already exists)`.

### Example output

```json
{
  "className": "Update{EntityName}Integration",
  "transaction": "TXN-SAMPLE-001",
  "loanTransactionId": "LN-2025-001",
  "amount": "75000.00",
  "isWaived": "Y"
}
```

> **STRICT RULE: Never overwrite existing files.**
> Before writing any file, check whether it already exists at the target path.
> If it exists → **STOP for that file**, report it as `⚠️ SKIPPED (already exists)`, and continue with the remaining files.
> Never merge, patch, or replace existing classes.

| Layer | Package | Path |
|---|---|---|
| Entity | `com.loanservice.entity` | `vistahackathon26/LoanService/src/main/java/com/loanservice/entity/{EntityName}.java` |
| Model | `com.loanservice.model` | `vistahackathon26/LoanService/src/main/java/com/loanservice/model/{EntityName}Model.java` |
| Repository (interface) | `com.loanservice.repository` | `vistahackathon26/LoanService/src/main/java/com/loanservice/repository/{EntityName}Repository.java` |
| Repository (custom interface) | `com.loanservice.repository` | `vistahackathon26/LoanService/src/main/java/com/loanservice/repository/{EntityName}RepositoryCustom.java` |
| Repository (impl) | `com.loanservice.repository` | `vistahackathon26/LoanService/src/main/java/com/loanservice/repository/{EntityName}RepositoryImpl.java` |
| Service | `com.loanservice.service` | `vistahackathon26/LoanService/src/main/java/com/loanservice/service/Update{EntityName}Integration.java` |
| Sample JSON | `vistahackathon26/LoanService/requirements/` | `vistahackathon26/LoanService/requirements/update-{EntityName}.json` |

---

## Naming Conventions Summary

| Artefact | Naming Pattern | Example |
|---|---|---|
| Entity class | `{EntityName}` | `LoanPrincipalPayment` |
| Model class | `{EntityName}Model` | `LoanPrincipalPaymentModel` |
| Repository interface | `{EntityName}Repository` | `LoanPrincipalPaymentRepository` |
| Custom repository interface | `{EntityName}RepositoryCustom` | `LoanPrincipalPaymentRepositoryCustom` |
| Repository implementation | `{EntityName}RepositoryImpl` | `LoanPrincipalPaymentRepositoryImpl` |
| Service class | `Update{EntityName}Integration` | `UpdateLoanPrincipalPaymentIntegration` |
| Spring bean name | `"Update{EntityName}Integration"` | `"UpdateLoanPrincipalPaymentIntegration"` |

---

## Guardrail Checklist

Before completing generation, verify:

- [ ] Spreadsheet contained an `Update` tab (exit code 2 = halt)
- [ ] `INTEGRATION_CLASS` was read and `EntityName` correctly derived by stripping `Update` prefix and `Integration` suffix
- [ ] Each target file was checked for existence **before** writing — existing files were skipped, not overwritten
- [ ] Entity class maps **all** `InputFields` (both identifier and updatable/read-only)
- [ ] Model class includes `className`, `transaction`, and all `InputFields`
- [ ] `Success`, `Message`, `updateTimeStamp` (OUTPUT fields) excluded from entity and model
- [ ] Entity has `@PrePersist` / `@PreUpdate` lifecycle methods
- [ ] All Boolean Y/N fields have `YNBooleanSerializer` in entity and `YNBooleanDeserializer` in model
- [ ] Spring bean name in `@Service(...)` exactly matches `INTEGRATION_CLASS`
- [ ] `basicValidation()` covers all INPUT fields with `Required = Y`
- [ ] `basicExecute()` uses `repository.findById()` (upsert pattern) and maps only `Updatable=Y` fields
- [ ] `basicExecute()` uses `repository.save()` for existing records and `repository.create()` for new
- [ ] All six files placed in correct packages (or reported as skipped if already existing)
- [ ] Sample request JSON written to `vistahackathon26/LoanService/requirements/update-{EntityName}.json` (or skipped if already existing)

---

## Common Pitfalls

| Pitfall | Resolution |
|---|---|
| Overwriting an existing file at the target path | NEVER overwrite. Check existence first — if file exists, skip it and report `⚠️ SKIPPED (already exists)` |
| Only mapping `Updatable=Y` fields to entity columns | Entity needs ALL `InputFields` as columns — `Updatable=N` fields are still stored |
| Patching `Updatable=N` fields in `basicExecute()` | Only map `Updatable=Y` fields from model to entity in the update logic |
| Naming the model class the same as the entity | Model must have `Model` suffix: `{EntityName}Model` |
| Bean name mismatch in `@Service` | Bean name must be `"Update{EntityName}Integration"` — matches `INTEGRATION_CLASS` exactly |
| Using `repository.create()` unconditionally | Update uses upsert: `save()` for existing, `create()` for new — check `existing.isPresent()` |
| Accepting `updateTimeStamp` from the request | `updateTimeStamp` is server-managed via `@PreUpdate` — never map it from the model |
| Missing null-safe defaults for Boolean Y/N fields | Always apply: `request.getField() != null ? request.getField() : Boolean.FALSE` |
