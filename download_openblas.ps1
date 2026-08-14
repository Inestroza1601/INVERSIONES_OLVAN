$jars = @(
    "https://repo1.maven.org/maven2/org/bytedeco/openblas/0.3.23-1.5.9/openblas-0.3.23-1.5.9.jar",
    "https://repo1.maven.org/maven2/org/bytedeco/openblas/0.3.23-1.5.9/openblas-0.3.23-1.5.9-windows-x86_64.jar"
)

$targetDir = "drivers"

foreach ($url in $jars) {
    $fileName = [System.IO.Path]::GetFileName($url)
    $destination = Join-Path -Path $targetDir -ChildPath $fileName
    
    if (!(Test-Path -Path $destination)) {
        Write-Host "Descargando $fileName ..."
        Invoke-WebRequest -Uri $url -OutFile $destination
    } else {
        Write-Host "$fileName ya existe. Omitiendo."
    }
}
Write-Host "OpenBLAS descargado."
