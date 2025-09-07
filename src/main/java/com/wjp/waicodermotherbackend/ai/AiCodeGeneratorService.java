package com.wjp.waicodermotherbackend.ai;

import com.wjp.waicodermotherbackend.ai.model.HtmlCodeResult;
import com.wjp.waicodermotherbackend.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * 生成代码的模式[通过AI调用，返回结果]
 */
public interface AiCodeGeneratorService {

    /**
     * 生成 HTML 代码
     * @param userMessage 用户消息
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt") // 系统提示词注解
    HtmlCodeResult generateHTMLCode(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage 用户消息
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt") // 系统提示词注解
    MultiFileCodeResult generateMultiFileCode(String userMessage);


    /**
     * 生成 HTML 代码（SSE流式）
     * @param userMessage 用户消息
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt") // 系统提示词注解
    Flux<String> generateHTMLCodeStream(String userMessage);

    /**
     * 生成多文件代码（SSE流式）
     * @param userMessage 用户消息
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt") // 系统提示词注解
    Flux<String>  generateMultiFileCodeStream(String userMessage);


    /**
     * 生成Vue项目代码（SSE流式）
     * @param appId 应用ID(LangChain4j 会默认带上appId)，加上 @MemoryId，会自动传递给工具调用的 @ToolMemoryId long appId
     * @param userMessage 用户消息 只要用了@MemoryId，@UserMessage也要加上
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt") // 系统提示词注解
    Flux<String> generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);

}
