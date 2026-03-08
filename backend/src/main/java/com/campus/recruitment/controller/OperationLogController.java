package com.campus.recruitment.controller;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.common.Result;
import com.campus.recruitment.entity.OperationLog;
import com.campus.recruitment.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /** 查看操作日志（管理员） */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<OperationLog>> listLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(operationLogService.listLogs(page, size, keyword));
    }
}
