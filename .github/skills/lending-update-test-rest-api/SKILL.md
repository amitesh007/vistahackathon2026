---
name: lending-update-test-rest-api
description: >
  Skill for generating JUnit5 unit test classes for any LoanIQ Update REST API stack —
  Entity test, Model test, and Service test — driven by the `Update` tab of a requirement
  spreadsheet. Covers every ATTRIBUTE_FIELD_NAME with test scenarios derived from the
  ATTRIBUTE_DESCRIPTION column, respecting the UPDATABLE column to classify fields.
---

# LoanIQ Update REST API — JUnit Test Generation Skill

> **Purpose:** Generate three complete JUnit5 test classes for any LoanIQ Update API stack,
> driven by the `Update` tab of the requirement spreadsheet:
> 1. **`{EntityName}Test`** — entity getter/setter and `@PrePersist` / `@PreUpdate` lifecycle tests
> 2. **`{EntityName}ModelTest`** — model (request POJO) getter/setter tests
> 3. **`Update{EntityName}IntegrationTest`** — service validation and execution tests with Mockito

---

## When to Use This Skill

Use `lending-update-test-rest-api` when:
- Generating JUnit5 test classes for a newly generated Update API stack
- Every `Update` tab attribute needs coverage in all three test classes
- Tests must cover identifier validation, updatable field patching, and read-only field handling

DO NOT use this skill for:
- Integration tests with real DB (use `lending-update-test-api`)
- Create/Query/Delete test generation (use the respective test skills)
- Generating implementation classes (use `lending-update-rest-api`)

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
| `find-update-workflow-sheet.ps1` | Locates `Update` tab, extracts metadata, derives EntityName *(shared from lending-update-rest-api)* |
| `read-update-test-attributes.ps1` | Reads all INPUT fields with **UPDATABLE classification** and **TestHints** derived from `ATTRIBUTE_DESCRIPTION` |

### Invocation sequence

```powershell
$createDir = ".github\skills\lending-create-rest-api\scripts"
$updateDir = ".github\skills\lending-update-rest-api\scripts"
$testDir   = ".github\skills\lending-update-test-rest-api\scripts"

# 1. Resolve spreadsheet from requirements folder; EntityName = xlsx filename (no extension)
$reqDir   = "vistahackathon26\requirements"
$xlsx     = Get-ChildItem $reqDir -Filter "*.xlsx" | Select-Object -First 1
$EntityName = [System.IO.Path]::GetFileNameWithoutExtension($xlsx.Name)

# 2. Extract xlsx
$extracted = & "$createDir\extract-spreadsheet.ps1" -SpreadsheetPath $xlsx.FullName

# 2. Locate Update tab (guardrail: exit 2 if missing)
$meta = & "$updateDir\find-update-workflow-sheet.ps1" -ExtractedPath $extracted
if ($LASTEXITCODE -eq 2) { Write-Error "No Update tab - halting"; exit }

# 3. Read fields with UPDATABLE classification and test hints
$attrs = & "$testDir\read-update-test-attributes.ps1" `
              -ExtractedPath $extracted `
              -SheetFile $meta.SheetFile `
              -OutputJson "C:\Auto\API\update_test_attributes.json"

# Key derived sets
$identifierFields = $attrs | Where-Object { $_.Updatable -eq 'N' -and $_.Required -eq 'Y' }
$updatableFields  = $attrs | Where-Object { $_.Updatable -eq 'Y' }
$readOnlyFields   = $attrs | Where-Object { $_.Updatable -eq 'N' -and $_.Required -eq 'N' }

