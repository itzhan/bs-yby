package com.campus.recruitment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AnnouncementDTO {

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    /** system / recruitment / activity */
    private String type;

    /** 0=草稿 1=已发布 */
    private Integer status;
}
