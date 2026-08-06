---
name: lending-create-test-rest-api
description: >
  Skill for generating JUnit5 unit test classes for any LoanIQ Create REST API stack —
  Entity test, Model test, and Service test — driven by the `Create` tab of a requirement
  spreadsheet. Covers every ATTRIBUTE_FIELD_NAME with test scenarios derived from the
  ATTRIBUTE_DESCRIPTION column.
---

# LoanIQ Create REST API — JUnit Test Generation Skill

> **Purpose:** Generate three complete JUnit5 test classes for any LoanIQ Create API stack,
> driven by the `Create` tab of the requirement spreadsheet:
> 1. **`{EntityName}Test`** — entity getter/setter and `@PrePersist` / `@PreUpdate` lifecycle tests
> 2. **`{EntityName}ModelTest`** — model (request POJO) getter/setter tests
> 3. **`Create{EntityName}IntegrationTest`** — service validation and execution tests with Mockito

---

## When to Use This Skill

Use `lending-create-test-rest-api` when:
- Generating JUnit5 test classes for a newly generated Create API stack
- Every spreadsheet `Create` tab attribute needs test coverage
- Tests must cover both validation (`basicValidation`) and execution (`basicExecute`) logic

DO NOT use this skill for:
- Integration tests with real DB (use `lending-create-test-api`)
- Update/Query/Delete test generation (use the respective test skills)
- Generating the implementation classes (use `lending-create-rest-api`)

---

## Inputs

| Input | Description |
|---|---|
| `SpreadsheetPath` | Path to the requirement spreadsheet (`.xlsx`) under `vistahackathon26\requirements\`. The entity name is derived from the file name (e.g. `LoanPrincipalPayment.xlsx` → `EntityName = LoanPrincipalPayment`). |
| `EntityName` | The business object name (e.g. `LoanPrincipalPayment`) |

---

## Scripts

| Script | Purpose |
|---|---|
| `extract-spreadsheet.ps1` | Unzips `.xlsx` to raw XML *(shared from lending-create-rest-api)* |
| `find-create-workflow-sheet.ps1` | Locates `Create` tab, extracts metadata, derives EntityName *(shared from lending-create-rest-api)* |
| `read-create-test-attributes.ps1` | Reads all INPUT fields **plus `TestHints`** derived from `ATTRIBUTE_DESCRIPTION` |

### Invocation sequence

```powershell
$createDir = ".github\skills\lending-create-rest-api\scripts"
$testDir   = ".github\skills\lending-create-test-rest-api\scripts"

# 1. Resolve spreadsheet from requirements folder; EntityName = xlsx filename (no extension)
$reqDir   = "vistahackathon26\requirements"
$xlsx     = Get-ChildItem $reqDir -Filter "*.xlsx" | Select-Object -First 1
$EntityName = [System.IO.Path]::GetFileNameWithoutExtension($xlsx.Name)

# 2. Extract xlsx
$extracted = & "$createDir\extract-spreadsheet.ps1" -SpreadsheetPath $xlsx.FullName

# 2. Locate Create tab (guardrail: exit 2 if missing)
$meta = & "$createDir\find-create-workflow-sheet.ps1" -ExtractedPath $extracted
if ($LASTEXITCODE -eq 2) { Write-Error "No Create tab - halting"; exit }

# 3. Read fields with test hints
$attrs = & "$testDir\read-create-test-attributes.ps1" `
              -ExtractedPath $extracted `
              -SheetFile $meta.SheetFile `
              -OutputJson "C:\Auto\API\create_test_attributes.json"

Write-Host "EntityName : $($meta.EntityName)"
Write-Host "Fields for test generation: $($attrs.Count)"
$attrs | Format-Table FieldName, DataType, Required, TestHints
```

---

## Spreadsheet Layout — What Drives Test Generation

Each row in the Create tab `INPUT` section produces tests via two columns:

| Column | Header | Drives |
|---|---|---|
| O | `ATTRIBUTE_FIELD_NAME` | Test method names, getter/setter calls |
| Q | `DATA_TYPE` | Test value types (`String`, `Boolean`, `LocalDate`, etc.) |
| R | `REQUIRED` | Whether null/blank failure tests are generated |
| S | `ATTRIBUTE_DESCRIPTION` | `@DisplayName` content + business-rule scenario tests |
| X | `MAX_SIZE` | Max-length boundary tests |

### TestHints derived from field metadata

The script derives `TestHints` for each field:

