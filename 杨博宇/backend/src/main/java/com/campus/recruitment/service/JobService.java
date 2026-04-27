package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.AuditDTO;
import com.campus.recruitment.dto.JobDTO;
import com.campus.recruitment.dto.JobVO;

import java.util.List;

public interface JobService {

    PageResult<JobVO> listJobs(int page, int size, String keyword, String city,
                               String category, String jobType, Integer status, Long companyId);

    JobVO getJobDetail(Long id);

    void createJob(Long companyId, JobDTO dto);

    void updateJob(Long id, JobDTO dto);

    void deleteJob(Long id);

    void closeJob(Long id);

    void auditJob(Long id, AuditDTO dto);

    /** 人岗匹配：根据当前学生 profile 计算所有已发布岗位的匹配分并排序 */
    List<JobVO> matchJobs(Long userId);

    /** 记录用户行为（1=浏览 2=投递） */
    void recordBehavior(Long userId, Long jobId, Integer behaviorType);

    /** 协同过滤推荐：返回个性化推荐岗位列表 */
    List<JobVO> getRecommendedJobs(Long userId, int limit);
}