Write-Host "EntityName         : $($meta.EntityName)"
Write-Host "Identifier field(s): $($identifierFields.Count)"
Write-Host "Updatable fields   : $($updatableFields.Count)"
Write-Host "Read-only fields   : $($readOnlyFields.Count)"
```

---

## Spreadsheet Layout — What Drives Test Generation

| Column | Header | Drives |
|---|---|---|
| O | `ATTRIBUTE_FIELD_NAME` | Test method names, getter/setter calls |
| Q | `DATA_TYPE` | Test value types (`String`, `Boolean`, `LocalDate`) |
| R | `REQUIRED` | Whether null/blank failure tests apply |
| **U** | **`UPDATABLE`** | **Field role: IDENTIFIER / UPDATABLE / READ-ONLY** |
| S | `ATTRIBUTE_DESCRIPTION` | `@DisplayName` content and business-rule tests |
| X | `MAX_SIZE` | Max-length boundary tests |

### UPDATABLE column drives test strategy

| UPDATABLE | REQUIRED | Role | Test strategy |
|---|---|---|---|
| `N` | `Y` | **IDENTIFIER** | null-fails, blank-fails (String), max-length, valid-passes |
| `Y` | `Y` | **UPDATABLE+REQUIRED** | null-fails, valid-passes, max-length, Boolean-default |
| `Y` | `N` | **UPDATABLE+OPTIONAL** | null-allowed, max-length, Boolean-default |
| `N` | `N` | **READ-ONLY** | null-allowed, max-length (informational only in update) |

### TestHints derived from each field

| TestHint | Condition | Test scenario |
|---|---|---|
| `IDENTIFIER_NULL_FAILS` | `Updatable=N, Required=Y` | Null identifier → `assertThrows` |
| `IDENTIFIER_BLANK_FAILS` | `Updatable=N, Required=Y, String` | Blank identifier → `assertThrows` |
| `IDENTIFIER_VALID_PASSES` | `Updatable=N, Required=Y` | Valid identifier → `assertDoesNotThrow` |
| `IDENTIFIER_MAXLENGTH_EXCEEDED` | `Updatable=N, Required=Y, MaxSize>0` | Identifier too long → `assertThrows` |
| `REQUIRED_NULL_FAILS` | `Updatable=Y, Required=Y` | Null updatable field → `assertThrows` |
| `VALID_VALUE_PASSES` | `Updatable=Y, Required=Y` | Valid updatable value → `assertDoesNotThrow` |
| `OPTIONAL_NULL_ALLOWED` | `Required=N` | Null → `assertDoesNotThrow` |
| `MAXLENGTH_EXCEEDED_FAILS` | `MaxSize>0` | String too long → `assertThrows` |
| `MAXLENGTH_BOUNDARY_PASSES` | `MaxSize>0, Updatable=Y` | Exactly MaxSize chars → `assertDoesNotThrow` |
| `BOOLEAN_NULL_DEFAULTS_FALSE` | `Boolean` | Null Boolean → `basicExecute` sets entity field to `false` |
| `DATE_NULL_FAILS` | `LocalDate, Required=Y, Updatable=Y` | Null date → `assertThrows` |
| `BUSINESS_RULE_DATE_NOT_PRIOR` | Description mentions "prior to" | Date constraint scenario |
| `BUSINESS_RULE_ONE_OF_REQUIRED` | Description mentions "either/or" | At least one of two fields |
| `INFORMATIONAL_ONLY` | Description mentions "information only" | Field ignored in processing |
| `BUSINESS_RULE_MODIFYING_CREATES_EVENT` | Description mentions "creates an event" | Amount modification note |

---

## Step 0 — Guardrail: Verify `Update` Tab Exists

Run `find-update-workflow-sheet.ps1`. If exit code = 2, **stop immediately**.

---

## Step 1 — Generate Entity Test Class

**Class name:** `{EntityName}Test`
**File path:** `vistahackathon26/LoanService/src/test/java/com/loanservice/entity/{EntityName}Test.java`

### Rules

1. Package: `com.loanservice.entity`. Imports: `org.junit.jupiter.api.*`, `java.time.*`, `static org.junit.jupiter.api.Assertions.*`.
2. `@BeforeEach setUp()` — instantiates `new {EntityName}()`.
3. For **all** fields from `$attrs` (identifier + updatable + read-only — all become entity columns):
   - **String / LocalDate fields:** one test — `entity.set{Field}(value)` → `assertEquals`.
     - `@DisplayName("{fieldName}: getter and setter work correctly")`
   - **Boolean Y/N fields:** two tests:
     - Default false: `assertFalse(entity.get{Field}())` — `@DisplayName("{fieldName}: defaults to false")`
     - Set true: `entity.set{Field}(Boolean.TRUE)` → `assertTrue` — `@DisplayName("{fieldName}: setter persists true")`
4. Always add two lifecycle tests:
   - `onCreate_setsTimestamps()` — calls `entity.onCreate()`, asserts both timestamps set within time window.
   - `onUpdate_updatesUpdateTimestamp()` — calls `entity.onUpdate()`, asserts `updateTimeStamp` refreshed.
5. Use `@DisplayName` drawn from the first sentence of `ATTRIBUTE_DESCRIPTION`.

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
   - `className_getterSetter()` — sets `"Update{EntityName}Integration"`, asserts equal.
   - `transaction_getterSetter()` — sets `"{EntityName}"`, asserts equal.
4. For every field from `$attrs` (all roles):
   - **String / LocalDate fields:** one test — `request.set{Field}(value)` → `assertEquals`.
   - **Boolean fields:** two tests:
     - Getter/setter: `request.set{Field}(Boolean.TRUE)` → `assertTrue`.
     - Null allowed: `request.set{Field}(null)` → `assertNull(request.get{Field}())`.
5. No lifecycle tests in model class.
6. `@DisplayName` format: `"{fieldName}: getter and setter work correctly"`.

### Reference Implementation

`LoanService/src/test/java/com/loanservice/model/LoanRequestTest.java`

---

## Step 3 — Generate Service Test Class

**Class name:** `Update{EntityName}IntegrationTest`
**File path:** `vistahackathon26/LoanService/src/test/java/com/loanservice/service/Update{EntityName}IntegrationTest.java`

### Rules

1. Package: `com.loanservice.service`. Annotations: `@ExtendWith(MockitoExtension.class)`.
2. `@Mock {EntityName}Repository repository` — Mockito mock.
3. `@InjectMocks Update{EntityName}Integration service`.
4. `@BeforeEach setUp()` — builds a fully valid `{EntityName}Model validRequest` with:
   - All `IDENTIFIER` fields (UPDATABLE=N, REQUIRED=Y) set.
   - All `UPDATABLE+REQUIRED` fields (UPDATABLE=Y, REQUIRED=Y) set.
   - Optional fields left null (populated per-test when needed).

5. For each **IDENTIFIER** field: generate a section with:
   - `{fieldName}_required_nullShouldFail()` — set to null → `assertThrows(IllegalArgumentException)`.
   - If String: `{fieldName}_required_blankShouldFail()` — set to `"  "` → `assertThrows`.
   - If `MaxSize > 0`: `{fieldName}_maxLength_exceeded()` — string of `MaxSize+1` → `assertThrows`.
   - `{fieldName}_valid()` — valid value → `assertDoesNotThrow`.

6. For each **UPDATABLE** field: generate a section with:
   - If `Required=Y`: null-fails test + valid-passes test.
   - If `Required=N, String, MaxSize>0`: max-length-exceeded test.
   - If `Required=N, Boolean`: `{fieldName}_optional_nullDefaultsFalse()` — call `basicExecute` with null field, stub `repository.findById` to return `Optional.empty()`, stub `repository.create` to return argument, assert entity field is `false`.
   - If `Required=N, LocalDate`: `{fieldName}_optional_nullAllowed()` — `assertDoesNotThrow`.
   - Description-based test if `ATTRIBUTE_DESCRIPTION` contains a notable business rule.

7. For each **READ-ONLY** field: generate a minimal section with:
   - `{fieldName}_optional_nullAllowed()` — `assertDoesNotThrow`.
   - If `MaxSize > 0`: `{fieldName}_maxLength_exceeded()`.
   - If Boolean: `{fieldName}_optional_nullDefaultsFalse()`.

8. `basicExecute_existingRecord_updatesAndSaves()` — stub `repository.findById` to return an existing entity, stub `repository.save` to return argument, call `basicExecute`, assert `repository.save` called (not `create`), assert UPDATABLE fields are patched on entity.

9. `basicExecute_newRecord_createsEntity()` — stub `repository.findById` to return `Optional.empty()`, stub `repository.create` to return argument, call `basicExecute`, assert `repository.create` called.

10. Section comment format:
    ```java
    // ---- {fieldName} ----
    // {role: IDENTIFIER | UPDATABLE | READ-ONLY} - {first sentence of ATTRIBUTE_DESCRIPTION}
    ```

### Mockito setup patterns

```java
// For basicExecute tests — existing record path
when(repository.findById(any())).thenReturn(Optional.of(existingEntity));
when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

