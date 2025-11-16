package com.wjp.waicodermotherbackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.wjp.waicodermotherbackend.exception.ErrorCode;
import com.wjp.waicodermotherbackend.exception.ThrowUtils;
import com.wjp.waicodermotherbackend.manager.CosManager;
import com.wjp.waicodermotherbackend.service.ScreenshotService;
import com.wjp.waicodermotherbackend.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;


    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 1、参数校验
        if(StrUtil.isBlank(webUrl)) {
            log.error("网页URL不能为空: {}", webUrl);
            return null;
        }
        // 2、本地截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(localScreenshotPath == null, ErrorCode.OPERATION_ERROR, "网页截图失败");
        try {
            // 3、上传到COS
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");
            log.info("网页截图成功并上传成功: {} -> {}", webUrl, cosUrl);
            return cosUrl;
        } finally {
            // 4、清理本地文件
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 上传截图到 COS
     * @param localScreenshotPath 本地截图路径
     * @return
     */
    private String uploadScreenshotToCos(String localScreenshotPath) {
        if(StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if(!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }
        // 生成 COS 对象键
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String key = generateScreenotsKey(fileName);
        return cosManager.updateFile(key, screenshotFile);
    }

    /**
     * 生成截图对象键
     * @param fileName
     * @return
     */
    private String generateScreenotsKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if(localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已经清理: {}", localFilePath);
        }
    }
}
