package com.wjp.waicodermotherbackend.langgraph4j.ai;

import com.wjp.waicodermotherbackend.langgraph4j.model.QualityResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 代码质量检查服务
 */
public interface CodeQualityCheckService {
    /**
     * 检查代码质量
     * AI 会分析代码并返回质量检查结果
     * @param codeContent 代码内容
     * @return
     */

    @SystemMessage(fromResource = "prompt/code-quality-check-system-prompt.txt")
    QualityResult checkCodeQuality(@UserMessage String codeContent);

}
