---
name: lending-delete-test-rest-api
description: >
  Skill for generating JUnit5 unit test classes for any LoanIQ Delete REST API stack —
  Entity test, Model test, and Service test — driven by the `Delete` tab of a requirement
  spreadsheet. Covers every ATTRIBUTE_FIELD_NAME with test scenarios derived from the
  ATTRIBUTE_DESCRIPTION column.
---

# LoanIQ Delete REST API — JUnit Test Generation Skill

> **Purpose:** Generate three complete JUnit5 test classes for any LoanIQ Delete API stack,
> driven by the `Delete` tab of the requirement spreadsheet:
> 1. **`{EntityName}Test`** — entity getter/setter for the identifier field and lifecycle tests
> 2. **`{EntityName}ModelTest`** — model (request POJO) getter/setter tests
> 3. **`Delete{EntityName}IntegrationTest`** — service validation and deletion tests with Mockito

---

## When to Use This Skill

Use `lending-delete-test-rest-api` when:
- Generating JUnit5 test classes for a newly generated Delete API stack
- The Delete tab INPUT identifier field needs validation tests
- The `basicExecute` deletion flow needs confirmation-response tests

DO NOT use this skill for:
- Integration tests with real DB (use `lending-delete-test-api`)
- Create/Update/Query test generation (use the respective test skills)
- Generating implementation classes (use `lending-delete-rest-api`)

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
| `find-delete-workflow-sheet.ps1` | Locates `Delete` tab, extracts metadata, derives EntityName *(shared from lending-delete-rest-api)* |
| `read-delete-test-attributes.ps1` | Reads INPUT identifier fields and OUTPUT confirmation fields with **TestHints** derived from `ATTRIBUTE_DESCRIPTION` |

### Invocation sequence

```powershell
$createDir = ".github\skills\lending-create-rest-api\scripts"
$deleteDir = ".github\skills\lending-delete-rest-api\scripts"
$testDir   = ".github\skills\lending-delete-test-rest-api\scripts"

# 1. Resolve spreadsheet from requirements folder; EntityName = xlsx filename (no extension)
$reqDir   = "vistahackathon26\requirements"
$xlsx     = Get-ChildItem $reqDir -Filter "*.xlsx" | Select-Object -First 1
$EntityName = [System.IO.Path]::GetFileNameWithoutExtension($xlsx.Name)

# 2. Extract xlsx
$extracted = & "$createDir\extract-spreadsheet.ps1" -SpreadsheetPath $xlsx.FullName

# 2. Locate Delete tab (guardrail: exit 2 if missing)
$meta = & "$deleteDir\find-delete-workflow-sheet.ps1" -ExtractedPath $extracted
if ($LASTEXITCODE -eq 2) { Write-Error "No Delete tab - halting"; exit }

# 3. Read INPUT identifier and OUTPUT confirmation fields with test hints
$result = & "$testDir\read-delete-test-attributes.ps1" `
               -ExtractedPath $extracted `
               -SheetFile $meta.SheetFile `
               -OutputJson "C:\Auto\API\delete_test_attributes.json"

