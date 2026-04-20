package com.eduplatform.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI对话记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_record")
public class AiChatRecord extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 课程ID */
    private Long courseId;

    /** 会话ID */
    private String sessionId;

    /** 角色: user/assistant/system */
    private String role;

    /** 消息内容 */
    private String content;

    /** 使用的模型 */
    private String model;

    /** 消耗的token数 */
    private Integer tokensUsed;
}
