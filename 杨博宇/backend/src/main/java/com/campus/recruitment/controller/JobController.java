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
import java.util.List;

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
            status = 2;
        }
        return Result.success(jobService.listJobs(page, size, keyword, city, category, jobType, status, companyId));
    }

    /** 职位详情（同时记录浏览行为） */
    @GetMapping("/{id}")
    public Result<JobVO> getJobDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            String role = SecurityUtils.getCurrentRole();
            if ("STUDENT".equals(role)) {
                jobService.recordBehavior(userId, id, 1);
            }
        }
        return Result.success(jobService.getJobDetail(id));
    }

    /** 人岗匹配（学生） */
    @GetMapping("/match")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<JobVO>> matchJobs() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(jobService.matchJobs(userId));
    }

    /** 个性化推荐岗位（登录用户） */
    @GetMapping("/recommended")
    public Result<List<JobVO>> getRecommendedJobs(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            // 未登录返回最新发布岗位
            return Result.success(jobService.listJobs(1, limit, null, null, null, null, 2, null).getRecords());
        }
        return Result.success(jobService.getRecommendedJobs(userId, limit));
    }

    /** 记录行为（前端主动上报） */
    @PostMapping("/{id}/behavior")
    public Result<Void> recordBehavior(@PathVariable Long id,
                                       @RequestParam(defaultValue = "1") Integer type) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            jobService.recordBehavior(userId, id, type);
        }
        return Result.success();
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
