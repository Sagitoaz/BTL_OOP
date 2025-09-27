param(
  [string]$Base="http://127.0.0.1:8443"
)
Write-Host "GET $Base/ping"
try {
  $resp = Invoke-RestMethod -Uri "$Base/ping" -Method GET -Headers @{ "Accept"="application/json" }
  $resp | ConvertTo-Json -Depth 5
} catch {
  Write-Error $_
  exit 1
}
