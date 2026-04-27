package com.campus.recruitment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class OfferDTO {

    @NotNull(message = "申请ID不能为空")
    private Long applicationId;

    private Integer salary;

    private LocalDate startDate;

    private String description;

    private LocalDate deadline;
}
