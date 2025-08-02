package com.wjp.waicodermotherbackend.ai;

import com.wjp.waicodermotherbackend.ai.model.HtmlCodeResult;
import com.wjp.waicodermotherbackend.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 生成代码的模式
 */
@SpringBootTest
class AiCodeGeneratorServiceTest {

    /**
     * AI Service
     */
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 生成 HTML 代码
     */
    @Test
    void generateHTMLCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHTMLCode("作者π的博客，不要超过20行");
        Assertions.assertNotNull(result);
    }

    /**
     * 生成多文件代码
     */
    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("作者π的美食网站，不要超过20行");
        Assertions.assertNotNull(result);
    }
}