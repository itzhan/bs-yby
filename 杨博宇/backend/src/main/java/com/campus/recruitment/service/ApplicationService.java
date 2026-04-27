package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.ApplicationDTO;
import com.campus.recruitment.dto.ApplicationVO;

public interface ApplicationService {

    /**
     * 投递申请
     */
    void apply(Long studentId, ApplicationDTO dto);

    /**
     * 学生查询自己的申请列表
     */
    PageResult<ApplicationVO> listByStudent(Long studentId, int page, int size, Integer status);

    /**
     * 根据职位查询申请列表
     */
    PageResult<ApplicationVO> listByJob(Long jobId, int page, int size, Integer status);

    /**
     * 管理员查询所有申请
     */
    PageResult<ApplicationVO> listAll(int page, int size, Integer status, String keyword);

    /**
     * 更新申请状态
     */
    void updateStatus(Long id, Integer status, String remark);

    /**
     * 学生撤回申请
     */
    void withdraw(Long id, Long studentId);
}
