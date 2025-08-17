package com.wjp.waicodermotherbackend.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wjp.waicodermotherbackend.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 服务创建工厂
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    /**
     *  模型
     */
    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;


    /**
     * AI 服务实例缓存
     * 缓存策略:
     *  - 最大缓存：1000个实例
     *  - 写入后 30分钟过期
     *  - 访问后 10分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除, appId:{},原因:{}", key, cause);
            })
            .build();

    private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
        log.info("为 appId: {} 创建新的 AI服务实例", appId);
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * 根据 appId 获取服务
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {

        // 如果appId有现成的AI Service，那就直接返回，如果没有，则去 createAiCodeGeneratorService 创建一个
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }
    /**
     * 创建 AI 代码生成器服务
     * @return
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0);
    }

}
