$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$tomcatVersion = '10.1.60'
$expectedHash = '706E0FBB79BC476EBA9524C251280259E56B56BCD1298B11FC77A60818AB4D24D811420F3C247699A57B8DB1418FD474868A7B01CA43D6F4BFF788B4E1A0C201'
$installRoot = Join-Path $project 'target\local-tomcat'
$archive = Join-Path $installRoot 'tomcat.zip'
$tomcat = Join-Path $installRoot "apache-tomcat-$tomcatVersion"

if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    throw 'Port 8080 is already in use. Stop the other server, then run this script again.'
}

if (!$env:JAVA_HOME) {
    $localJdk = 'C:\Program Files\Java\jdk-25.0.2'
    if (!(Test-Path (Join-Path $localJdk 'bin\java.exe'))) { throw 'Set JAVA_HOME to your JDK 21+ installation.' }
    $env:JAVA_HOME = $localJdk
}

$maven = (Get-Command mvn.cmd -ErrorAction SilentlyContinue).Source
if (!$maven) {
    $localMaven = 'C:\Program Files\Apache\Maven\apache-maven-3.9.16\bin\mvn.cmd'
    if (!(Test-Path $localMaven)) { throw 'Install Maven 3.9+ or add mvn.cmd to PATH.' }
    $maven = $localMaven
}

Push-Location $project
try {
    & $maven package
    if ($LASTEXITCODE -ne 0) { throw 'Maven build failed.' }
    if (!(Test-Path $tomcat)) {
        New-Item -ItemType Directory -Force -Path $installRoot | Out-Null
        $url = "https://dlcdn.apache.org/tomcat/tomcat-10/v$tomcatVersion/bin/apache-tomcat-$tomcatVersion.zip"
        Write-Host "Downloading Apache Tomcat $tomcatVersion once..."
        try {
            Invoke-WebRequest -Uri $url -OutFile $archive
        } catch {
            $archiveUrl = "https://archive.apache.org/dist/tomcat/tomcat-10/v$tomcatVersion/bin/apache-tomcat-$tomcatVersion.zip"
            Invoke-WebRequest -Uri $archiveUrl -OutFile $archive
        }
        $hash = (Get-FileHash -LiteralPath $archive -Algorithm SHA512).Hash
        if ($hash -ne $expectedHash) { throw 'Tomcat download checksum did not match Apache SHA-512.' }
        Expand-Archive -LiteralPath $archive -DestinationPath $installRoot
        $defaultRoot = Join-Path $tomcat 'webapps\ROOT'
        $resolvedRoot = [IO.Path]::GetFullPath($defaultRoot)
        if (!$resolvedRoot.StartsWith([IO.Path]::GetFullPath($installRoot), [StringComparison]::OrdinalIgnoreCase)) { throw 'Unsafe Tomcat path.' }
        Remove-Item -LiteralPath $defaultRoot -Recurse -Force
        $serverXml = Join-Path $tomcat 'conf\server.xml'
        $configuration = [IO.File]::ReadAllText($serverXml)
        $configuration = $configuration.Replace('<Connector port="8080" protocol="HTTP/1.1"', '<Connector address="127.0.0.1" port="8080" protocol="HTTP/1.1"')
        [IO.File]::WriteAllText($serverXml, $configuration)
    }
    Copy-Item -LiteralPath (Join-Path $project 'target\student-result-management-1.0.0.war') -Destination (Join-Path $tomcat 'webapps\ROOT.war') -Force
    $env:CATALINA_HOME = $tomcat
    if (!$env:SRM_DATA_DIR) { $env:SRM_DATA_DIR = Join-Path $project 'data' }
    Write-Host 'Open http://localhost:8080/login — keep this PowerShell window open. Press Ctrl+C to stop.'
    & (Join-Path $tomcat 'bin\catalina.bat') run
} finally {
    Pop-Location
}
