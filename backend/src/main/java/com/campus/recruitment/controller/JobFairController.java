package com.campus.recruitment.controller;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.AuditDTO;
import com.campus.recruitment.dto.JobFairDTO;
import com.campus.recruitment.dto.JobFairVO;
import com.campus.recruitment.entity.JobFairBooking;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.CompanyProfileService;
import com.campus.recruitment.service.JobFairService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 招聘会控制器
 */
@RestController
@RequestMapping("/api/job-fairs")
@RequiredArgsConstructor
public class JobFairController {

    private final JobFairService jobFairService;
    private final CompanyProfileService companyProfileService;

    /** 招聘会列表（公开：已通过status=1；管理员：全部；企业：自己发布的） */
    @GetMapping
    public Result<PageResult<JobFairVO>> listJobFairs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        String role = SecurityUtils.getCurrentRole();
        Long companyId = null;
        if ("COMPANY".equals(role)) {
            companyId = companyProfileService.getByUserId(SecurityUtils.getCurrentUserId()).getId();
        } else if (!"ADMIN".equals(role)) {
            // 非管理员且非企业用户，只能看已通过的招聘会
            status = 1;
        }
        return Result.success(jobFairService.listJobFairs(page, size, status, companyId));
    }

    /** 招聘会详情 */
    @GetMapping("/{id}")
    public Result<JobFairVO> getJobFairDetail(@PathVariable Long id) {
        return Result.success(jobFairService.getJobFairDetail(id));
    }

    /** 创建招聘会（企业） */
    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> createJobFair(@Valid @RequestBody JobFairDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long companyId = companyProfileService.getByUserId(userId).getId();
        jobFairService.createJobFair(companyId, dto);
        return Result.success();
    }

    /** 更新招聘会（企业） */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> updateJobFair(@PathVariable Long id, @Valid @RequestBody JobFairDTO dto) {
        jobFairService.updateJobFair(id, dto);
        return Result.success();
    }

    /** 审核招聘会（管理员） */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditJobFair(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        jobFairService.auditJobFair(id, dto);
        return Result.success();
    }

    /** 学生预约招聘会 */
    @PostMapping("/{id}/book")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> bookJobFair(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        jobFairService.bookJobFair(id, userId);
        return Result.success();
    }

    /** 学生取消预约 */
    @DeleteMapping("/{id}/book")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> cancelBooking(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        jobFairService.cancelBooking(id, userId);
        return Result.success();
    }

    /** 学生查询自己已预约的宣讲会ID列表 */
    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<java.util.List<Long>> getMyBookedFairIds() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(jobFairService.getBookedFairIds(userId));
    }

    /** 查看招聘会的预约列表（企业/管理员） */
    @GetMapping("/{id}/bookings")
    public Result<PageResult<JobFairBooking>> listBookings(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(jobFairService.listBookings(id, page, size));
    }
}
