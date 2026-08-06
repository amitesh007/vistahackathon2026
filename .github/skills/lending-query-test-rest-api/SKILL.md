---
name: lending-query-test-rest-api
description: >
  Skill for generating JUnit5 unit test classes for any LoanIQ Query (GetByID) REST API stack —
  Entity test, Model test, and Service test — driven by the `GetByID` tab of a requirement
  spreadsheet. Covers every ATTRIBUTE_FIELD_NAME with test scenarios derived from the
  ATTRIBUTE_DESCRIPTION column.
---

# LoanIQ Query (GetByID) REST API — JUnit Test Generation Skill

> **Purpose:** Generate three complete JUnit5 test classes for any LoanIQ Query/GetByID API stack,
> driven by the `GetByID` tab of the requirement spreadsheet:
> 1. **`{EntityName}Test`** — entity getter/setter and `@PrePersist` / `@PreUpdate` lifecycle tests (OUTPUT entity fields)
> 2. **`{EntityName}ModelTest`** — model (request POJO) getter/setter tests (INPUT + OUTPUT entity fields)
> 3. **`Get{EntityName}IntegrationTest`** — service validation and lookup tests with Mockito

---

## When to Use This Skill

Use `lending-query-test-rest-api` when:
- Generating JUnit5 test classes for a newly generated Query/GetByID API stack
- INPUT fields from the GetByID tab need validation-focused tests
- OUTPUT entity fields need getter/setter tests and response-assertion tests

DO NOT use this skill for:
- Integration tests with real DB (use `lending-query-test-api`)
- Create/Update/Delete test generation (use the respective test skills)
- Generating implementation classes (use `lending-query-rest-api`)

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
| `find-getbyid-workflow-sheet.ps1` | Locates `GetByID` tab, extracts metadata, derives EntityName *(shared from lending-query-rest-api)* |
| `read-getbyid-test-attributes.ps1` | Reads INPUT (query param) and OUTPUT (entity) fields with **TestHints** derived from `ATTRIBUTE_DESCRIPTION` |

### Invocation sequence

```powershell
$createDir = ".github\skills\lending-create-rest-api\scripts"
$queryDir  = ".github\skills\lending-query-rest-api\scripts"
$testDir   = ".github\skills\lending-query-test-rest-api\scripts"

# 1. Resolve spreadsheet from requirements folder; EntityName = xlsx filename (no extension)
$reqDir   = "vistahackathon26\requirements"
$xlsx     = Get-ChildItem $reqDir -Filter "*.xlsx" | Select-Object -First 1
$EntityName = [System.IO.Path]::GetFileNameWithoutExtension($xlsx.Name)

# 2. Extract xlsx
$extracted = & "$createDir\extract-spreadsheet.ps1" -SpreadsheetPath $xlsx.FullName

# 2. Locate GetByID tab (guardrail: exit 2 if missing)
$meta = & "$queryDir\find-getbyid-workflow-sheet.ps1" -ExtractedPath $extracted
if ($LASTEXITCODE -eq 2) { Write-Error "No GetByID tab - halting"; exit }

# 3. Read INPUT and OUTPUT fields with test hints
$result = & "$testDir\read-getbyid-test-attributes.ps1" `
               -ExtractedPath $extracted `
               -SheetFile $meta.SheetFile `
               -OutputJson "C:\Auto\API\getbyid_test_attributes.json"

# Key derived sets
$inputFields  = $result.InputFields
$entityFields = $result.OutputFields | Where-Object { -not $_.IsSystemMeta }

