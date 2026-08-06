---
name: lending-query-rest-api
description: >
  Skill for generating a full Query (GetByID) REST API stack (Entity, Model, Repository, Service)
  for any LoanIQ business object from a requirement spreadsheet. Derives all class structure
  from the `GetByID` tab of the spreadsheet and mirrors the coding patterns found in the
  LoanService reference implementation.
---

# LoanIQ Query (GetByID) REST API — Full Stack Generation Skill

> **Purpose:** Generate the complete Query API stack — **Entity**, **Model**, **Repository**, and **Service** — for any LoanIQ business object, driven by the `GetByID` tab of a requirement spreadsheet.
>
> This skill does **NOT** copy existing code verbatim. It derives structure from the spreadsheet and applies the patterns established by the LoanService reference implementation.

---

## When to Use This Skill

Use `lending-query-rest-api` when:
- Scaffolding a **new** business object Query/GetByID stack from a requirement spreadsheet
- Generating all four layers (Entity / Model / Repository / Service) for a read operation
- The spreadsheet contains a `GetByID` tab with INPUT and OUTPUT field definitions

DO NOT use this skill for:
- Create operations (use `lending-create-rest-api`)
- Update operations (use `lending-update-rest-api`)
- Delete operations (use `lending-delete-rest-api`)
- Modifying existing classes already generated

---

## Scripts

Three PowerShell scripts are provided under `.github/skills/lending-query-rest-api/scripts/`.

> **Note:** `extract-spreadsheet.ps1` is shared with `lending-create-rest-api`.
> Copy or reference the version at `.github/skills/lending-create-rest-api/scripts/extract-spreadsheet.ps1`.

| Script | Purpose |
|---|---|
| `extract-spreadsheet.ps1` | Unzips the `.xlsx` into raw XML files for parsing *(shared)* |
| `find-getbyid-workflow-sheet.ps1` | Locates the `GetByID` tab (guardrail: exits code 2 if missing), extracts metadata, derives `EntityName` |
| `read-getbyid-workflow-attributes.ps1` | Reads INPUT fields (query parameters) and OUTPUT fields (response/entity data) from the GetByID tab |

### Invocation sequence

```powershell
$scriptDir = ".github\skills\lending-create-rest-api\scripts"
$queryDir  = ".github\skills\lending-query-rest-api\scripts"

# 1. Resolve spreadsheet from requirements folder; EntityName = xlsx filename (no extension)
$reqDir   = "vistahackathon26\requirements"
$xlsx     = Get-ChildItem $reqDir -Filter "*.xlsx" | Select-Object -First 1
$EntityName = [System.IO.Path]::GetFileNameWithoutExtension($xlsx.Name)

# 2. Extract the xlsx (uses shared script)
$extracted = & "$scriptDir\extract-spreadsheet.ps1" -SpreadsheetPath $xlsx.FullName

# 2. Locate GetByID tab + extract metadata  (exits with code 2 if no GetByID tab)
$meta = & "$queryDir\find-getbyid-workflow-sheet.ps1" -ExtractedPath $extracted
if ($LASTEXITCODE -eq 2) {
    Write-Error "No 'GetByID' tab found - skill execution halted."
    exit
}

# 3. Read INPUT and OUTPUT field definitions
$result = & "$queryDir\read-getbyid-workflow-attributes.ps1" `
               -ExtractedPath $extracted `
               -SheetFile $meta.SheetFile `
               -OutputJson "C:\Auto\API\getbyid_attributes.json"

# Use $meta.EntityName, $result.InputFields, $result.OutputFields
Write-Host "EntityName    : $($meta.EntityName)"
Write-Host "Input fields  : $($result.InputFields.Count)"
Write-Host "Output fields : $($result.OutputFields.Count)"

# Entity fields = output fields where IsSystemMeta = $false
$entityFields = $result.OutputFields | Where-Object { -not $_.IsSystemMeta }
```

---

## Inputs

| Input | Description |
|---|---|
| `ExcelFilePath` | Absolute path to the requirement spreadsheet (`.xlsx`) |

---

## Spreadsheet Layout — GetByID Workflow Tab

The `GetByID` tab has the same two-zone structure as the Create tab, plus an extended OUTPUT zone:

### Metadata zone (rows 1–9)

| Row | Column A (key) | Column B (value) | Usage |
|---|---|---|---|
| 5 | `FILE_OP_PATH` | e.g. `C:\REST_AUTO_FILE_GEN\principal_payment` | Output path hint |
| 7 | **`INTEGRATION_CLASS`** | e.g. `GetLoanPrincipalPaymentIntegration` | **className** |
| 8 | `RESPONSE_CLASS` | e.g. `LiqAPILoanPrincipalPaymentIntegrationAsReturnValue` | Response class |
| 9 | `PACKAGE_NAME` | e.g. `com.misys.liq.api.rest.data.outstanding.principal` | LoanIQ package |

### Input zone (query parameters)

| Row | Content |
|---|---|
| Section marker | Column A = `Input` |
| Column headers | Column A = `SL_NO` |
| Data rows | Query parameter fields (typically just the primary key, e.g. `loanTransactionId`) |
| Stop marker | Column A = `OUTPUT` |

### Output zone (response / entity fields)

| Row | Content |
|---|---|
| Section marker | Column A = `OUTPUT` |
| Column headers | Column A = `SL_NO` |
| Data rows | Response fields mapped to entity columns |
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

**System-metadata OUTPUT fields** (excluded from entity class, not mapped to DB columns):

| Field | Reason to exclude |
|---|---|
| `success` | Boolean response indicator — not an entity attribute |
| `StatusCode` | Processing status string — not an entity attribute |
| `Message` | Error/warning message — not an entity attribute |

---

## Step 0 — Guardrail: Verify `GetByID` Tab Exists

**Before doing any work**, run `find-getbyid-workflow-sheet.ps1`.
If it exits with code 2, the spreadsheet has no `GetByID` tab — **stop immediately**.

```text
IF find-getbyid-workflow-sheet.ps1 exits with code 2:
    STOP immediately.
    Output: "No 'GetByID' tab found in spreadsheet. Skill execution halted."
    Do not generate any files.
