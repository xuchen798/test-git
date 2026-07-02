# 博客系统一键关闭脚本
# 功能：按端口号（后端 8081 / 前端 8082）查找并关闭对应进程
# 使用方式：
#   1) 右键 -> 使用 PowerShell 运行
#   2) 或双击同目录下的 stop-all.cmd

$ErrorActionPreference = "Stop"

$ports = @(
    @{ Port = 8081; Name = "后端(blog-backend)" },
    @{ Port = 8082; Name = "前端(blog-frontend)" }
)

function Get-PidByPort([int]$port) {
    $lines = netstat -ano | Select-String -Pattern "LISTENING"
    $pids = @()
    foreach ($line in $lines) {
        if ($line.Line -match ":$port\s") {
            $parts = $line.Line -split "\s+"
            $pidStr = $parts[$parts.Count - 1]
            if ($pidStr -match "^\d+$") {
                $pids += [int]$pidStr
            }
        }
    }
    return ($pids | Select-Object -Unique)
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "   博客系统一键关闭" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

$killedCount = 0
$skipCount = 0

foreach ($item in $ports) {
    $port = $item.Port
    $name = $item.Name
    Write-Host "[检查] $name 端口 $port ..." -ForegroundColor Gray

    $pids = Get-PidByPort $port

    if (-not $pids -or $pids.Count -eq 0) {
        Write-Host "   -> 未监听，跳过" -ForegroundColor DarkGreen
        $skipCount++
        continue
    }

    foreach ($pid in $pids) {
        try {
            $proc = Get-Process -Id $pid -ErrorAction Stop
            $procName = $proc.ProcessName
            $procPath = $proc.Path
            Write-Host "   -> 发现进程 PID=$pid 名称=$procName" -ForegroundColor Yellow
            if ($procPath) {
                Write-Host "      路径: $procPath" -ForegroundColor Gray
            }
            Stop-Process -Id $pid -Force -ErrorAction Stop
            Start-Sleep -Milliseconds 300
            try {
                $proc2 = Get-Process -Id $pid -ErrorAction SilentlyContinue
                if ($proc2) {
                    Write-Host "      进程仍存在，再次强制结束..." -ForegroundColor Yellow
                    Stop-Process -Id $pid -Force
                }
            } catch {}
            Write-Host "   -> 已关闭 PID=$pid ($name)" -ForegroundColor Green
            $killedCount++
        } catch {
            Write-Host "   -> 关闭 PID=$pid 失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "--------------------------------" -ForegroundColor Cyan
Write-Host "完成: 已关闭 $killedCount 个进程，跳过 $skipCount 个未占用端口" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Start-Sleep -Seconds 1
