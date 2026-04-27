package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.AuditDTO;
import com.campus.recruitment.dto.JobFairDTO;
import com.campus.recruitment.dto.JobFairVO;
import com.campus.recruitment.entity.JobFairBooking;

public interface JobFairService {

    /**
     * 分页查询招聘会列表
     */
    PageResult<JobFairVO> listJobFairs(int page, int size, Integer status, Long companyId);

    /**
     * 获取招聘会详情
     */
    JobFairVO getJobFairDetail(Long id);

    /**
     * 创建招聘会
     */
    void createJobFair(Long companyId, JobFairDTO dto);

    /**
     * 更新招聘会
     */
    void updateJobFair(Long id, JobFairDTO dto);

    /**
     * 审核招聘会
     */
    void auditJobFair(Long id, AuditDTO dto);

    /**
     * 学生预约招聘会
     */
    void bookJobFair(Long jobFairId, Long studentId);

    /**
     * 学生取消预约
     */
    void cancelBooking(Long jobFairId, Long studentId);

    /**
     * 查询招聘会的预约列表
     */
    PageResult<JobFairBooking> listBookings(Long jobFairId, int page, int size);

    /**
     * 查询学生已预约的宣讲会ID列表
     */
    java.util.List<Long> getBookedFairIds(Long studentId);
}
