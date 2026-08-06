---
name: lending-delete-rest-api
description: >
  Skill for generating a full Delete REST API stack (Entity, Model, Repository, Service)
  for any LoanIQ business object from a requirement spreadsheet. Derives all class structure
  from the `Delete` tab of the spreadsheet and mirrors the coding patterns found in the
  LoanService reference implementation.
---

# LoanIQ Delete REST API — Full Stack Generation Skill

> **Purpose:** Generate the complete Delete API stack — **Entity**, **Model**, **Repository**, and **Service** — for any LoanIQ business object, driven by the `Delete` tab of a requirement spreadsheet.
>
> This skill does **NOT** copy existing code verbatim. It derives structure from the spreadsheet and applies the patterns established by the LoanService reference implementation.

---

## When to Use This Skill

Use `lending-delete-rest-api` when:
- Scaffolding a **new** business object Delete stack from a requirement spreadsheet
- Generating all four layers (Entity / Model / Repository / Service) for a delete operation
- The spreadsheet contains a `Delete` tab with INPUT identifier fields

DO NOT use this skill for:
- Create operations (use `lending-create-rest-api`)
- Query/GetByID operations (use `lending-query-rest-api`)
- Update operations (use `lending-update-rest-api`)
- Modifying existing classes already generated

---

## Scripts

Two PowerShell scripts are provided under `.github/skills/lending-delete-rest-api/scripts/`.

> **Note:** `extract-spreadsheet.ps1` is shared with `lending-create-rest-api`.
> Reference the version at `.github/skills/lending-create-rest-api/scripts/extract-spreadsheet.ps1`.

| Script | Purpose |
|---|---|
| `extract-spreadsheet.ps1` | Unzips the `.xlsx` into raw XML files for parsing *(shared from lending-create-rest-api)* |
| `find-delete-workflow-sheet.ps1` | Locates the `Delete` tab (guardrail: exits code 2 if missing), extracts metadata, derives `EntityName` |
| `read-delete-workflow-attributes.ps1` | Reads INPUT identifier fields and OUTPUT confirmation fields from the Delete tab |

### Invocation sequence

```powershell
$createDir = ".github\skills\lending-create-rest-api\scripts"
$deleteDir = ".github\skills\lending-delete-rest-api\scripts"

# 1. Resolve spreadsheet from requirements folder; EntityName = xlsx filename (no extension)
$reqDir   = "vistahackathon26\requirements"
$xlsx     = Get-ChildItem $reqDir -Filter "*.xlsx" | Select-Object -First 1
$EntityName = [System.IO.Path]::GetFileNameWithoutExtension($xlsx.Name)

# 2. Extract the xlsx (uses shared script)
$extracted = & "$createDir\extract-spreadsheet.ps1" -SpreadsheetPath $xlsx.FullName

# 2. Locate Delete tab + extract metadata  (exits with code 2 if no Delete tab)
$meta = & "$deleteDir\find-delete-workflow-sheet.ps1" -ExtractedPath $extracted
if ($LASTEXITCODE -eq 2) {
    Write-Error "No 'Delete' tab found - skill execution halted."
    exit
}

# 3. Read INPUT identifier and OUTPUT confirmation field definitions
$result = & "$deleteDir\read-delete-workflow-attributes.ps1" `
               -ExtractedPath $extracted `
               -SheetFile $meta.SheetFile `
               -OutputJson "C:\Auto\API\delete_attributes.json"

# Use $meta.EntityName and $result.InputFields
Write-Host "EntityName      : $($meta.EntityName)"
Write-Host "Input fields    : $($result.InputFields.Count)"   # typically 1 (primary key)
Write-Host "Output fields   : $($result.OutputFields.Count)"  # all system-meta

