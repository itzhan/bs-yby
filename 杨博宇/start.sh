#!/bin/bash
# ============================================================
# 校园招聘系统 - 启动脚本 (Mac/Linux)
# ============================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目目录
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="campus_recruitment"
DB_USER="root"
DB_PASS="ab123168"

# 端口配置
BACKEND_PORT=8080
ADMIN_PORT=8001
FRONTEND_PORT=8002

# 日志和PID目录
LOGS_DIR=".logs"
PIDS_DIR=".pids"
mkdir -p "$LOGS_DIR" "$PIDS_DIR"

# 清理函数
cleanup() {
    echo -e "\n${YELLOW}[!] 收到中断信号，正在停止所有服务...${NC}"
    if [ -f "$PIDS_DIR/backend.pid" ]; then
        kill $(cat "$PIDS_DIR/backend.pid") 2>/dev/null || true
        rm -f "$PIDS_DIR/backend.pid"
    fi
    if [ -f "$PIDS_DIR/admin.pid" ]; then
        kill $(cat "$PIDS_DIR/admin.pid") 2>/dev/null || true
        rm -f "$PIDS_DIR/admin.pid"
    fi
    if [ -f "$PIDS_DIR/frontend.pid" ]; then
        kill $(cat "$PIDS_DIR/frontend.pid") 2>/dev/null || true
        rm -f "$PIDS_DIR/frontend.pid"
    fi
    # 杀死端口上的进程
    lsof -ti:$BACKEND_PORT | xargs kill -9 2>/dev/null || true
    lsof -ti:$ADMIN_PORT | xargs kill -9 2>/dev/null || true
    lsof -ti:$FRONTEND_PORT | xargs kill -9 2>/dev/null || true
    # 杀死tail进程
    pkill -f "tail.*\.logs" 2>/dev/null || true
    echo -e "${GREEN}[✓] 所有服务已停止${NC}"
    exit 0
}

# 注册清理函数
trap cleanup SIGINT SIGTERM

# Banner
echo -e "${CYAN}"
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                                                            ║"
echo "║              校园招聘系统 - 启动脚本                        ║"
echo "║                                                            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# 检查函数
check_command() {
    if command -v "$1" &> /dev/null; then
        echo -e "${GREEN}[✓]${NC} $2 已安装: $(command -v $1)"
        return 0
    else
        echo -e "${RED}[✗]${NC} $2 未安装，请先安装 $2"
        return 1
    fi
}

# 1. 检查 Java
echo -e "\n${BLUE}[1/10] 检查 Java...${NC}"
if check_command "java" "Java"; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo -e "   ${CYAN}版本: $JAVA_VERSION${NC}"
else
    exit 1
fi

# 2. 检查 Maven
echo -e "\n${BLUE}[2/10] 检查 Maven...${NC}"
if check_command "mvn" "Maven"; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo -e "   ${CYAN}$MVN_VERSION${NC}"
else
    exit 1
fi

# 3. 检查 Node.js
echo -e "\n${BLUE}[3/10] 检查 Node.js...${NC}"
if check_command "node" "Node.js"; then
    NODE_VERSION=$(node -v)
    echo -e "   ${CYAN}版本: $NODE_VERSION${NC}"
else
    exit 1
fi

# 4. 检查/安装 pnpm
echo -e "\n${BLUE}[4/10] 检查 pnpm...${NC}"
if check_command "pnpm" "pnpm"; then
    PNPM_VERSION=$(pnpm -v)
    echo -e "   ${CYAN}版本: $PNPM_VERSION${NC}"
else
    echo -e "${YELLOW}[!] pnpm 未安装，正在通过 npm 安装...${NC}"
    npm install -g pnpm
    if check_command "pnpm" "pnpm"; then
        echo -e "${GREEN}[✓] pnpm 安装成功${NC}"
    else
        echo -e "${RED}[✗] pnpm 安装失败${NC}"
        exit 1
    fi
fi

