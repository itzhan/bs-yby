package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.*;
import com.campus.recruitment.entity.CompanyProfile;
import com.campus.recruitment.entity.StudentProfile;
import com.campus.recruitment.entity.User;
import com.campus.recruitment.mapper.CompanyProfileMapper;
import com.campus.recruitment.mapper.StudentProfileMapper;
import com.campus.recruitment.mapper.UserMapper;
import com.campus.recruitment.security.JwtUtil;
import com.campus.recruitment.security.LoginUser;
import com.campus.recruitment.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final CompanyProfileMapper companyProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public Result<LoginVO> login(LoginDTO dto) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        User user = loginUser.getUser();

        // 生成 token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 构建 UserVO
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setRole(user.getRole());
        userVO.setStatus(user.getStatus());
        userVO.setCreatedAt(user.getCreatedAt());

        // 构建 LoginVO
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(userVO);

        return Result.success("登录成功", loginVO);
    }

    @Override
    @Transactional
    public Result<Void> register(RegisterDTO dto) {
        // 校验用户名唯一
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 校验角色
        String role = dto.getRole();
        if (!"STUDENT".equals(role) && !"COMPANY".equals(role)) {
            throw new BusinessException("注册角色只能为 STUDENT 或 COMPANY");
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(role);
        user.setStatus(1); // 正常状态
        userMapper.insert(user);

        // 根据角色创建空的 Profile
        if ("STUDENT".equals(role)) {
            StudentProfile profile = new StudentProfile();
            profile.setUserId(user.getId());
            studentProfileMapper.insert(profile);
        } else {
            CompanyProfile profile = new CompanyProfile();
            profile.setUserId(user.getId());
            profile.setAuditStatus(0); // 待审核
            companyProfileMapper.insert(profile);
        }

        return Result.success("注册成功", null);
    }
}
