Write-Host "START login.ps1"

param(
  [string]$Base      = "http://127.0.0.1:8080",
  [string]$Username  = "admin",
  [string]$Password  = "admin123",
  [string]$OutTokenFile = "$PSScriptRoot\token.txt"
)

Write-Host "Params: Base=$Base Username=$Username"

$body = @{ username = $Username; password = $Password } | ConvertTo-Json -Depth 3
$uri = "$Base/auth/login"
Write-Host "POST $uri"

try {
  $resp = Invoke-WebRequest -Uri $uri -Method POST -ContentType "application/json" -Body $body -UseBasicParsing
  Write-Host "Status: $($resp.StatusCode)"
  $json = $resp.Content | ConvertFrom-Json
  if (-not $json.access_token) {
    Write-Warning "no access_token in response: $($resp.Content)"
    exit 1
  }
  $token = $json.access_token
  $env:TOKEN = $token
  Set-Content -Path $OutTokenFile -Value $token -NoNewline -Encoding ascii
  Write-Host "TOKEN saved to $OutTokenFile"
  Write-Host "TOKEN (env): $token"
  exit 0
} catch {
  Write-Error "ERROR: $($_.Exception.Message)"
  exit 1
}
