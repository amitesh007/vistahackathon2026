<#
.SYNOPSIS
    Reads INPUT and OUTPUT field attributes from the "Delete" workflow tab of an
    extracted .xlsx spreadsheet specifically for JUnit test generation.

.DESCRIPTION
    Parses both sections of the Delete tab with TestHint derivation:

    INPUT section (identifier fields — between "Input" and "OUTPUT"):
        - Drives basicValidation() tests: null-fails, blank-fails, max-length, valid-passes.
        - For Delete, this is typically just the primary key (e.g. loanTransactionId).

    OUTPUT section (confirmation response — between "OUTPUT" and "Back to Index"):
        - All output fields for Delete are system-meta (Success, Message, updateTimeStamp).
        - These are captured but flagged IsSystemMeta=true.
        - They drive basicExecute() response-structure tests (not entity/model tests).

    Column mapping:
        O  = ATTRIBUTE_FIELD_NAME
        Q  = DATA_TYPE
        R  = REQUIRED
        S  = ATTRIBUTE_DESCRIPTION  <- drives @DisplayName and business-rule scenarios
        X  = MAX_SIZE

.PARAMETER ExtractedPath
    Path to the directory produced by extract-spreadsheet.ps1.

.PARAMETER SheetFile
    The worksheet XML filename returned by find-delete-workflow-sheet.ps1 (e.g. "sheet7.xml").

.PARAMETER OutputJson
    (Optional) Writes both input and output attribute arrays as JSON.

.OUTPUTS
    PSCustomObject with:
        InputFields  - Identifier fields (drives basicValidation + entity primary key tests)
        OutputFields - Confirmation response fields (all IsSystemMeta=true)

    Each field object:
        SlNo, FieldName, ColumnName, DataType, Required, IsYNBoolean,
        MaxSize, Description, IsSystemMeta, TestHints

.EXAMPLE
    $result = .\read-delete-test-attributes.ps1 `
                  -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted" `
                  -SheetFile "sheet7.xml"
    $result.InputFields | Format-Table FieldName, Required, TestHints
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

$systemMetaFields = @('success','Success','StatusCode','Message','message',
                      'updateTimeStamp','UpdateTimeStamp','statuscode')

$sharedStringsPath = Join-Path $ExtractedPath 'xl\sharedStrings.xml'
$sheetPath         = Join-Path $ExtractedPath "xl\worksheets\$SheetFile"

foreach ($p in @($sharedStringsPath, $sheetPath)) {
    if (-not (Test-Path $p)) { Write-Error "Required file not found: $p"; exit 1 }
}

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
Write-Host "Total rows in Delete sheet: $($rows.Count)"

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

function TrimVal($v) { if ($v) { return $v.Trim() } return $null }

function To-ScreamingSnake([string]$camel) {
    return ([regex]::Replace($camel.Trim(), '(?<=[a-z0-9])([A-Z])', '_$1')).ToUpper()
}

# Derive test hints for INPUT identifier fields
function Get-InputTestHints($dataType, $required, $maxSize, $description) {
    $hints = @()
    if ($required -eq 'Y') {
        $hints += 'REQUIRED_NULL_FAILS'
        if ($dataType -eq 'String') {
            $hints += 'REQUIRED_BLANK_FAILS'
            $hints += 'REQUIRED_WHITESPACE_FAILS'
        }
        $hints += 'VALID_VALUE_PASSES'
    } else {
        $hints += 'OPTIONAL_NULL_ALLOWED'
    }
    if ($maxSize -gt 0) {
        $hints += 'MAXLENGTH_EXCEEDED_FAILS'
        $hints += 'MAXLENGTH_BOUNDARY_PASSES'
    }
    # Business-rule hints from description
    if ($description -imatch 'must exist|must be.*valid') {
        $hints += 'BUSINESS_RULE_MUST_EXIST'
    }
    return $hints -join ','
}

# Derive test hints for OUTPUT (system-meta) confirmation fields
function Get-OutputTestHints($fieldName, $dataType) {
    $hints = @()
    if ($fieldName -imatch 'success') { $hints += 'EXECUTE_RETURNS_SUCCESS_STATUS' }
    elseif ($fieldName -imatch 'message') { $hints += 'EXECUTE_RETURNS_MESSAGE_WITH_ID' }
    else { $hints += 'SYSTEM_META_CONFIRMATION' }
    return $hints -join ','
}

