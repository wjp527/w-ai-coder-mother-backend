package com.wjp.waicodermotherbackend.langgraph4j.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 图片收集AI服务接口
 * 使用AI 调用工具手机不同类型的图片资源
 */
public interface ImageCollectionService {
    /**
     * 根据用户提示词收集所需要的图片资源
     * AI 会根据需求自主选择调用相应的工具
     * @param userPrompt 用户提示词
     * @return 图片资源
     */

    @SystemMessage(fromResource = "prompt/image-collection-system-prompt.txt")
    String collectImages(@UserMessage String userPrompt);

}
