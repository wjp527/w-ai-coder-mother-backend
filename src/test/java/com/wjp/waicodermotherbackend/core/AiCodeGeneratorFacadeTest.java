package com.wjp.waicodermotherbackend.core;

import com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 门面单元测试
 */
@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
//        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个π的博客，不要超过20行", CodeGenTypeEnum.HTML);
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个π的博客，不要超过20行", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);

    }
}