package com.campus.recruitment.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    /** 修改密码时传入，不修改则为 null */
    private String password;
}