Write-Host "EntityName     : $($meta.EntityName)"
Write-Host "Input fields   : $($result.InputFields.Count)  <- identifier field(s)"
Write-Host "Output fields  : $($result.OutputFields.Count)  <- all system-meta"
$result.InputFields | Format-Table FieldName, DataType, Required, TestHints
```

---

## Spreadsheet Layout — What Drives Test Generation

### INPUT section → identifier / `basicValidation` tests

| Column | Header | Drives |
|---|---|---|
| O | `ATTRIBUTE_FIELD_NAME` | Test method names, validation calls |
| Q | `DATA_TYPE` | String null/blank tests |
| R | `REQUIRED` | Mandatory null/blank failure tests |
| S | `ATTRIBUTE_DESCRIPTION` | `@DisplayName` content and business-rule scenarios |
| X | `MAX_SIZE` | Max-length boundary tests |

### OUTPUT section → `basicExecute` response-structure tests

| Output field | System-meta meaning | Test driven |
|---|---|---|
| `Success` | Boolean deletion confirmation | `result.get("status") == "SUCCESS"` |
| `Message` | Info/error message string | `result.get("message")` contains identifier |
| `updateTimeStamp` | Post-deletion timestamp | `SYSTEM_META_CONFIRMATION` — not entity-mapped |

### TestHints derived per field

**INPUT field hints:**

| TestHint | Condition | Test scenario |
|---|---|---|
| `REQUIRED_NULL_FAILS` | `Required=Y` | Null → `assertThrows(IllegalArgumentException)` |
| `REQUIRED_BLANK_FAILS` | `Required=Y, String` | Empty string → `assertThrows` |
| `REQUIRED_WHITESPACE_FAILS` | `Required=Y, String` | Whitespace-only → `assertThrows` |
| `VALID_VALUE_PASSES` | `Required=Y` | Valid value → `assertDoesNotThrow` |
| `OPTIONAL_NULL_ALLOWED` | `Required=N` | Null → `assertDoesNotThrow` |
| `MAXLENGTH_EXCEEDED_FAILS` | `MaxSize>0` | Exceeds → `assertThrows` |
| `MAXLENGTH_BOUNDARY_PASSES` | `MaxSize>0` | At boundary → `assertDoesNotThrow` |
| `BUSINESS_RULE_MUST_EXIST` | Description mentions "must exist" | Record-not-found scenario |

**OUTPUT response hints (service test only):**

| TestHint | Test scenario |
|---|---|
| `EXECUTE_RETURNS_SUCCESS_STATUS` | `result.get("status") == "SUCCESS"` |
| `EXECUTE_RETURNS_MESSAGE_WITH_ID` | `result.get("message")` contains the identifier value |
| `SYSTEM_META_CONFIRMATION` | Not entity/model — response structure only |

---

## Step 0 — Guardrail: Verify `Delete` Tab Exists

Run `find-delete-workflow-sheet.ps1`. If exit code = 2, **stop immediately**.

---

## Step 1 — Generate Entity Test Class

**Class name:** `{EntityName}Test`
**File path:** `vistahackathon26/LoanService/src/test/java/com/loanservice/entity/{EntityName}Test.java`

### Rules

1. Package: `com.loanservice.entity`. Imports: `org.junit.jupiter.api.*`, `java.time.*`, `static org.junit.jupiter.api.Assertions.*`.
2. `@BeforeEach setUp()` — instantiates `new {EntityName}()`.
3. For the primary key INPUT field (e.g. `loanTransactionId`):
   - `{fieldName}_getterSetter()` — set a sample 24-char value → `assertEquals`.
   - `@DisplayName("{fieldName}: getter and setter work correctly")`
4. If the entity has additional columns (from a shared Create/GetByID entity), generate getter/setter tests for all of them following the same pattern:
   - **String / LocalDate:** one getter/setter test.
   - **Boolean Y/N:** two tests — `defaultFalse` and `setTrue`.
5. Always add lifecycle tests:
   - `onCreate_setsTimestamps()` — calls `entity.onCreate()`, asserts both timestamps non-null within time window.
   - `onUpdate_updatesUpdateTimestamp()` — calls `entity.onUpdate()`, asserts `updateTimeStamp` refreshed.
6. `@DisplayName` incorporates first sentence of `ATTRIBUTE_DESCRIPTION`.

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
   - `className_getterSetter()` — sets `"Delete{EntityName}Integration"`, asserts equal.
   - `transaction_getterSetter()` — sets `"{EntityName}"`, asserts equal.
4. For each INPUT identifier field from `$result.InputFields`:
   - One getter/setter test: `request.set{Field}(value)` → `assertEquals`.
5. Do **not** include `Success`, `Message`, `updateTimeStamp` from OUTPUT — those are system-meta.
6. No lifecycle tests in model class.

### Reference Implementation

`LoanService/src/test/java/com/loanservice/model/LoanRequestTest.java`

---

## Step 3 — Generate Service Test Class

**Class name:** `Delete{EntityName}IntegrationTest`
**File path:** `vistahackathon26/LoanService/src/test/java/com/loanservice/service/Delete{EntityName}IntegrationTest.java`

### Rules

1. Package: `com.loanservice.service`. Annotations: `@ExtendWith(MockitoExtension.class)`.
2. `@Mock {EntityName}Repository repository`.
3. `@InjectMocks Delete{EntityName}Integration service`.
4. `@BeforeEach setUp()`:
   - `validRequest` with `className = "Delete{EntityName}Integration"`, `transaction = "{EntityName}"`, and all INPUT identifier fields set to valid sample values.

5. **Validation tests** — for each INPUT field, generate a section:
   - `{fieldName}_required_nullShouldFail()` — set to null → `assertThrows(IllegalArgumentException)`.
   - If String: `{fieldName}_required_blankShouldFail()` — set to `""` → `assertThrows`.
   - If String: `{fieldName}_required_whitespaceShouldFail()` — set to `"   "` → `assertThrows`.
   - If `MaxSize > 0`: `{fieldName}_maxLength_exceeded()` — set to string of `MaxSize+1` chars → `assertThrows`.
   - `{fieldName}_valid_24chars()` — set valid value → `assertDoesNotThrow`.
   - If `MaxSize > 0`: `{fieldName}_valid_maxBoundary()` — set to string of exactly `MaxSize` chars → `assertDoesNotThrow`.

6. **`basicExecute` tests** (one section per OUTPUT hint):
   - `basicExecute_deleteCalledWithCorrectId()`:
     - `doNothing().when(repository).deleteBy{PrimaryKeyField}(id)`
     - call `service.basicExecute(validRequest)`
     - `verify(repository, times(1)).deleteBy{PrimaryKeyField}(id)`
   - `basicExecute_returnsSuccessMap()`:
     - stub doNothing for delete method
     - cast result to `Map<String, String>`
     - `assertEquals("SUCCESS", result.get("status"))`
   - `basicExecute_successMessageContainsId()`:
     - assert `result.get("message").contains(identifier value)`
   - `basicExecute_createNeverCalled()`:
     - `verify(repository, never()).create(any())`
   - `basicExecute_saveNeverCalled()`:
     - `verify(repository, never()).save(any())`

7. **Full flow test** (one combined test):
   - `fullFlow_validRequestValidatesAndDeletesSuccessfully()`:
     - stub doNothing for delete
     - call `basicValidation` then `basicExecute`
     - assert no exception + result contains success status

8. Section comment format:
   ```java
   // ---- {fieldName} ----
   // {first sentence of ATTRIBUTE_DESCRIPTION}
   ```

### Key imports

```java
import com.loanservice.model.{EntityName}Model;
import com.loanservice.repository.{EntityName}Repository;
import java.util.Map;
import static org.mockito.Mockito.*;
```

### Mockito setup pattern

```java
// Stub the delete method (void return)
doNothing().when(repository).deleteBy{PrimaryKeyField}(any());

