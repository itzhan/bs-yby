package com.campus.recruitment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview")
public class Interview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private Long companyId;

    private Long studentId;

    private Long jobId;

    private LocalDateTime interviewTime;

    private String location;

    /** 线上 / 线下 */
    private String interviewType;

    private String description;

    private String contact;

    /** 0=待确认 1=已接受 2=已拒绝 3=已完成 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
