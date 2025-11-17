package com.wjp.waicodermotherbackend.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 文件下载服务
 */
public interface ProjectDownloadService {

    /**
     * 下载项目为压缩包
     * @param projectPath   项目路径
     * @param downloadFileName 下载文件名
     * @param response 响应（返回给前端的响应头）
     * @return
     */
    void downloadProject(String projectPath, String downloadFileName, HttpServletResponse response);

}
