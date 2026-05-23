package io.github.verypond.frps.launcher;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * FRPS Java Launcher
 * <p>
 * 用 Java 启动 frps 服务端进程。
 * 适用于只能运行 Java 的 MC 等游戏面板服环境。
 * <p>
 * 用法: java -jar frps-launcher.jar [选项]
 * <p>
 * 选项:
 *   --config <path>   指定 frps 配置文件路径（默认: ./etc/frps.toml）
 *   --frps-bin <path> 指定 frps 可执行文件路径（默认: ./bin/frps）
 *   --help            显示帮助信息
 */
public class Main {

    private static final String LOG_PREFIX = "[FRPS-Launcher]";
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 默认路径（相对于 JAR 所在目录）
    private static final String DEFAULT_FRPS_PATH = "bin/frps";
    private static final String DEFAULT_CONFIG_PATH = "etc/frps.toml";

    private static String frpsPath;
    private static String configPath;

    public static void main(String[] args) {
        parseArgs(args);

        String jarDir = getJarDirectory();
        String frpsExecutable = resolvePath(jarDir, frpsPath, DEFAULT_FRPS_PATH);
        String frpsConfig = resolvePath(jarDir, configPath, DEFAULT_CONFIG_PATH);

        log("FRPS Java Launcher v2.0.0");
        log("JAR 目录: " + jarDir);
        log("FRPS 路径: " + frpsExecutable);
        log("配置路径: " + frpsConfig);
        log("操作系统: " + System.getProperty("os.name"));

        // 检查 frps 可执行文件是否存在
        File frpsFile = new File(frpsExecutable);
        if (!frpsFile.exists() || !frpsFile.isFile()) {
            logErr("错误: frps 可执行文件不存在: " + frpsExecutable);
            logErr("请确保 " + DEFAULT_FRPS_PATH + " 文件存在并可执行");
            System.exit(1);
        }

        // 检查配置文件是否存在
        File configFile = new File(frpsConfig);
        if (!configFile.exists() || !configFile.isFile()) {
            logErr("错误: 配置文件不存在: " + frpsConfig);
            logErr("请确保 " + DEFAULT_CONFIG_PATH + " 文件存在");
            System.exit(1);
        }

        // 确保可执行权限（Linux/macOS）
        if (!isWindows()) {
            if (!frpsFile.canExecute()) {
                log("设置 frps 可执行权限...");
                if (!frpsFile.setExecutable(true)) {
                    logErr("警告: 无法设置可执行权限，尝试 chmod...");
                    execCommand("chmod", "+x", frpsExecutable);
                }
            }
        }

        // 注册 JVM 关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(Main::cleanup, "shutdown-hook"));

        // 启动 frps 进程
        startFrpsProcess(frpsExecutable, frpsConfig);
    }

    /**
     * 启动 frps 进程并等待
     */
    private static void startFrpsProcess(String executable, String config) {
        Process process = null;
        try {
            log("正在启动 frps...");

            ProcessBuilder pb = new ProcessBuilder(executable, "-c", config);
            pb.inheritIO(); // 输出重定向到当前控制台
            process = pb.start();

            // 保存进程引用，供关闭钩子使用
            FrpsProcessHolder.setProcess(process);

            log("frps 已启动 (PID: " + process.pid() + ")");

            // 等待进程结束（阻塞）
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                logErr("frps 进程异常退出，退出码: " + exitCode);
            } else {
                log("frps 进程正常退出");
            }

        } catch (IOException e) {
            logErr("启动 frps 失败: " + e.getMessage());
            logErr("请确认 frps 可执行文件与当前系统架构匹配");
            System.exit(2);
        } catch (InterruptedException e) {
            log("frps 进程被中断");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 进程退出时清理
     */
    private static void cleanup() {
        Process process = FrpsProcessHolder.getProcess();
        if (process != null && process.isAlive()) {
            log("正在停止 frps (PID: " + process.pid() + ")...");
            process.destroy();

            // 等待最多 5 秒让进程优雅退出
            try {
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    log("frps 未在 5 秒内退出，强制终止...");
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }

            log("frps 已停止");
        }
    }

    /**
     * 解析命令行参数
     */
    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--config":
                    if (i + 1 < args.length) {
                        configPath = args[++i];
                    } else {
                        logErr("错误: --config 需要指定文件路径");
                        System.exit(1);
                    }
                    break;
                case "--frps-bin":
                    if (i + 1 < args.length) {
                        frpsPath = args[++i];
                    } else {
                        logErr("错误: --frps-bin 需要指定文件路径");
                        System.exit(1);
                    }
                    break;
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
                default:
                    logErr("错误: 未知参数: " + args[i]);
                    logErr("使用 --help 查看帮助");
                    System.exit(1);
            }
        }
    }

    /**
     * 获取 JAR 文件所在目录
     */
    private static String getJarDirectory() {
        try {
            return new File(Main.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException e) {
            logErr("警告: 无法解析 JAR 路径，使用当前目录");
            return ".";
        }
    }

    /**
     * 解析文件路径：优先使用用户指定路径，否则使用相对于 JAR 目录的默认路径
     */
    private static String resolvePath(String jarDir, String userPath, String defaultRelativePath) {
        if (userPath != null) {
            return userPath;
        }
        return new File(jarDir, defaultRelativePath).getAbsolutePath();
    }

    /**
     * 执行系统命令
     */
    private static void execCommand(String... command) {
        try {
            new ProcessBuilder(command).start().waitFor();
        } catch (IOException | InterruptedException e) {
            logErr("执行命令失败: " + String.join(" ", command) + " - " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 打印帮助信息
     */
    private static void printHelp() {
        System.out.println();
        System.out.println("FRPS Java Launcher v2.0.0");
        System.out.println("用 Java 启动 frps 服务端，适用于游戏面板服");
        System.out.println();
        System.out.println("用法: java -jar frps-launcher.jar [选项]");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  --config <path>   指定 frps 配置文件路径");
        System.out.println("                    默认: <JAR_DIR>/etc/frps.toml");
        System.out.println("  --frps-bin <path> 指定 frps 可执行文件路径");
        System.out.println("                    默认: <JAR_DIR>/bin/frps");
        System.out.println("  --help            显示此帮助信息");
        System.out.println();
        System.out.println("目录结构:");
        System.out.println("  frps---java/");
        System.out.println("    ├── frps-launcher.jar   ← 本启动器");
        System.out.println("    ├── bin/frps            ← frps 可执行文件");
        System.out.println("    └── etc/frps.toml       ← frps 配置文件");
        System.out.println();
        System.out.println("项目地址: https://github.com/VeryPond/frps---java");
        System.out.println();
    }

    // ========== 工具方法 ==========

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static String timestamp() {
        return LocalDateTime.now().format(DT_FORMAT);
    }

    static void log(String message) {
        System.out.println(timestamp() + " " + LOG_PREFIX + " " + message);
    }

    static void logErr(String message) {
        System.err.println(timestamp() + " " + LOG_PREFIX + " [ERROR] " + message);
    }

    /**
     * 持有 frps 进程引用，确保 shutdown hook 能访问到
     */
    private static class FrpsProcessHolder {
        private static volatile Process process;

        static void setProcess(Process p) {
            process = p;
        }

        static Process getProcess() {
            return process;
        }
    }
}
