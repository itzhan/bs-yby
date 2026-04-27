package com.campus.recruitment.controller;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.entity.Message;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** 消息列表（当前用户收到的消息） */
    @GetMapping
    public Result<PageResult<Message>> listMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(messageService.listMessages(userId, page, size, type, isRead));
    }

    /** 标记单条消息已读 */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        messageService.markAllAsRead(userId);
        return Result.success();
    }

    /** 获取未读消息数 */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(messageService.getUnreadCount(userId));
    }
}
