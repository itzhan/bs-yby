package com.campus.recruitment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class JobFairDTO {

    @NotBlank(message = "招聘会标题不能为空")
    private String title;

    private String description;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer maxCapacity;
}
