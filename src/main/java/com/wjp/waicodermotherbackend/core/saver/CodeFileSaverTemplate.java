package com.wjp.waicodermotherbackend.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.wjp.waicodermotherbackend.exception.BusinessException;
import com.wjp.waicodermotherbackend.exception.ErrorCode;
import com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器 - 模板方法模式
 */
public abstract class CodeFileSaverTemplate<T> {

    /**
     * 文件保存目录
     */
    protected static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 模版方法: 保存代码的标准流程
     * @param result 代码结果对象
     * @return 保存的目录
     */
    public final File saveCode(T result) {
        // 1.验证输入
        validateInput(result);

        // 2.构建唯一目录
        String baseDirPath = buildUniqueDir();

        // 3.保存文件（具体实现由子类提供）
        saveFiles(result, baseDirPath);

        // 4.返回目录文件对象
        return new File(baseDirPath);
    }

    /**
     * 1.验证输入(可由子类覆盖)
     * @param result
     */
    protected void validateInput(T result) {
        if(result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
        }
    }

    /**
     * 2.构建唯一目录
     * 构建文件的唯一路径: tmp/code_output/bizType_雪花ID
     *
     * @return
     */
    protected String buildUniqueDir() {
        // 获取业务类型
        String bizType = getCodeType().getValue();
        // 雪花算法
        String snowflakeNextIdStr = IdUtil.getSnowflakeNextIdStr();
        String uniqueDirName = StrUtil.format("{}_{}", bizType, snowflakeNextIdStr);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 获取代码类型 (由子类实现)
     * @return 代码生成类型枚举
     */
    protected abstract CodeGenTypeEnum getCodeType();


    /**
     * 3.保存文件（具体实现由子类提供）
     * @param result
     * @param baseDirPath
     */
    protected abstract void saveFiles(T result, String baseDirPath);


    /**
     * 保存单个文件
     *
     * @param dirPath
     * @param filename
     * @param content
     */
    protected final void writeToFile(String dirPath, String filename, String content){
        if(StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

}
