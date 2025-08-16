package com.wjp.waicodermotherbackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.wjp.waicodermotherbackend.ai.model.HtmlCodeResult;
import com.wjp.waicodermotherbackend.ai.model.MultiFileCodeResult;
import com.wjp.waicodermotherbackend.exception.BusinessException;
import com.wjp.waicodermotherbackend.exception.ErrorCode;
import com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum;


/**
 * 多文件代码保存模板类
 * 
 * 该类继承自CodeFileSaverTemplate，专门用于处理多文件代码的保存逻辑。
 * 多文件代码通常包含HTML、CSS、JavaScript等不同类型的文件，需要分别保存到不同的文件中。
 * 
 * 主要功能：
 * 1. 定义多文件代码的业务类型标识
 * 2. 实现多文件代码的保存逻辑
 * 3. 提供输入验证，确保代码完整性
 * 
 * 使用场景：
 * - 前端应用代码生成后的文件保存
 * - 多文件项目的代码管理
 * - 代码生成器的文件输出处理
 * 
 * @author dhbxs
 * @since 2025/8/16
 * @version 1.0
 * @see CodeFileSaverTemplate
 * @see MultiFileCodeResult
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    
    /**
     * 获取代码业务类型标识
     * 
     * 该方法返回多文件代码的业务类型枚举值，用于系统识别和处理不同类型的代码。
     * 返回MULTI_FILE类型，表示这是一个包含多个文件的代码项目。
     * 
     * @return CodeGenTypeEnum.MULTI_FILE 多文件代码类型枚举
     * 
     * @see CodeGenTypeEnum#MULTI_FILE
     */
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    /**
     * 保存多文件代码到指定目录
     * 
     * 该方法负责将多文件代码结果保存到文件系统中，每个代码片段保存为独立的文件。
     * 保存的文件包括：
     * - index.html：HTML页面结构
     * - style.css：CSS样式文件
     * - script.js：JavaScript脚本文件
     * 
     * 文件保存顺序：
     * 1. 首先保存HTML文件（作为入口文件）
     * 2. 然后保存CSS文件（样式定义）
     * 3. 最后保存JS文件（交互逻辑）
     * 
     * @param result 多文件代码结果对象，包含HTML、CSS、JS代码内容
     * @param baseDirPath 基础目录路径，所有文件将保存到此目录下
     * 
     * @see MultiFileCodeResult
     * @see #writeToFile(String, String, String)
     */
    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        // 依次写入文件，确保文件保存的完整性和顺序性
        
        // 保存HTML文件：作为应用的主入口文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        
        // 保存CSS文件：包含页面的样式定义
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        
        // 保存JavaScript文件：包含页面的交互逻辑
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }


    /**
     * 验证多文件代码输入的完整性
     * 
     * 该方法在保存文件之前对输入进行验证，确保代码的完整性和有效性。
     * 验证规则：
     * 1. 调用父类的通用验证逻辑
     * 2. 检查HTML代码是否为空（HTML是必需的入口文件）
     * 3. 如果验证失败，抛出业务异常
     * 
     * 验证失败的情况：
     * - HTML代码为空或只包含空白字符
     * - 其他父类验证失败的情况
     * 
     * @param result 待验证的多文件代码结果对象
     * @throws BusinessException 当验证失败时抛出业务异常
     * 
     * @see BusinessException
     * @see ErrorCode#SYSTEM_ERROR
     */
    @Override
    protected void validateInput(MultiFileCodeResult result) {
        // 调用父类的通用验证逻辑，确保基础验证通过
        super.validateInput(result);
        
        // 验证HTML代码不能为空
        // HTML是多文件应用的核心文件，必须存在有效内容
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "多文件代码不能为空");
        }
    }
}
