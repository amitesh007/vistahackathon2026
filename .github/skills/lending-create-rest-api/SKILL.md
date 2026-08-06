---
name: lending-create-rest-api
description: >
  Skill for generating a full Create REST API stack (Entity, Model, Repository, Service)
  for any LoanIQ business object from a requirement spreadsheet. Derives all class structure
  from the `create` tab of the spreadsheet and mirrors the coding patterns found in
  the LoanService reference implementation.
---

# LoanIQ Create REST API — Full Stack Generation Skill

> **Purpose:** Generate the complete Create API stack — **Entity**, **Model**, **Repository**, and **Service** — for any LoanIQ business object, driven by a requirement spreadsheet.
>
> This skill does **NOT** copy existing code verbatim. It derives structure from the spreadsheet and applies the patterns established by the LoanService reference implementation.

---

## When to Use This Skill

Use `lending-create-rest-api` when:
- Scaffolding a **new** business object Create stack from a requirement spreadsheet
- Generating all four layers (Entity / Model / Repository / Service) in one pass
- The spreadsheet contains a `create` tab with field definitions

DO NOT use this skill for:
- Update, Query, or Delete operations (use the respective `lending-update-rest-api`, `lending-query-rest-api`, or `lending-delete-rest-api` skills)
- Modifying existing classes that were already generated

---

## Scripts

Three PowerShell scripts are provided under `.github/skills/lending-create-rest-api/scripts/`.
Run them in order before starting code generation:

| Script | Purpose |
|---|---|
| `extract-spreadsheet.ps1` | Unzips the `.xlsx` into raw XML files for parsing |
| `find-create-workflow-sheet.ps1` | Locates the `Create` tab, validates it exists, extracts metadata (INTEGRATION_CLASS, FILE_OP_PATH, PACKAGE_NAME), derives `EntityName` |
| `read-create-workflow-attributes.ps1` | Reads all INPUT field rows from the Create tab and returns structured attribute data |

### Invocation sequence

```powershell
# Resolve the spreadsheet from the requirements folder
# EntityName is taken directly from the .xlsx file name (without extension)
$reqDir   = "vistahackathon26\requirements"
$xlsx     = Get-ChildItem $reqDir -Filter "*.xlsx" | Select-Object -First 1
$EntityName = [System.IO.Path]::GetFileNameWithoutExtension($xlsx.Name)

# 1. Extract the xlsx
$extracted = .\extract-spreadsheet.ps1 -SpreadsheetPath $xlsx.FullName

# 2. Locate Create tab + extract metadata  (exits with code 2 if no Create tab exists)
$meta = .\find-create-workflow-sheet.ps1 -ExtractedPath $extracted

# 3. Read all INPUT field definitions
$attrs = .\read-create-workflow-attributes.ps1 `
             -ExtractedPath $extracted `
             -SheetFile $meta.SheetFile `
             -OutputJson "C:\Auto\API\create_attributes.json"

# Use $meta.EntityName and $attrs in downstream code generation
Write-Host "EntityName : $($meta.EntityName)"
$attrs | Format-Table FieldName, DataType, Required, IsYNBoolean, MaxSize
```

---

## Inputs

| Input | Description |
|---|---|
| `ExcelFilePath` | Path to the requirement spreadsheet (`.xlsx`) under `vistahackathon26\requirements\`. The entity name is derived from the file name (e.g. `LoanPrincipalPayment.xlsx` → `EntityName = LoanPrincipalPayment`). |

---

## Spreadsheet Layout — Create Workflow Tab

The `Create` tab has a fixed two-zone structure:

### Metadata zone (rows 1–9)

| Row | Column A (key) | Column B (value) | Usage |
|---|---|---|---|
| 1 | `R` | `Required for Code Generation` | Legend |
| 2 | `NR` | `Not required for Code Generation` | Legend |
| 4 | `PCP` | Y / N | Pre-condition check flag |
| 5 | `FILE_OP_PATH` | e.g. `C:\REST_AUTO_FILE_GEN\principal_payment` | Output path hint |
| 7 | **`INTEGRATION_CLASS`** | e.g. `CreateLoanPrincipalPaymentIntegration` | **className** |
| 8 | `RESPONSE_CLASS` | e.g. `LiqAPILoanPrincipalPaymentIntegrationAsReturnValue` | Response class name |
| 9 | `PACKAGE_NAME` | e.g. `com.misys.liq.api.rest.data.outstanding.principal` | LoanIQ package |

### Input zone (rows 10+)

| Row | Content |
|---|---|
| Section marker | Column A = `Input` |
| Column headers | Column A = `SL_NO`, Column O = `ATTRIBUTE_FIELD_NAME`, Column Q = `DATA_TYPE`, Column R = `REQUIRED`, Column X = `MAX_SIZE`, Column Y = `Default Value` |
| Data rows | One row per input field (stop at the row where column A = `OUTPUT`) |

**Key columns for code generation:**

| Column | Header | Used for |
|---|---|---|
| A | SL_NO | Serial number (numeric = data row) |
| B | CLASS_NAME | `className` value (e.g. `CreateLoanPrincipalPaymentIntegration`) |
| O | ATTRIBUTE_FIELD_NAME | Java field name (camelCase) |
| Q | DATA_TYPE | `String` / `Boolean` / `LocalDate` / `LocalDateTime` |
| R | REQUIRED | `Y` = mandatory, `N` = optional |
| X | MAX_SIZE | Column length for `@Column(length = ...)` on String fields |
| Y | Default Value | Field default (Booleans default `false` if blank) |

---

## Step 0 — Guardrail: Verify `create` Tab Exists

**Before doing any work**, run `find-create-workflow-sheet.ps1`.
If it exits with code 2, the spreadsheet has no `Create` tab — **stop immediately**.

```text
IF find-create-workflow-sheet.ps1 exits with code 2:
    STOP immediately.
    Output: "No 'Create' tab found in spreadsheet. Skill execution halted."
    Do not generate any files.
