package com.campus.recruitment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobFairVO {

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ---- 关联冗余字段 ----

    private String companyName;
}
