package com.wjp.waicodermotherbackend.ai;

import com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;
import org.springframework.stereotype.Service;

/**
 * AI代码生成类型整你路由服务
 * 使用结构化输出直接返回枚举类型
 *
 * @author wjp
 */
public interface AiCodeGenTypeRoutingService {

    /**
     * 根据用户需求只能选择代码生成类型
     * @param userPrompt 用户输入的需求描述
     * @return 推荐的代码生成类型
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);

}
