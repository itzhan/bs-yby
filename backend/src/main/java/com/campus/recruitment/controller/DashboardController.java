package com.campus.recruitment.controller;

import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.DashboardVO;
import com.campus.recruitment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 数据看板控制器
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;

    /** 获取看板统计数据 */
    @GetMapping
    public Result<DashboardVO> getDashboard() {
        return Result.success(userService.getDashboard());
    }
}
