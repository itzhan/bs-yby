#!/bin/bash
# ============================================================
# 校园招聘系统 - 停止脚本 (Mac/Linux)
# ============================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目目录
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

PIDS_DIR=".pids"
LOGS_DIR=".logs"

# 端口配置
BACKEND_PORT=8080
ADMIN_PORT=8001
FRONTEND_PORT=8002

echo -e "${CYAN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║              校园招聘系统 - 停止脚本                       ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# 停止函数
stop_service() {
    local name=$1
    local pid_file=$2
    local port=$3
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo -e "${YELLOW}[!]${NC} 正在停止 $name (PID: $pid)..."
            kill "$pid" 2>/dev/null || true
            sleep 2
            # 如果还在运行，强制杀死
            if ps -p "$pid" > /dev/null 2>&1; then
                kill -9 "$pid" 2>/dev/null || true
            fi
            echo -e "${GREEN}[✓]${NC} $name 已停止"
        else
            echo -e "${YELLOW}[!]${NC} $name 进程不存在 (PID: $pid)"
        fi
        rm -f "$pid_file"
    else
        echo -e "${YELLOW}[!]${NC} $name PID 文件不存在"
    fi
    
    # 通过端口杀死进程
    local port_pids=$(lsof -ti:$port 2>/dev/null || true)
    if [ -n "$port_pids" ]; then
        echo -e "${YELLOW}[!]${NC} 正在停止占用端口 $port 的进程..."
        echo "$port_pids" | xargs kill -9 2>/dev/null || true
        echo -e "${GREEN}[✓]${NC} 端口 $port 已释放"
    fi
}

# 停止所有服务
stop_service "后端" "$PIDS_DIR/backend.pid" $BACKEND_PORT
stop_service "管理端" "$PIDS_DIR/admin.pid" $ADMIN_PORT
stop_service "前端" "$PIDS_DIR/frontend.pid" $FRONTEND_PORT

# 停止 tail 进程
echo -e "\n${YELLOW}[!]${NC} 正在停止日志监控进程..."
pkill -f "tail.*\.logs" 2>/dev/null || true
echo -e "${GREEN}[✓]${NC} 日志监控已停止"

# 清理 PID 目录
if [ -d "$PIDS_DIR" ]; then
    rm -rf "$PIDS_DIR"
    echo -e "${GREEN}[✓]${NC} PID 文件已清理"
fi

# 可选：清理日志目录（注释掉以保留日志）
# if [ -d "$LOGS_DIR" ]; then
#     rm -rf "$LOGS_DIR"
#     echo -e "${GREEN}[✓]${NC} 日志文件已清理"
# fi

echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                    所有服务已停止                          ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
