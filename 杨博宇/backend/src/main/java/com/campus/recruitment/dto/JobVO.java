package com.campus.recruitment.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class JobVO {

    private Long id;

    private Long companyId;

    private String title;

    private String description;

    private String requirements;

    /** 全职 / 兼职 / 实习 */
    private String jobType;

    private Integer salaryMin;

    private Integer salaryMax;

    private String city;

    private String address;

    private String educationReq;

    private String majorReq;

    private Integer headcount;

    private String category;

    /** 0=草稿 1=待审核 2=已发布 3=已关闭 4=已拒绝 */
    private Integer status;

    private String auditRemark;

    private LocalDate deadline;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ---- 关联冗余字段 ----

    private String companyName;

    private String companyLogo;

    private String industry;

    /** 人岗匹配分值 0-100，仅匹配接口返回时有值 */
    private Integer matchScore;
}
