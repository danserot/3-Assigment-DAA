param(
    [ValidateSet('build', 'test', 'smoke', 'benchmark', 'demo')]
    [string]$Action = 'test',
    [string]$GraphFile = 'data/connected.txt'
)

$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot

try {
    $jdkCandidates = @()
    if ($env:JAVA_HOME) {
        $jdkCandidates += $env:JAVA_HOME
    }
    $jdkRoots = @((Join-Path $env:ProgramFiles 'Java'), (Join-Path $env:USERPROFILE '.jdks'))
    foreach ($jdkRoot in $jdkRoots) {
        if (Test-Path -LiteralPath $jdkRoot) {
            $jdkCandidates += @(Get-ChildItem -LiteralPath $jdkRoot -Directory |
                Sort-Object Name -Descending | Select-Object -ExpandProperty FullName)
        }
    }

    $selectedJdk = $null
    foreach ($jdkCandidate in $jdkCandidates) {
        $compilerPath = Join-Path $jdkCandidate 'bin/javac.exe'
        if (!(Test-Path -LiteralPath $compilerPath)) { continue }
        $compilerVersion = & $compilerPath -version 2>&1
        if ($LASTEXITCODE -eq 0 -and "$compilerVersion" -match 'javac (\d+)' -and
            [int]$Matches[1] -ge 17) {
            $selectedJdk = $jdkCandidate
            break
        }
    }
    if (!$selectedJdk) {
        throw 'JDK 17+ was not found. Set JAVA_HOME to your JDK installation.'
    }
    $env:JAVA_HOME = $selectedJdk
    $javaExecutable = Join-Path $selectedJdk 'bin/java.exe'

    $mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    $mavenExecutable = if ($mavenCommand) { $mavenCommand.Source } else { $null }
    $jetBrainsRoot = Join-Path $env:ProgramFiles 'JetBrains'
    if (!$mavenExecutable -and (Test-Path -LiteralPath $jetBrainsRoot)) {
        foreach ($installation in (Get-ChildItem -LiteralPath $jetBrainsRoot -Directory |
                Sort-Object Name -Descending)) {
            $candidate = Join-Path $installation.FullName 'plugins/maven/lib/maven3/bin/mvn.cmd'
            if (Test-Path -LiteralPath $candidate) {
                $mavenExecutable = $candidate
                break
            }
        }
    }
    if (!$mavenExecutable) {
        throw 'Maven was not found. Install Maven or run the Maven goals in IntelliJ IDEA.'
    }

    Write-Host "JDK: $selectedJdk"
    Write-Host "Maven: $mavenExecutable"

    $mavenGoal = if ($Action -eq 'test') { 'test' } else { 'package' }
    & $mavenExecutable -B $mavenGoal
    if ($LASTEXITCODE -ne 0) { throw 'Maven build failed.' }

    switch ($Action) {
        'smoke' {
            & $javaExecutable -Xmx2g -jar target/graphs-1.0.0.jar benchmark --smoke
            if ($LASTEXITCODE -ne 0) { throw 'Smoke benchmark failed.' }
        }
        'benchmark' {
            & $javaExecutable -Xmx2g -jar target/graphs-1.0.0.jar benchmark
            if ($LASTEXITCODE -ne 0) { throw 'Full benchmark failed.' }
        }
        'demo' {
            & $javaExecutable -Xmx2g -jar target/graphs-1.0.0.jar demo $GraphFile
            if ($LASTEXITCODE -ne 0) { throw 'Demo failed.' }
        }
    }
} finally {
    Pop-Location
}
