<#
.SYNOPSIS
    Reads INPUT field attributes from the "Delete" workflow tab of an extracted
    .xlsx spreadsheet for Delete API code generation.

.DESCRIPTION
    The Delete tab has two sections. This script parses both:

    INPUT section  (between "Input" header and "OUTPUT" marker):
        - Contains the identifier field(s) used to locate the record to delete
          (typically just the primary key, e.g. loanTransactionId).
        - These drive basicValidation() in the service class.

    OUTPUT section (between "OUTPUT" marker and "Back to Index" / end):
        - Contains confirmation fields returned after deletion.
        - These are all system-meta (Success, Message, updateTimeStamp) and
          are NOT mapped to entity columns.
        - They are captured and flagged with IsSystemMeta=$true.

    Column mapping (identical to Create/GetByID tabs):
        A  = SL_NO
        B  = CLASS_NAME
        O  = ATTRIBUTE_FIELD_NAME    <- Java field name (camelCase)
        Q  = DATA_TYPE               <- String / Boolean / LocalDate / LocalDateTime
        R  = REQUIRED                <- Y = mandatory, N = optional
        S  = ATTRIBUTE_DESCRIPTION
        X  = MAX_SIZE                <- String column length (-1 = no constraint)

    System-metadata output fields (Success, Message, updateTimeStamp) are
    excluded from entity/model generation — only InputFields drive the entity.

.PARAMETER ExtractedPath
    Path to the directory produced by extract-spreadsheet.ps1.

.PARAMETER SheetFile
    The worksheet XML filename returned by find-delete-workflow-sheet.ps1 (e.g. "sheet7.xml").

.PARAMETER OutputJson
    (Optional) If provided, writes both input and output attribute arrays as JSON.

.OUTPUTS
    PSCustomObject with two properties:
        InputFields  - Identifier fields (drives entity, model, validation)
        OutputFields - Confirmation response fields (all IsSystemMeta=true)

    Each field object has:
        SlNo         - Serial number
        FieldName    - Java field name (camelCase, trimmed)
        ColumnName   - DB column name (SCREAMING_SNAKE_CASE)
        DataType     - String | Boolean | LocalDate | LocalDateTime
        Required     - Y or N
        IsYNBoolean  - $true when DataType is Boolean
        MaxSize      - Integer max length; -1 if not specified
        Description  - First 120 chars of description
        IsSystemMeta - $true for output-only meta fields (Success, Message, updateTimeStamp)

.EXAMPLE
    $result = .\read-delete-workflow-attributes.ps1 `
                  -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted" `
                  -SheetFile "sheet7.xml"

    Write-Host "Input (identifier) fields:"
    $result.InputFields | Format-Table FieldName, DataType, Required

    Write-Host "Output (response) fields:"
    $result.OutputFields | Format-Table FieldName, DataType, IsSystemMeta

