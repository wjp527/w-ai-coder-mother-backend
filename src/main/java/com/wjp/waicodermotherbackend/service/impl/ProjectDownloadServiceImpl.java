package com.wjp.waicodermotherbackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.wjp.waicodermotherbackend.exception.BusinessException;
import com.wjp.waicodermotherbackend.exception.ErrorCode;
import com.wjp.waicodermotherbackend.exception.ThrowUtils;
import com.wjp.waicodermotherbackend.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * 文件下载服务
 */
@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 需要过滤的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );


    /**
     * 下载项目为压缩包
     * @param projectPath   项目路径
     * @param downloadFileName 下载文件名
     * @param response 响应（返回给前端的响应头）
     * @return
     */
    @Override
    public void downloadProject(String projectPath, String downloadFileName, HttpServletResponse response) {
        // 基础校验
        ThrowUtils.throwIf(projectPath == null || projectPath.isEmpty(), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
        ThrowUtils.throwIf(downloadFileName == null || downloadFileName.isEmpty(), ErrorCode.PARAMS_ERROR, "下载文件名不能为空");
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.PARAMS_ERROR, "项目路径不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR, "项目路径不是目录");
        log.info("开始打包下载项目: {} -> {}.zip", projectPath, downloadFileName);
        // 设置 HTTP 响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", downloadFileName));

        // 定义文件过滤器
        FileFilter filter = file -> isPathAllowed(projectDir.toPath(), file.toPath());

        try {
            // 使用 HuTool 的 ZipUtil 直接将过滤后的目录压缩大搜相应输出流
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, filter, projectDir);
            log.info("项目打包下载完成: {}", downloadFileName);
        } catch(Exception e) {
            log.error("项目打包异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "项目打包异常");
        }
    }

    /**
     * 校验路径是否要包含在压缩包中
     * @param projectRoot 项目的根目录(绝对路径)
     * @param fullPath 项目的完整路径(相对路径)
     * @return 是否允许
     */
    private boolean isPathAllowed(Path projectRoot, Path fullPath) {
        Path relativizePath = projectRoot.relativize(fullPath);
        // 检查路晋中的每一部分代码是否符合要求
        for (Path path : relativizePath) {
            String partName = path.toString();
            // 检查是否在忽略名称列表中
            if(IGNORED_NAMES.contains(partName)) {
                return false;
            }
            // 检查是否已忽略扩展名结尾
            if(IGNORED_EXTENSIONS.stream().anyMatch(ext -> partName.toLowerCase().endsWith(ext))) {
                return false;
            }
        }
        return true;
    }
}
