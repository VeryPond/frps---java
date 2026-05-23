# 🚀 FRPS Java Launcher

用 **Java** 启动 [frps](https://github.com/fatedier/frp) 服务端。

适用于 **MC 等游戏面板服** 等只能运行 Java、不能装 frp 的环境。把 frps 二进制扔到 `bin/` 目录，启动器会自动拉起。

---

## 📋 目录结构

```
frps---java/
├── frps-launcher.jar     ← 编译后的启动器（可执行 JAR）
├── bin/
│   └── frps              ← frps 服务端可执行文件（需自行下载）
├── etc/
│   └── frps.toml         ← frps 配置文件
├── src/
│   ├── main/java/...     ← Java 源码
│   └── scripts/          ← 启动脚本
├── pom.xml               ← Maven 构建文件
└── README.md
```

---

## 🛠️ 快速开始

### 1️⃣ 下载 frps

从 [frp Releases](https://github.com/fatedier/frp/releases) 下载对应系统架构的 frps，放到 `bin/` 目录：

```bash
# Linux amd64 示例
wget https://github.com/fatedier/frp/releases/latest/download/frp_XXX_linux_amd64.tar.gz
tar xzf frp_XXX_linux_amd64.tar.gz
cp frp_XXX_linux_amd64/frps bin/

# 设置可执行权限
chmod +x bin/frps
```

### 2️⃣ 编译打包

```bash
# 需要 Maven 3+ 和 JDK 11+
mvn clean package
```

生成的 JAR 在 `target/frps-launcher.jar`。

### 3️⃣ 配置 frps

编辑 `etc/frps.toml`，按需要配置：

```toml
bindPort = 7000
```

更多 frps 配置项参考 [frp 官方文档](https://github.com/fatedier/frp#configuration)。

### 4️⃣ 启动

```bash
# 方式一：直接运行 JAR
java -jar frps-launcher.jar

# 方式二：使用启动脚本
chmod +x src/scripts/launch.sh
./src/scripts/launch.sh

# 方式三：指定自定义配置
java -jar frps-launcher.jar --config /path/to/frps.toml --frps-bin /path/to/frps
```

---

## ⚙️ 命令行选项

| 选项 | 说明 | 默认值 |
|------|------|--------|
| `--config <path>` | 指定 frps 配置文件路径 | `<JAR_DIR>/etc/frps.toml` |
| `--frps-bin <path>` | 指定 frps 可执行文件路径 | `<JAR_DIR>/bin/frps` |
| `--help` | 显示帮助信息 | - |

---

## 📌 注意事项

- **JDK 版本**: 需要 **Java 11+**
- **frps 兼容性**: 确保下载的 frps 版本与服务器架构匹配（Linux amd64/arm64 等）
- **后台运行**: 面板服一般自带进程管理，如需后台运行可配合 `nohup` 或 `screen`
- **日志**: 启动器日志直接输出到 stdout/stderr，由面板接管
- **进程管理**: JVM 退出时会自动终止 frps 进程（有 5 秒优雅关闭等待）

---

## 🏗️ 构建要求

| 工具 | 版本 |
|------|------|
| JDK | 11+ |
| Maven | 3.6+ |

---

## 📜 许可

MIT License © 2025-2026 VeryPond

---

## 🔗 相关链接

- [frp - 内网穿透工具](https://github.com/fatedier/frp)
- [Java 11 下载](https://adoptium.net/)
