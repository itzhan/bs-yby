package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.entity.OperationLog;

public interface OperationLogService {

    /**
     * 分页查询操作日志
     */
    PageResult<OperationLog> listLogs(int page, int size, String keyword);

    /**
     * 保存操作日志
     */
    void saveLog(OperationLog log);
}
