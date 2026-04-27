package com.campus.recruitment.dto;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private UserVO user;
}
