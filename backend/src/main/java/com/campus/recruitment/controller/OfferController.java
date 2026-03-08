package com.campus.recruitment.controller;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.OfferDTO;
import com.campus.recruitment.entity.Offer;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.CompanyProfileService;
import com.campus.recruitment.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * Offer 管理控制器
 */
@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;
    private final CompanyProfileService companyProfileService;

    /** 发放 Offer（企业） */
    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> createOffer(@Valid @RequestBody OfferDTO dto) {
        offerService.createOffer(dto);
        return Result.success();
    }

    /** 我的 Offer 列表（学生看收到的，企业看发出的） */
    @GetMapping("/my")
    public Result<PageResult<Offer>> listMy(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String role = SecurityUtils.getCurrentRole();
        Long userId = SecurityUtils.getCurrentUserId();
        if ("STUDENT".equals(role)) {
            return Result.success(offerService.listByStudent(userId, page, size));
        } else {
            Long companyId = companyProfileService.getByUserId(userId).getId();
            return Result.success(offerService.listByCompany(companyId, page, size));
        }
    }

    /** 学生回复 Offer（接受/拒绝） */
    @PutMapping("/{id}/respond")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> respond(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        Long userId = SecurityUtils.getCurrentUserId();
        offerService.respond(id, status, userId);
        return Result.success();
    }
}
