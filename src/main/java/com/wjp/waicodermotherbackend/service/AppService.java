package com.wjp.waicodermotherbackend.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wjp.waicodermotherbackend.model.dto.app.AppQueryRequest;
import com.wjp.waicodermotherbackend.model.dto.app.AppVO;
import com.wjp.waicodermotherbackend.model.entity.App;
import com.wjp.waicodermotherbackend.model.entity.User;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/wjp527">π</a>
 */
public interface AppService extends IService<App> {

    /**
     * 通过对话生成应用代码
     * @param appId 应用id
     * @param message 提示词
     * @param loginUser 登录用户
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 获取应用封装类
     * @param app
     * @return
     */
    AppVO getAppVO(App app);


    /**
     * 获取查询条件
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用列表封装类
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

}
