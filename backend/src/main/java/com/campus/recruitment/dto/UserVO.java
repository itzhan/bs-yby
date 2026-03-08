package com.campus.recruitment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    /** ADMIN / STUDENT / COMPANY */
    private String role;

    /** 0=禁用 1=正常 2=待审核 */
    private Integer status;

    private LocalDateTime createdAt;
}
