package com.wjp.waicodermotherbackend.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wjp.waicodermotherbackend.exception.BusinessException;
import com.wjp.waicodermotherbackend.model.dto.app.AppAddRequest;
import com.wjp.waicodermotherbackend.model.dto.app.AppQueryRequest;
import com.wjp.waicodermotherbackend.model.enums.AppCodeGenEnum;
import com.wjp.waicodermotherbackend.model.vo.AppVO;
import com.wjp.waicodermotherbackend.model.entity.App;
import com.wjp.waicodermotherbackend.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerMapping;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.wjp.waicodermotherbackend.exception.ErrorCode.SYSTEM_ERROR;

/**
 * 应用服务接口
 * <p>
 * 该接口定义了应用相关的所有业务操作，是应用模块的核心服务层。
 * 继承自MyBatis-Flex的IService接口，提供基础的CRUD操作，同时扩展了应用特有的业务方法。
 * <p>
 * 主要功能：
 * 1. 应用代码生成：通过AI对话生成应用代码
 * 2. 应用部署管理：部署应用到生产环境
 * 3. 应用查询和封装：提供灵活的应用查询和VO转换
 * 4. 应用生命周期管理：创建、更新、删除等操作
 * <p>
 * 设计原则：
 * - 接口与实现分离，便于测试和维护
 * - 支持响应式编程，提高系统性能
 * - 提供灵活的查询条件构建
 * - 统一的异常处理和业务逻辑
 *
 * @author <a href="https://github.com/wjp527">π</a>
 * @version 1.0
 * @see com.mybatisflex.core.service.IService
 * @see App
 * @see AppVO
 * @see User
 * @since 2025/8/16
 */
public interface AppService extends IService<App> {

    /**
     * 通过AI对话生成应用代码
     * <p>
     * 该方法使用AI模型根据用户的提示词生成应用代码。支持流式响应，可以实时返回生成进度。
     * 生成的代码会根据应用类型保存为相应的文件格式。
     * <p>
     * 工作流程：
     * 1. 验证用户权限和应用状态
     * 2. 调用AI模型生成代码
     * 3. 保存生成的代码到文件系统
     * 4. 更新应用状态和版本信息
     * <p>
     * 技术特点：
     * - 使用Reactor的Flux实现流式响应
     * - 支持长对话和上下文理解
     * - 自动保存和版本管理
     *
     * @param appId     要生成代码的应用ID，必须是已存在的有效应用
     * @param message   用户的提示词，描述需要生成的功能或修改
     * @param loginUser 当前登录用户，用于权限验证和操作记录
     * @return Flux<String> 流式返回生成的代码内容，支持实时预览
     * @throws IllegalArgumentException 当appId无效或message为空时
     * @throws SecurityException        当用户没有操作该应用的权限时
     * @throws RuntimeException         当AI服务不可用或代码生成失败时
     * @example 使用示例：
     * Flux<String> codeStream = appService.chatToGenCode(123L, "添加一个登录表单", currentUser);
     * codeStream.subscribe(code -> System.out.println("生成的代码: " + code));
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 部署应用到生产环境
     * <p>
     * 该方法将已生成代码的应用部署到生产环境，使其可以被外部用户访问。
     * 部署过程包括文件复制、权限配置、域名绑定等操作。
     * <p>
     * 部署流程：
     * 1. 验证应用状态和代码完整性
     * 2. 生成唯一的部署标识
     * 3. 复制代码文件到部署目录
     * 4. 配置访问权限和域名
     * 5. 更新应用部署状态
     * <p>
     * 部署结果：
     * - 返回部署后的访问URL
     * - 更新应用的部署时间和部署键
     * - 记录部署操作日志
     *
     * @param appId     要部署的应用ID，应用必须已经生成代码
     * @param loginUser 当前登录用户，用于权限验证和操作记录
     * @return String 部署成功后的访问URL，可以直接在浏览器中打开
     * @throws IllegalArgumentException 当appId无效或应用未生成代码时
     * @throws SecurityException        当用户没有部署该应用的权限时
     * @throws RuntimeException         当部署过程中出现文件系统错误或配置错误时
     * @example 使用示例：
     * String deployUrl = appService.deployApp(123L, currentUser);
     * System.out.println("应用已部署，访问地址: " + deployUrl);
     */
    String deployApp(Long appId, User loginUser);

