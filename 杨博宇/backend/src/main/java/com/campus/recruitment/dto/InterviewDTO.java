package com.campus.recruitment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class InterviewDTO {

    @NotNull(message = "申请ID不能为空")
    private Long applicationId;

    @NotNull(message = "面试时间不能为空")
    private LocalDateTime interviewTime;

    private String location;

    /** 线上 / 线下 */
    private String interviewType;

    private String description;

    private String contact;
}
