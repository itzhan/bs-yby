package com.campus.recruitment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class JobDTO {

    @NotBlank(message = "职位标题不能为空")
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

    private LocalDate deadline;
}
