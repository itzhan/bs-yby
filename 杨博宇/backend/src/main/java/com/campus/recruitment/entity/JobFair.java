package com.campus.recruitment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job_fair")
public class JobFair {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companyId;

    private String title;

    private String description;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer maxCapacity;

    private Integer currentCount;

    /** 0=待审核 1=已通过 2=已拒绝 3=已完成 4=已取消 */
    private Integer status;

    private String auditRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
