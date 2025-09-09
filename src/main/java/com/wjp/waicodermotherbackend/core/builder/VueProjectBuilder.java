package com.wjp.waicodermotherbackend.core.builder;

import cn.hutool.core.util.RuntimeUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 构建Vue项目
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 异步构建 Vue项目
     * @param projectPath
     */
    public void buildProjectAsync(String projectPath) {
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis())
                .start(() -> {
                    try {
                        buildProject(projectPath);
                    } catch (Exception e) {
                        log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
                    }
                });
    }

    /**
     * 构建Vue项目
     *
     * @param projectPath 项目路径
     * @return 是否构建成功
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if(!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在：{}", projectPath);
            return false;
        }

        // 检查 package.json 是否存在
        File packageJson = new File(projectDir, "package.json");
        if(!packageJson.exists()) {
            log.error("项目目录下不存在 package.json 文件：{}", projectPath);
            return false;
        }
        log.info("开始构建Vue项目: {}", projectPath);

        // 执行 npm install
        if(!executeNpmInstall(projectDir)) {
            log.error("npm install 执行失败");
            return false;
        }

        // 执行 npm run build
        if(!executeNpmBuild(projectDir)) {
            log.error("npm run build 执行失败");
            return false;
        }

        // 验证 dist 目录是否生成
        File distDir = new File(projectDir, "dist");
        if(!distDir.exists() || !distDir.isDirectory()) {
            log.error("构建完成单 dist 目录未生成：{}", projectPath);
            return false;
        }
        log.info("Vue 项目构建成功，dist目录：{}", distDir.getAbsolutePath());
        return true;
    }


    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300); // 5分钟超时
    }


    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }

    /**
     * 根据操作系统构造命令
     * @param baseCommand
     * @return
     */
    private String buildCommand(String baseCommand) {
        if(isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 操作系统检测
     * @return
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            // 手动等待进程完成，使用轮询和睡眠来实现超时控制，避免依赖 TimeUnit
            long deadlineMillis = System.currentTimeMillis() + timeoutSeconds * 1000L;
            boolean finished = false;
            while (System.currentTimeMillis() < deadlineMillis) {
                if (!process.isAlive()) {
                    finished = true;
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }


}