# Entity is built from InputFields (the identifier fields)
$result.InputFields | Format-Table FieldName, DataType, Required, MaxSize
```

---

## Inputs

| Input | Description |
|---|---|
| `ExcelFilePath` | Absolute path to the requirement spreadsheet (`.xlsx`) |

---

## Spreadsheet Layout — Delete Workflow Tab

The `Delete` tab follows the same two-zone pattern as Create and GetByID tabs.

### Metadata zone (rows 1–9)

| Row | Column A (key) | Column B (value) | Usage |
|---|---|---|---|
| 5 | `FILE_OP_PATH` | e.g. `C:\REST_AUTO_FILE_GEN\principal_payment` | Output path hint |
| 7 | **`INTEGRATION_CLASS`** | e.g. `DeleteLoanPrincipalPaymentIntegration` | **className** |
| 8 | `RESPONSE_CLASS` | e.g. `LiqAPILoanPrincipalPaymentIntegrationAsReturnValue` | Response class |
| 9 | `PACKAGE_NAME` | e.g. `com.misys.liq.api.rest.data.outstanding.principal` | LoanIQ package |

### Input zone (identifier fields)

| Row | Content |
|---|---|
| Section marker | Column A = `Input` |
| Column headers | Column A = `SL_NO` |
| Data rows | Identifier field(s) used to locate the record for deletion (typically just the primary key) |
| Stop marker | Column A = `OUTPUT` |

### Output zone (response confirmation — all system-meta)

| Row | Content |
|---|---|
| Section marker | Column A = `OUTPUT` |
| Column headers | Column A = `SL_NO` |
| Data rows | Confirmation fields returned after deletion |
| Stop marker | Column A = `Back to Index` (or end of rows) |

**Key columns (same for both zones):**

| Column | Header | Used for |
|---|---|---|
| A | SL_NO | Serial number |
| B | CLASS_NAME | className value |
| O | ATTRIBUTE_FIELD_NAME | Java field name (camelCase) |
| Q | DATA_TYPE | `String` / `Boolean` / `LocalDate` / `LocalDateTime` |
| R | REQUIRED | `Y` = mandatory, `N` = optional |
| X | MAX_SIZE | Column length for `@Column(length = ...)` on String fields |

**Delete OUTPUT fields are always system-meta** — they are never mapped to entity columns:

| Field | Reason to exclude from entity |
|---|---|
| `Success` / `success` | Boolean deletion confirmation — not an entity attribute |
| `Message` | Error/warning message — not an entity attribute |
| `updateTimeStamp` | Post-deletion timestamp — managed by `@PreUpdate`, not an input field |
| `StatusCode` | Processing status — not an entity attribute |

---

## Step 0 — Guardrail: Verify `Delete` Tab Exists

**Before doing any work**, run `find-delete-workflow-sheet.ps1`.
If it exits with code 2, the spreadsheet has no `Delete` tab — **stop immediately**.

```text
IF find-delete-workflow-sheet.ps1 exits with code 2:
    STOP immediately.
    Output: "No 'Delete' tab found in spreadsheet. Skill execution halted."
    Do not generate any files.
```

Only proceed to Step 1 when the script returns a valid `$meta` object.

---

## Step 1 — Derive the Entity Name

The entity name is derived by `find-delete-workflow-sheet.ps1` automatically.
It reads **row 7, column B** (`INTEGRATION_CLASS`) and applies:

```text
EntityName = INTEGRATION_CLASS
    → Remove leading prefix  "Delete"
    → Remove trailing suffix "Integration"

Example:
  DeleteLoanPrincipalPaymentIntegration  →  LoanPrincipalPayment
