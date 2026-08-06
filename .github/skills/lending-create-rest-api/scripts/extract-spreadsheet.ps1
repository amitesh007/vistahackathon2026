<#
.SYNOPSIS
    Extracts an .xlsx spreadsheet to its raw XML files for downstream parsing.

.DESCRIPTION
    Copies the .xlsx file to a temporary .zip, extracts it, and cleans up the zip.
    The resulting directory contains:
        xl/workbook.xml           – sheet registry
        xl/_rels/workbook.xml.rels – rId → XML file mapping
        xl/sharedStrings.xml      – all string values
        xl/worksheets/sheet*.xml  – individual worksheet data

.PARAMETER SpreadsheetPath
    Full path to the .xlsx file.
    Example: "C:\Auto\API\Principal Payment API v1.10.xlsx"

.PARAMETER OutputPath
    (Optional) Destination directory for extracted files.
    Defaults to a sibling folder with the same base name plus '_extracted'.

.OUTPUTS
    String – the resolved path of the extracted directory.

.EXAMPLE
    .\extract-spreadsheet.ps1 -SpreadsheetPath "C:\Auto\API\Principal Payment API v1.10.xlsx"
    # Extracts to: C:\Auto\API\Principal_Payment_API_v1.10_extracted
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$SpreadsheetPath,

    [Parameter(Mandatory = $false)]
    [string]$OutputPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# ── Validate input ──────────────────────────────────────────────────────────
if (-not (Test-Path $SpreadsheetPath)) {
    Write-Error "Spreadsheet not found: $SpreadsheetPath"
    exit 1
}

$ext = [System.IO.Path]::GetExtension($SpreadsheetPath).ToLower()
if ($ext -ne '.xlsx') {
    Write-Error "File must be a .xlsx spreadsheet. Got: $ext"
    exit 1
}

# ── Derive output directory ─────────────────────────────────────────────────
if (-not $OutputPath) {
    $baseName   = [System.IO.Path]::GetFileNameWithoutExtension($SpreadsheetPath) -replace '\s+', '_'
    $parentDir  = [System.IO.Path]::GetDirectoryName($SpreadsheetPath)
    $OutputPath = Join-Path $parentDir "${baseName}_extracted"
}

# ── Clean any previous extraction ───────────────────────────────────────────
if (Test-Path $OutputPath) {
    Remove-Item -Recurse -Force $OutputPath
    Write-Host "Removed previous extraction: $OutputPath"
}

# ── Copy to temp .zip and expand ────────────────────────────────────────────
$parentDir  = [System.IO.Path]::GetDirectoryName($SpreadsheetPath)
$tempZip    = Join-Path $parentDir ("_tmp_" + [guid]::NewGuid().ToString("N") + ".zip")

Copy-Item $SpreadsheetPath $tempZip
Expand-Archive -Path $tempZip -DestinationPath $OutputPath -Force
Remove-Item $tempZip

# ── Verify expected structure ────────────────────────────────────────────────
$required = @(
    "xl\workbook.xml",
    "xl\_rels\workbook.xml.rels",
    "xl\sharedStrings.xml"
)
foreach ($rel in $required) {
    $full = Join-Path $OutputPath $rel
    if (-not (Test-Path $full)) {
        Write-Error "Expected file missing after extraction: $full"
        exit 1
    }
}

Write-Host ""
Write-Host "Extraction complete: $OutputPath"
Write-Host ""
Write-Host "Key files:"
$required | ForEach-Object { Write-Host "  $_" }
$sheets = Get-ChildItem (Join-Path $OutputPath "xl\worksheets") -Filter "*.xml" |
          Select-Object -ExpandProperty Name
Write-Host "  xl\worksheets\ -> $($sheets -join ', ')"

return $OutputPath