// For basicExecute tests — new record path
when(repository.findById(any())).thenReturn(Optional.empty());
when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
```

### Reference Implementation

`LoanService/src/test/java/com/loanservice/service/UpdateLoanPrincipalPaymentIntegrationTest.java`

---

## Output File Placement

| Class | Path |
|---|---|
| `{EntityName}Test` | `LoanService/src/test/java/com/loanservice/entity/{EntityName}Test.java` |
| `{EntityName}ModelTest` | `LoanService/src/test/java/com/loanservice/model/{EntityName}ModelTest.java` |
| `Update{EntityName}IntegrationTest` | `LoanService/src/test/java/com/loanservice/service/Update{EntityName}IntegrationTest.java` |

---

## Complete Test Coverage Matrix

| Field role | Entity test | Model test | Service test |
|---|---|---|---|
| **IDENTIFIER** (e.g. `loanTransactionId`) | getter/setter | getter/setter | null-fails, blank-fails, maxLen, valid-passes |
| **UPDATABLE+REQUIRED** (e.g. `requestedAmount`) | getter/setter | getter/setter | null-fails, valid-passes, [maxLen if specified] |
| **UPDATABLE+OPTIONAL Boolean** (e.g. `autoReduceFacility`) | default-false, set-true | getter-true, null-allowed | null-defaults-false in basicExecute |
| **UPDATABLE+OPTIONAL LocalDate** (e.g. `effectiveDate`) | getter/setter | getter/setter | null-fails (if REQUIRED), valid-passes |
| **READ-ONLY String** (e.g. `loanAlias`, `loanId`) | getter/setter | getter/setter | null-allowed, [maxLen if specified] |
| **READ-ONLY Boolean** (e.g. `suppressBreakfunding`) | default-false, set-true | getter-true, null-allowed | null-defaults-false in basicExecute |
| All fields | — | — | `basicExecute_existingRecord_updatesAndSaves` + `basicExecute_newRecord_createsEntity` |
| System | `onCreate_setsTimestamps`, `onUpdate_updatesTimestamp` | — | — |

---

## Guardrail Checklist

Before completing generation, verify:

- [ ] Spreadsheet has an `Update` tab (exit code 2 from find script = halt)
- [ ] All `ATTRIBUTE_FIELD_NAME` fields from the spreadsheet have at least one test in each of the three classes
- [ ] `@DisplayName` text incorporates content from `ATTRIBUTE_DESCRIPTION`
- [ ] IDENTIFIER field has null-fails AND blank-fails (if String) tests
- [ ] UPDATABLE Boolean fields have `null-defaults-false` test via `basicExecute`
- [ ] READ-ONLY fields are tested for null-allowed (not null-fails)
- [ ] `basicExecute_existingRecord_updatesAndSaves` verifies `repository.save()` is called
- [ ] `basicExecute_newRecord_createsEntity` verifies `repository.create()` is called
- [ ] Entity lifecycle methods (`onCreate`, `onUpdate`) tested in entity test class
- [ ] No integration/real-DB calls — all service tests use Mockito only

---

## Common Pitfalls

| Pitfall | Resolution |
|---|---|
| Treating READ-ONLY fields the same as UPDATABLE | READ-ONLY fields use `null-allowed` only — never `null-fails` in service test |
| Missing `Optional` import | Update service uses `java.util.Optional` — always import it |
| Calling only `repository.create()` in `basicExecute` | Update uses upsert: `save()` for existing, `create()` for new — test both paths |
| `@DisplayName` not reflecting `ATTRIBUTE_DESCRIPTION` | Always use the description's first sentence in the display name |
| Lifecycle tests in model class | `onCreate`/`onUpdate` belong only in the entity test |
| UPDATABLE=N field treated as mandatory in service | UPDATABLE=N + REQUIRED=N = informational — only null-allowed test, never null-fails |
| Missing `when(repository.findById(...))` stub | Every `basicExecute` test must stub `findById` — it is always called first |
