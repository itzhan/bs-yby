package com.campus.recruitment.controller;

import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.DashboardVO;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;

    /** 管理员全局看板（公开可访问，首页统计也使用此接口） */
    @GetMapping
    public Result<DashboardVO> getDashboard() {
        return Result.success(userService.getDashboard());
    }

    /** 企业专属工作台数据 */
    @GetMapping("/company")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<DashboardVO> getCompanyDashboard() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getCompanyDashboard(userId));
    }
}
