[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$InputPath,
    [Parameter(Mandatory = $true)][string]$OutputPdfPath
)

$ErrorActionPreference = "Stop"
$inputFile = (Resolve-Path -LiteralPath $InputPath).Path
$outputFile = [System.IO.Path]::GetFullPath($OutputPdfPath)
$outputDirectory = [System.IO.Path]::GetDirectoryName($outputFile)
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null

$word = $null
$document = $null
try {
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $word.DisplayAlerts = 0
    $document = $word.Documents.Open($inputFile, $false, $true)
    $document.Repaginate()
    $document.ExportAsFixedFormat($outputFile, 17)
}
finally {
    if ($null -ne $document) {
        $document.Close(0)
        [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($document)
    }
    if ($null -ne $word) {
        $word.Quit()
        [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($word)
    }
    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}

Write-Output $outputFile
