package com.campus.recruitment.dto;

import lombok.Data;

@Data
public class StudentProfileDTO {

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
}
