package com.campus.recruitment.controller;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.AuditDTO;
import com.campus.recruitment.dto.CompanyProfileDTO;
import com.campus.recruitment.entity.CompanyProfile;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.CompanyProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 企业信息控制器
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyProfileController {

    private final CompanyProfileService companyProfileService;

    /** 企业列表（公开：已审核通过auditStatus=1；管理员：所有状态） */
    @GetMapping
    public Result<PageResult<CompanyProfile>> listCompanies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus) {
        String role = SecurityUtils.getCurrentRole();
        // 非管理员只能看到审核通过的企业
        if (!"ADMIN".equals(role)) {
            auditStatus = 1;
        }
        return Result.success(companyProfileService.listCompanies(page, size, auditStatus, keyword));
    }

    /** 获取当前登录企业的信息 */
    @GetMapping("/current")
    public Result<CompanyProfile> getCurrentProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(companyProfileService.getByUserId(userId));
    }

    /** 根据企业档案ID获取详情 */
    @GetMapping("/{id}")
    public Result<CompanyProfile> getById(@PathVariable Long id) {
        return Result.success(companyProfileService.getById(id));
    }

    /** 新增或更新当前企业信息 */
    @PutMapping
    public Result<Void> saveOrUpdate(@Valid @RequestBody CompanyProfileDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        companyProfileService.saveOrUpdate(userId, dto);
        return Result.success();
    }

    /** 审核企业（管理员） */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        companyProfileService.audit(id, dto);
        return Result.success();
    }
}