```

The result is available as `$meta.EntityName` after running the script.

---

## Step 2 — Read Delete Tab Attributes

Run `read-delete-workflow-attributes.ps1`. It returns an object with two arrays:

| Property | Contents | Used for |
|---|---|---|
| `InputFields` | Identifier fields (between `Input` and `OUTPUT` markers) | Entity `@Id`, model field, `basicValidation()`, `deleteBy*()` |
| `OutputFields` | Confirmation response fields (all `IsSystemMeta=true`) | Not used for entity/model — documentation only |

**Entity fields = `InputFields`** (the identifier(s) used to locate the record).

Each field object has:

| Property | Source column | Description |
|---|---|---|
| `FieldName` | O (`ATTRIBUTE_FIELD_NAME`) | Java field name (camelCase, trimmed) |
| `ColumnName` | Derived | DB column name (SCREAMING_SNAKE_CASE) |
| `DataType` | Q (`DATA_TYPE`) | `String` / `Boolean` / `LocalDate` / `LocalDateTime` |
| `Required` | R (`REQUIRED`) | `Y` = mandatory, `N` = optional |
| `IsYNBoolean` | Derived | `$true` when DataType is `Boolean` |
| `MaxSize` | X (`MAX_SIZE`) | Integer column length; `-1` if not specified |
| `IsSystemMeta` | Derived | `$true` for `Success`, `Message`, `updateTimeStamp`, `StatusCode` |

> **Note:** For Delete, the entity class typically only needs the primary key field from `InputFields`.
> The full entity definition (all columns) already exists if Create was previously generated.
> If generating the Delete stack standalone, create a minimal entity with the identifier and
> any system-managed timestamps (`createTimeStamp`, `updateTimeStamp`).

---

## Step 3 — Generate Entity Class

**Class name:** `{EntityName}`
**Full path:** `LoanService/src/main/java/com/loanservice/entity/{EntityName}.java`

### Rules

1. Annotate with `@Entity` and `@Table(name = "SCREAMING_SNAKE_CASE_OF_ENTITY_NAME")`.
2. The primary key (`@Id`) is the first field from `InputFields` (e.g. `loanTransactionId`). Mark it `@Column(name = "...", length = 24)`.
3. Map all additional `InputFields` (if any) as `@Column` entries.
4. Boolean Y/N fields (`IsYNBoolean = true`): add `@JsonSerialize(using = YNBooleanSerializer.class)` and default to `Boolean.FALSE`.
5. Add `createTimeStamp` (`@Column(name = "CREATE_TIMESTAMP", updatable = false)`) and `updateTimeStamp` (`@Column(name = "UPDATE_TIMESTAMP")`), both `LocalDateTime`.
6. Add `@PrePersist protected void onCreate()` and `@PreUpdate protected void onUpdate()`.
7. Generate standard getter/setter for every field (timestamps: getter only).

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
3. Add the INPUT identifier fields from `$result.InputFields`.
4. Each field gets `@JsonProperty("fieldName")`.
5. Boolean Y/N fields get `@JsonDeserialize(using = YNBooleanDeserializer.class)`.
6. Do **not** include `createTimeStamp` / `updateTimeStamp` — those are entity-only.
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
  e.g.:  void deleteByLoanTransactionId(String loanTransactionId);
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

**Class name:** `Delete{EntityName}Integration`
**Full path:** `LoanService/src/main/java/com/loanservice/service/Delete{EntityName}Integration.java`

### Rules

1. Annotate with `@Service("Delete{EntityName}Integration")` — bean name must exactly match `INTEGRATION_CLASS` from the spreadsheet.
2. Extend `BaseIntegrationService`.
3. Inject `{EntityName}Repository` via `@Autowired`.
4. Override `basicValidation({EntityName}Model request)`:
   - For every INPUT field with `Required = Y`: call `assertNotBlank(fieldName, ...)`.
   - For INPUT fields with `MaxSize > 0`: call `assertMaxLength(fieldName, ..., maxSize)`.
5. Override `basicExecute({EntityName}Model request)`:
   - Annotate with `@Transactional`.
   - Call `repository.deleteBy{PrimaryKeyField}(request.get{PrimaryKeyField}())`.
   - Return a `Map.of("status", "SUCCESS", "message", "...")` confirmation map.
   - **Never** return the deleted entity or throw on missing record (delete is idempotent).

### Required Imports

```java
import com.loanservice.model.{EntityName}Model;
import com.loanservice.repository.{EntityName}Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
```

### Reference Implementation

`LoanService/src/main/java/com/loanservice/service/DeleteLoanPrincipalPaymentIntegration.java`

---

## Step 7 — Generate Sample Request JSON

**File:** `delete-{EntityName}.json`
**Path:** `vistahackathon26/LoanService/requirements/delete-{EntityName}.json`

After all Java classes are generated, produce a sample JSON request payload that can be used directly in Postman or curl to test the Delete endpoint.

### Rules

1. The JSON object must include `className` and `transaction` as the first two fields.
2. `className` value must match the `INTEGRATION_CLASS` from the spreadsheet (e.g. `"DeleteLoanPrincipalPaymentIntegration"`).
3. `transaction` value is a placeholder string: `"TXN-SAMPLE-001"`.
4. Include only the INPUT identifier fields — these are the primary key(s) used for deletion.
   - `String` fields → realistic lookup key value.
5. Do NOT include OUTPUT fields (`Success`, `Message`, `updateTimeStamp`, `StatusCode`) — these are response-meta only.
6. If the target file `delete-{EntityName}.json` already exists → skip and report `⚠️ SKIPPED (already exists)`.

### Example output

```json
{
  "className": "Delete{EntityName}Integration",
  "transaction": "TXN-SAMPLE-001",
  "loanTransactionId": "LN-2025-001"
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
| Service | `com.loanservice.service` | `vistahackathon26/LoanService/src/main/java/com/loanservice/service/Delete{EntityName}Integration.java` |
| Sample JSON | `vistahackathon26/LoanService/requirements/` | `vistahackathon26/LoanService/requirements/delete-{EntityName}.json` |

---

## Naming Conventions Summary

| Artefact | Naming Pattern | Example |
|---|---|---|
| Entity class | `{EntityName}` | `LoanPrincipalPayment` |
| Model class | `{EntityName}Model` | `LoanPrincipalPaymentModel` |
| Repository interface | `{EntityName}Repository` | `LoanPrincipalPaymentRepository` |
| Custom repository interface | `{EntityName}RepositoryCustom` | `LoanPrincipalPaymentRepositoryCustom` |
| Repository implementation | `{EntityName}RepositoryImpl` | `LoanPrincipalPaymentRepositoryImpl` |
| Service class | `Delete{EntityName}Integration` | `DeleteLoanPrincipalPaymentIntegration` |
| Spring bean name | `"Delete{EntityName}Integration"` | `"DeleteLoanPrincipalPaymentIntegration"` |

---

## Guardrail Checklist

Before completing generation, verify:

- [ ] Spreadsheet contained a `Delete` tab (exit code 2 = halt)
- [ ] `INTEGRATION_CLASS` was read and `EntityName` correctly derived by stripping `Delete` prefix and `Integration` suffix
- [ ] Each target file was checked for existence **before** writing — existing files were skipped, not overwritten
- [ ] Entity class is built from `InputFields` only (the identifier fields)
- [ ] `Success`, `Message`, `updateTimeStamp`, `StatusCode` excluded from entity and model
- [ ] Entity has `@PrePersist` / `@PreUpdate` lifecycle methods
- [ ] Repository declares `deleteBy{PrimaryKeyField}(String id)` method
- [ ] Spring bean name in `@Service(...)` exactly matches `INTEGRATION_CLASS`
- [ ] `basicValidation()` covers all INPUT fields with `Required = Y`
- [ ] `basicExecute()` is annotated `@Transactional` and calls `repository.deleteBy*()`
- [ ] `basicExecute()` returns a `Map.of(...)` confirmation — never returns the deleted entity
- [ ] All six files placed in correct packages (or reported as skipped if already existing)
- [ ] Sample request JSON written to `vistahackathon26/LoanService/requirements/delete-{EntityName}.json` (or skipped if already existing)

---

## Common Pitfalls

| Pitfall | Resolution |
|---|---|
| Overwriting an existing file at the target path | NEVER overwrite. Check existence first — if file exists, skip it and report `⚠️ SKIPPED (already exists)` |
| Including `Success`/`Message`/`updateTimeStamp` in entity or model | These are response-meta only — all `IsSystemMeta=true`, never map to DB columns |
| Naming the model class the same as the entity | Model must have `Model` suffix: `{EntityName}Model` |
| Bean name mismatch in `@Service` | Bean name must be `"Delete{EntityName}Integration"` — matches `INTEGRATION_CLASS` exactly |
| Missing `@Transactional` on `basicExecute()` | Delete operations require a transaction — always annotate `basicExecute()` with `@Transactional` |
| Throwing `NOT_FOUND` exception for missing record | Delete is idempotent — if the record does not exist, return success silently |
| Using `repository.create()` in delete service | Delete services call `repository.deleteBy{PrimaryKeyField}()` — never `create()` |
| Building entity from OUTPUT fields | For Delete, the entity uses INPUT identifier fields only — OUTPUT fields are all system-meta |
