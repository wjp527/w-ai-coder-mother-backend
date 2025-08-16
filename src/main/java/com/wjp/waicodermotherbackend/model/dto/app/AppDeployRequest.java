package com.wjp.waicodermotherbackend.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用部署请求数据传输对象
 * 
 * 该DTO用于接收前端发送的应用部署请求，包含部署应用所需的基本信息。
 * 主要用于应用部署流程中的数据传输，确保前后端数据交互的一致性和安全性。
 * 
 * 使用场景：
 * 1. 用户点击部署按钮时，前端发送部署请求
 * 2. 管理员批量部署应用时使用
 * 3. 自动化部署脚本调用时使用
 * 
 * @author dhbxs
 * @since 2025/8/16
 * @version 1.0
 */
@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用唯一标识符
     * 
     * 该字段用于标识要部署的具体应用，是部署操作的核心参数。
     * 系统会根据这个ID查找对应的应用信息，包括：
     * - 应用的基本信息（名称、描述、版本等）
     * - 应用的代码文件路径
     * - 应用的配置信息
     * - 部署相关的元数据
     * 
     * 约束条件：
     * - 不能为null
     * - 必须是数据库中存在的有效应用ID
     * - 应用状态必须允许部署（如：已生成、未部署等）
     * 
     * @example 示例值：123456789
     */
    private Long appId;

    /**
     * 序列化版本号
     * 
     * 该字段用于Java序列化机制，确保对象在不同JVM版本间的兼容性。
     * 当类的结构发生变化时，需要更新这个版本号。
     * 
     * 注意事项：
     * - 建议在类结构变更时手动更新
     * - 保持与数据库版本的一致性
     * - 便于版本控制和兼容性管理
     */
    private static final long serialVersionUID = 1L;
}
