package com.campus.recruitment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    /** 教育经历（JSON） */
    private String educationExperience;

    /** 工作/实习经历（JSON） */
    private String workExperience;

    /** 项目经历（JSON） */
    private String projectExperience;

    private String skills;

    private String awards;

    private String selfEvaluation;

    /** 是否默认简历 */
    private Boolean isDefault;

    /** 0=私密 1=公开 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
