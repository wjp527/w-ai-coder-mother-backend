package com.wjp.waicodermotherbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wjp.waicodermotherbackend.constant.AppConstant;
import com.wjp.waicodermotherbackend.core.AiCodeGeneratorFacade;
import com.wjp.waicodermotherbackend.exception.BusinessException;
import com.wjp.waicodermotherbackend.exception.ErrorCode;
import com.wjp.waicodermotherbackend.exception.ThrowUtils;
import com.wjp.waicodermotherbackend.model.dto.app.AppQueryRequest;
import com.wjp.waicodermotherbackend.model.vo.AppVO;
import com.wjp.waicodermotherbackend.model.entity.App;
import com.wjp.waicodermotherbackend.mapper.AppMapper;
import com.wjp.waicodermotherbackend.model.entity.User;
import com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum;
import com.wjp.waicodermotherbackend.model.vo.UserVO;
import com.wjp.waicodermotherbackend.service.AppService;
import com.wjp.waicodermotherbackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用服务实现类
 * 
 * 该类实现了AppService接口，提供应用相关的所有业务逻辑实现。
 * 继承自MyBatis-Flex的ServiceImpl，获得基础的CRUD操作能力，同时实现自定义的业务方法。
 * 
 * 核心功能：
 * 1. AI代码生成：通过对话生成应用代码，支持流式响应
 * 2. 应用部署：将生成的代码部署到生产环境
 * 3. 数据转换：实体对象与视图对象之间的转换
 * 4. 查询构建：动态构建复杂的数据库查询条件
 * 5. 批量处理：优化批量数据查询和转换性能
 * 
 * 技术特点：
 * - 使用Reactor实现响应式编程
 * - 集成AI代码生成服务
 * - 支持文件系统操作
 * - 优化N+1查询问题
 * - 统一的异常处理机制
 * 
 * @author <a href="https://github.com/wjp527">π</a>
 * @since 2025/8/16
 * @version 1.0
 * @see AppService
 * @see ServiceImpl
 * @see AiCodeGeneratorFacade
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    /**
     * 用户服务依赖注入
     * 
     * 用于查询应用创建者的详细信息，支持用户信息的关联查询。
     * 在应用VO转换过程中，需要获取创建用户的详细信息。
     */
    @Resource
    private UserService userService;

    /**
     * AI代码生成器门面类
     * 
     * 负责调用AI模型生成应用代码，支持流式响应和代码保存。
     * 是AI代码生成功能的核心组件。
     */
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    /**
     * 通过AI对话生成应用代码
     * 
     * 该方法实现了AI驱动的代码生成功能，用户可以通过自然语言描述需求，
     * 系统自动生成相应的应用代码。支持流式响应，用户可以实时看到生成进度。
     * 
     * 业务流程：
     * 1. 参数校验：验证应用ID和提示词的有效性
     * 2. 权限验证：确保只有应用创建者可以生成代码
     * 3. 类型识别：获取应用的代码生成类型
     * 4. AI生成：调用AI服务生成代码
     * 5. 流式返回：实时返回生成的代码内容
     * 
     * 安全控制：
     * - 应用ID必须有效且存在
     * - 提示词不能为空
     * - 只有应用创建者可以操作
     * - 代码生成类型必须有效
     * 
     * @param appId 要生成代码的应用ID，必须是已存在的有效应用
     * @param message 用户的提示词，描述需要生成的功能或修改
     * @param loginUser 当前登录用户，用于权限验证
     * @return Flux<String> 流式返回生成的代码内容
     * 
     * @throws BusinessException 当权限不足、应用不存在或系统错误时
     * @throws IllegalArgumentException 当参数无效时
     * 
     * @example 使用示例：
     * Flux<String> codeStream = appService.chatToGenCode(123L, "添加一个登录表单", currentUser);
     * codeStream.subscribe(code -> System.out.println("生成的代码: " + code));
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验：确保应用ID和提示词的有效性
        ThrowUtils.throwIf(appId == null || appId <= 0 || message == null, ErrorCode.PARAMS_ERROR);
        
        // 2. 查询应用信息：从数据库获取应用详情
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        
        // 3. 权限验证：确保只有应用创建者可以生成代码
        if(!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        
        // 4. 获取应用的代码生成类型，用于确定代码生成策略
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if(codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        
        // 5. 调用AI代码生成器，返回流式响应
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

    /**
     * 部署应用到生产环境
     * 
     * 该方法实现了应用的完整部署流程，将已生成的代码部署到生产环境，
     * 使其可以被外部用户访问。部署过程包括文件复制、版本管理、状态更新等。
     * 
     * 部署流程：
     * 1. 参数校验和权限验证
     * 2. 生成或获取部署标识
     * 3. 检查源代码目录
     * 4. 复制文件到部署目录
     * 5. 更新应用状态和版本
     * 6. 返回部署访问地址
     * 
     * 版本管理：
     * - 每次部署都会创建新的版本目录
     * - 版本号自动递增
     * - 支持多版本并存
     * - 便于版本回滚和比较
     * 
     * @param appId 要部署的应用ID，应用必须已经生成代码
     * @param loginUser 当前登录用户，用于权限验证
     * @return String 部署成功后的访问URL
     * 
     * @throws BusinessException 当权限不足、应用不存在、代码未生成或部署失败时
     * @throws IllegalArgumentException 当参数无效时
     * 
     * @example 使用示例：
     * String deployUrl = appService.deployApp(123L, currentUser);
     * System.out.println("应用已部署，访问地址: " + deployUrl);
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验：确保应用ID和登录用户的有效性
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2. 查询应用信息：从数据库获取应用详情
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);

        // 3. 权限验证：确保只有应用创建者可以部署
        if(!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 4. 检查是否已有部署标识，如果没有则生成新的
        String deployKey = app.getDeployKey();
        Integer version = app.getVersion();
        
        // 没有部署标识则生成6位随机字符串作为部署键
        if(deployKey == null) {
            deployKey = RandomUtil.randomString(6);
        }
        
        // 5. 获取代码生成类型，构建源代码目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        // 6. 检查源代码目录是否存在，确保代码已经生成
        File sourceDir = new File(sourceDirPath);
        if(!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "原目录不存在,请先生成代码");
        }

        // 7. 复制文件到部署目录，创建新版本
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey + File.separator + "V" + ++version;
        try {
            // 复制源代码到部署目录，支持递归复制
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch(Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败: " + e.getMessage());
        }
        
        // 8. 更新数据库中的应用部署信息
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setVersion(version);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");

        // 9. 返回部署后的访问地址
        return String.format("%s/%s/V%s", AppConstant.CODE_DEPLOY_HOST, deployKey, version);
    }

    /**
     * 将应用实体转换为视图对象
     * 
     * 该方法实现了应用实体对象到视图对象的转换，包括基本字段映射和关联数据查询。
     * 转换后的VO对象更适合前端展示，去除了敏感字段，增加了用户信息。
     * 
     * 转换逻辑：
     * - 使用BeanUtil进行基本字段的自动映射
     * - 查询并封装创建用户的详细信息
     * - 过滤敏感字段（如逻辑删除标识）
     * - 确保返回对象的完整性
     * 
     * 性能优化：
     * - 只在需要时查询用户信息
     * - 避免不必要的数据库查询
     * - 支持空值处理
     * 
     * @param app 要转换的应用实体对象
     * @return AppVO 转换后的应用视图对象，如果输入为null则返回null
     * 
     * @example 使用示例：
     * App app = appService.getById(123L);
     * AppVO appVO = appService.getAppVO(app);
     * return ResponseEntity.ok(appVO);
     */
    @Override
    public AppVO getAppVO(App app) {
        // 空值检查：如果输入为null，直接返回null
        if(app == null) {
            return null;
        }

        // 创建新的VO对象并复制基本属性
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        
        // 关联查询用户信息：获取应用创建者的详细信息
        Long userId = app.getUserId();
        if(userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        
        return appVO;
    }

    /**
     * 构建应用查询条件
     * 
     * 该方法根据查询请求参数动态构建MyBatis-Flex的查询条件对象。
     * 支持多种查询条件的组合，提供灵活的查询能力。
     * 
     * 支持的查询条件：
     * - 精确查询：ID、代码生成类型、部署键、优先级、用户ID
     * - 模糊查询：应用名称、封面、初始化提示词
     * - 排序：支持字段排序和排序方向
     * - 分页：支持分页查询参数
     * 
     * 查询优化：
     * - 动态构建查询条件，避免无效查询
     * - 支持空值过滤，提高查询效率
     * - 统一的排序规则处理
     * 
     * @param appQueryRequest 应用查询请求对象，包含各种查询条件
     * @return QueryWrapper 构建好的查询条件对象
     * 
     * @throws BusinessException 当查询请求参数为空时
     * 
     * @example 使用示例：
     * AppQueryRequest request = new AppQueryRequest();
     * request.setAppName("博客");
     * request.setCodeGenType("MULTI_FILE");
     * QueryWrapper wrapper = appService.getQueryWrapper(request);
     * List<App> apps = appService.list(wrapper);
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        // 参数校验：确保查询请求对象不为空
        if(appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        
        // 提取查询参数：从请求对象中获取各种查询条件
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        // 构建查询条件：使用MyBatis-Flex的QueryWrapper
        return QueryWrapper.create()
                .eq("id", id)                    // 精确查询ID
                .like("appName", appName)        // 模糊查询应用名称
                .like("cover", cover)            // 模糊查询封面
                .like("initPrompt", initPrompt)  // 模糊查询初始化提示词
                .eq("codeGenType", codeGenType)  // 精确查询代码生成类型
                .eq("deployKey", deployKey)      // 精确查询部署键
                .eq("priority", priority)        // 精确查询优先级
                .eq("userId", userId)            // 精确查询用户ID
                .orderBy(sortField, "ascend".equals(sortOrder)); // 动态排序
    }

    /**
     * 批量转换应用实体列表为视图对象列表
     * 
     * 该方法优化了批量数据转换的性能，通过批量查询用户信息避免N+1查询问题。
     * 适用于应用列表、搜索结果等需要批量展示数据的场景。
     * 
     * 性能优化策略：
     * 1. 批量查询：一次性查询所有需要的用户信息
     * 2. 内存映射：使用Map结构快速查找用户信息
     * 3. 流式处理：使用Stream API进行高效的数据转换
     * 4. 避免重复：减少数据库查询次数
     * 
     * 处理流程：
     * 1. 收集所有应用的用户ID
     * 2. 批量查询用户信息
     * 3. 构建用户ID到用户VO的映射
     * 4. 批量转换应用对象
     * 
     * @param appList 要转换的应用实体对象列表
     * @return List<AppVO> 转换后的应用视图对象列表
     * 
     * @example 使用示例：
     * List<App> apps = appService.list();
     * List<AppVO> appVOs = appService.getAppVOList(apps);
     * return ResponseEntity.ok(appVOs);
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        // 空值检查：如果列表为空，直接返回空列表
        if(CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }

        // 批量获取用户信息：收集所有应用的用户ID，避免N+1查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());

        // 构建用户ID到用户VO的映射：一次性查询所有用户信息
        Map<Long, UserVO> userVOMap =
                userService.listByIds(userIds)           // 批量查询用户列表
                        .stream()                        // 转换为流
                        .collect(Collectors.toMap(       // 构建映射关系
                                User::getId,             // 用户ID作为键
                                userService::getUserVO   // 用户VO作为值
                        ));
        
        // 批量转换应用对象：使用预查询的用户信息进行转换
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);                 // 转换基本应用信息
            UserVO userVO = userVOMap.get(app.getUserId()); // 从映射中获取用户信息
            appVO.setUser(userVO);                       // 设置用户信息
            return appVO;                                // 返回完整的应用VO
        }).collect(Collectors.toList());                 // 收集为列表
    }
}