| TestHint | Generated when | Test scenario |
|---|---|---|
| `REQUIRED_NULL_FAILS` | `Required=Y` | Null input → `assertThrows(IllegalArgumentException)` |
| `REQUIRED_BLANK_FAILS` | `Required=Y, DataType=String` | Blank string → `assertThrows` |
| `VALID_VALUE_PASSES` | `Required=Y` | Valid input → `assertDoesNotThrow` |
| `OPTIONAL_NULL_ALLOWED` | `Required=N` | Null input → `assertDoesNotThrow` |
| `MAXLENGTH_EXCEEDED_FAILS` | `MaxSize > 0` | String of `MaxSize+1` → `assertThrows` |
| `MAXLENGTH_BOUNDARY_PASSES` | `MaxSize > 0` | String of exactly `MaxSize` → `assertDoesNotThrow` |
| `BOOLEAN_DEFAULT_FALSE` | `DataType=Boolean` | Fresh entity → `assertFalse(entity.getField())` |
| `BOOLEAN_SET_TRUE` | `DataType=Boolean` | `entity.setField(true)` → `assertTrue` |
| `BOOLEAN_NULL_DEFAULTS_FALSE` | `DataType=Boolean` | Null in request → entity field defaults `false` |
| `DATE_NULL_FAILS` | `DataType=LocalDate, Required=Y` | Null date → `assertThrows` |
| `DATE_VALID_VALUE` | `DataType=LocalDate` | Set and get a `LocalDate` |
| `BUSINESS_RULE_AMOUNT_EXCEEDED` | Description mentions "exceed" | Amount value too large → test scenario |
| `BUSINESS_RULE_DATE_NOT_PRIOR` | Description mentions "prior to" | Date constraint scenario |
| `BUSINESS_RULE_ONE_OF_REQUIRED` | Description mentions "either/or" | At least one of two fields |

---

## Step 0 — Guardrail: Verify `Create` Tab Exists

Run `find-create-workflow-sheet.ps1`. If exit code = 2, **stop immediately**.

---

## Step 1 — Generate Entity Test Class

**Class name:** `{EntityName}Test`
**File path:** `vistahackathon26/LoanService/src/test/java/com/loanservice/entity/{EntityName}Test.java`

### Rules

1. Package: `com.loanservice.entity`. Imports: `org.junit.jupiter.api.*`, `java.time.*`, `static org.junit.jupiter.api.Assertions.*`.
2. `@BeforeEach setUp()` — instantiates `new {EntityName}()`.
3. For every field from `$attrs`:
   - **String / LocalDate fields:** one test — `entity.set{Field}(value)` → `assertEquals`.
     - `@DisplayName("{fieldName}: getter and setter work correctly")`
   - **Boolean Y/N fields:** two tests:
     - Default false: `assertFalse(entity.get{Field}())`  — `@DisplayName("{fieldName}: defaults to false")`
     - Set true: `entity.set{Field}(Boolean.TRUE)` → `assertTrue` — `@DisplayName("{fieldName}: setter persists true")`
4. Always add two lifecycle tests regardless of spreadsheet:
   - `onCreate_setsTimestamps()` — calls `entity.onCreate()`, asserts both timestamps non-null and within time window.
   - `onUpdate_updatesUpdateTimestamp()` — calls `entity.onUpdate()`, asserts `updateTimeStamp` is updated.
5. Derive `@DisplayName` from `ATTRIBUTE_DESCRIPTION` for description-based tests — use the first sentence.

### Test value examples by type

| DataType | Example test value |
|---|---|
| `String` (general) | `"testValue"` or a domain-appropriate sample |
| `String` (amount) | `"1000000.00"` |
| `String` (ID) | `"A1B2C3D4E5F6G7H8I9J0K1L2"` |
| `LocalDate` | `LocalDate.of(2026, 1, 1)` |
| `Boolean` | `Boolean.TRUE` / `Boolean.FALSE` |

### Reference Implementation

`LoanService/src/test/java/com/loanservice/entity/LoanPrincipalPaymentTest.java`

---

## Step 2 — Generate Model Test Class

**Class name:** `{EntityName}ModelTest`
**File path:** `vistahackathon26/LoanService/src/test/java/com/loanservice/model/{EntityName}ModelTest.java`

### Rules

1. Package: `com.loanservice.model`. Same imports as entity test.
2. `@BeforeEach setUp()` — instantiates `new {EntityName}Model()`.
3. Always start with `className` and `transaction` fields (these are always present in the model):
   - `className_getterSetter()` — sets `"Create{EntityName}Integration"`, asserts equal.
   - `transaction_getterSetter()` — sets `"{EntityName}"`, asserts equal.
4. For every field from `$attrs`:
   - **String / LocalDate fields:** one test — `request.set{Field}(value)` → `assertEquals`.
   - **Boolean fields:** two tests:
     - Getter/setter round-trip: `request.set{Field}(Boolean.TRUE)` → `assertTrue`.
     - Null is allowed: `request.set{Field}(null)` → `assertNull(request.get{Field}())`.
5. No lifecycle method tests — those belong to the entity test.
6. `@DisplayName` format: `"{fieldName}: getter and setter work correctly"` or `"{fieldName}: null is allowed"`.

### Reference Implementation

`LoanService/src/test/java/com/loanservice/model/LoanRequestTest.java`

---

## Step 3 — Generate Service Test Class

**Class name:** `Create{EntityName}IntegrationTest`
**File path:** `vistahackathon26/LoanService/src/test/java/com/loanservice/service/Create{EntityName}IntegrationTest.java`

### Rules

