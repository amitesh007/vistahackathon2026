<#
.SYNOPSIS
    Reads all INPUT field attributes from the "Update" workflow tab of an extracted
    .xlsx spreadsheet specifically for JUnit test generation.

.DESCRIPTION
    Identical column mapping to read-update-workflow-attributes.ps1 but additionally
    captures the full ATTRIBUTE_DESCRIPTION and derives TestHints for each field.
    The UPDATABLE column (U) is preserved so the test generator can distinguish:
        - Identifier fields (UPDATABLE=N, REQUIRED=Y) -> findById + mandatory validation tests
        - Updatable fields   (UPDATABLE=Y)             -> entity-patch tests + optional/max-length
        - Read-only fields   (UPDATABLE=N, REQUIRED=N) -> max-length only (informational)

    Column mapping:
        O  = ATTRIBUTE_FIELD_NAME
        Q  = DATA_TYPE
        R  = REQUIRED
        S  = ATTRIBUTE_DESCRIPTION   <- drives @DisplayName and business-rule scenarios
        U  = UPDATABLE               <- Y=modifiable, N=identifier or read-only
        X  = MAX_SIZE
        Y  = Default Value

    Section boundaries:
        Row A="Input"   -> start
        Row A="SL_NO"   -> header row (skip)
        Row A="OUTPUT"  -> stop

.PARAMETER ExtractedPath
    Path to the directory produced by extract-spreadsheet.ps1.

.PARAMETER SheetFile
    The worksheet XML filename returned by find-update-workflow-sheet.ps1 (e.g. "sheet5.xml").

.PARAMETER OutputJson
    (Optional) Writes the attribute array as JSON to this path.

.OUTPUTS
    Array of PSCustomObject with:
        SlNo          - Serial number
        FieldName     - Java field name (camelCase, trimmed)
        ColumnName    - DB column name (SCREAMING_SNAKE_CASE)
        DataType      - String | Boolean | LocalDate | LocalDateTime
        Required      - Y or N
        Updatable     - Y (modifiable) or N (identifier/read-only)
        IsYNBoolean   - $true when DataType is Boolean
        MaxSize       - Integer max length; -1 if not specified
        DefaultValue  - Default value string
        Description   - Full ATTRIBUTE_DESCRIPTION
        TestHints     - Derived comma-separated test scenario codes

.EXAMPLE
    $attrs = .\read-update-test-attributes.ps1 `
                 -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted" `
                 -SheetFile "sheet5.xml"
    $attrs | Format-Table FieldName, Required, Updatable, TestHints
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ExtractedPath,

    [Parameter(Mandatory = $true)]
    [string]$SheetFile,

    [Parameter(Mandatory = $false)]
    [string]$OutputJson
)

$ErrorActionPreference = 'Stop'

# Validate inputs
$sharedStringsPath = Join-Path $ExtractedPath 'xl\sharedStrings.xml'
$sheetPath         = Join-Path $ExtractedPath "xl\worksheets\$SheetFile"

foreach ($p in @($sharedStringsPath, $sheetPath)) {
    if (-not (Test-Path $p)) {
        Write-Error "Required file not found: $p"
        exit 1
    }
}

# Load shared strings
[xml]$ssXml = [System.IO.File]::ReadAllText($sharedStringsPath)
$strings = @()
foreach ($si in $ssXml.sst.si) {
    if ($si.t -is [string]) { $strings += $si.t }
    elseif ($si.t -ne $null) { $strings += $si.t.InnerText }
    else { $strings += (($si.r | ForEach-Object { $_.t }) -join '') }
}
Write-Host "Shared strings loaded: $($strings.Count)"

[xml]$sheetXml = [System.IO.File]::ReadAllText($sheetPath)
$rows = $sheetXml.worksheet.sheetData.row
Write-Host "Total rows in Update sheet: $($rows.Count)"

