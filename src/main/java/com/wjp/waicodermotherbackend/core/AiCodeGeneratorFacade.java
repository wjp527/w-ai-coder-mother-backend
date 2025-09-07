package com.wjp.waicodermotherbackend.core;

import com.wjp.waicodermotherbackend.ai.AiCodeGeneratorService;
import com.wjp.waicodermotherbackend.ai.AiCodeGeneratorServiceFactory;
import com.wjp.waicodermotherbackend.ai.model.HtmlCodeResult;
import com.wjp.waicodermotherbackend.ai.model.MultiFileCodeResult;
import com.wjp.waicodermotherbackend.core.parser.CodeParserExecutor;
import com.wjp.waicodermotherbackend.core.saver.CodeFileSaverExecutor;
import com.wjp.waicodermotherbackend.exception.BusinessException;
import com.wjp.waicodermotherbackend.exception.ErrorCode;
import com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    // 单个 AI Service
//    @Resource
//    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 根据 appId 生成独立的 AI Service
     */
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;


    /**
     * 统一入口: 根据类型生成代码流并保存代码
     * @param codeStream AI返回的结果(流)
     * @param codeGenType 代码生成类型(业务类型 HTML 还是 MULTI_FILE)
     * @return 生成的代码文件
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        // 当流式返回生成代码完成后，在保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream
                .doOnNext(chunk -> {
                    // 实时收集代码片段
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流式返回完成后保存代码
                    try {
                        String completeCode = codeBuilder.toString();
                        // 使用执行器解析代码
                        // 内部会根据不同的业务类型进行执行对应的执行器
                        Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                        // 使用执行器保存代码
                        File saveDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                        log.info("保存成功，路径为: " + saveDir.getAbsolutePath());
                    } catch(Exception e) {
                        log.error("保存失败: " + e.getMessage());
                    }
                });
    }

    // region SSE 流失输出

    /**
     * 统一入口: 根据类型生成并保存代码（SSE流式）
     * @param userMessage 用户消息
     * @param codeGenTypeEnum 代码生成类型
     * @return 生成的代码文件
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if(codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器类型为空");
        }

        // 根据 appId 获取相应的 AI Service [根据代码生成类型获取对应的AI服务]
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        return switch (codeGenTypeEnum) {
            case HTML -> {
                // AI返回的结果
                Flux<String> result = aiCodeGeneratorService.generateHTMLCodeStream(userMessage);
                // yield: 等到所有的数据都处理完成后再返回结果
                yield processCodeStream(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型:" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    // endregion



    /**
     * 统一入口: 根据类型生成并保存代码
     * @param userMessage 用户消息
     * @param codeGenTypeEnum 代码生成类型
     * @return 生成的代码文件
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if(codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器类型为空");
        }

        // 根据 appId 获取相应的 AI Service
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        return switch (codeGenTypeEnum) {
            case HTML -> {
                // 获取AI生成的结果
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userMessage);
                // 进行存储文件
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型:" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }
}