    Long createApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 异步生成应用截图，并更新封面
     * @param appId 应用ID
     * @param appDeployUrl 应用部署URL
     */
    void generateAppScreenshotAsync(Long appId, String appDeployUrl);

    /**
     * 将应用实体转换为视图对象
     * <p>
     * 该方法将数据库中的应用实体对象转换为适合前端展示的视图对象。
     * 转换过程包括字段映射、关联对象查询、数据格式化等操作。
     * <p>
     * 转换内容：
     * - 基本字段的直接映射
     * - 关联用户信息的查询和封装
     * - 敏感字段的过滤
     * - 数据格式的标准化
     * <p>
     * 使用场景：
     * - 应用详情页面展示
     * - 应用列表数据返回
     * - API接口数据封装
     *
     * @param app 要转换的应用实体对象，不能为null
     * @return AppVO 转换后的应用视图对象，包含完整的展示信息
     * @throws IllegalArgumentException 当app参数为null时
     * @throws RuntimeException         当关联数据查询失败时
     * @example 使用示例：
     * App app = appService.getById(123L);
     * AppVO appVO = appService.getAppVO(app);
     * return ResponseEntity.ok(appVO);
     */
    AppVO getAppVO(App app);

    /**
     * 构建应用查询条件
     * <p>
     * 该方法根据查询请求参数构建MyBatis-Flex的查询条件对象。
     * 支持多种查询条件的组合，提供灵活的查询能力。
     * <p>
     * 支持的查询条件：
     * - 应用名称模糊查询
     * - 代码生成类型精确查询
     * - 创建时间范围查询
     * - 用户ID精确查询
     * - 优先级范围查询
     * - 部署状态查询
     * <p>
     * 查询特性：
     * - 支持分页查询
     * - 支持排序设置
     * - 支持逻辑删除过滤
     * - 支持权限控制
     *
     * @param appQueryRequest 应用查询请求对象，包含各种查询条件
     * @return QueryWrapper 构建好的查询条件对象，可直接用于数据库查询
     * @throws IllegalArgumentException 当查询参数格式不正确时
     * @example 使用示例：
     * AppQueryRequest request = new AppQueryRequest();
     * request.setAppName("博客");
     * request.setCodeGenType("MULTI_FILE");
     * QueryWrapper wrapper = appService.getQueryWrapper(request);
     * List<App> apps = appService.list(wrapper);
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 批量转换应用实体列表为视图对象列表
     * <p>
     * 该方法批量处理应用实体对象列表，将其转换为视图对象列表。
     * 批量转换可以提高性能，减少数据库查询次数。
     * <p>
     * 优化策略：
     * - 批量查询关联用户信息
     * - 减少数据库连接次数
     * - 支持并发处理
     * - 内存使用优化
     * <p>
     * 使用场景：
     * - 应用列表页面展示
     * - 搜索结果批量返回
     * - 数据导出和报表生成
     *
     * @param appList 要转换的应用实体对象列表，可以为空列表但不能为null
     * @return List<AppVO> 转换后的应用视图对象列表，保持原有的顺序
     * @throws IllegalArgumentException 当appList参数为null时
     * @throws RuntimeException         当批量查询关联数据失败时
     * @example 使用示例：
     * List<App> apps = appService.list();
     * List<AppVO> appVOs = appService.getAppVOList(apps);
     * return ResponseEntity.ok(appVOs);
     */
    List<AppVO> getAppVOList(List<App> appList);


    /**
     * 获取代码片段内容
     *
     * @param sourceDirPath 源代码目录路径
     * @return 包含各文件内容的Map
     */
    Map<String, String> getCodeSnippets(String sourceDirPath);



}
