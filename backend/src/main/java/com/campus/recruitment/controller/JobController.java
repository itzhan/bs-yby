package com.campus.recruitment.controller;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.AuditDTO;
import com.campus.recruitment.dto.JobDTO;
import com.campus.recruitment.dto.JobVO;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.CompanyProfileService;
import com.campus.recruitment.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 职位管理控制器
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final CompanyProfileService companyProfileService;

    /** 职位列表（公开：已发布status=2；管理员：全部；企业：自己的职位） */
    @GetMapping
    public Result<PageResult<JobVO>> listJobs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Integer status) {
        String role = SecurityUtils.getCurrentRole();
        Long companyId = null;
        if ("COMPANY".equals(role)) {
            companyId = companyProfileService.getByUserId(SecurityUtils.getCurrentUserId()).getId();
        } else if (!"ADMIN".equals(role)) {
            // 非管理员且非企业用户，只能看已发布的职位
            status = 2;
        }
        return Result.success(jobService.listJobs(page, size, keyword, city, category, jobType, status, companyId));
    }

    /** 职位详情 */
    @GetMapping("/{id}")
    public Result<JobVO> getJobDetail(@PathVariable Long id) {
        return Result.success(jobService.getJobDetail(id));
    }

    /** 发布职位（企业） */
    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> createJob(@Valid @RequestBody JobDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long companyId = companyProfileService.getByUserId(userId).getId();
        jobService.createJob(companyId, dto);
        return Result.success();
    }

    /** 更新职位（企业） */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> updateJob(@PathVariable Long id, @Valid @RequestBody JobDTO dto) {
        jobService.updateJob(id, dto);
        return Result.success();
    }

    /** 关闭职位（企业） */
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> closeJob(@PathVariable Long id) {
        jobService.closeJob(id);
        return Result.success();
    }

    /** 删除职位 */
    @DeleteMapping("/{id}")
    public Result<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return Result.success();
    }

    /** 审核职位（管理员） */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditJob(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        jobService.auditJob(id, dto);
        return Result.success();
    }
}
