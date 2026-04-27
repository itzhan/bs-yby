package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.InterviewDTO;
import com.campus.recruitment.entity.Interview;

public interface InterviewService {

    /**
     * 创建面试
     */
    void createInterview(InterviewDTO dto);

    /**
     * 学生查询自己的面试列表
     */
    PageResult<Interview> listByStudent(Long studentId, int page, int size);

    /**
     * 企业查询自己的面试列表
     */
    PageResult<Interview> listByCompany(Long companyId, int page, int size);

    /**
     * 更新面试状态
     */
    void updateStatus(Long id, Integer status);
}
