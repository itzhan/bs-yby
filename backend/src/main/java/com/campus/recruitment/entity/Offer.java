package com.campus.recruitment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("offer")
public class Offer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private Long companyId;

    private Long studentId;

    private Long jobId;

    private Integer salary;

    private LocalDate startDate;

    private String description;

    private LocalDate deadline;

    /** 0=待回复 1=已接受 2=已拒绝 3=已过期 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
