package com.example;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) {
        try {
            // 获取 JAR 所在目录
            String jarDir = new File(Main.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParent();

            // 定义 FRPS 路径和配置文件路径
            String frpsPath = new File(jarDir, "bin/frps").getAbsolutePath();
            String configPath = new File(jarDir, "etc/frps.toml").getAbsolutePath();

            // 设置可执行权限（Linux/macOS 需要）
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("chmod", "+x", frpsPath).start().waitFor();
            }

            // 启动 FRPS
            Process process = new ProcessBuilder(frpsPath, "-c", configPath)
                    .inheritIO()  // 输出到当前控制台
                    .start();

            // 注册 JVM 关闭钩子（确保退出时终止 FRPS）
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (process.isAlive()) {
                    process.destroy();
                    System.out.println("FRPS 已终止");
                }
            }));

            // 等待进程结束（阻塞）
            process.waitFor();

        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
