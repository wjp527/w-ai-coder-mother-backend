package com.wjp.waicodermotherbackend.core.saver;

import com.wjp.waicodermotherbackend.ai.model.HtmlCodeResult;
import com.wjp.waicodermotherbackend.ai.model.MultiFileCodeResult;
import com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存执行器
 * 根据带阿米生成类型执行相应的保存逻辑
 */
public class CodeFileSaverExecutor {

    /**
     * html代码保存模板
     */
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();

    /**
     * 多文件代码保存模板
     */
    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaverTemplate = new MultiFileCodeFileSaverTemplate();

    /**
     * 执行代码保存
     * @param codeResult 代码结果对象
     * @param codeGenTypeEnum 代码生成类型
     * @Param appId 应用id
     * @return 保存的目录
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) codeResult, appId);
            case MULTI_FILE -> multiFileCodeFileSaverTemplate.saveCode((MultiFileCodeResult) codeResult, appId);
            default -> {
                String errorMessage = "不支持的生成类型:" + codeGenTypeEnum.getValue();
                throw new RuntimeException(errorMessage);
            }
        };
    }

}
