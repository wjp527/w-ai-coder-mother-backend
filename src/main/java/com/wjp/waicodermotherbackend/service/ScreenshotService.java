package com.wjp.waicodermotherbackend.service;

/**
 * 截图服务
 */
public interface ScreenshotService {

    /**
     * 通用的截图服务、可以得到访问地址
     * @param webUrl 网页地址
     * @return
     */
    String generateAndUploadScreenshot(String webUrl);

}