Write-Host "EntityName     : $($meta.EntityName)"
Write-Host "Input fields   : $($inputFields.Count)  <- drive basicValidation tests"
Write-Host "Entity fields  : $($entityFields.Count)  <- drive entity + basicExecute tests"
```

---

## Spreadsheet Layout — What Drives Test Generation

### INPUT section → `basicValidation` tests

| Column | Header | Drives |
|---|---|---|
| O | `ATTRIBUTE_FIELD_NAME` | Test method names, validation calls |
| Q | `DATA_TYPE` | String null/blank, LocalDate null tests |
| R | `REQUIRED` | Whether null/blank failure tests apply |
| S | `ATTRIBUTE_DESCRIPTION` | `@DisplayName` content |
| X | `MAX_SIZE` | Max-length boundary tests |

### OUTPUT section → entity getter/setter + `basicExecute` response tests

| Column | Header | Drives |
|---|---|---|
| O | `ATTRIBUTE_FIELD_NAME` | Entity method names, assertion field names |
| Q | `DATA_TYPE` | String/Boolean/LocalDate test patterns |
| S | `ATTRIBUTE_DESCRIPTION` | `@DisplayName` content |

**System-meta OUTPUT fields** (`success`, `StatusCode`, `Message`) are flagged `IsSystemMeta=true` — excluded from entity and model tests.

### TestHints derived per field

**INPUT field hints:**

| TestHint | Condition | Test scenario |
|---|---|---|
| `REQUIRED_NULL_FAILS` | `Required=Y` | Null → `assertThrows(IllegalArgumentException)` |
| `REQUIRED_BLANK_FAILS` | `Required=Y, String` | Blank → `assertThrows` |
| `VALID_VALUE_PASSES` | `Required=Y` | Valid → `assertDoesNotThrow` |
| `OPTIONAL_NULL_ALLOWED` | `Required=N` | Null → `assertDoesNotThrow` |
| `MAXLENGTH_EXCEEDED_FAILS` | `MaxSize>0` | Exceeds → `assertThrows` |
| `MAXLENGTH_BOUNDARY_PASSES` | `MaxSize>0` | At boundary → `assertDoesNotThrow` |

**OUTPUT entity field hints:**

| TestHint | Condition | Test scenario |
|---|---|---|
| `ENTITY_GETTER_SETTER` | String / LocalDate | Set and get round-trip |
| `ENTITY_DEFAULT_FALSE` | Boolean | Fresh entity → `assertFalse` |
| `ENTITY_SET_TRUE` | Boolean | Set true → `assertTrue` |
| `EXECUTE_RETURNS_VALUE` | String | `basicExecute` result has expected value |
| `EXECUTE_RETURNS_DATE` | LocalDate | `basicExecute` result has expected date |
| `EXECUTE_VERIFY_TRANSACTION_ID` | ID field | Response has correct identifier |

---

## Step 0 — Guardrail: Verify `GetByID` Tab Exists

Run `find-getbyid-workflow-sheet.ps1`. If exit code = 2, **stop immediately**.

---

## Step 1 — Generate Entity Test Class

**Class name:** `{EntityName}Test`
**File path:** `vistahackathon26/LoanService/src/test/java/com/loanservice/entity/{EntityName}Test.java`

### Rules

1. Package: `com.loanservice.entity`. Imports: `org.junit.jupiter.api.*`, `java.time.*`, `static org.junit.jupiter.api.Assertions.*`.
2. `@BeforeEach setUp()` — instantiates `new {EntityName}()`.
3. For every OUTPUT entity field (`IsSystemMeta=false`) from `$entityFields`:
   - **String / LocalDate fields:** one test — `entity.set{Field}(value)` → `assertEquals`.
     - `@DisplayName("{fieldName}: getter and setter work correctly")`
   - **Boolean Y/N fields:** two tests:
     - Default false: `assertFalse(entity.get{Field}())` — `@DisplayName("{fieldName}: defaults to false")`
     - Set true: `entity.set{Field}(Boolean.TRUE)` → `assertTrue` — `@DisplayName("{fieldName}: setter persists true")`
4. Always add lifecycle tests:
   - `onCreate_setsTimestamps()` — calls `entity.onCreate()`, asserts both timestamps non-null within time window.
   - `onUpdate_updatesUpdateTimestamp()` — calls `entity.onUpdate()`, asserts `updateTimeStamp` refreshed.

### Test value examples

| DataType | Example |
|---|---|
| `String` (ID) | `"A1B2C3D4E5F6G7H8I9J0K1L2"` |
| `String` (amount) | `"1000000.00"` |
| `String` (alias) | `"LoanAlias123"` |
| `LocalDate` | `LocalDate.of(2026, 1, 1)` |
| `Boolean` | `Boolean.TRUE` |

### Reference Implementation

`LoanService/src/test/java/com/loanservice/entity/LoanPrincipalPaymentTest.java`

---

## Step 2 — Generate Model Test Class

**Class name:** `{EntityName}ModelTest`
**File path:** `vistahackathon26/LoanService/src/test/java/com/loanservice/model/{EntityName}ModelTest.java`

### Rules

1. Package: `com.loanservice.model`. Same imports as entity test.
2. `@BeforeEach setUp()` — instantiates `new {EntityName}Model()`.
3. Always start with `className` and `transaction` fields:
   - `className_getterSetter()` — sets `"Get{EntityName}Integration"`, asserts equal.
   - `transaction_getterSetter()` — sets `"{EntityName}"`, asserts equal.
4. Add INPUT query parameter fields:
   - `{inputField}_getterSetter()` — set and assert value.
5. Add OUTPUT entity fields (`IsSystemMeta=false`):
   - **String / LocalDate fields:** one getter/setter test.
   - **Boolean fields:** two tests — getter/setter true, null-allowed.
6. No lifecycle tests.

### Reference Implementation

`LoanService/src/test/java/com/loanservice/model/LoanRequestTest.java`

---

## Step 3 — Generate Service Test Class

**Class name:** `Get{EntityName}IntegrationTest`
**File path:** `vistahackathon26/LoanService/src/test/java/com/loanservice/service/Get{EntityName}IntegrationTest.java`

### Rules

1. Package: `com.loanservice.service`. Annotations: `@ExtendWith(MockitoExtension.class)`.
2. `@Mock {EntityName}Repository repository`.
3. `@InjectMocks Get{EntityName}Integration service`.
4. `@BeforeEach setUp()`:
   - `validRequest` — model with `className`, `transaction`, and all INPUT identifier fields set.
   - `existingEntity` — entity with primary key + key OUTPUT entity fields pre-populated.

5. **Validation tests** (from `InputFields`): for the primary key and each input parameter:
   - Null-fails: `assertThrows(IllegalArgumentException)`.
   - If String: blank-fails: `assertThrows`.
   - If `MaxSize > 0`: max-length exceeded → `assertThrows`; at boundary → `assertDoesNotThrow`.
   - Valid value → `assertDoesNotThrow`.

6. **`basicExecute` — record found** tests:
   - `basicExecute_recordFound_returnsEntity()` — stub `findById` → `Optional.of(existingEntity)`, assert result non-null and key fields match.
   - For each mandatory OUTPUT entity field: one assertion test verifying the returned value.
   - `@DisplayName` incorporates `ATTRIBUTE_DESCRIPTION` first sentence.

7. **`basicExecute` — record not found** tests:
   - `basicExecute_recordNotFound_throws404()` — stub `findById` → `Optional.empty()`, assert `ResponseStatusException` thrown.
   - `basicExecute_recordNotFound_errorMessageContainsId()` — assert exception reason contains the identifier value.

8. **Repository interaction** tests:
   - `basicExecute_findByIdCalledOnce()` — `verify(repository, times(1)).findById(id)`.
   - `basicExecute_createNeverCalled()` — `verify(repository, never()).create(any())`.
   - `basicExecute_saveNeverCalled()` — `verify(repository, never()).save(any())`.

9. Section comment format:
   ```java
   // ---- {fieldName} ----
   // {first sentence of ATTRIBUTE_DESCRIPTION}
   ```

### Key imports

```java
import com.loanservice.entity.{EntityName};
import com.loanservice.model.{EntityName}Model;
import com.loanservice.repository.{EntityName}Repository;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.mockito.Mockito.*;
```

### Mockito setup patterns

```java
// Record found
when(repository.findById("A1B2C3D4E5F6G7H8I9J0K1L2"))
    .thenReturn(Optional.of(existingEntity));

