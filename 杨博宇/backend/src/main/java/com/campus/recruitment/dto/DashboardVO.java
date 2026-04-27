package com.campus.recruitment.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {

    /** 学生总数 */
    private Long totalStudents;

    /** 企业总数 */
    private Long totalCompanies;

    /** 职位总数 */
    private Long totalJobs;

    /** 申请总数 */
    private Long totalApplications;

    /** 最近发布的职位 */
    private List<JobVO> recentJobs;

    /** 申请状态统计，key=状态值, value=数量 */
    private Map<String, Long> applicationStatusStats;

    /** 行业分布统计，key=行业名, value=数量 */
    private Map<String, Long> industryStats;

    /** 月度统计（按月份聚合的数据列表） */
    private List<Map<String, Object>> monthlyStats;
}