1. Package: `com.loanservice.service`. Annotations: `@ExtendWith(MockitoExtension.class)`.
2. `@Mock {EntityName}Repository repository` — Mockito mock.
3. `@InjectMocks Create{EntityName}Integration service`.
4. `@BeforeEach setUp()` — builds a fully valid `{EntityName}Model validRequest` with all mandatory fields set.
5. For each mandatory field (`Required=Y`): generate a **section** with:
   - `{fieldName}_required_nullShouldFail()` — set field to `null`, assert `IllegalArgumentException`.
   - If `String`: `{fieldName}_required_blankShouldFail()` — set to `"  "`, assert throws.
   - `{fieldName}_valid()` — valid value, assert `assertDoesNotThrow`.
   - If `ATTRIBUTE_DESCRIPTION` contains a business rule: one additional description-based test.
6. For each optional field (`Required=N`): generate a **section** with:
   - `{fieldName}_optional_nullAllowed()` — set to `null`, assert `assertDoesNotThrow`.
   - If `MaxSize > 0`: `{fieldName}_maxLength_exceeded()` and `{fieldName}_maxLength_boundary()`.
   - If `Boolean`: `{fieldName}_optional_nullDefaultsFalse()` — call `basicExecute`, verify entity field is `false`.
   - If description contains a business rule scenario: one additional description-based test.
7. `basicExecute_mapsAllFieldsToEntity()` — sets all fields, mocks `repository.create(any())` to return the argument, calls `basicExecute`, asserts all entity fields are set correctly.
8. `basicExecute_generatesTransactionId()` — verifies `loanTransactionId` is non-null and non-blank.
9. `@DisplayName` format: `"{fieldName}: {description excerpt or rule name}"`.

### Section comment format

```java
// ---- {fieldName} ----
// {first sentence of ATTRIBUTE_DESCRIPTION}
```

### Mockito setup for basicExecute tests

```java
when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
```

### Reference Implementation

`LoanService/src/test/java/com/loanservice/service/CreateLoanPrincipalPaymentIntegrationTest.java`

---

## Output File Placement

| Class | Path |
|---|---|
| `{EntityName}Test` | `LoanService/src/test/java/com/loanservice/entity/{EntityName}Test.java` |
| `{EntityName}ModelTest` | `LoanService/src/test/java/com/loanservice/model/{EntityName}ModelTest.java` |
| `Create{EntityName}IntegrationTest` | `LoanService/src/test/java/com/loanservice/service/Create{EntityName}IntegrationTest.java` |

---

## Complete Test Coverage Matrix

For each attribute row in the Create tab, the following tests are generated:

| Field type | Entity test | Model test | Service test |
|---|---|---|---|
| `String` mandatory | getter/setter | getter/setter | null-fails, blank-fails, valid-passes, description-rule |
| `String` optional | getter/setter | getter/setter | null-allowed, [maxLen-exceeded, maxLen-boundary if MaxSize>0] |
| `Boolean` | default-false, set-true | getter-true, null-allowed | null-defaults-false, set-true-in-entity |
| `LocalDate` mandatory | getter/setter | getter/setter | null-fails, valid-passes |
| `LocalDate` optional | getter/setter | getter/setter | null-allowed, valid-passes |
| All fields | — | — | `basicExecute_mapsAllFieldsToEntity` (one combined test) |
| System | `onCreate_setsTimestamps`, `onUpdate_updatesTimestamp` | — | `basicExecute_generatesTransactionId` |

---

## Guardrail Checklist

Before completing generation, verify:

- [ ] Spreadsheet has a `Create` tab (exit code 2 from find script = halt)
- [ ] All `ATTRIBUTE_FIELD_NAME` fields from the spreadsheet have at least one test in each of the three test classes
- [ ] Every `ATTRIBUTE_DESCRIPTION` sentence drives at least one `@DisplayName` that quotes or paraphrases the rule
- [ ] Mandatory fields have both null-fails and valid-passes tests in the service test
- [ ] Boolean fields have `defaultFalse` tests in the entity test
- [ ] `@PrePersist` and `@PreUpdate` lifecycle methods are tested in the entity test
- [ ] `basicExecute_mapsAllFieldsToEntity` validates every field mapping in one comprehensive test
- [ ] No integration/real-DB calls — all service tests use Mockito (`@Mock`, `@InjectMocks`)

---

## Common Pitfalls

| Pitfall | Resolution |
|---|---|
| Skipping the `className` and `transaction` fields in model test | Always start model test with these two fields — they are always present |
| Missing `@ExtendWith(MockitoExtension.class)` on service test | Required for Mockito injection to work |
| `basicExecute` test without mocking `repository.create()` | Always stub with `when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0))` |
| Using real DB in service test | These are unit tests with mocks — for real-DB tests use `lending-create-test-api` |
| Generating entity lifecycle tests for model class | Lifecycle tests (`onCreate`, `onUpdate`) belong only in the entity test |
| `@DisplayName` not referencing the field description | Each test's display name must incorporate the description context |
