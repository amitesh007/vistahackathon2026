<#
.SYNOPSIS
    Reads all INPUT field attributes from the "Create" workflow tab of an extracted
    .xlsx spreadsheet specifically for JUnit test generation.

.DESCRIPTION
    Identical parsing logic to read-create-workflow-attributes.ps1 but captures
    additional columns needed for test generation:
        O  = ATTRIBUTE_FIELD_NAME    <- Java field name (camelCase)
        Q  = DATA_TYPE               <- String / Boolean / LocalDate / LocalDateTime
        R  = REQUIRED                <- Y = mandatory, N = optional
        S  = ATTRIBUTE_DESCRIPTION   <- Business rules / validation description
        X  = MAX_SIZE                <- Max string length (-1 = no constraint)
        Y  = Default Value

    Section detection (same as all other tabs):
        Row A="Input"   -> start of input section
        Row A="SL_NO"   -> column header row (skip)
        Row A="OUTPUT"  -> stop collecting

    The ATTRIBUTE_DESCRIPTION drives description-based test scenarios:
        - Mandatory/not-blank validation tests
        - Max-length boundary tests
        - Boolean default value tests
        - Null-allowed tests for optional fields
        - Business rule scenario hints

.PARAMETER ExtractedPath
    Path to the directory produced by extract-spreadsheet.ps1.

.PARAMETER SheetFile
    The worksheet XML filename returned by find-create-workflow-sheet.ps1 (e.g. "sheet3.xml").

.PARAMETER OutputJson
    (Optional) Writes the attribute array as JSON to this path.

.OUTPUTS
    Array of PSCustomObject, each with:
        SlNo          - Serial number
        FieldName     - Java field name (camelCase, trimmed)
        ColumnName    - DB column name (SCREAMING_SNAKE_CASE)
        DataType      - String | Boolean | LocalDate | LocalDateTime
        Required      - Y or N
        IsYNBoolean   - $true when DataType is Boolean
        MaxSize       - Integer max length; -1 if not specified
        DefaultValue  - Default value string (may be empty)
        Description   - Full ATTRIBUTE_DESCRIPTION (used for test @DisplayName and business rule scenarios)
        TestHints     - Derived list of test scenarios for this field

.EXAMPLE
    $attrs = .\read-create-test-attributes.ps1 `
                 -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted" `
                 -SheetFile "sheet3.xml"
    $attrs | Format-Table FieldName, DataType, Required, MaxSize
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

# Load worksheet
[xml]$sheetXml = [System.IO.File]::ReadAllText($sheetPath)
$rows = $sheetXml.worksheet.sheetData.row
Write-Host "Total rows in Create sheet: $($rows.Count)"

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

# Helper: derive test hints from field metadata and description
function Get-TestHints($fieldName, $dataType, $required, $maxSize, $defaultVal, $description) {
    $hints = @()

    # Mandatory field tests
    if ($required -eq 'Y') {
        $hints += 'REQUIRED_NULL_FAILS'
        $hints += 'REQUIRED_BLANK_FAILS'
        $hints += 'VALID_VALUE_PASSES'
    } else {
        $hints += 'OPTIONAL_NULL_ALLOWED'
    }

    # Max length tests for strings
    if ($dataType -eq 'String' -and $maxSize -gt 0) {
        $hints += 'MAXLENGTH_EXCEEDED_FAILS'
        $hints += 'MAXLENGTH_BOUNDARY_PASSES'
    }

    # Boolean default value tests
    if ($dataType -eq 'Boolean') {
        if ($defaultVal -imatch 'false|unchecked|N' -or $defaultVal -eq '') {
            $hints += 'BOOLEAN_DEFAULT_FALSE'
        }
        $hints += 'BOOLEAN_SET_TRUE'
        $hints += 'BOOLEAN_NULL_DEFAULTS_FALSE'
    }

    # LocalDate tests
    if ($dataType -eq 'LocalDate') {
        if ($required -eq 'Y') {
            $hints += 'DATE_NULL_FAILS'
        }
        $hints += 'DATE_VALID_VALUE'
    }

    # Business-rule hints from description
    if ($description -imatch 'cannot.*exceed|must not exceed|cannot be more') {
        if (-not ($hints -contains 'MAXLENGTH_EXCEEDED_FAILS')) {
            $hints += 'BUSINESS_RULE_AMOUNT_EXCEEDED'
        }
    }
    if ($description -imatch 'prior to|must not be prior') {
        $hints += 'BUSINESS_RULE_DATE_NOT_PRIOR'
    }
    if ($description -imatch 'either.*or.*must be provided|at least one') {
        $hints += 'BUSINESS_RULE_ONE_OF_REQUIRED'
    }
    if ($description -imatch 'unique') {
        $hints += 'BUSINESS_RULE_UNIQUE'
    }
    if ($description -imatch 'valid.*code table|code table') {
        $hints += 'BUSINESS_RULE_CODE_TABLE'
    }

    return $hints -join ','
}

