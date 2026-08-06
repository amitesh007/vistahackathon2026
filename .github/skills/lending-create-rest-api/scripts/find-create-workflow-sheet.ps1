<#
.SYNOPSIS
    Locates the "Create" workflow sheet in an extracted .xlsx workbook and
    returns the key metadata needed to drive code generation.

.DESCRIPTION
    Reads xl/workbook.xml and xl/_rels/workbook.xml.rels to find the sheet
    whose name matches "create" (case-insensitive).

    If found, reads the metadata section (rows 1-9) of the worksheet to extract:
        INTEGRATION_CLASS  (row 7, column B)  e.g. CreateLoanPrincipalPaymentIntegration
        FILE_OP_PATH       (row 5, column B)  e.g. C:\REST_AUTO_FILE_GEN\principal_payment
        PACKAGE_NAME       (row 9, column B)  e.g. com.misys.liq.api.rest.data.outstanding.principal

    EntityName is derived from INTEGRATION_CLASS by stripping "Create" prefix and
    "Integration" suffix.

.PARAMETER ExtractedPath
    Path to the directory produced by extract-spreadsheet.ps1.
    Example: "C:\Auto\API\Principal_Payment_API_v1.10_extracted"

.OUTPUTS
    PSCustomObject with:
        SheetFile        - XML filename, e.g. "sheet3.xml"
        SheetName        - Original tab name, e.g. "Create"
        IntegrationClass - Value of INTEGRATION_CLASS metadata row
        EntityName       - Derived entity name (prefix/suffix stripped)
        FileOpPath       - Value of FILE_OP_PATH metadata row (may be empty)
        PackageName      - Value of PACKAGE_NAME metadata row (may be empty)

    Exits with code 2 if no "Create" tab exists (guardrail failure).

.EXAMPLE
    $meta = .\find-create-workflow-sheet.ps1 -ExtractedPath "C:\Auto\API\Principal_Payment_API_v1.10_extracted"
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

# Load workbook XML using a namespace manager (workbook.xml has a default namespace)
$wbPath   = Join-Path $ExtractedPath 'xl\workbook.xml'
$relsPath = Join-Path $ExtractedPath 'xl\_rels\workbook.xml.rels'

[xml]$wb   = [System.IO.File]::ReadAllText($wbPath)
[xml]$rels = [System.IO.File]::ReadAllText($relsPath)

$nsm = New-Object System.Xml.XmlNamespaceManager($wb.NameTable)
$nsm.AddNamespace('ns', 'http://schemas.openxmlformats.org/spreadsheetml/2006/main')

$sheetNodes = $wb.SelectNodes('//ns:sheets/ns:sheet', $nsm)

Write-Host 'Available sheets in workbook:'
foreach ($s in $sheetNodes) {
    $rIdAttr = $s.GetAttribute('id', 'http://schemas.openxmlformats.org/officeDocument/2006/relationships')
    Write-Host "  sheetId=$($s.GetAttribute('sheetId'))  rId=$rIdAttr  name='$($s.GetAttribute('name'))'"
}
Write-Host ''

# Find the Create sheet (case-insensitive name match)
$createSheetNode = $null
foreach ($s in $sheetNodes) {
    if ($s.GetAttribute('name') -imatch '^create$') {
        $createSheetNode = $s
        break
    }
}

if ($createSheetNode -eq $null) {
    Write-Error 'GUARDRAIL FAIL: No tab named "Create" found in workbook.'
    Write-Error 'Skill execution halted - a Create tab is required to proceed.'
    exit 2
}

$createSheetName = $createSheetNode.GetAttribute('name')
$createSheetRId  = $createSheetNode.GetAttribute('id', 'http://schemas.openxmlformats.org/officeDocument/2006/relationships')
Write-Host "Create sheet found: '$createSheetName'  rId=$createSheetRId"

# Resolve rId to worksheet XML filename (use raw text search; rels may or may not have namespace)
$relsContent = [System.IO.File]::ReadAllText($relsPath)
if ($relsContent -match "Id=""$createSheetRId""[^/]*/>[^<]*Target=""([^""]+)""" -or
    $relsContent -match "Id=""$createSheetRId""\s+[^>]*Target=""([^""]+)""" -or
    $relsContent -match "Target=""([^""]+)""\s+Id=""$createSheetRId""") {
    $sheetTarget = $Matches[1]
} else {
    # Fallback: use XPath with pkg namespace
    $relNsm = New-Object System.Xml.XmlNamespaceManager($rels.NameTable)
    $relNsm.AddNamespace('pkg', 'http://schemas.openxmlformats.org/package/2006/relationships')
    $relNode = $rels.SelectSingleNode("//pkg:Relationship[@Id='$createSheetRId']", $relNsm)
    if ($relNode -eq $null) {
        Write-Error "Cannot resolve relationship rId=$createSheetRId"
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

# Helper: get cell value by column letter from a row node
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

# Read metadata rows 1-9 from the Create sheet
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
$integrationClass = $metaRows['INTEGRATION_CLASS']
if (-not $integrationClass) {
    Write-Error 'INTEGRATION_CLASS not found in metadata section (row 7, column B).'
    exit 1
}

$entityName = $integrationClass -replace '^Create', '' -replace 'Integration$', ''
Write-Host ''
Write-Host "INTEGRATION_CLASS : $integrationClass"
Write-Host "EntityName derived: $entityName"

# Return result object
$result = [PSCustomObject]@{
    SheetFile        = $sheetFile
    SheetName        = $createSheetName
    IntegrationClass = $integrationClass
    EntityName       = $entityName
    FileOpPath       = if ($metaRows.ContainsKey('FILE_OP_PATH')) { $metaRows['FILE_OP_PATH'] } else { '' }
    PackageName      = if ($metaRows.ContainsKey('PACKAGE_NAME')) { $metaRows['PACKAGE_NAME'] } else { '' }
}

return $result
