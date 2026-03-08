package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.entity.OperationLog;
import com.campus.recruitment.mapper.OperationLogMapper;
import com.campus.recruitment.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    public PageResult<OperationLog> listLogs(int page, int size, String keyword) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(OperationLog::getUsername, keyword)
                    .or().like(OperationLog::getOperation, keyword)
                    .or().like(OperationLog::getMethod, keyword));
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);

        Page<OperationLog> pageParam = new Page<>(page, size);
        Page<OperationLog> pageResult = operationLogMapper.selectPage(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public void saveLog(OperationLog log) {
        operationLogMapper.insert(log);
    }
}
