package com.campus.recruitment.controller;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.InterviewDTO;
import com.campus.recruitment.entity.Interview;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.CompanyProfileService;
import com.campus.recruitment.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 面试管理控制器
 */
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final CompanyProfileService companyProfileService;

    /** 创建面试邀请（企业） */
    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> createInterview(@Valid @RequestBody InterviewDTO dto) {
        interviewService.createInterview(dto);
        return Result.success();
    }

    /** 我的面试列表（学生看自己的面试，企业看自己发出的面试） */
    @GetMapping("/my")
    public Result<PageResult<Interview>> listMy(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String role = SecurityUtils.getCurrentRole();
        Long userId = SecurityUtils.getCurrentUserId();
        if ("STUDENT".equals(role)) {
            return Result.success(interviewService.listByStudent(userId, page, size));
        } else {
            Long companyId = companyProfileService.getByUserId(userId).getId();
            return Result.success(interviewService.listByCompany(companyId, page, size));
        }
    }

    /** 更新面试状态 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        interviewService.updateStatus(id, status);
        return Result.success();
    }
}
