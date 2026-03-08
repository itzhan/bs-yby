package com.campus.recruitment.service;

import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.LoginDTO;
import com.campus.recruitment.dto.LoginVO;
import com.campus.recruitment.dto.RegisterDTO;

public interface AuthService {

    /**
     * 用户登录
     */
    Result<LoginVO> login(LoginDTO dto);

    /**
     * 用户注册
     */
    Result<Void> register(RegisterDTO dto);
}
