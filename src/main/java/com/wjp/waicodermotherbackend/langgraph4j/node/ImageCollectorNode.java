package com.wjp.waicodermotherbackend.langgraph4j.node;

import com.wjp.waicodermotherbackend.langgraph4j.ai.ImageCollectionService;
import com.wjp.waicodermotherbackend.langgraph4j.model.ImageResource;
import com.wjp.waicodermotherbackend.langgraph4j.model.enums.ImageCategoryEnum;
import com.wjp.waicodermotherbackend.langgraph4j.state.WorkflowContext;
import com.wjp.waicodermotherbackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.Arrays;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片收集节点
 */
@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 图片收集");
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = "";
            
           try {
               // 获取AI图片收集服务
               // 这里static 是无法直接 获取 Bean 的，只能通过 SpringContextUtil.getBean() 获取
               ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
               // 使用 AI 服务进行智能图片获取
               imageListStr = imageCollectionService.collectImages(originalPrompt);
               imageCollectionService.collectImages(originalPrompt);
           } catch(Exception e) {

           }

            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            return WorkflowContext.saveContext(context);
        });
    }
}
