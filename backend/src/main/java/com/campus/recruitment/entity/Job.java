package com.campus.recruitment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("job")
public class Job {

    @TableId(type = IdType.AUTO)
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

    /** 学历要求: 不限/专科/本科/硕士/博士 */
    private String educationReq;

    private String majorReq;

    private Integer headcount;

    /** 岗位类别: 技术/产品/运营/市场/人事/财务等 */
    private String category;

    /** 0=草稿 1=待审核 2=已发布 3=已关闭 4=已拒绝 */
    private Integer status;

    private String auditRemark;

    private LocalDate deadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private LocalDateTime deletedAt;
}
