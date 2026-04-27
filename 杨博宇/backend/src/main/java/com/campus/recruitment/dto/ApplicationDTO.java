package com.campus.recruitment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApplicationDTO {

    @NotNull(message = "职位ID不能为空")
    private Long jobId;

    /** 简历ID，不传则自动使用默认简历 */
    private Long resumeId;
}
