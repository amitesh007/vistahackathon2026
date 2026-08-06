<#
.SYNOPSIS
    Reads all INPUT field attributes from the "Create" workflow tab of an
    extracted .xlsx spreadsheet and outputs structured data for code generation.

.DESCRIPTION
    Parses the Create worksheet XML to extract every INPUT field defined between
    the "Input" section header and the "OUTPUT" section header.

    Spreadsheet column mapping (Principal Payment API layout):
        A  = SL_NO
        B  = CLASS_NAME
        O  = ATTRIBUTE_FIELD_NAME    <- Java field name (camelCase)
        Q  = DATA_TYPE               <- String / Boolean / LocalDate / LocalDateTime
        R  = REQUIRED                <- Y = mandatory, N = optional
        S  = ATTRIBUTE_DESCRIPTION
        X  = MAX_SIZE                <- String column length (blank = no constraint)
        Y  = Default Value

    Section detection rules:
        - Row where A = "Input"   -> start of input section
        - Row where A = "SL_NO"   -> column header row (skip)
        - Row where A = "OUTPUT"  -> end of input section (stop)

    All Boolean fields are treated as Y/N booleans requiring
    YNBooleanSerializer (entity) and YNBooleanDeserializer (model).

    DB column names are auto-derived by converting camelCase to SCREAMING_SNAKE_CASE.

.PARAMETER ExtractedPath
    Path to the directory produced by extract-spreadsheet.ps1.

.PARAMETER SheetFile
    The worksheet XML filename returned by find-create-workflow-sheet.ps1 (e.g. "sheet3.xml").

.PARAMETER OutputJson
    (Optional) If provided, writes the attribute array as JSON to this path.

.OUTPUTS
    Array of PSCustomObject, each with:
        SlNo         - Serial number from spreadsheet
        FieldName    - Java field name (camelCase)
        ColumnName   - DB column name (SCREAMING_SNAKE_CASE)
        DataType     - String | Boolean | LocalDate | LocalDateTime
        Required     - Y or N
        IsYNBoolean  - $true when DataType is Boolean
        MaxSize      - Integer max length; -1 if not specified
        DefaultValue - Default value string (may be empty)
        Description  - First 120 chars of attribute description

.EXAMPLE
    $attrs = .\read-create-workflow-attributes.ps1 `
                -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted" `
                -SheetFile "sheet3.xml"
    $attrs | Format-Table FieldName, DataType, Required, MaxSize

.EXAMPLE
    .\read-create-workflow-attributes.ps1 `
        -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted" `
        -SheetFile "sheet3.xml" `
        -OutputJson "C:\Auto\API\create_attributes.json"
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
    $snake = [regex]::Replace($camel, '(?<=[a-z0-9])([A-Z])', '_$1')
    return $snake.ToUpper()
}

# Locate section boundaries and collect input fields
# Layout:
#   Row N (A="Input")   -> INPUT section start marker
#   Row N+1 (A="SL_NO") -> column header row (skip it)
#   Rows N+2..M-1       -> data rows
#   Row M (A="OUTPUT")  -> stop

$inInputSection = $false
$headerRowFound = $false
$attributes     = @()

Write-Host ''
Write-Host 'Scanning Create sheet for INPUT fields...'
Write-Host ('-' * 70)

foreach ($row in $rows) {
    $colA = Get-CellVal $row 'A'
    if ($colA -ne $null) { $colA = $colA.Trim() }

    $colO = Get-CellVal $row 'O'   # ATTRIBUTE_FIELD_NAME
    $colQ = Get-CellVal $row 'Q'   # DATA_TYPE
    $colR = Get-CellVal $row 'R'   # REQUIRED
    $colX = Get-CellVal $row 'X'   # MAX_SIZE
    $colY = Get-CellVal $row 'Y'   # Default Value
    $colS = Get-CellVal $row 'S'   # ATTRIBUTE_DESCRIPTION

    # Detect Input section header
    if ($colA -eq 'Input') {
        $inInputSection = $true
        Write-Host "  [Row $($row.r)] Found 'Input' section header"
        continue
    }

    # Detect column header row inside Input section
    if ($inInputSection -and $colA -eq 'SL_NO') {
        $headerRowFound = $true
        Write-Host "  [Row $($row.r)] Found column header row - data rows follow"
        continue
    }

    # Detect Output section - stop collecting input fields
    if ($inInputSection -and $colA -eq 'OUTPUT') {
        Write-Host "  [Row $($row.r)] Found 'OUTPUT' section - stopping input field collection"
        break
    }

    # Collect field row
    if ($inInputSection -and $headerRowFound -and $colO) {
        $fieldName = $colO.Trim()
        $dataType  = if ($colQ) { $colQ.Trim() } else { 'String' }
        $required  = if ($colR -and $colR.Trim() -eq 'Y') { 'Y' } else { 'N' }
        $maxSizeRaw = if ($colX) { $colX.Trim() } else { '' }
        $maxSizeInt = -1
        if ($maxSizeRaw -match '^\d+$') { $maxSizeInt = [int]$maxSizeRaw }

        $isYNBoolean = ($dataType -eq 'Boolean')
        $columnName  = To-ScreamingSnake $fieldName
        $defaultVal  = if ($colY) { $colY.Trim() } else { '' }
        $desc        = if ($colS) { $colS.Trim() } else { '' }
        if ($desc.Length -gt 120) { $desc = $desc.Substring(0, 120) }

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
        }

        $attributes += $attr

        $reqLabel  = if ($attr.Required -eq 'Y') { '[MANDATORY]' } else { '[OPTIONAL]' }
        $ynLabel   = if ($attr.IsYNBoolean) { ' (Y/N Boolean)' } else { '' }
        $sizeLabel = if ($attr.MaxSize -gt 0) { " maxLen=$($attr.MaxSize)" } else { '' }
        Write-Host ("  [Row {0}] {1,-12} {2,-42} {3}{4}{5}" -f $row.r, $reqLabel, $attr.FieldName, $attr.DataType, $ynLabel, $sizeLabel)
    }
}

Write-Host ('-' * 70)
Write-Host "Total INPUT fields extracted: $($attributes.Count)"

if ($attributes.Count -eq 0) {
    Write-Warning "No input fields found. Verify the spreadsheet has an 'Input' section followed by an 'OUTPUT' section in the Create tab."
}

# Optionally write JSON output
if ($OutputJson) {
    $attributes | ConvertTo-Json -Depth 5 | Out-File -FilePath $OutputJson -Encoding UTF8
    Write-Host ''
    Write-Host "Attributes written to: $OutputJson"
}

# Print summary table
Write-Host ''
Write-Host '=== FIELD SUMMARY ==='
$attributes | Format-Table SlNo, FieldName, DataType, Required, IsYNBoolean, MaxSize -AutoSize

return $attributes