// Record not found
when(repository.findById(validRequest.getLoanTransactionId()))
    .thenReturn(Optional.empty());
```

### Reference Implementation

`LoanService/src/test/java/com/loanservice/service/GetLoanPrincipalPaymentIntegrationTest.java`

---

## Output File Placement

| Class | Path |
|---|---|
| `{EntityName}Test` | `LoanService/src/test/java/com/loanservice/entity/{EntityName}Test.java` |
| `{EntityName}ModelTest` | `LoanService/src/test/java/com/loanservice/model/{EntityName}ModelTest.java` |
| `Get{EntityName}IntegrationTest` | `LoanService/src/test/java/com/loanservice/service/Get{EntityName}IntegrationTest.java` |

---

## Complete Test Coverage Matrix

| Field / scenario | Entity test | Model test | Service test |
|---|---|---|---|
| INPUT String mandatory (e.g. `loanTransactionId`) | — | getter/setter | null-fails, blank-fails, [maxLen], valid-passes |
| OUTPUT String (e.g. `requestedAmount`) | getter/setter | getter/setter | `basicExecute` returns correct value |
| OUTPUT Boolean (e.g. `preventOnlineDeletionIndicator`) | default-false, set-true | getter-true, null-allowed | — |
| OUTPUT LocalDate (e.g. `effectiveDate`) | getter/setter | getter/setter | `basicExecute` returns correct date |
| Record found | — | — | `basicExecute_recordFound_returnsEntity` + per-field assertions |
| Record not found | — | — | `basicExecute_recordNotFound_throws404` + error message test |
| Repository interactions | — | — | `findById` called once, `create` never called, `save` never called |
| System | `onCreate_setsTimestamps`, `onUpdate_updatesTimestamp` | — | — |

---

## Guardrail Checklist

Before completing generation, verify:

- [ ] Spreadsheet has a `GetByID` tab (exit code 2 = halt)
- [ ] All INPUT field names from spreadsheet have validation tests in service test
- [ ] All OUTPUT entity fields (`IsSystemMeta=false`) have getter/setter tests in entity test
- [ ] `success`, `StatusCode`, `Message` excluded from all test classes
- [ ] `@DisplayName` text incorporates `ATTRIBUTE_DESCRIPTION` content
- [ ] `basicExecute` record-found test asserts at least the primary key and one data field
- [ ] `basicExecute` record-not-found test asserts `ResponseStatusException` (404)
- [ ] Repository tests verify `findById` is called, `create` and `save` are never called
- [ ] Entity lifecycle tests (`onCreate`, `onUpdate`) in entity test class
- [ ] No integration/real-DB calls — all service tests use Mockito only

---

## Common Pitfalls

| Pitfall | Resolution |
|---|---|
| Including `success`/`StatusCode`/`Message` in entity or model tests | These are system-meta — `IsSystemMeta=true`, skip entirely |
| Missing `ResponseStatusException` import | Required for the `throws404` test assertions |
| Forgetting `verify(repository, never()).create(any())` | GET operations must never call `create` — always assert this |
| Testing `basicValidation` with null when field is optional | INPUT fields with `Required=N` use `assertDoesNotThrow` for null, not `assertThrows` |
| `@DisplayName` not referencing the description | Always draw the display name from `ATTRIBUTE_DESCRIPTION` first sentence |
| Missing `Optional` import | `basicExecute` stubs use `Optional.of(...)` and `Optional.empty()` |
| Building entity test from INPUT fields | Entity tests use OUTPUT entity fields (`IsSystemMeta=false`) only |
