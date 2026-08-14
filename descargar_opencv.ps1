$jars = @(
    "https://repo1.maven.org/maven2/org/bytedeco/javacv/1.5.9/javacv-1.5.9.jar",
    "https://repo1.maven.org/maven2/org/bytedeco/javacpp/1.5.9/javacpp-1.5.9.jar",
    "https://repo1.maven.org/maven2/org/bytedeco/javacpp/1.5.9/javacpp-1.5.9-windows-x86_64.jar",
    "https://repo1.maven.org/maven2/org/bytedeco/opencv/4.7.0-1.5.9/opencv-4.7.0-1.5.9.jar",
    "https://repo1.maven.org/maven2/org/bytedeco/opencv/4.7.0-1.5.9/opencv-4.7.0-1.5.9-windows-x86_64.jar",
    "https://repo1.maven.org/maven2/com/github/sarxos/webcam-capture/0.3.12/webcam-capture-0.3.12.jar",
    "https://repo1.maven.org/maven2/com/nativelibs4java/bridj/0.7.0/bridj-0.7.0.jar",
    "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.2/slf4j-api-1.7.2.jar"
)

$targetDir = "drivers"

if (!(Test-Path -Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir
}

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
Write-Host "Descarga de librerías completada exitosamente."