// Verify the delete was called
verify(repository, times(1)).deleteBy{PrimaryKeyField}("A1B2C3D4E5F6G7H8I9J0K1L2");
```

### Reference Implementation

`LoanService/src/test/java/com/loanservice/service/DeleteLoanPrincipalPaymentIntegrationTest.java`

---

## Output File Placement

| Class | Path |
|---|---|
| `{EntityName}Test` | `LoanService/src/test/java/com/loanservice/entity/{EntityName}Test.java` |
| `{EntityName}ModelTest` | `LoanService/src/test/java/com/loanservice/model/{EntityName}ModelTest.java` |
| `Delete{EntityName}IntegrationTest` | `LoanService/src/test/java/com/loanservice/service/Delete{EntityName}IntegrationTest.java` |

---

## Complete Test Coverage Matrix

| Field / scenario | Entity test | Model test | Service test |
|---|---|---|---|
| INPUT String identifier (e.g. `loanTransactionId`) | getter/setter | getter/setter | null-fails, blank-fails, whitespace-fails, [maxLen], valid-passes |
| `className` / `transaction` | — | getter/setter | — |
| `deleteBy*` repository method called | — | — | `verify(times(1))` |
| Response `status == "SUCCESS"` | — | — | `basicExecute_returnsSuccessMap` |
| Response `message` contains identifier | — | — | `basicExecute_successMessageContainsId` |
| `create()` never called | — | — | `verify(never())` |
| `save()` never called | — | — | `verify(never())` |
| Full validate-then-delete flow | — | — | `fullFlow_validRequestValidatesAndDeletesSuccessfully` |
| System | `onCreate_setsTimestamps`, `onUpdate_updatesTimestamp` | — | — |

---

## Guardrail Checklist

Before completing generation, verify:

- [ ] Spreadsheet has a `Delete` tab (exit code 2 = halt)
- [ ] All INPUT field names have validation tests in the service test
- [ ] `@DisplayName` text incorporates `ATTRIBUTE_DESCRIPTION` content
- [ ] `doNothing().when(repository).deleteBy{PrimaryKeyField}(any())` stub used for all `basicExecute` tests
- [ ] `verify(repository, times(1)).deleteBy{PrimaryKeyField}(id)` asserted
- [ ] `verify(repository, never()).create(any())` asserted
- [ ] `verify(repository, never()).save(any())` asserted
- [ ] `result.get("status") == "SUCCESS"` asserted
- [ ] `result.get("message").contains(identifier)` asserted
- [ ] Entity lifecycle tests (`onCreate`, `onUpdate`) in entity test class
- [ ] No `Success`, `Message`, `updateTimeStamp` fields in model or entity test classes
- [ ] No integration/real-DB calls — all service tests use Mockito only

---

## Common Pitfalls

| Pitfall | Resolution |
|---|---|
| Using `when(repository.deleteBy...()).thenReturn(...)` for void method | Delete returns void — always use `doNothing().when(repository).deleteBy*(any())` |
| Missing `@SuppressWarnings("unchecked")` on Map cast | Add `@SuppressWarnings("unchecked")` before the Map cast line |
| Including `Success`/`Message`/`updateTimeStamp` in entity or model tests | These are system-meta — never mapped to entity columns or model fields |
| Missing whitespace-only test for identifier field | Always add `{fieldName}_required_whitespaceShouldFail()` in addition to blank test |
| Not asserting `create()` and `save()` never called | Delete operations must never call write methods — always assert both |
| `@DisplayName` not referencing description | Always draw display name from `ATTRIBUTE_DESCRIPTION` first sentence |
