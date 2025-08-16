package com.wjp.waicodermotherbackend.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用实体类
 * 
 * 该实体类代表系统中的应用对象，包含应用的基本信息、配置信息、部署信息等。
 * 应用是系统的核心业务对象，用户可以通过AI生成代码创建应用，也可以部署和管理应用。
 * 
 * 主要功能：
 * 1. 存储应用的基本信息（名称、封面、描述等）
 * 2. 记录应用的代码生成类型和初始化提示
 * 3. 管理应用的部署状态和部署信息
 * 4. 跟踪应用的版本和编辑历史
 * 5. 支持逻辑删除和软删除
 * 
 * 数据库映射：
 * - 表名：app
 * - 主键：id（雪花算法生成）
 * - 逻辑删除字段：isDelete
 * 
 * @author <a href="https://github.com/wjp527">π</a>
 * @since 2025/8/16
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app")
public class App implements Serializable {

    /**
     * 序列化版本号
     * 
     * 该字段用于Java序列化机制，确保对象在不同JVM版本间的兼容性。
     * 当类的结构发生变化时，需要更新这个版本号。
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用唯一标识符
     * 
     * 该字段是应用的主键，使用雪花算法自动生成，确保全局唯一性。
     * 雪花算法生成的ID具有以下特点：
     * - 64位长整型，支持大规模数据
     * - 包含时间戳信息，便于排序和查询
     * - 分布式环境下唯一性保证
     * 
     * 数据库配置：
     * - 主键字段，不允许为空
     * - 自动生成，无需手动设置
     * - 支持范围查询和排序
     * 
     * @example 示例值：1234567890123456789
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 应用名称
     * 
     * 该字段存储应用的显示名称，是用户识别应用的主要标识。
     * 应用名称应该具有描述性，能够清楚地表达应用的功能和用途。
     * 
     * 约束条件：
     * - 不能为空
     * - 建议长度在2-50个字符之间
     * - 支持中文、英文、数字和常用符号
     * 
     * @example 示例值："个人博客系统"、"在线购物车"、"任务管理工具"
     */
    @Column("appName")
    private String appName;

    /**
     * 应用封面图片
     * 
     * 该字段存储应用的封面图片URL或路径，用于在应用列表中展示应用的可视化标识。
     * 封面图片可以提升用户体验，帮助用户快速识别不同的应用。
     * 
     * 存储格式：
     * - 可以是完整的HTTP URL
     * - 可以是相对路径
     * - 支持常见的图片格式（JPG、PNG、GIF等）
     * 
     * @example 示例值："https://example.com/images/blog-cover.jpg"、"/static/covers/blog.png"
     */
    private String cover;

    /**
     * 应用初始化的提示词
     * 
     * 该字段存储用户创建应用时输入的初始提示词，用于AI生成代码的指导。
     * 提示词的质量直接影响生成代码的质量和符合度。
     * 
     * 内容要求：
     * - 应该清晰描述应用的功能需求
     * - 包含技术栈、设计风格等具体要求
     * - 支持多语言（中文、英文等）
     * 
     * @example 示例值："创建一个现代化的个人博客系统，使用Vue.js前端框架，支持响应式设计"
     */
    @Column("initPrompt")
    private String initPrompt;

    /**
     * 代码生成类型
     * 
     * 该字段标识应用使用的代码生成类型，系统根据不同的类型采用不同的代码生成策略。
     * 支持的类型包括HTML单文件、多文件项目等。
     * 
     * 枚举值：
     * - HTML：单文件HTML应用
     * - MULTI_FILE：多文件项目（HTML+CSS+JS）
     * - 其他：根据业务需求扩展
     * 
     * @see com.wjp.waicodermotherbackend.model.enums.CodeGenTypeEnum
     */
    @Column("codeGenType")
    private String codeGenType;

