package com.campus.recruitment.controller;

import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.StudentProfileDTO;
import com.campus.recruitment.entity.StudentProfile;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 学生档案控制器
 */
@RestController
@RequestMapping("/api/student-profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    /** 获取当前登录学生的档案 */
    @GetMapping("/current")
    public Result<StudentProfile> getCurrentProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(studentProfileService.getByUserId(userId));
    }

    /** 根据userId获取学生档案（企业查看投递学生信息） */
    @GetMapping("/{userId}")
    public Result<StudentProfile> getByUserId(@PathVariable Long userId) {
        return Result.success(studentProfileService.getByUserId(userId));
    }

    /** 新增或更新当前学生档案 */
    @PutMapping
    public Result<Void> saveOrUpdate(@Valid @RequestBody StudentProfileDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        studentProfileService.saveOrUpdate(userId, dto);
        return Result.success();
    }
}
