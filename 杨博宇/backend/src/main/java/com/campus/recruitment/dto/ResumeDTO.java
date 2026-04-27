package com.campus.recruitment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ResumeDTO {

    @NotBlank(message = "简历标题不能为空")
    private String title;

    /** 教育经历（JSON） */
    private String educationExperience;

    /** 工作/实习经历（JSON） */
    private String workExperience;

    /** 项目经历（JSON） */
    private String projectExperience;

    private String skills;

    private String awards;

    private String selfEvaluation;

    /** 是否默认简历 */
    private Boolean isDefault;

    /** 0=私密 1=公开 */
    private Integer status;
}
