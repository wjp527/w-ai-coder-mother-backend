package com.wjp.waicodermotherbackend.ai;

import com.wjp.waicodermotherbackend.ai.model.HtmlCodeResult;
import com.wjp.waicodermotherbackend.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
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

}
