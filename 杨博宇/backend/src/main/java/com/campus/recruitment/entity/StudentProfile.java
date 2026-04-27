package com.campus.recruitment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_profile")
public class StudentProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    /** 男 / 女 */
    private String gender;

    private String birthDate;

    private String school;

    private String major;

    /** 专科 / 本科 / 硕士 / 博士 */
    private String education;

    private Integer graduationYear;

    private String skills;

    private String jobIntention;

    private Integer expectedSalaryMin;

    private Integer expectedSalaryMax;

    private String expectedCity;

    private String selfIntroduction;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
