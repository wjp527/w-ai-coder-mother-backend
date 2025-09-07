package com.wjp.waicodermotherbackend.ai.handle;

import com.wjp.waicodermotherbackend.model.entity.User;
import com.wjp.waicodermotherbackend.model.enums.ChatHistoryMessageTypeEnum;
import com.wjp.waicodermotherbackend.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流 (HTML + MULTI_FILE)
     * 直接收集完整的文本响应
     * @param orginFlux 原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId 应用 ID
     * @param loginUser 登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> orginFlux, ChatHistoryService chatHistoryService, long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return orginFlux.map(chunk -> {
            // 实时收集 AI 响应的内容
            aiResponseBuilder.append(chunk);
            return chunk;
        }).doOnComplete(() -> {
            // 流式返回完成后，保存 AI 消息到对话历史中
            String aiResponse = aiResponseBuilder.toString();
            chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
        }).doOnError(err -> {
            // 如果 AI 回复失败，也需要保存记录到数据库中
            String errorMessage = "AI 回复失败: " + err.getMessage();
            chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
        });
    }

}