    /**
     * 应用部署标识键
     * 
     * 该字段存储应用部署后的唯一标识，用于访问已部署的应用。
     * 部署键通常用于构建应用的访问URL，确保部署后的应用能够被正确访问。
     * 
     * 生成规则：
     * - 系统自动生成，确保唯一性
     * - 通常包含时间戳和随机字符
     * - 用于构建部署URL的一部分
     * 
     * @example 示例值："deploy_20250816_123456"、"app_abc123_def456"
     */
    @Column("deployKey")
    private String deployKey;

    /**
     * 应用部署时间
     * 
     * 该字段记录应用最后一次成功部署的时间，用于跟踪应用的部署历史。
     * 部署时间对于应用的生命周期管理和版本控制非常重要。
     * 
     * 时间格式：
     * - 使用LocalDateTime类型，支持纳秒精度
     * - 自动记录部署操作的时间戳
     * - 可用于计算部署频率和部署间隔
     * 
     * @example 示例值：2025-08-16T10:30:00
     */
    @Column("deployedTime")
    private LocalDateTime deployedTime;

    /**
     * 应用优先级
     * 
     * 该字段定义应用在系统中的优先级，用于控制应用的显示顺序、资源分配等。
     * 优先级越高，应用在相关列表中的排序越靠前。
     * 
     * 优先级规则：
     * - 数值越大，优先级越高
     * - 默认优先级为0
     * - 特殊应用（如精选应用）可以设置更高的优先级
     * 
     * @example 示例值：99（精选应用）、50（普通应用）、0（默认优先级）
     */
    private Integer priority;

    /**
     * 创建用户的唯一标识
     * 
     * 该字段记录创建应用的用户ID，用于用户权限管理和应用归属管理。
     * 系统根据用户ID控制用户对应用的访问权限和操作权限。
     * 
     * 关联关系：
     * - 外键关联用户表
     * - 支持用户查询自己创建的应用
     * - 用于权限控制和数据隔离
     * 
     * @example 示例值：123456
     */
    @Column("userId")
    private Long userId;

    /**
     * 应用版本号
     * 
     * 该字段记录应用的当前版本号，用于版本管理和更新追踪。
     * 版本号通常采用语义化版本控制，便于理解版本的变化程度。
     * 
     * 版本规则：
     * - 从1开始递增
     * - 每次编辑或更新时递增
     * - 支持版本回滚和比较
     * 
     * @example 示例值：1、2、3
     */
    @Column("version")
    private Integer version;

    /**
     * 应用最后编辑时间
     * 
     * 该字段记录应用最后一次被编辑的时间，用于跟踪应用的修改历史。
     * 编辑时间对于应用的变更追踪和审计非常重要。
     * 
     * 更新时机：
     * - 应用信息被修改时自动更新
     * - 代码重新生成时更新
     * - 用于计算应用的活跃度
     * 
     * @example 示例值：2025-08-16T15:45:30
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 应用创建时间
     * 
     * 该字段记录应用被创建的时间，是应用生命周期的起点。
     * 创建时间不可修改，用于应用的创建历史追踪。
     * 
     * 特点：
     * - 在应用创建时自动设置
     * - 后续不可修改
     * - 用于计算应用的年龄和统计信息
     * 
     * @example 示例值：2025-08-16T09:00:00
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 应用最后更新时间
     * 
     * 该字段记录应用最后一次被更新的时间，包括所有字段的变更。
     * 更新时间用于数据同步、缓存失效等场景。
     * 
     * 更新机制：
     * - 任何字段变更时自动更新
     * - 支持乐观锁和并发控制
     * - 用于数据一致性检查
     * 
     * @example 示例值：2025-08-16T16:20:15
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识
     * 
     * 该字段用于实现逻辑删除，而不是物理删除数据。
     * 逻辑删除可以保留数据的历史记录，同时避免数据丢失。
     * 
     * 删除机制：
     * - 0：正常状态
     * - 1：已删除状态
     * - 查询时自动过滤已删除的记录
     * - 支持数据恢复功能
     * 
     * @example 示例值：0（正常）、1（已删除）
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
