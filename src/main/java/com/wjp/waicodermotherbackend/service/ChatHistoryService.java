package com.wjp.waicodermotherbackend.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wjp.waicodermotherbackend.model.dto.chathistory.ChatHistoryQueryRequest;
import com.wjp.waicodermotherbackend.model.entity.ChatHistory;
import com.wjp.waicodermotherbackend.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/wjp527">π</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话历史
     * @param appId
     * @param message
     * @param messageType
     * @param userId
     * @return
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 将数据库的会话记忆保存到内存中
     * @param appId
     * @param chatMemory
     * @param maxCount
     * @return
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 根据应用 id 删除对话记录
     * @param appId
     * @return
     */
    boolean deleteByAppId(Long appId);


    /**
     * 游标查询服务
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    /**
     * 获取查询包装类
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
