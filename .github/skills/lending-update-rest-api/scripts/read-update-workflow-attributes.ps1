<#
.SYNOPSIS
    Reads INPUT field attributes from the "Update" workflow tab of an extracted
    .xlsx spreadsheet for Update API code generation.

.DESCRIPTION
    The Update tab has two sections. This script parses both:

    INPUT section  (between "Input" header and "OUTPUT" marker):
        - Contains all fields that may be submitted in an Update request.
        - Includes both the identifier field (loanTransactionId) and updatable fields.
        - The UPDATABLE column (U) indicates which fields are actually modifiable
          (Y = updatable, N = identifier or read-only).
        - These drive entity columns, model fields, basicValidation(), and basicExecute().

    OUTPUT section (between "OUTPUT" marker and end of data):
        - Contains confirmation response fields returned after update.
        - These are all system-meta (Success, Message, loanTransactionId, updateTimeStamp).
        - Flagged with IsSystemMeta=$true, excluded from entity generation.

    Column mapping:
        A  = SL_NO
        B  = CLASS_NAME
        O  = ATTRIBUTE_FIELD_NAME    <- Java field name (camelCase)
        Q  = DATA_TYPE               <- String / Boolean / LocalDate / LocalDateTime
        R  = REQUIRED                <- Y = mandatory, N = optional
        S  = ATTRIBUTE_DESCRIPTION
        U  = UPDATABLE               <- Y = field can be changed; N = identifier/read-only
        X  = MAX_SIZE                <- String column length (-1 = no constraint)
        Y  = Default Value

    Special rules for Update:
    - The primary key field (UPDATABLE=N, REQUIRED=Y) is the record identifier — used in
      repository.findById() and set on the entity but NOT modified.
    - Fields where UPDATABLE=Y are the actual patch fields mapped from model to entity.
    - All Boolean fields are Y/N booleans (YNBooleanSerializer/Deserializer).
    - updateTimeStamp is a system field managed by @PreUpdate — never accepted from request.

.PARAMETER ExtractedPath
    Path to the directory produced by extract-spreadsheet.ps1.

.PARAMETER SheetFile
    The worksheet XML filename returned by find-update-workflow-sheet.ps1 (e.g. "sheet5.xml").

.PARAMETER OutputJson
    (Optional) If provided, writes both input and output attribute arrays as JSON.

.OUTPUTS
    PSCustomObject with two properties:
        InputFields  - All update input fields (both identifier and updatable)
        OutputFields - Confirmation response fields (all IsSystemMeta=true)

    Each field object has:
        SlNo         - Serial number
        FieldName    - Java field name (camelCase, trimmed)
        ColumnName   - DB column name (SCREAMING_SNAKE_CASE)
        DataType     - String | Boolean | LocalDate | LocalDateTime
        Required     - Y or N
        Updatable    - Y (modifiable field) or N (identifier/read-only)
        IsYNBoolean  - $true when DataType is Boolean
        MaxSize      - Integer max length; -1 if not specified
        DefaultValue - Default value string (may be empty)
        Description  - First 120 chars of description
        IsSystemMeta - $true for response-only meta fields

.EXAMPLE
    $result = .\read-update-workflow-attributes.ps1 `
                  -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted" `
                  -SheetFile "sheet5.xml"

    Write-Host "Identifier field:"
    $result.InputFields | Where-Object { $_.Updatable -eq 'N' -and $_.Required -eq 'Y' } | Format-Table FieldName, DataType

    Write-Host "Updatable fields:"
    $result.InputFields | Where-Object { $_.Updatable -eq 'Y' } | Format-Table FieldName, DataType, Required

