$body = @{
    email = "admin@cvr.ac.in"
    password = "admin123"
} | ConvertTo-Json

Write-Host "Testing login with admin@cvr.ac.in / admin123..." -ForegroundColor Yellow
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/auth/login' `
        -Method POST `
        -Body $body `
        -ContentType 'application/json' `
        -UseBasicParsing
    
    Write-Host "✅ Login successful!" -ForegroundColor Green
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    Write-Host $response.Content
    
} catch {
    Write-Host "❌ Login failed!" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $responseBody = $reader.ReadToEnd()
    
    Write-Host "Error Response:" -ForegroundColor Red
    Write-Host $responseBody
}