.EXAMPLE
    .\read-delete-workflow-attributes.ps1 `
        -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted" `
        -SheetFile "sheet7.xml" `
        -OutputJson "C:\Auto\API\delete_attributes.json"
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

# System-metadata field names: present in OUTPUT but never mapped to entity columns
$systemMetaFields = @('success', 'Success', 'StatusCode', 'Message', 'message',
                      'updateTimeStamp', 'UpdateTimeStamp', 'statuscode')

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
Write-Host "Total rows in Delete sheet: $($rows.Count)"

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

# Helper: build a field object from a row
function Build-FieldObject($row, $section) {
    $colO = Get-CellVal $row 'O'
    if (-not $colO) { return $null }

    $fieldName = $colO.Trim()
    $colQ = Get-CellVal $row 'Q'
    $colR = Get-CellVal $row 'R'
    $colX = Get-CellVal $row 'X'
    $colS = Get-CellVal $row 'S'
    $colA = Get-CellVal $row 'A'

    $dataType   = if ($colQ) { $colQ.Trim() } else { 'String' }
    $required   = if ($colR -and $colR.Trim() -eq 'Y') { 'Y' } else { 'N' }
    $maxSizeRaw = if ($colX) { $colX.Trim() } else { '' }
    $maxSizeInt = -1
    if ($maxSizeRaw -match '^-?\d+$') { $maxSizeInt = [int]$maxSizeRaw }
    if ($maxSizeInt -lt 0) { $maxSizeInt = -1 }

    $isYNBoolean  = ($dataType -eq 'Boolean')
    $columnName   = To-ScreamingSnake $fieldName
    $desc         = if ($colS) { $colS.Trim() } else { '' }
    if ($desc.Length -gt 120) { $desc = $desc.Substring(0, 120) }
    $isSystemMeta = ($systemMetaFields -contains $fieldName)

    return [PSCustomObject]@{
        SlNo         = if ($colA) { $colA.Trim() } else { '' }
        FieldName    = $fieldName
        ColumnName   = $columnName
        DataType     = $dataType
        Required     = $required
        IsYNBoolean  = $isYNBoolean
        MaxSize      = $maxSizeInt
        Description  = $desc
        IsSystemMeta = $isSystemMeta
        Section      = $section
    }
}

# State machine to parse both INPUT and OUTPUT sections
# Delete tab layout (same two-zone pattern as GetByID):
#   Row N  (A="Input")   -> INPUT section start
#   Row N+1 (A="SL_NO") -> column header row (skip)
#   Rows ...             -> input identifier fields (stop at A="OUTPUT")
#   Row M  (A="OUTPUT")  -> OUTPUT section start
#   Row M+1 (A="SL_NO") -> column header row (skip)
#   Rows ...             -> output confirmation fields (stop at A="Back to Index" or end)

$state        = 'BEFORE_INPUT'
$inputFields  = @()
$outputFields = @()

Write-Host ''
Write-Host 'Scanning Delete sheet for INPUT and OUTPUT fields...'
Write-Host ('-' * 70)

foreach ($row in $rows) {
    $colA = Get-CellVal $row 'A'
    if ($colA -ne $null) { $colA = $colA.Trim() }

    switch ($state) {
        'BEFORE_INPUT' {
            if ($colA -eq 'Input') {
                $state = 'IN_INPUT_HEADER'
                Write-Host "  [Row $($row.r)] Found 'Input' section marker"
            }
        }
        'IN_INPUT_HEADER' {
            if ($colA -eq 'SL_NO') {
                $state = 'IN_INPUT'
                Write-Host "  [Row $($row.r)] Input column headers - data rows follow"
            }
        }
        'IN_INPUT' {
            if ($colA -eq 'OUTPUT') {
                $state = 'IN_OUTPUT_HEADER'
                Write-Host "  [Row $($row.r)] Found 'OUTPUT' section marker"
            } else {
                $f = Build-FieldObject $row 'INPUT'
                if ($f) {
                    $inputFields += $f
                    $lbl = if ($f.Required -eq 'Y') { '[MANDATORY]' } else { '[OPTIONAL]' }
                    Write-Host ("  [Row {0}] INPUT  {1,-12} {2,-40} {3}" -f $row.r, $lbl, $f.FieldName, $f.DataType)
                }
            }
        }
        'IN_OUTPUT_HEADER' {
            if ($colA -eq 'SL_NO') {
                $state = 'IN_OUTPUT'
                Write-Host "  [Row $($row.r)] Output column headers - data rows follow"
            }
        }
        'IN_OUTPUT' {
            if ($colA -imatch '^back to index$' -or $colA -eq '') {
                $state = 'DONE'
                Write-Host "  [Row $($row.r)] End of OUTPUT section"
            } else {
                $f = Build-FieldObject $row 'OUTPUT'
                if ($f) {
                    $outputFields += $f
                    $metaTag = if ($f.IsSystemMeta) { ' [META]' } else { '' }
                    $lbl = if ($f.Required -eq 'Y') { '[MANDATORY]' } else { '[OPTIONAL]' }
                    Write-Host ("  [Row {0}] OUTPUT {1,-12} {2,-40} {3}{4}" -f $row.r, $lbl, $f.FieldName, $f.DataType, $metaTag)
                }
            }
        }
        'DONE' { }
    }
}

Write-Host ('-' * 70)
Write-Host "INPUT  fields extracted : $($inputFields.Count)"
Write-Host "OUTPUT fields extracted : $($outputFields.Count) (all are system-meta for Delete)"

# Print summaries
Write-Host ''
Write-Host '=== INPUT FIELDS (identifier — drives entity primary key and basicValidation) ==='
$inputFields | Format-Table SlNo, FieldName, DataType, Required, MaxSize -AutoSize

Write-Host '=== OUTPUT FIELDS (response confirmation — all IsSystemMeta=True, excluded from entity) ==='
$outputFields | Format-Table SlNo, FieldName, DataType, IsSystemMeta -AutoSize

# Build result
$result = [PSCustomObject]@{
    InputFields  = $inputFields
    OutputFields = $outputFields
}

# Optionally write JSON
if ($OutputJson) {
    $result | ConvertTo-Json -Depth 6 | Out-File -FilePath $OutputJson -Encoding UTF8
    Write-Host ''
    Write-Host "Attributes written to: $OutputJson"
}

return $result
