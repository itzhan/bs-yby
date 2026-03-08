package com.campus.recruitment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job_fair_booking")
public class JobFairBooking {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobFairId;

    private Long studentId;

    /** 0=已预约 1=已取消 2=已签到 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
