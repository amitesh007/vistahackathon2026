<#
.SYNOPSIS
    Locates the "GetByID" workflow sheet in an extracted .xlsx workbook and
    returns key metadata needed to drive Query API code generation.

.DESCRIPTION
    Reads xl/workbook.xml and xl/_rels/workbook.xml.rels to find the sheet
    whose name matches "getbyid" (case-insensitive).

    GUARDRAIL: If no "GetByID" tab exists the script exits with code 2.
    Callers must check the exit code and halt processing when it is 2.

    If found, reads the metadata section (rows 1-9) of the worksheet to extract:
        INTEGRATION_CLASS  (row 7, column B)  e.g. GetLoanPrincipalPaymentIntegration
        FILE_OP_PATH       (row 5, column B)
        PACKAGE_NAME       (row 9, column B)

    EntityName is derived from INTEGRATION_CLASS by stripping:
        - Leading prefix "Get" or "Query" (whichever is present)
        - Trailing suffix "Integration"

.PARAMETER ExtractedPath
    Path to the directory produced by extract-spreadsheet.ps1.
    Example: "C:\Auto\API\Principal_Payment_API_v1.10_extracted"

.OUTPUTS
    PSCustomObject with:
        SheetFile        - XML filename, e.g. "sheet9.xml"
        SheetName        - Original tab name, e.g. "GetByID"
        IntegrationClass - Value of INTEGRATION_CLASS metadata row
        EntityName       - Derived entity name (prefix/suffix stripped)
        FileOpPath       - Value of FILE_OP_PATH metadata row (may be empty)
        PackageName      - Value of PACKAGE_NAME metadata row (may be empty)

    Exit code 2 = guardrail failure (no GetByID tab found).

.EXAMPLE
    $meta = .\find-getbyid-workflow-sheet.ps1 -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted"
    if ($LASTEXITCODE -eq 2) { Write-Error "No GetByID tab - halting"; exit }
    Write-Host "EntityName : $($meta.EntityName)"
    Write-Host "SheetFile  : $($meta.SheetFile)"
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ExtractedPath
)

$ErrorActionPreference = 'Stop'

# Validate extracted directory
foreach ($relPath in @('xl\workbook.xml', 'xl\_rels\workbook.xml.rels', 'xl\sharedStrings.xml')) {
    if (-not (Test-Path (Join-Path $ExtractedPath $relPath))) {
        Write-Error "Missing expected file '$relPath' under: $ExtractedPath"
        Write-Error 'Run extract-spreadsheet.ps1 first.'
        exit 1
    }
}

# Load workbook XML using namespace manager
$wbPath   = Join-Path $ExtractedPath 'xl\workbook.xml'
$relsPath = Join-Path $ExtractedPath 'xl\_rels\workbook.xml.rels'

[xml]$wb = [System.IO.File]::ReadAllText($wbPath)
$nsm = New-Object System.Xml.XmlNamespaceManager($wb.NameTable)
$nsm.AddNamespace('ns', 'http://schemas.openxmlformats.org/spreadsheetml/2006/main')
$sheetNodes = $wb.SelectNodes('//ns:sheets/ns:sheet', $nsm)

Write-Host 'Available sheets in workbook:'
foreach ($s in $sheetNodes) {
    $rIdAttr = $s.GetAttribute('id', 'http://schemas.openxmlformats.org/officeDocument/2006/relationships')
    Write-Host "  sheetId=$($s.GetAttribute('sheetId'))  rId=$rIdAttr  name='$($s.GetAttribute('name'))'"
}
Write-Host ''

# GUARDRAIL: Find the GetByID sheet (case-insensitive)
$targetSheetNode = $null
foreach ($s in $sheetNodes) {
    if ($s.GetAttribute('name') -imatch '^getbyid$') {
        $targetSheetNode = $s
        break
    }
}

if ($targetSheetNode -eq $null) {
    Write-Error 'GUARDRAIL FAIL: No tab named "GetByID" found in workbook.'
    Write-Error 'Skill execution halted - a GetByID tab is required to proceed with Query API generation.'
    exit 2
}