# 5. 检查 MySQL
echo -e "\n${BLUE}[5/10] 检查 MySQL...${NC}"
if mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -P"$DB_PORT" -e "SELECT 1" 2>/dev/null; then
    echo -e "${GREEN}[✓] MySQL 连接成功${NC}"
else
    echo -e "${YELLOW}[!] MySQL 未运行，尝试启动...${NC}"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # Mac
        if brew services list | grep -q "mysql.*started"; then
            echo -e "${GREEN}[✓] MySQL 服务已启动${NC}"
        else
            brew services start mysql 2>/dev/null || {
                echo -e "${RED}[✗] 无法启动 MySQL，请手动启动: brew services start mysql${NC}"
                exit 1
            }
            sleep 3
        fi
    else
        # Linux
        sudo systemctl start mysql 2>/dev/null || sudo service mysql start 2>/dev/null || {
            echo -e "${RED}[✗] 无法启动 MySQL，请手动启动: sudo systemctl start mysql${NC}"
            exit 1
        }
        sleep 3
    fi
    # 再次检查
    if mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -P"$DB_PORT" -e "SELECT 1" 2>/dev/null; then
        echo -e "${GREEN}[✓] MySQL 已启动并连接成功${NC}"
    else
        echo -e "${RED}[✗] MySQL 连接失败，请检查配置${NC}"
        exit 1
    fi
fi

# 6. 检查数据库并导入SQL
echo -e "\n${BLUE}[6/10] 检查数据库...${NC}"
TABLE_COUNT=$(mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -P"$DB_PORT" -e "USE $DB_NAME; SHOW TABLES;" 2>/dev/null | wc -l)
if [ "$TABLE_COUNT" -lt 2 ]; then
    echo -e "${YELLOW}[!] 数据库表数量不足 ($TABLE_COUNT)，正在导入 SQL 文件...${NC}"
    if [ -f "sql/init.sql" ] && [ -f "sql/data.sql" ]; then
        mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -P"$DB_PORT" < sql/init.sql
        mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -P"$DB_PORT" < sql/data.sql
        echo -e "${GREEN}[✓] SQL 文件导入成功${NC}"
    else
        echo -e "${RED}[✗] SQL 文件不存在: sql/init.sql 或 sql/data.sql${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}[✓] 数据库已存在，表数量: $((TABLE_COUNT - 1))${NC}"
fi

# 7. 检查依赖
echo -e "\n${BLUE}[7/10] 检查项目依赖...${NC}"

# Backend
if [ ! -d "backend/target" ] || [ ! -f "backend/target/classes" ]; then
    echo -e "${YELLOW}[!] 后端依赖未编译，正在编译...${NC}"
    cd backend
    mvn compile -q
    cd ..
    echo -e "${GREEN}[✓] 后端编译完成${NC}"
else
    echo -e "${GREEN}[✓] 后端依赖已就绪${NC}"
fi

# Admin
if [ ! -d "admin/node_modules" ]; then
    echo -e "${YELLOW}[!] 管理端依赖未安装，正在安装...${NC}"
    cd admin
    pnpm install --silent
    cd ..
    echo -e "${GREEN}[✓] 管理端依赖安装完成${NC}"
else
    echo -e "${GREEN}[✓] 管理端依赖已就绪${NC}"
fi

# Frontend
if [ ! -d "frontend/node_modules" ]; then
    echo -e "${YELLOW}[!] 前端依赖未安装，正在安装...${NC}"
    cd frontend
    pnpm install --silent
    cd ..
    echo -e "${GREEN}[✓] 前端依赖安装完成${NC}"
else
    echo -e "${GREEN}[✓] 前端依赖已就绪${NC}"
fi

# 8. 检查端口冲突
echo -e "\n${BLUE}[8/10] 检查端口占用...${NC}"
check_port() {
    local port=$1
    local name=$2
    if lsof -ti:$port &>/dev/null; then
        echo -e "${RED}[✗]${NC} 端口 $port ($name) 已被占用"
        echo -e "   ${YELLOW}请运行 ./stop.sh 停止现有服务，或手动释放端口${NC}"
        return 1
    else
        echo -e "${GREEN}[✓]${NC} 端口 $port ($name) 可用"
        return 0
    fi
}

