package com.campus.recruitment.controller;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.ApplicationDTO;
import com.campus.recruitment.dto.ApplicationVO;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 求职申请控制器
 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /** 学生投递简历 */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> apply(@Valid @RequestBody ApplicationDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        applicationService.apply(userId, dto);
        return Result.success();
    }

    /** 学生查看自己的申请列表 */
    @GetMapping("/my")
    public Result<PageResult<ApplicationVO>> listByStudent(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(applicationService.listByStudent(userId, page, size, status));
    }

    /** 企业查看某个职位的申请列表 */
    @GetMapping("/job/{jobId}")
    public Result<PageResult<ApplicationVO>> listByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return Result.success(applicationService.listByJob(jobId, page, size, status));
    }

    /** 管理员查看所有申请 */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<ApplicationVO>> listAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(applicationService.listAll(page, size, status, keyword));
    }

    /** 更新申请状态（企业/管理员） */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Integer status = body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : null;
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        applicationService.updateStatus(id, status, remark);
        return Result.success();
    }

    /** 学生撤回申请 */
    @PutMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> withdraw(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        applicationService.withdraw(id, userId);
        return Result.success();
    }
}
