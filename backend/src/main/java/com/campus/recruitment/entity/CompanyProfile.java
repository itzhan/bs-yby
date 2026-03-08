package com.campus.recruitment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("company_profile")
public class CompanyProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String companyName;

    private String logo;

    /** 互联网/金融/教育/制造/医疗等 */
    private String industry;

    /** 0-50人 / 50-200人 / 200-500人 / 500-1000人 / 1000人以上 */
    private String scale;

    private String address;

    private String city;

    private String description;

    private String website;

    private String contactPerson;

    private String contactPhone;

    private String contactEmail;

    /** 营业执照图片URL */
    private String licenseUrl;

    /** 0=待审核 1=已通过 2=已拒绝 */
    private Integer auditStatus;

    private String auditRemark;

    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
