$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080/api/v1"
$email = "verify.user@planit.local"
$phone = "+919111111111"
$password = "Password@123"

Write-Host "Verifying backend at $baseUrl ..."

$registerPayload = @{
  email = $email
  phone = $phone
  password = $password
  role = "USER"
  firstName = "Verify"
  lastName = "User"
} | ConvertTo-Json

try {
  Invoke-RestMethod -Method Post -Uri "$baseUrl/auth/register" -ContentType "application/json" -Body $registerPayload | Out-Null
  Write-Host "Register: OK"
}
catch {
  $body = $_.ErrorDetails.Message
  if ($body -and $body -match "already registered") {
    Write-Host "Register: already exists (OK)"
  } else {
    throw
  }
}

$loginPayload = @{
  identifier = $email
  password = $password
} | ConvertTo-Json

$login = Invoke-RestMethod -Method Post -Uri "$baseUrl/auth/login" -ContentType "application/json" -Body $loginPayload
$token = $login.data.accessToken
if (-not $token) { throw "Login failed: no access token" }
Write-Host "Login: OK"

$me = Invoke-RestMethod -Method Get -Uri "$baseUrl/users/me" -Headers @{ Authorization = "Bearer $token" }
if (-not $me.data.id) { throw "Profile read failed" }
Write-Host "Profile: OK"

$products = Invoke-RestMethod -Method Get -Uri "$baseUrl/products"
Write-Host "Products: $($products.data.Count) records"

$bookings = Invoke-RestMethod -Method Get -Uri "$baseUrl/bookings" -Headers @{ Authorization = "Bearer $token" }
Write-Host "Bookings: $($bookings.data.Count) records"

$conversations = Invoke-RestMethod -Method Get -Uri "$baseUrl/chat/conversations" -Headers @{ Authorization = "Bearer $token" }
Write-Host "Conversations: $($conversations.data.Count) records"

$otpPayload = @{
  phone = $phone
  purpose = "LOGIN"
} | ConvertTo-Json

$otp = Invoke-RestMethod -Method Post -Uri "$baseUrl/auth/send-otp" -ContentType "application/json" -Body $otpPayload
if (-not $otp.data.otpId) { throw "Send OTP failed" }
Write-Host "Send OTP: OK"

Invoke-RestMethod -Method Post -Uri "$baseUrl/auth/logout" -Headers @{ Authorization = "Bearer $token" } | Out-Null
Write-Host "Logout: OK"

Write-Host "API verification completed successfully."