# Helper: resolve a cell value by column letter
function Get-CellVal($rowNode, $colLetter) {
    foreach ($c in $rowNode.c) {
        $col = $c.r -replace '\d+', ''
        if ($col -eq $colLetter) {
            if ($c.t -eq 's') { return $strings[[int]$c.v] }
            return $c.v
        }
    }
    return $null
}

# Helper: camelCase -> SCREAMING_SNAKE_CASE
function To-ScreamingSnake([string]$camel) {
    return ([regex]::Replace($camel.Trim(), '(?<=[a-z0-9])([A-Z])', '_$1')).ToUpper()
}

# Helper: derive test hints from field metadata, updatable flag, and description
function Get-TestHints($dataType, $required, $updatable, $maxSize, $description) {
    $hints = @()

    # Identifier field (UPDATABLE=N, REQUIRED=Y)
    if ($updatable -eq 'N' -and $required -eq 'Y') {
        $hints += 'IDENTIFIER_NULL_FAILS'
        $hints += 'IDENTIFIER_BLANK_FAILS'
        $hints += 'IDENTIFIER_VALID_PASSES'
        if ($maxSize -gt 0) {
            $hints += 'IDENTIFIER_MAXLENGTH_EXCEEDED'
        }
    }
    # Updatable fields (UPDATABLE=Y)
    elseif ($updatable -eq 'Y') {
        if ($required -eq 'Y') {
            $hints += 'REQUIRED_NULL_FAILS'
            $hints += 'VALID_VALUE_PASSES'
        } else {
            $hints += 'OPTIONAL_NULL_ALLOWED'
        }
        if ($maxSize -gt 0) {
            $hints += 'MAXLENGTH_EXCEEDED_FAILS'
            $hints += 'MAXLENGTH_BOUNDARY_PASSES'
        }
        if ($dataType -eq 'Boolean') {
            $hints += 'BOOLEAN_NULL_DEFAULTS_FALSE'
        }
        if ($dataType -eq 'LocalDate' -and $required -eq 'Y') {
            $hints += 'DATE_NULL_FAILS'
        }
    }
    # Read-only / informational (UPDATABLE=N, REQUIRED=N)
    else {
        $hints += 'OPTIONAL_NULL_ALLOWED'
        if ($maxSize -gt 0) {
            $hints += 'MAXLENGTH_EXCEEDED_FAILS'
        }
        if ($dataType -eq 'Boolean') {
            $hints += 'BOOLEAN_NULL_DEFAULTS_FALSE'
        }
    }

    # Business-rule hints from description text
    if ($description -imatch 'cannot.*prior|must not be prior|cannot be prior') {
        $hints += 'BUSINESS_RULE_DATE_NOT_PRIOR'
    }
    if ($description -imatch 'either.*or.*must|at least one') {
        $hints += 'BUSINESS_RULE_ONE_OF_REQUIRED'
    }
    if ($description -imatch 'information.?only|ignored in.*input') {
        $hints += 'INFORMATIONAL_ONLY'
    }
    if ($description -imatch 'code table') {
        $hints += 'BUSINESS_RULE_CODE_TABLE'
    }
    if ($description -imatch 'modif.*amount|creates.*event') {
        $hints += 'BUSINESS_RULE_MODIFYING_CREATES_EVENT'
    }

    return $hints -join ','
}

# Parse input section
$inInputSection = $false
$headerRowFound = $false
$attributes     = @()

Write-Host ''
Write-Host 'Scanning Update sheet for INPUT fields (with test hint derivation)...'
Write-Host ('-' * 70)