function Build-Field($row, $section) {
    $colO = Get-CellVal $row 'O'
    if (-not $colO) { return $null }
    $fieldName  = $colO.Trim()
    $dataType   = TrimVal (Get-CellVal $row 'Q')
    $required   = TrimVal (Get-CellVal $row 'R')
    $colX       = TrimVal (Get-CellVal $row 'X')
    $desc       = TrimVal (Get-CellVal $row 'S')
    $colA       = TrimVal (Get-CellVal $row 'A')

    if (-not $dataType)  { $dataType = 'String' }
    $required   = if ($required -eq 'Y') { 'Y' } else { 'N' }
    $maxSizeInt = -1
    if ($colX -match '^-?\d+$') { $maxSizeInt = [int]$colX }
    if ($maxSizeInt -lt 0) { $maxSizeInt = -1 }
    if (-not $desc) { $desc = '' }
    if ($desc.Length -gt 150) { $desc = $desc.Substring(0, 150) }

    $isYNBoolean  = ($dataType -eq 'Boolean')
    $columnName   = To-ScreamingSnake $fieldName
    $isSystemMeta = ($systemMetaFields -contains $fieldName)

    $testHints = if ($section -eq 'INPUT') {
        Get-InputTestHints $dataType $required $maxSizeInt $desc
    } else {
        Get-OutputTestHints $fieldName $dataType
    }

    return [PSCustomObject]@{
        SlNo         = if ($colA) { $colA } else { '' }
        FieldName    = $fieldName
        ColumnName   = $columnName
        DataType     = $dataType
        Required     = $required
        IsYNBoolean  = $isYNBoolean
        MaxSize      = $maxSizeInt
        Description  = $desc
        IsSystemMeta = $isSystemMeta
        Section      = $section
        TestHints    = $testHints
    }
}

# State machine — identical two-zone pattern
$state        = 'BEFORE_INPUT'
$inputFields  = @()
$outputFields = @()

Write-Host ''
Write-Host 'Scanning Delete sheet for INPUT and OUTPUT fields (with test hints)...'
Write-Host ('-' * 70)

foreach ($row in $rows) {
    $colA = Get-CellVal $row 'A'
    if ($colA -ne $null) { $colA = $colA.Trim() }

    switch ($state) {
        'BEFORE_INPUT' {
            if ($colA -eq 'Input') { $state = 'IN_INPUT_HEADER'; Write-Host "  [Row $($row.r)] Found 'Input' section" }
        }
        'IN_INPUT_HEADER' {
            if ($colA -eq 'SL_NO') { $state = 'IN_INPUT'; Write-Host "  [Row $($row.r)] Input headers - data rows follow" }
        }
        'IN_INPUT' {
            if ($colA -eq 'OUTPUT') {
                $state = 'IN_OUTPUT_HEADER'
                Write-Host "  [Row $($row.r)] Found 'OUTPUT' section"
            } else {
                $f = Build-Field $row 'INPUT'
                if ($f) {
                    $inputFields += $f
                    $lbl = if ($f.Required -eq 'Y') { '[MANDATORY]' } else { '[OPTIONAL]' }
                    Write-Host ("  [Row {0}] INPUT  {1,-12} {2,-40} {3} hints={4}" -f $row.r, $lbl, $f.FieldName, $f.DataType, ($f.TestHints -split ',').Count)
                }
            }
        }
        'IN_OUTPUT_HEADER' {
            if ($colA -eq 'SL_NO') { $state = 'IN_OUTPUT'; Write-Host "  [Row $($row.r)] Output headers - data rows follow" }
        }
        'IN_OUTPUT' {
            if ($colA -imatch '^back to index$' -or $colA -eq '') {
                $state = 'DONE'
                Write-Host "  [Row $($row.r)] End of OUTPUT section"
            } else {
                $f = Build-Field $row 'OUTPUT'
                if ($f) {
                    $outputFields += $f
                    Write-Host ("  [Row {0}] OUTPUT {1,-40} {2} [META] hints={3}" -f $row.r, $f.FieldName, $f.DataType, ($f.TestHints -split ',').Count)
                }
            }
        }
        'DONE' { }
    }
}

Write-Host ('-' * 70)
Write-Host "INPUT  fields: $($inputFields.Count) (identifier fields - drive entity @Id and basicValidation tests)"
Write-Host "OUTPUT fields: $($outputFields.Count) (all system-meta - drive basicExecute response tests)"

Write-Host ''
Write-Host '=== INPUT FIELDS (basicValidation tests) ==='
$inputFields | Format-Table SlNo, FieldName, DataType, Required, MaxSize, TestHints -AutoSize

Write-Host '=== OUTPUT FIELDS (basicExecute response confirmation — all IsSystemMeta=True) ==='
$outputFields | Format-Table SlNo, FieldName, DataType, IsSystemMeta, TestHints -AutoSize

$result = [PSCustomObject]@{
    InputFields  = $inputFields
    OutputFields = $outputFields
}

if ($OutputJson) {
    $result | ConvertTo-Json -Depth 6 | Out-File -FilePath $OutputJson -Encoding UTF8
    Write-Host "Attributes written to: $OutputJson"
}

return $result
