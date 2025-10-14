# 🚀 JAVAFX FRONTEND LAUNCHER - NGÀY 8 INTEGRATION
# Launch JavaFX application với REST backend integration

Write-Host "==== JAVAFX FRONTEND LAUNCHER ====" -ForegroundColor Green

$javaFXPath = "c:\BTL_OOP\BTL_OOP\OOP\oop_ui"
$backendUrl = "http://localhost:8080"

function Test-Backend-Connection {
    try {
        Write-Host "🔍 Kiểm tra kết nối backend..." -ForegroundColor Yellow
        $response = Invoke-RestMethod -Uri "$backendUrl/health" -Method GET -TimeoutSec 5
        Write-Host "✅ Backend server đã sẵn sàng!" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "❌ Không thể kết nối backend server!" -ForegroundColor Red
        return $false
    }
}

function Start-Backend-Instructions {
    Write-Host "`n📋 HƯỚNG DẪN KHỞI ĐỘNG BACKEND:" -ForegroundColor Cyan
    Write-Host "1. Mở terminal mới" -ForegroundColor White
    Write-Host "2. cd c:\BTL_OOP\BTL_OOP\OOP\mini-boot" -ForegroundColor White  
    Write-Host "3. mvn compile exec:java" -ForegroundColor White
    Write-Host "4. Đợi thấy 'Server started on port 8080'" -ForegroundColor White
    Write-Host "5. Chạy lại script này" -ForegroundColor White
    Write-Host ""
    Write-Host "Hoặc chạy song song:" -ForegroundColor Yellow
    Write-Host "Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd c:\BTL_OOP\BTL_OOP\OOP\mini-boot; mvn compile exec:java'" -ForegroundColor Gray
}

function Launch-JavaFX {
    Write-Host "`n🚀 Khởi động JavaFX Frontend..." -ForegroundColor Green
    
    try {
        Set-Location $javaFXPath
        Write-Host "📂 Current directory: $(Get-Location)" -ForegroundColor Gray
        
        # Check if pom.xml exists
        if (!(Test-Path "pom.xml")) {
            Write-Host "❌ Không tìm thấy pom.xml trong $javaFXPath" -ForegroundColor Red
            return $false
        }
        
        Write-Host "🔄 Đang compile và chạy JavaFX application..." -ForegroundColor Yellow
        Write-Host "💡 Có thể mất vài phút lần đầu tiên..." -ForegroundColor Cyan
        
        # Launch JavaFX with Maven
        $process = Start-Process "mvn" -ArgumentList "clean", "javafx:run" -NoNewWindow -PassThru -Wait
        
        if ($process.ExitCode -eq 0) {
            Write-Host "✅ JavaFX application đã chạy thành công!" -ForegroundColor Green
            return $true
        } else {
            Write-Host "❌ Có lỗi khi chạy JavaFX application (Exit code: $($process.ExitCode))" -ForegroundColor Red
            return $false
        }
        
    } catch {
        Write-Host "❌ Lỗi khi khởi động JavaFX: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# ======================
# MAIN EXECUTION
# ======================

Write-Host "🎯 NGÀY 8 - FRONTEND BACKEND INTEGRATION" -ForegroundColor Cyan
Write-Host "Ứng dụng Inventory Management System" -ForegroundColor White
Write-Host ""

# Check backend first
if (-not (Test-Backend-Connection)) {
    Write-Host "⚠️  Backend server chưa sẵn sàng!" -ForegroundColor Yellow
    Start-Backend-Instructions
    
    Write-Host "`n❓ Bạn có muốn thử khởi động frontend không? (y/N): " -ForegroundColor Yellow -NoNewline
    $choice = Read-Host
    
    if ($choice -ne "y" -and $choice -ne "Y") {
        Write-Host "👋 Thoát. Khởi động backend trước rồi chạy lại script này." -ForegroundColor White
        exit 1
    }
}

Write-Host "`n🎊 SẴN SÀNG KHỞI ĐỘNG FRONTEND!" -ForegroundColor Green
Write-Host ""

# Launch JavaFX
$success = Launch-JavaFX

if ($success) {
    Write-Host "`n🎉 FRONTEND LAUNCHED SUCCESSFULLY!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 FEATURES AVAILABLE:" -ForegroundColor Cyan
    Write-Host "✅ Inventory Management với REST API" -ForegroundColor White
    Write-Host "✅ Stock Movement Tracking" -ForegroundColor White
    Write-Host "✅ Real-time Alert System" -ForegroundColor White
    Write-Host "✅ Product CRUD Operations" -ForegroundColor White
    Write-Host "✅ Initial Stock Management" -ForegroundColor White
    Write-Host ""
    Write-Host "🔗 Backend API: $backendUrl" -ForegroundColor Gray
    Write-Host "🖥️  Frontend: JavaFX Application" -ForegroundColor Gray
    Write-Host ""
    Write-Host "🎊 NGÀY 8 HOÀN THÀNH THÀNH CÔNG! 🎊" -ForegroundColor Green
} else {
    Write-Host "`n❌ FRONTEND LAUNCH FAILED" -ForegroundColor Red
    Write-Host ""
    Write-Host "💡 TROUBLESHOOTING:" -ForegroundColor Yellow
    Write-Host "1. Đảm bảo Java 11+ được cài đặt" -ForegroundColor White
    Write-Host "2. Kiểm tra Maven hoạt động: mvn --version" -ForegroundColor White
    Write-Host "3. Thử clean build: mvn clean compile" -ForegroundColor White
    Write-Host "4. Kiểm tra module-info.java hợp lệ" -ForegroundColor White
    Write-Host "5. Đảm bảo không có conflict JavaFX version" -ForegroundColor White
    
    Write-Host "`n🔧 Manual Launch Commands:" -ForegroundColor Cyan
    Write-Host "cd $javaFXPath" -ForegroundColor Gray
    Write-Host "mvn clean compile" -ForegroundColor Gray  
    Write-Host "mvn javafx:run" -ForegroundColor Gray
}

Write-Host "`n👋 Script completed. Press any key to exit..." -ForegroundColor White
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")