```

Only proceed to Step 1 when the script returns a valid `$meta` object.

---

## Step 1 — Derive the Entity Name

The entity name is derived by `find-getbyid-workflow-sheet.ps1` automatically.
It reads **row 7, column B** (`INTEGRATION_CLASS`) and applies:

```text
EntityName = INTEGRATION_CLASS
    → Remove leading prefix  "Get"   or "Query"  (whichever is present)
    → Remove trailing suffix "Integration"

Examples:
  GetLoanPrincipalPaymentIntegration   → LoanPrincipalPayment
  QueryLoanDrawdownIntegration         → LoanDrawdown
```

The result is available as `$meta.EntityName` after running the script.

---

## Step 2 — Read GetByID Tab Attributes

Run `read-getbyid-workflow-attributes.ps1`. It returns an object with two arrays:

| Property | Contents | Used for |
|---|---|---|
| `InputFields` | Query parameter fields (between `Input` and `OUTPUT` markers) | `basicValidation()` in service |
| `OutputFields` | Response fields (after `OUTPUT` marker); `IsSystemMeta=$true` for `success`/`StatusCode`/`Message` | Entity class definition |

**Entity fields** = `OutputFields` where `IsSystemMeta = $false`.

Each field object has:

| Property | Source column | Description |
|---|---|---|
| `FieldName` | O (`ATTRIBUTE_FIELD_NAME`) | Java field name (camelCase, trimmed) |
| `ColumnName` | Derived | DB column name (SCREAMING_SNAKE_CASE) |
| `DataType` | Q (`DATA_TYPE`) | `String` / `Boolean` / `LocalDate` / `LocalDateTime` |
| `Required` | R (`REQUIRED`) | `Y` = mandatory, `N` = optional |
| `IsYNBoolean` | Derived | `$true` when DataType is `Boolean` |
| `MaxSize` | X (`MAX_SIZE`) | Integer column length; `-1` if not specified |
| `IsSystemMeta` | Derived | `$true` for `success`, `StatusCode`, `Message` |

Treat `createTimeStamp` and `updateTimeStamp` as **system-managed** fields — always include them in the entity with `@PrePersist` / `@PreUpdate` lifecycle hooks.

---

## Step 3 — Generate Entity Class

**Class name:** `{EntityName}`
**Full path:** `LoanService/src/main/java/com/loanservice/entity/{EntityName}.java`

### Rules

1. Annotate with `@Entity` and `@Table(name = "SCREAMING_SNAKE_CASE_OF_ENTITY_NAME")`.
2. The primary key (`@Id`) is the field from `OutputFields` that matches the input query field (typically `loanTransactionId`). Mark it `@Column(name = "...", length = 24)`.
3. Map every OUTPUT field where `IsSystemMeta = false` as a `@Column`.
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
3. Add the INPUT fields (query parameters) from `$result.InputFields`.
4. Add the OUTPUT fields (response data) from `$result.OutputFields` where `IsSystemMeta = false`.
5. Each field gets `@JsonProperty("fieldName")`.
6. Boolean Y/N fields get `@JsonDeserialize(using = YNBooleanDeserializer.class)`.
7. Do **not** include `createTimeStamp` / `updateTimeStamp` — those are entity-only.
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
- Plain interface
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

**Class name:** `Get{EntityName}Integration`
**Full path:** `LoanService/src/main/java/com/loanservice/service/Get{EntityName}Integration.java`

### Rules

1. Annotate with `@Service("Get{EntityName}Integration")` — bean name must match `INTEGRATION_CLASS` from the spreadsheet.
2. Extend `BaseIntegrationService`.
3. Inject `{EntityName}Repository` via `@Autowired`.
4. Override `basicValidation({EntityName}Model request)`:
   - For every INPUT field with `Required = Y`: call `assertNotBlank(fieldName, ...)`.
   - For INPUT fields with `MaxSize > 0`: call `assertMaxLength(fieldName, ..., maxSize)`.
5. Override `basicExecute({EntityName}Model request)`:
   - Call `repository.findById(request.get{PrimaryKeyField}())`.
   - Throw `ResponseStatusException(HttpStatus.NOT_FOUND, ...)` when the record is absent.
   - Return the found entity.

### Required Imports

```java
import com.loanservice.entity.{EntityName};
import com.loanservice.model.{EntityName}Model;
import com.loanservice.repository.{EntityName}Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
```

### Reference Implementation

`LoanService/src/main/java/com/loanservice/service/GetLoanPrincipalPaymentIntegration.java`

---

## Step 7 — Generate Sample Request JSON

**File:** `query-{EntityName}.json`
**Path:** `vistahackathon26/LoanService/requirements/query-{EntityName}.json`

After all Java classes are generated, produce a sample JSON request payload that can be used directly in Postman or curl to test the Query (GetByID) endpoint.

### Rules

1. The JSON object must include `className` and `transaction` as the first two fields.
2. `className` value must match the `INTEGRATION_CLASS` from the spreadsheet (e.g. `"GetLoanPrincipalPaymentIntegration"`).
3. `transaction` value is a placeholder string: `"TXN-SAMPLE-001"`.
4. Include only the INPUT fields (query parameters) — these drive `basicValidation()`.
   - `String` fields → realistic lookup key value.
   - `LocalDate` / `LocalDateTime` → appropriate date format.
5. Do NOT include OUTPUT fields in the request payload — they are response fields only.
6. If the target file `query-{EntityName}.json` already exists → skip and report `⚠️ SKIPPED (already exists)`.

### Example output

```json
{
  "className": "Get{EntityName}Integration",
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
| Service | `com.loanservice.service` | `vistahackathon26/LoanService/src/main/java/com/loanservice/service/Get{EntityName}Integration.java` |
| Sample JSON | `vistahackathon26/LoanService/requirements/` | `vistahackathon26/LoanService/requirements/query-{EntityName}.json` |

---

## Naming Conventions Summary

| Artefact | Naming Pattern | Example |
|---|---|---|
| Entity class | `{EntityName}` | `LoanPrincipalPayment` |
| Model class | `{EntityName}Model` | `LoanPrincipalPaymentModel` |
| Repository interface | `{EntityName}Repository` | `LoanPrincipalPaymentRepository` |
| Custom repository interface | `{EntityName}RepositoryCustom` | `LoanPrincipalPaymentRepositoryCustom` |
| Repository implementation | `{EntityName}RepositoryImpl` | `LoanPrincipalPaymentRepositoryImpl` |
| Service class | `Get{EntityName}Integration` | `GetLoanPrincipalPaymentIntegration` |
| Spring bean name | `"Get{EntityName}Integration"` | `"GetLoanPrincipalPaymentIntegration"` |

---

## Guardrail Checklist

Before completing generation, verify:

- [ ] Spreadsheet contained a `GetByID` tab (exit code 2 = halt)
- [ ] `INTEGRATION_CLASS` was read and `EntityName` correctly derived by stripping `Get`/`Query` prefix and `Integration` suffix
- [ ] Each target file was checked for existence **before** writing — existing files were skipped, not overwritten
- [ ] Entity class maps only OUTPUT fields where `IsSystemMeta = false`
- [ ] `success`, `StatusCode`, `Message` are excluded from the entity
- [ ] Entity has `@PrePersist` / `@PreUpdate` lifecycle methods
- [ ] All Boolean Y/N fields have `YNBooleanSerializer` in entity and `YNBooleanDeserializer` in model
- [ ] Spring bean name in `@Service(...)` exactly matches the `INTEGRATION_CLASS` value
- [ ] `basicValidation()` covers all INPUT fields with `Required = Y`
- [ ] `basicExecute()` uses `repository.findById(...)` and throws `ResponseStatusException.NOT_FOUND` when absent
- [ ] All six files placed in correct packages (or reported as skipped if already existing)
- [ ] Sample request JSON written to `vistahackathon26/LoanService/requirements/query-{EntityName}.json` (or skipped if already existing)

---

## Common Pitfalls

| Pitfall | Resolution |
|---|---|
| Overwriting an existing file at the target path | NEVER overwrite. Check existence first — if file exists, skip it and report `⚠️ SKIPPED (already exists)` |
| Including `success`/`StatusCode`/`Message` in entity | These are response-meta only — `IsSystemMeta=true`, exclude from entity and DB mapping |
| Naming the model class the same as the entity | Model must have `Model` suffix: `{EntityName}Model` |
| Bean name mismatch in `@Service` | Bean name must be `"Get{EntityName}Integration"` — matches `INTEGRATION_CLASS` exactly |
| Using `repository.create()` instead of `findById()` | Query operations NEVER write data — always use `findById()` |
| Missing `ResponseStatusException` for not-found | Always throw `HttpStatus.NOT_FOUND` when the entity is absent, never return `null` |
| Entity primary key derived from INPUT instead of OUTPUT | The `@Id` field is in the OUTPUT section (it is returned, and also the lookup key) |
