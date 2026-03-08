package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.AuditDTO;
import com.campus.recruitment.dto.JobDTO;
import com.campus.recruitment.dto.JobVO;

public interface JobService {

    /**
     * 分页查询职位列表
     */
    PageResult<JobVO> listJobs(int page, int size, String keyword, String city,
                               String category, String jobType, Integer status, Long companyId);

    /**
     * 获取职位详情
     */
    JobVO getJobDetail(Long id);

    /**
     * 创建职位
     */
    void createJob(Long companyId, JobDTO dto);

    /**
     * 更新职位
     */
    void updateJob(Long id, JobDTO dto);

    /**
     * 删除职位
     */
    void deleteJob(Long id);

    /**
     * 关闭职位
     */
    void closeJob(Long id);

    /**
     * 审核职位
     */
    void auditJob(Long id, AuditDTO dto);
}
