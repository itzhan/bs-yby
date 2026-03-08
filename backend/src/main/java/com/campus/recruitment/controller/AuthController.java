package com.campus.recruitment.controller;

import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.LoginDTO;
import com.campus.recruitment.dto.LoginVO;
import com.campus.recruitment.dto.RegisterDTO;
import com.campus.recruitment.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器 — 登录 / 注册
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

    /** 注册 */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        return authService.register(dto);
    }
}