```

Only proceed to Step 1 when the script returns a valid `$meta` object.

---

## Step 1 — Derive the Entity Name

The entity name is derived by `find-create-workflow-sheet.ps1` automatically.
It reads **row 7, column B** (`INTEGRATION_CLASS`) from the Create tab and strips the naming conventions:

```text
EntityName = INTEGRATION_CLASS
    → Remove leading prefix  "Create"       (if present)
    → Remove trailing suffix "Integration"  (if present)

Example:
  INTEGRATION_CLASS = "CreateLoanPrincipalPaymentIntegration"
  EntityName        = "LoanPrincipalPayment"
```

The result is available as `$meta.EntityName` after running the script.
All generated class names are derived from `EntityName` as documented below.

---

## Step 2 — Read Create Tab Attributes

Run `read-create-workflow-attributes.ps1` to extract every INPUT field from the Create tab.
The script returns an array of objects, each representing one field:

| Property | Source column | Description |
|---|---|---|
| `FieldName` | O (`ATTRIBUTE_FIELD_NAME`) | Java field name (camelCase) |
| `ColumnName` | Derived | DB column name (SCREAMING_SNAKE_CASE auto-converted) |
| `DataType` | Q (`DATA_TYPE`) | `String` / `Boolean` / `LocalDate` / `LocalDateTime` |
| `Required` | R (`REQUIRED`) | `Y` = mandatory, `N` = optional |
| `IsYNBoolean` | Derived | `$true` when `DataType` is `Boolean` |
| `MaxSize` | X (`MAX_SIZE`) | Integer column length; `-1` if not specified |
| `DefaultValue` | Y (`Default Value`) | Default value string (may be empty) |
| `Description` | S (`ATTRIBUTE_DESCRIPTION`) | Field description (first 120 chars) |

The script automatically stops collecting input fields when it encounters the `OUTPUT` section header in column A.

Treat `createTimeStamp` and `updateTimeStamp` as **system-managed** fields — always add them with `@PrePersist` / `@PreUpdate` lifecycle hooks regardless of whether they appear in the spreadsheet.

---

## Step 3 — Generate Entity Class

**File:** `com/loanservice/entity/{EntityName}.java`
**Full path:** `LoanService/src/main/java/com/loanservice/entity/{EntityName}.java`

### Rules

1. Annotate the class with `@Entity` and `@Table(name = "SCREAMING_SNAKE_CASE_OF_ENTITY_NAME")`.
2. The primary key field (`@Id`) must be the `loanTransactionId`-equivalent column from the spreadsheet. Mark it with `@Column(name = "...", length = 24)`.
3. Map every spreadsheet `create` field as a `@Column`. Include `length` when the datatype is `String`.
4. For Boolean fields flagged as Y/N: add `@JsonSerialize(using = YNBooleanSerializer.class)` and default the field to `Boolean.FALSE`.
5. Add `createTimeStamp` (`@Column(name = "CREATE_TIMESTAMP", updatable = false)`) and `updateTimeStamp` (`@Column(name = "UPDATE_TIMESTAMP")`), both of type `LocalDateTime`.
6. Add `@PrePersist protected void onCreate()` — sets both timestamps to `LocalDateTime.now()`.
7. Add `@PreUpdate protected void onUpdate()` — updates `updateTimeStamp` to `LocalDateTime.now()`.
8. Generate a standard getter and setter for every field (timestamps have getters only).

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
**File:** `com/loanservice/model/{EntityName}Model.java`
**Full path:** `LoanService/src/main/java/com/loanservice/model/{EntityName}Model.java`

### Rules

1. Annotate the class with `@JsonInclude(JsonInclude.Include.NON_NULL)`.
2. Add `className` and `transaction` as the first two fields — these are always present regardless of the spreadsheet.
3. Map every `create` tab field as a private field with `@JsonProperty("fieldName")`.
4. For Boolean Y/N fields: add `@JsonDeserialize(using = YNBooleanDeserializer.class)`.
5. Group the fields with section comments that reflect their operation scope (e.g., `// ------ Create fields ------`).
6. Generate a standard getter and setter for every field.
7. Do **not** include `createTimeStamp` or `updateTimeStamp` — those are entity-only system fields.
8. Do **not** include `@JsonInclude` on individual fields; rely on the class-level annotation.

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

