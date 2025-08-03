package com.wjp.waicodermotherbackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.wjp.waicodermotherbackend.ai.model.HtmlCodeResult;
import com.wjp.waicodermotherbackend.exception.BusinessException;
import com.wjp.waicodermotherbackend.exception.ErrorCode;
import com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum;


/**
 * html代码保存模板
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    /**
     * 获取代码业务类型 （HTML / MULTI_FILE）
     * @return
     */
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    /**
     * 保存文件
     * @param result
     * @param baseDirPath
     */
    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        // 写入单个文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }


    /**
     * 验证输入
     * @param result
     */
    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码不能为空");
        }
    }
}