if ! check_port $BACKEND_PORT "后端"; then exit 1; fi
if ! check_port $ADMIN_PORT "管理端"; then exit 1; fi
if ! check_port $FRONTEND_PORT "前端"; then exit 1; fi

# 9. 启动服务
echo -e "\n${BLUE}[9/10] 启动服务...${NC}"

# 启动后端
echo -e "${CYAN}[后端]${NC} 正在启动 Spring Boot..."
cd backend
nohup mvn spring-boot:run > "../$LOGS_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > "../$PIDS_DIR/backend.pid"
cd ..
echo -e "${GREEN}[✓]${NC} 后端已启动 (PID: $BACKEND_PID)"

# 启动管理端
echo -e "${CYAN}[管理端]${NC} 正在启动 Vue 2 管理面板..."
cd admin
nohup npx vue-cli-service serve --port $ADMIN_PORT > "../$LOGS_DIR/admin.log" 2>&1 &
ADMIN_PID=$!
echo $ADMIN_PID > "../$PIDS_DIR/admin.pid"
cd ..
echo -e "${GREEN}[✓]${NC} 管理端已启动 (PID: $ADMIN_PID)"

# 启动前端
echo -e "${CYAN}[前端]${NC} 正在启动 Vue 3 用户前端..."
cd frontend
nohup npx vite --port $FRONTEND_PORT > "../$LOGS_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > "../$PIDS_DIR/frontend.pid"
cd ..
echo -e "${GREEN}[✓]${NC} 前端已启动 (PID: $FRONTEND_PID)"

# 10. 等待端口就绪
echo -e "\n${BLUE}[10/10] 等待服务就绪...${NC}"

wait_for_port() {
    local port=$1
    local name=$2
    local max_attempts=60
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if nc -z localhost $port 2>/dev/null; then
            echo -e "${GREEN}[✓]${NC} $name 已就绪 (端口 $port)"
            return 0
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 1
    done
    
    echo -e "\n${RED}[✗]${NC} $name 启动超时 (端口 $port)"
    return 1
}

wait_for_port $BACKEND_PORT "后端"
wait_for_port $ADMIN_PORT "管理端"
wait_for_port $FRONTEND_PORT "前端"

# 显示访问信息
echo -e "\n${CYAN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║                    服务启动成功！                            ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}访问地址:${NC}"
echo -e "  ${CYAN}后端 API:${NC}    http://localhost:$BACKEND_PORT"
echo -e "  ${CYAN}管理端:${NC}      http://localhost:$ADMIN_PORT"
echo -e "  ${CYAN}用户前端:${NC}    http://localhost:$FRONTEND_PORT"
echo ""
echo -e "${GREEN}测试账号:${NC}"
echo -e "  ${CYAN}管理员:${NC}     admin / 123456"
echo -e "  ${CYAN}学生:${NC}        student1 / 123456 (张明)"
echo -e "  ${CYAN}企业:${NC}        company1 / 123456 (字节跳动)"
echo ""
echo -e "${YELLOW}提示:${NC}"
echo -e "  - 日志文件位于: $LOGS_DIR/"
echo -e "  - 按 Ctrl+C 停止所有服务"
echo -e "  - 使用 ./stop.sh 停止服务"
echo ""

# 实时显示日志
echo -e "${CYAN}开始显示实时日志 (按 Ctrl+C 停止)...${NC}\n"

# 使用 tail -f 显示日志，带颜色前缀
tail -f "$LOGS_DIR/backend.log" | sed "s/^/${RED}[后端]${NC} /" &
TAIL_BACKEND_PID=$!
tail -f "$LOGS_DIR/admin.log" | sed "s/^/${GREEN}[管理端]${NC} /" &
TAIL_ADMIN_PID=$!
tail -f "$LOGS_DIR/frontend.log" | sed "s/^/${BLUE}[前端]${NC} /" &
TAIL_FRONTEND_PID=$!

# 等待用户中断
wait