.EXAMPLE
    .\read-update-workflow-attributes.ps1 `
        -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted" `
        -SheetFile "sheet5.xml" `
        -OutputJson "C:\Auto\API\update_attributes.json"
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

# System-metadata output field names — excluded from entity/model
$systemMetaFields = @('success', 'Success', 'Message', 'message',
                      'updateTimeStamp', 'UpdateTimeStamp', 'StatusCode', 'statuscode')

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

# Helper: build a field object from a row
function Build-FieldObject($row, $section) {
    $colO = Get-CellVal $row 'O'
    if (-not $colO) { return $null }

    $fieldName = $colO.Trim()
    $colQ = Get-CellVal $row 'Q'
    $colR = Get-CellVal $row 'R'
    $colU = Get-CellVal $row 'U'
    $colX = Get-CellVal $row 'X'
    $colY = Get-CellVal $row 'Y'
    $colS = Get-CellVal $row 'S'
    $colA = Get-CellVal $row 'A'

    $dataType   = if ($colQ) { $colQ.Trim() } else { 'String' }
    $required   = if ($colR -and $colR.Trim() -eq 'Y') { 'Y' } else { 'N' }
    $updatable  = if ($colU -and $colU.Trim() -eq 'Y') { 'Y' } else { 'N' }
    $maxSizeRaw = if ($colX) { $colX.Trim() } else { '' }
    $maxSizeInt = -1
    if ($maxSizeRaw -match '^-?\d+$') { $maxSizeInt = [int]$maxSizeRaw }
    if ($maxSizeInt -lt 0) { $maxSizeInt = -1 }

    $isYNBoolean  = ($dataType -eq 'Boolean')
    $columnName   = To-ScreamingSnake $fieldName
    $defaultVal   = if ($colY) { $colY.Trim() } else { '' }
    $desc         = if ($colS) { $colS.Trim() } else { '' }
    if ($desc.Length -gt 120) { $desc = $desc.Substring(0, 120) }
    $isSystemMeta = ($systemMetaFields -contains $fieldName)

    return [PSCustomObject]@{
        SlNo         = if ($colA) { $colA.Trim() } else { '' }
        FieldName    = $fieldName
        ColumnName   = $columnName
        DataType     = $dataType
        Required     = $required
        Updatable    = $updatable
        IsYNBoolean  = $isYNBoolean
        MaxSize      = $maxSizeInt
        DefaultValue = $defaultVal
        Description  = $desc
        IsSystemMeta = $isSystemMeta
        Section      = $section
    }
}

# State machine to parse INPUT and OUTPUT sections
# Update tab layout:
#   Row N  (A="Input")   -> INPUT section start
#   Row N+1 (A="SL_NO") -> column header row (skip)
#   Rows ...             -> input field data rows (stop at A="OUTPUT")
#   Row M  (A="OUTPUT")  -> OUTPUT section start
#   Row M+1 (A="SL_NO") -> column header row (skip)
#   Rows ...             -> output confirmation fields (stop at end or A matches end sentinel)

$state        = 'BEFORE_INPUT'
$inputFields  = @()
$outputFields = @()

Write-Host ''
Write-Host 'Scanning Update sheet for INPUT and OUTPUT fields...'
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
                    $reqLbl  = if ($f.Required  -eq 'Y') { '[MANDATORY]' } else { '[OPTIONAL]' }
                    $updLbl  = if ($f.Updatable -eq 'Y') { '[UPDATABLE]' } else { '[IDENTIFIER]' }
                    $ynLbl   = if ($f.IsYNBoolean) { ' (Y/N)' } else { '' }
                    Write-Host ("  [Row {0}] {1,-12} {2,-14} {3,-40} {4}{5}" -f $row.r, $reqLbl, $updLbl, $f.FieldName, $f.DataType, $ynLbl)
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
$identifierCount = @($inputFields | Where-Object { $_.Updatable -eq 'N' -and $_.Required -eq 'Y' }).Count
$updatableCount  = @($inputFields | Where-Object { $_.Updatable -eq 'Y' }).Count
Write-Host "INPUT  fields total     : $($inputFields.Count)"
Write-Host "  - Identifier fields   : $identifierCount  (UPDATABLE=N, REQUIRED=Y)"
Write-Host "  - Updatable fields    : $updatableCount   (UPDATABLE=Y)"
Write-Host "OUTPUT fields extracted : $($outputFields.Count) (system-meta only for Update)"

# Print summaries
Write-Host ''
Write-Host '=== IDENTIFIER FIELDS (drives findById lookup — UPDATABLE=N, REQUIRED=Y) ==='
$inputFields | Where-Object { $_.Updatable -eq 'N' -and $_.Required -eq 'Y' } | Format-Table SlNo, FieldName, DataType, MaxSize -AutoSize

Write-Host '=== UPDATABLE FIELDS (patched on entity — UPDATABLE=Y) ==='
$inputFields | Where-Object { $_.Updatable -eq 'Y' } | Format-Table SlNo, FieldName, DataType, Required, IsYNBoolean -AutoSize

Write-Host '=== ALL INPUT FIELDS (entity columns + model fields) ==='
$inputFields | Format-Table SlNo, FieldName, DataType, Required, Updatable, IsYNBoolean, MaxSize -AutoSize

Write-Host '=== OUTPUT FIELDS (response confirmation — all IsSystemMeta=True) ==='
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