Three files are required. All live under:
`LoanService/src/main/java/com/loanservice/repository/`

### 5a — Primary Repository Interface

**Class name:** `{EntityName}Repository`
**File:** `{EntityName}Repository.java`

```text
Rules:
- Annotate with @Repository
- Extend JpaRepository<{EntityName}, String>
- Also extend {EntityName}RepositoryCustom
- Declare: void deleteBy{PrimaryKeyField}(String id);
```

### 5b — Custom Repository Fragment Interface

**Class name:** `{EntityName}RepositoryCustom`
**File:** `{EntityName}RepositoryCustom.java`

```text
Rules:
- Plain interface (no annotations)
- Declare one method: {EntityName} create({EntityName} entity);
```

### 5c — Custom Repository Implementation

**Class name:** `{EntityName}RepositoryImpl`
**File:** `{EntityName}RepositoryImpl.java`

```text
Rules:
- Annotate with @Repository
- Implement {EntityName}RepositoryCustom
- Inject EntityManager via @PersistenceContext
- Implement create(): annotate with @Transactional, call entityManager.persist(entity), return entity
```

### Required Imports (Impl)

```java
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
```

### Reference Implementations

- `LoanService/src/main/java/com/loanservice/repository/LoanPrincipalPaymentRepository.java`
- `LoanService/src/main/java/com/loanservice/repository/LoanPrincipalPaymentRepositoryCustom.java`
- `LoanService/src/main/java/com/loanservice/repository/LoanPrincipalPaymentRepositoryImpl.java`

---

## Step 6 — Generate Service Class

**Class name:** `Create{EntityName}Integration`
**File:** `Create{EntityName}Integration.java`
**Full path:** `LoanService/src/main/java/com/loanservice/service/Create{EntityName}Integration.java`

### Rules

1. Annotate with `@Service("Create{EntityName}Integration")` — the bean name must match the `className` value sent in the JSON payload.
2. Extend `BaseIntegrationService`.
3. Inject `{EntityName}Repository` via `@Autowired`.
4. Override `basicValidation({EntityName}Model request)`:
   - Call `assertNotBlank(...)` for every mandatory field identified in the spreadsheet.
   - Call `assertNotNull(...)` for mandatory `LocalDate` / non-String fields.
   - Call `assertMaxLength(...)` for every String field that has a `length` in the spreadsheet.
5. Override `basicExecute({EntityName}Model request)`:
   - Instantiate `new {EntityName}()`.
   - Auto-generate the primary key with `TransactionIdGenerator.generate()`.
   - Map every `create` field from the model to the entity using the matching setter.
   - For Boolean Y/N fields apply a null-safe default: `request.getField() != null ? request.getField() : Boolean.FALSE`.
   - Call `repository.create(entity)` and return the result.

### Required Imports

```java
import com.loanservice.entity.{EntityName};
import com.loanservice.model.{EntityName}Model;
import com.loanservice.repository.{EntityName}Repository;
import com.loanservice.util.TransactionIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
```

### Reference Implementation

`LoanService/src/main/java/com/loanservice/service/CreateLoanPrincipalPaymentIntegration.java`

---

## Step 7 — Generate Sample Request JSON

**File:** `create-{EntityName}.json`
**Path:** `vistahackathon26/LoanService/requirements/create-{EntityName}.json`

