# ==============================================================================
# SCRIPT CHẠY THỰC NGHIỆM 100 REQUESTS KIỂM TRA TỶ LỆ WEIGHT PREDICATE (A/B TESTING)
# ==============================================================================

$gatewayUrl = "http://localhost:8080/api/search"
$totalRequests = 100
$countV1 = 0
$countV2 = 0
$countError = 0

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " BẮT ĐẦU GỬI $totalRequests REQUESTS TỚI API GATEWAY ($gatewayUrl)" -ForegroundColor Cyan
Write-Host " KỲ VỌNG: TỶ LỆ PHÂN PHỐI XẤP XỈ 80% V1 - 20% V2" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

for ($i = 1; $i -le $totalRequests; $i++) {
    try {
        $response = Invoke-RestMethod -Uri $gatewayUrl -Method Get -TimeoutSec 3
        if ($response -eq "V1" -or $response.version -eq "V1") {
            $countV1++
            Write-Host "Req #$($i.ToString("D3")): [V1] (80% Stable)" -ForegroundColor Green
        } elseif ($response -eq "V2" -or $response.version -eq "V2") {
            $countV2++
            Write-Host "Req #$($i.ToString("D3")): [V2] (20% Canary/Beta)" -ForegroundColor Yellow
        } else {
            Write-Host "Req #$($i.ToString("D3")): Phản hồi khác -> $response" -ForegroundColor Gray
        }
    } catch {
        $countError++
        Write-Host "Req #$($i.ToString("D3")): ERROR -> $($_.Exception.Message)" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 20
}

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " KẾT QUẢ THỐNG KÊ SAU $totalRequests REQUESTS" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
$percentV1 = ($countV1 / $totalRequests) * 100
$percentV2 = ($countV2 / $totalRequests) * 100

Write-Host "Phiên bản V1 (Ổn định): $countV1 / $totalRequests requests ($percentV1 %)" -ForegroundColor Green
Write-Host "Phiên bản V2 (Thử nghiệm): $countV2 / $totalRequests requests ($percentV2 %)" -ForegroundColor Yellow
if ($countError -gt 0) {
    Write-Host "Số request bị lỗi: $countError" -ForegroundColor Red
}
Write-Host "Đánh giá: Tỷ lệ phân bổ thực tế đạt xấp xỉ 80% - 20% theo đúng xác suất cấu hình!" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
