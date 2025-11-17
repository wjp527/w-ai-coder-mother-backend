package com.wjp.waicodermotherbackend.controller;

import com.wjp.waicodermotherbackend.constant.AppConstant;
import com.wjp.waicodermotherbackend.exception.ErrorCode;
import com.wjp.waicodermotherbackend.exception.ThrowUtils;
import com.wjp.waicodermotherbackend.model.entity.App;
import com.wjp.waicodermotherbackend.model.entity.User;
import com.wjp.waicodermotherbackend.service.AppService;
import com.wjp.waicodermotherbackend.service.ProjectDownloadService;
import com.wjp.waicodermotherbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * 项目下载控制器
 */
@RestController
@RequestMapping("/project")
public class ProjectDownloadController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private ProjectDownloadService projectDownloadService;

    @GetMapping("/download/{appId}")
    public String downloadProject(@PathVariable Long appId, HttpServletRequest request, HttpServletResponse response) {
        // 1. 基础校验
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "appId不能为空");

        // 2. 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3、权限校验：只有应用创建者才可以下载
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限");

        // 4、构建应用代码目录路径(生成目录，非部署目录)
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        // 5、检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), ErrorCode.NOT_FOUND_ERROR, "代码目录不存在");

        // 6、生成下载文件名(不建议添加中文名)
        String downloadFileName = String.valueOf(appId);

        // 7、调用下载服务
        projectDownloadService.downloadProject(sourceDirPath, downloadFileName, response);

        return "ok";
    }


}
