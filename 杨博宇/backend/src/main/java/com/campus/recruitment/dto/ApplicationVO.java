package com.campus.recruitment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationVO {

    private Long id;

    private Long studentId;

    private Long jobId;

    private Long resumeId;

    /** 0=待查看 1=已查看 2=面试中 3=已录用 4=已拒绝 5=已撤回 */
    private Integer status;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ---- 关联冗余字段 ----

    private String jobTitle;

    private String companyName;

    private String studentName;

    private String resumeTitle;
}