# Parse input section
$inInputSection = $false
$headerRowFound = $false
$attributes     = @()

Write-Host ''
Write-Host 'Scanning Create sheet for INPUT fields (with test hint derivation)...'
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
        $colX        = Get-CellVal $row 'X'
        $colY        = Get-CellVal $row 'Y'
        $colS        = Get-CellVal $row 'S'

        $dataType    = if ($colQ) { $colQ.Trim() } else { 'String' }
        $required    = if ($colR -and $colR.Trim() -eq 'Y') { 'Y' } else { 'N' }
        $maxSizeRaw  = if ($colX) { $colX.Trim() } else { '' }
        $maxSizeInt  = -1
        if ($maxSizeRaw -match '^-?\d+$') { $maxSizeInt = [int]$maxSizeRaw }
        if ($maxSizeInt -lt 0) { $maxSizeInt = -1 }

        $isYNBoolean = ($dataType -eq 'Boolean')
        $columnName  = To-ScreamingSnake $fieldName
        $defaultVal  = if ($colY) { $colY.Trim() } else { '' }
        $desc        = if ($colS) { $colS.Trim() } else { '' }
        $testHints   = Get-TestHints $fieldName $dataType $required $maxSizeInt $defaultVal $desc

        $attr = [PSCustomObject]@{
            SlNo         = if ($colA) { $colA } else { '' }
            FieldName    = $fieldName
            ColumnName   = $columnName
            DataType     = $dataType
            Required     = $required
            IsYNBoolean  = $isYNBoolean
            MaxSize      = $maxSizeInt
            DefaultValue = $defaultVal
            Description  = $desc
            TestHints    = $testHints
        }

        $attributes += $attr

        $reqLabel  = if ($attr.Required -eq 'Y') { '[MANDATORY]' } else { '[OPTIONAL]' }
        $ynLabel   = if ($attr.IsYNBoolean) { ' (Y/N Boolean)' } else { '' }
        $sizeLabel = if ($attr.MaxSize -gt 0) { " maxLen=$($attr.MaxSize)" } else { '' }
        $hintCount = ($testHints -split ',').Count
        Write-Host ("  [Row {0}] {1,-12} {2,-42} {3}{4}{5} -> {6} test hints" -f $row.r, $reqLabel, $attr.FieldName, $attr.DataType, $ynLabel, $sizeLabel, $hintCount)
    }
}

Write-Host ('-' * 70)
Write-Host "Total INPUT fields extracted for test generation: $($attributes.Count)"

# Print summary
Write-Host ''
Write-Host '=== FIELD SUMMARY WITH TEST HINTS ==='
$attributes | Format-Table SlNo, FieldName, DataType, Required, MaxSize, TestHints -AutoSize

# Optionally write JSON
if ($OutputJson) {
    $attributes | ConvertTo-Json -Depth 5 | Out-File -FilePath $OutputJson -Encoding UTF8
    Write-Host ''
    Write-Host "Attributes written to: $OutputJson"
}

return $attributes