After all Java classes are generated, produce a sample JSON request payload that can be used directly in Postman or curl to test the Create endpoint.

### Rules

1. The JSON object must include `className` and `transaction` as the first two fields.
2. `className` value must match the `INTEGRATION_CLASS` from the spreadsheet (e.g. `"CreateLoanPrincipalPaymentIntegration"`).
3. `transaction` value is a placeholder string: `"TXN-SAMPLE-001"`.
4. Include every INPUT field from the spreadsheet:
   - `String` fields → use a realistic sample string value (not empty, not `"string"`).
   - `Boolean` (Y/N) fields → use `"Y"` or `"N"` as a string value.
   - `LocalDate` fields → use `"2025-01-15"` format.
   - `LocalDateTime` fields → use `"2025-01-15T10:30:00"` format.
5. Mandatory fields (`Required=Y`) must have non-null, non-blank values.
6. Optional fields (`Required=N`) may be included with sample values or omitted.
7. Do NOT include `createTimeStamp` or `updateTimeStamp` — these are server-managed.
8. If the target file `create-{EntityName}.json` already exists → skip and report `⚠️ SKIPPED (already exists)`.

### Example output

```json
{
  "className": "Create{EntityName}Integration",
  "transaction": "TXN-SAMPLE-001",
  "loanTransactionId": "LN-2025-001",
  "amount": "50000.00",
  "effectiveDate": "2025-01-15",
  "isWaived": "N"
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
| Service | `com.loanservice.service` | `vistahackathon26/LoanService/src/main/java/com/loanservice/service/Create{EntityName}Integration.java` |
| Sample JSON | `vistahackathon26/LoanService/requirements/` | `vistahackathon26/LoanService/requirements/create-{EntityName}.json` |

---

## Naming Conventions Summary

| Artefact | Naming Pattern | Example |
|---|---|---|
| Entity class | `{EntityName}` | `LoanPrincipalPayment` |
| Model class | `{EntityName}Model` | `LoanPrincipalPaymentModel` |
| Repository interface | `{EntityName}Repository` | `LoanPrincipalPaymentRepository` |
| Custom repository interface | `{EntityName}RepositoryCustom` | `LoanPrincipalPaymentRepositoryCustom` |
| Repository implementation | `{EntityName}RepositoryImpl` | `LoanPrincipalPaymentRepositoryImpl` |
| Service class | `Create{EntityName}Integration` | `CreateLoanPrincipalPaymentIntegration` |
| Spring bean name | `"Create{EntityName}Integration"` | `"CreateLoanPrincipalPaymentIntegration"` |

---

## Guardrail Checklist

Before completing generation, verify:

- [ ] Spreadsheet contained a `create` tab (if not, execution was already halted)
- [ ] `className` column was read and entity name was correctly derived by stripping `Create` prefix and `Integration` suffix
- [ ] Each target file was checked for existence **before** writing — existing files were skipped, not overwritten
- [ ] Entity class has `@PrePersist` / `@PreUpdate` lifecycle methods
- [ ] Every Boolean Y/N field has `YNBooleanSerializer` in the entity and `YNBooleanDeserializer` in the model
- [ ] The Spring bean name in `@Service(...)` exactly matches the `className` value in the spreadsheet
- [ ] All mandatory fields from the spreadsheet have corresponding `assertNotBlank` / `assertNotNull` calls in `basicValidation`
- [ ] Repository implementation uses `@Transactional` on `create()`
- [ ] All six files are placed in the correct packages (or reported as skipped if already existing)
- [ ] Sample request JSON written to `vistahackathon26/LoanService/requirements/create-{EntityName}.json` (or skipped if already existing)

---

## Common Pitfalls

| Pitfall | Resolution |
|---|---|
| Overwriting an existing file at the target path | NEVER overwrite. Check existence first — if file exists, skip it and report `⚠️ SKIPPED (already exists)` |
| Naming the model class the same as the entity class | Model class must have `Model` suffix: `{EntityName}Model` |
| Forgetting the bean name in `@Service` | Bean name must be `"Create{EntityName}Integration"` — not the default Spring bean name |
| Missing `Boolean.FALSE` defaults for Y/N fields in `basicExecute` | Always apply null-safe default: `request.getField() != null ? request.getField() : Boolean.FALSE` |
| `createTimeStamp` / `updateTimeStamp` in model | These are server-managed — include only in the Entity, never in the Model |
| Not calling `TransactionIdGenerator.generate()` for the primary key | The primary key is always auto-generated; never accept it from the request payload |