$sheetTabName = $targetSheetNode.GetAttribute('name')
$sheetRId     = $targetSheetNode.GetAttribute('id', 'http://schemas.openxmlformats.org/officeDocument/2006/relationships')
Write-Host "GetByID sheet found: '$sheetTabName'  rId=$sheetRId"

# Resolve rId to worksheet XML filename via rels content
$relsContent = [System.IO.File]::ReadAllText($relsPath)

$sheetTarget = $null
if ($relsContent -match "Id=""$sheetRId""\s+[^>]*Target=""([^""]+)""") {
    $sheetTarget = $Matches[1]
} elseif ($relsContent -match "Target=""([^""]+)""\s+[^>]*Id=""$sheetRId""") {
    $sheetTarget = $Matches[1]
}

if (-not $sheetTarget) {
    # Fallback: XPath with pkg namespace
    [xml]$rels = $relsContent
    $relNsm = New-Object System.Xml.XmlNamespaceManager($rels.NameTable)
    $relNsm.AddNamespace('pkg', 'http://schemas.openxmlformats.org/package/2006/relationships')
    $relNode = $rels.SelectSingleNode("//pkg:Relationship[@Id='$sheetRId']", $relNsm)
    if ($relNode -eq $null) {
        Write-Error "Cannot resolve relationship rId=$sheetRId"
        exit 1
    }
    $sheetTarget = $relNode.Target
}

$sheetFile = [System.IO.Path]::GetFileName($sheetTarget)
$sheetPath = Join-Path $ExtractedPath "xl\worksheets\$sheetFile"

if (-not (Test-Path $sheetPath)) {
    Write-Error "Sheet XML not found: $sheetPath"
    exit 1
}
Write-Host "Sheet XML file: $sheetFile"

# Load shared strings
[xml]$ssXml = [System.IO.File]::ReadAllText((Join-Path $ExtractedPath 'xl\sharedStrings.xml'))
$strings = @()
foreach ($si in $ssXml.sst.si) {
    if ($si.t -is [string]) { $strings += $si.t }
    elseif ($si.t -ne $null) { $strings += $si.t.InnerText }
    else { $strings += (($si.r | ForEach-Object { $_.t }) -join '') }
}

# Helper: get cell value by column letter
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

# Read metadata rows 1-9
[xml]$sheetXml = [System.IO.File]::ReadAllText($sheetPath)
$rows = $sheetXml.worksheet.sheetData.row

$metaRows = @{}
foreach ($row in $rows) {
    $rNum = [int]$row.r
    if ($rNum -gt 9) { break }
    $keyVal = Get-CellVal $row 'A'
    $bVal   = Get-CellVal $row 'B'
    if ($keyVal) {
        $metaRows[$keyVal.Trim()] = if ($bVal) { $bVal.Trim() } else { '' }
    }
}

Write-Host ''
Write-Host 'Metadata rows extracted:'
foreach ($k in ($metaRows.Keys | Sort-Object)) {
    Write-Host "  $k = $($metaRows[$k])"
}

# Derive entity name from INTEGRATION_CLASS
# Strips "Get" or "Query" prefix AND "Integration" suffix
$integrationClass = $metaRows['INTEGRATION_CLASS']
if (-not $integrationClass) {
    Write-Error 'INTEGRATION_CLASS not found in metadata section (row 7, column B).'
    exit 1
}

$entityName = $integrationClass -replace '^(Get|Query)', '' -replace 'Integration$', ''
Write-Host ''
Write-Host "INTEGRATION_CLASS : $integrationClass"
Write-Host "EntityName derived: $entityName"

# Return result
$result = [PSCustomObject]@{
    SheetFile        = $sheetFile
    SheetName        = $sheetTabName
    IntegrationClass = $integrationClass
    EntityName       = $entityName
    FileOpPath       = if ($metaRows.ContainsKey('FILE_OP_PATH')) { $metaRows['FILE_OP_PATH'] } else { '' }
    PackageName      = if ($metaRows.ContainsKey('PACKAGE_NAME')) { $metaRows['PACKAGE_NAME'] } else { '' }
}

return $result
