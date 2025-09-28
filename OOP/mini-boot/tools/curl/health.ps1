param(
  [string]$Base = "http://127.0.0.1:8080",
  [string]$Path = "/health"
)

$ErrorActionPreference = "Stop"

$uri = "{0}{1}" -f $Base, $Path
Write-Host ">>> REQUEST: GET $uri"

try {
  $resp = Invoke-WebRequest -Uri $uri -Method GET -Headers @{ "Accept" = "application/json" } -UseBasicParsing
  Write-Host ">>> STATUS: $($resp.StatusCode)"
  $body = $resp.Content
  if ([string]::IsNullOrWhiteSpace($body)) { Write-Warning ">>> EMPTY BODY"; exit 2 }
  Write-Host ">>> BODY:"; $body; exit 0
} catch { Write-Error ">>> ERROR: $($_.Exception.Message)"; exit 1 }