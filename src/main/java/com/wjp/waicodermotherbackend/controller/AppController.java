package com.wjp.waicodermotherbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wjp.waicodermotherbackend.annotation.AuthCheck;
import com.wjp.waicodermotherbackend.common.BaseResponse;
import com.wjp.waicodermotherbackend.common.DeleteRequest;
import com.wjp.waicodermotherbackend.common.ResultUtils;
import com.wjp.waicodermotherbackend.constant.AppConstant;
import com.wjp.waicodermotherbackend.constant.UserConstant;
import com.wjp.waicodermotherbackend.exception.BusinessException;
import com.wjp.waicodermotherbackend.exception.ErrorCode;
import com.wjp.waicodermotherbackend.exception.ThrowUtils;
import com.wjp.waicodermotherbackend.model.dto.app.*;
import com.wjp.waicodermotherbackend.model.entity.User;
import com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum;
import com.wjp.waicodermotherbackend.model.enums.UserRoleEnum;
import com.wjp.waicodermotherbackend.model.vo.AppVO;
import com.wjp.waicodermotherbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import com.wjp.waicodermotherbackend.model.entity.App;
import com.wjp.waicodermotherbackend.service.AppService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.io.File;
import cn.hutool.core.io.FileUtil;
import java.util.HashMap;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/wjp527">π</a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;


    /**
     * 聊天
     * @param appId
     * @param message
     * @param request
     * @return
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                      @RequestParam String message,
                                      HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0 || message == null, ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        return contentFlux
                .map(chunk -> {
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        // 发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }


    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);

        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 Prompt 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(httpServletRequest);
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrmpt 前12位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 暂时设置为多文件生成
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 插入数据库
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if(deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long id = deleteRequest.getId();
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可以删除
        if(!oldApp.getUserId().equals(loginUser.getId()) && !UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = appService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 修改应用。
     * @param appUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        if(appUpdateRequest == null  || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = appUpdateRequest.getId();
        String appName = appUpdateRequest.getAppName();
        // 判断是否存在
        App oldApp= appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 仅本人可见
        if(!oldApp.getUserId().equals(loginUser.getId()) || !UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        App app = new App();
        app.setId(id);
        app.setAppName(appName);
        app.setEditTime(LocalDateTime.now());
        boolean save = appService.updateById(app);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取应用。
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类(包含用户信息)
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 分页获取当前用户创建的应用列表
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVoByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 限制每页最多20条数据
        int pageSize = appQueryRequest.getPageSize();
        int pageNum = appQueryRequest.getPageNum();
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询20条数据");

        // 只查询到当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVoPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVoPage.setRecords(appVOList);
        return ResultUtils.success(appVoPage);
    }

    /**
     * 分页获取精选应用列表
     * @param appQueryRequest
     * @return
     */
    @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null , ErrorCode.PARAMS_ERROR);
        // 每页限制最多20条
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询20条数据");
        // 只查询精选的应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        // 分页查询

        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 删除应用。【管理员接口】
     * @param deleteRequest
     * @return
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = appService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 应用更新应用。【管理员】
     * @param appAdminUpdateRequest
     * @return
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        if(appAdminUpdateRequest == null  || appAdminUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = appAdminUpdateRequest.getId();
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询应用列表。【管理员】
     * @param appQueryRequest
     * @return
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVoByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageNum < 1 || pageSize < 1, ErrorCode.PARAMS_ERROR, "分页参数错误");
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 获取应用详情。【管理员】
     * @param id
     * @return
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVOByIdAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        AppVO appVO = appService.getAppVO(app);
        return ResultUtils.success(appVO);
    }

    /**
     * 获取AI生成的代码片段
     * 
     * 该方法返回指定应用的最新生成代码，包括HTML、CSS、JavaScript等文件内容。
     * 主要用于代码编辑、版本比较、代码分享等场景。
     * 
     * @param appId 应用ID
     * @param request HTTP请求对象
     * @return 包含代码文件内容的响应
     */
    @GetMapping("/code/snippets/{appId}")
    public BaseResponse<Map<String, String>> getCodeSnippets(@PathVariable Long appId, 
                                                        HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        
        // 2. 获取登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        // 3. 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        
        // 4. 权限验证
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        
        // 5. 读取代码文件
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        
        // 6. 返回代码片段
        return ResultUtils.success(appService.getCodeSnippets(sourceDirPath));
    }

    /**
     * 根据主键获取应用。
     *
     * @param id 应用主键
     * @return 应用详情
     */
    @GetMapping("getInfo/{id}")
    public App getInfo(@PathVariable Long id) {
        return appService.getById(id);
    }

    /**
     * 分页查询应用。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<App> page(Page<App> page) {
        return appService.page(page);
    }

}
