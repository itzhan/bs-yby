package com.campus.recruitment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AuditDTO {

    /** 审核状态：1=通过 2=拒绝 */
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    /** 审核备注 */
    private String auditRemark;
}
