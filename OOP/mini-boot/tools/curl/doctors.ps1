param(
  [string]$Base = "http://127.0.0.1:8080",
  [string]$TokenFile = "$PSScriptRoot\token.txt",
  [string]$Specialty = "ophthalmology",
  [int]$Page = 1,
  [int]$Size = 10
)

$ErrorActionPreference = "Stop"

# Láº¥y token: Æ°u tiÃªn env, fallback file
$token = $env:TOKEN
if ([string]::IsNullOrWhiteSpace($token) -and (Test-Path $TokenFile)) {
  $token = Get-Content -Path $TokenFile -Encoding ascii | Select-Object -First 1
}
if ([string]::IsNullOrWhiteSpace($token)) { Write-Error "no token; run login.ps1 first"; exit 1 }

$uri = "$Base/doctors?specialty=$Specialty&page=$Page&size=$Size"
Write-Host ">>> REQUEST: GET $uri"
try {
  $resp = Invoke-WebRequest -Uri $uri -Method GET -Headers @{
    "Accept" = "application/json"
    "Authorization" = "Bearer $token"
  } -UseBasicParsing
  Write-Host ">>> STATUS: $($resp.StatusCode)"
  if ([string]::IsNullOrWhiteSpace($resp.Content)) { Write-Warning ">>> EMPTY BODY"; exit 2 }
  Write-Host ">>> BODY:"; $resp.Content
  exit 0
} catch {
  Write-Error ">>> ERROR: $($_.Exception.Message)"
  exit 1
}