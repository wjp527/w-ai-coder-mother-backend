package com.wjp.waicodermotherbackend.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 持久化对话记忆
 */
@Configuration
@Data
public class RedisChatMemoryStoreConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    /**
     * 键的存活时间
     */
    @Value("${spring.data.redis.ttl:3600}")
    private long ttl;

    /**
     * 创建RedisChatMemoryStore
     * RedisChatMemoryStore: LangChain 和 Redis 整合包中的对话记忆存储
     * @return
     */
    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        // 定义 Redis 的存储
        return RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                // 如果你的密码不为空，是需要加上 .user("用户名") 默认为 default
                .password(password.isEmpty() ? null : password)
                .ttl(ttl)
                .build();
    }
}
