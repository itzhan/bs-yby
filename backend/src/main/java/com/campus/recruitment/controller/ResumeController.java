package com.campus.recruitment.controller;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.ResumeDTO;
import com.campus.recruitment.entity.Resume;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 简历管理控制器
 */
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /** 简历列表（学生查看自己的简历） */
    @GetMapping
    public Result<PageResult<Resume>> listResumes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(resumeService.listResumes(page, size, userId));
    }

    /** 简历详情 */
    @GetMapping("/{id}")
    public Result<Resume> getResumeById(@PathVariable Long id) {
        return Result.success(resumeService.getResumeById(id));
    }

    /** 创建简历（学生） */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> createResume(@Valid @RequestBody ResumeDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        resumeService.createResume(userId, dto);
        return Result.success();
    }

    /** 更新简历 */
    @PutMapping("/{id}")
    public Result<Void> updateResume(@PathVariable Long id, @Valid @RequestBody ResumeDTO dto) {
        resumeService.updateResume(id, dto);
        return Result.success();
    }

    /** 删除简历 */
    @DeleteMapping("/{id}")
    public Result<Void> deleteResume(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return Result.success();
    }
}
