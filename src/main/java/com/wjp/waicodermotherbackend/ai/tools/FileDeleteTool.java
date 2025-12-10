package com.wjp.waicodermotherbackend.ai.tools;

import cn.hutool.json.JSONObject;
import com.wjp.waicodermotherbackend.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件删除工具
 * 支持 AI 通过工具调用的方式删除文件
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool {

    @Tool("删除指定路径的文件")
    public String deleteFile(
            @P("文件的相对路径") // 标记参数描述，帮助 AI 理解参数含义
            String relativeFilePath,
            @ToolMemoryId Long appId // 获取当前应用上下文 ID
    ) {
        try {
            // 将 字符串 转换为 Path对象(可以获取到 文件的绝对路径、文件名)
            Path path = Paths.get(relativeFilePath);
            // 如果不是绝对路径，就进入到 if
            if(!path.isAbsolute()) {
                // 根据 应用ID 创建项目名
                String projectDirName = "vue_project_" + appId;
                // 将多个路径片段 合并为一个完整路径
                // 获取到项目根目录的 Path对象
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                // 会先检查 relativeFilePath 是否是相对路径，如果是的的话，
                // projectRoot(绝对路径) 才会和 relativeFilePath 进行拼接，生成新的Path对象
                // 否则，直接返回 relativeFilePath
                path = projectRoot.resolve(relativeFilePath);
            }

            if(!Files.exists(path)) {
                return "警告: 文件不存在，无需删除 - " + relativeFilePath;
            }
            // isRegularFile: 判断是否是普通文件(不是目录、不是符号链接)
            if(!Files.isRegularFile(path)) {
                return "警告: 不是文件，无法删除 - " + relativeFilePath;
            }

            // 安全检查：避免删除重要文件
            String fileName = path.getFileName().toString();
            if(isImportantFile(fileName)) {
                return "错误，不允许删除重要文件 - " + fileName;
            }

            Files.delete(path);
            log.info("成功删除文件: {}", path.toAbsolutePath());
            return "文件删除成功: " + relativeFilePath;
        } catch(IOException e) {
            String errorMessage = "删除文件失败: " + relativeFilePath + ",错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 判断是否是重要文件，不允许删除
     * @param fileName 文件名
     * @return 是否是重要文件
     */
    private boolean isImportantFile(String fileName) {
        String[] importantFiles = {
                "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml","vite.config.js", "vite.config.ts", "vue.config.js",
                "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
                "index.html", "main.js", "main.ts", "App.vue", ".gitignore", "README.md"
        };
        for (String importantFile : importantFiles) {
            if(importantFile.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String getToolName() {
        return "deleteFile";
    }

    @Override
    public String getDisplayName() {
        return "删除文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format("[工具调用] %s %s", getDisplayName(), relativeFilePath);
    }
}
