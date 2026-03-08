package com.campus.recruitment.controller;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.dto.AnnouncementDTO;
import com.campus.recruitment.entity.Announcement;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 公告管理控制器
 */
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /** 公告列表（公开：status=1已发布；管理员：全部） */
    @GetMapping
    public Result<PageResult<Announcement>> listAnnouncements(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        String role = SecurityUtils.getCurrentRole();
        // 非管理员只能查看已发布的公告
        if (!"ADMIN".equals(role)) {
            status = 1;
        }
        return Result.success(announcementService.listAnnouncements(page, size, type, status));
    }

    /** 公告详情 */
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementById(id));
    }

    /** 创建公告（管理员） */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createAnnouncement(@Valid @RequestBody AnnouncementDTO dto) {
        Long publisherId = SecurityUtils.getCurrentUserId();
        announcementService.createAnnouncement(publisherId, dto);
        return Result.success();
    }

    /** 更新公告（管理员） */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateAnnouncement(@PathVariable Long id, @Valid @RequestBody AnnouncementDTO dto) {
        announcementService.updateAnnouncement(id, dto);
        return Result.success();
    }

    /** 删除公告（管理员） */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }
}
