#!/bin/bash
#
# FRPS Launcher - 启动脚本
# 用法: ./launch.sh [选项]
#
# 选项:
#   --config <path>   指定配置文件路径
#   --frps-bin <path> 指定 frps 可执行文件路径
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
JAR_PATH="$JAR_DIR/frps-launcher.jar"

# 检查 JAR 是否存在
if [ ! -f "$JAR_PATH" ]; then
    echo "[ERROR] 找不到 frps-launcher.jar"
    echo "[INFO]  期望路径: $JAR_PATH"
    echo "[INFO]  请先执行: mvn clean package"
    exit 1
fi

# 检查 frps 二进制是否存在
FRPS_BIN="$JAR_DIR/bin/frps"
if [ ! -f "$FRPS_BIN" ]; then
    echo "[WARN] frps 可执行文件不存在: $FRPS_BIN"
    echo "[WARN] 请从 https://github.com/fatedier/frp/releases 下载对应版本"
    echo "[WARN] 并放置到 $FRPS_BIN"
fi

echo "[INFO] 启动 FRPS Launcher..."
echo "[INFO] JAR: $JAR_PATH"
echo "[INFO] 参数: $*"

exec java -jar "$JAR_PATH" "$@"
