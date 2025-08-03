package com.wjp.waicodermotherbackend.core.parser;

import com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 * 根据代码生成类型执行响应的解析逻辑
 */
public class CodeParserExecutor {

    /**
     * HTML代码解析器
     */
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    /**
     * 多文件代码解析器
     */
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     * @param codeContent 代码内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果(HtmlCodeResult 或 MultiFileCodeResult)
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> {
                String errorMessage = "不支持的生成类型:" + codeGenTypeEnum.getValue();
                throw new RuntimeException(errorMessage);
            }
        };
    }

}
