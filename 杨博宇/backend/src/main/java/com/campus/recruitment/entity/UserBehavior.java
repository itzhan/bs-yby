package com.campus.recruitment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_behavior")
public class UserBehavior {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long jobId;

    /** 1=浏览 2=投递 */
    private Integer behaviorType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