foreach ($row in $rows) {
    $colA = Get-CellVal $row 'A'
    if ($colA -ne $null) { $colA = $colA.Trim() }

    if ($colA -eq 'Input') {
        $inInputSection = $true
        Write-Host "  [Row $($row.r)] Found 'Input' section marker"
        continue
    }

    if ($inInputSection -and $colA -eq 'SL_NO') {
        $headerRowFound = $true
        Write-Host "  [Row $($row.r)] Column headers - data rows follow"
        continue
    }

    if ($inInputSection -and $colA -eq 'OUTPUT') {
        Write-Host "  [Row $($row.r)] Found 'OUTPUT' - stopping input collection"
        break
    }

    if ($inInputSection -and $headerRowFound) {
        $colO = Get-CellVal $row 'O'
        if (-not $colO) { continue }

        $fieldName   = $colO.Trim()
        $colQ        = Get-CellVal $row 'Q'
        $colR        = Get-CellVal $row 'R'
        $colU        = Get-CellVal $row 'U'
        $colX        = Get-CellVal $row 'X'
        $colY        = Get-CellVal $row 'Y'
        $colS        = Get-CellVal $row 'S'

        $dataType    = if ($colQ) { $colQ.Trim() } else { 'String' }
        $required    = if ($colR -and $colR.Trim() -eq 'Y') { 'Y' } else { 'N' }
        $updatable   = if ($colU -and $colU.Trim() -eq 'Y') { 'Y' } else { 'N' }
        $maxSizeRaw  = if ($colX) { $colX.Trim() } else { '' }
        $maxSizeInt  = -1
        if ($maxSizeRaw -match '^-?\d+$') { $maxSizeInt = [int]$maxSizeRaw }
        if ($maxSizeInt -lt 0) { $maxSizeInt = -1 }

        $isYNBoolean = ($dataType -eq 'Boolean')
        $columnName  = To-ScreamingSnake $fieldName
        $defaultVal  = if ($colY) { $colY.Trim() } else { '' }
        $desc        = if ($colS) { $colS.Trim() } else { '' }
        $testHints   = Get-TestHints $dataType $required $updatable $maxSizeInt $desc

        $attr = [PSCustomObject]@{
            SlNo         = if ($colA) { $colA } else { '' }
            FieldName    = $fieldName
            ColumnName   = $columnName
            DataType     = $dataType
            Required     = $required
            Updatable    = $updatable
            IsYNBoolean  = $isYNBoolean
            MaxSize      = $maxSizeInt
            DefaultValue = $defaultVal
            Description  = $desc
            TestHints    = $testHints
        }

        $attributes += $attr

        $role     = if ($updatable -eq 'Y') { '[UPDATABLE]' } elseif ($required -eq 'Y') { '[IDENTIFIER]' } else { '[READ-ONLY]' }
        $ynLabel  = if ($isYNBoolean) { ' (Y/N)' } else { '' }
        $hintCount = ($testHints -split ',').Count
        Write-Host ("  [Row {0}] {1,-12} {2,-12} {3,-42} {4}{5} -> {6} hints" -f $row.r, $role, $required, $fieldName, $dataType, $ynLabel, $hintCount)
    }
}

Write-Host ('-' * 70)
Write-Host "Total INPUT fields extracted for test generation: $($attributes.Count)"
Write-Host "  Identifier fields : $(@($attributes | Where-Object { $_.Updatable -eq 'N' -and $_.Required -eq 'Y' }).Count)"
Write-Host "  Updatable fields  : $(@($attributes | Where-Object { $_.Updatable -eq 'Y' }).Count)"
Write-Host "  Read-only fields  : $(@($attributes | Where-Object { $_.Updatable -eq 'N' -and $_.Required -eq 'N' }).Count)"

Write-Host ''
Write-Host '=== FIELD SUMMARY WITH TEST HINTS ==='
$attributes | Format-Table SlNo, FieldName, DataType, Required, Updatable, MaxSize, TestHints -AutoSize

if ($OutputJson) {
    $attributes | ConvertTo-Json -Depth 5 | Out-File -FilePath $OutputJson -Encoding UTF8
    Write-Host ''
    Write-Host "Attributes written to: $OutputJson"
}

return $attributes
