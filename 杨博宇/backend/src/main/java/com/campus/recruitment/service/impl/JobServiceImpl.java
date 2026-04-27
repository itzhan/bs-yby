package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.AuditDTO;
import com.campus.recruitment.dto.JobDTO;
import com.campus.recruitment.dto.JobVO;
import com.campus.recruitment.entity.CompanyProfile;
import com.campus.recruitment.entity.Job;
import com.campus.recruitment.entity.StudentProfile;
import com.campus.recruitment.entity.UserBehavior;
import com.campus.recruitment.mapper.CompanyProfileMapper;
import com.campus.recruitment.mapper.JobMapper;
import com.campus.recruitment.mapper.StudentProfileMapper;
import com.campus.recruitment.mapper.UserBehaviorMapper;
import com.campus.recruitment.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;
    private final CompanyProfileMapper companyProfileMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final UserBehaviorMapper userBehaviorMapper;

    @Override
    public PageResult<JobVO> listJobs(int page, int size, String keyword, String city,
                                      String category, String jobType, Integer status, Long companyId) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Job::getStatus, status);
        }
        if (companyId != null) {
            wrapper.eq(Job::getCompanyId, companyId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Job::getTitle, keyword)
                    .or().like(Job::getDescription, keyword));
        }
        if (StringUtils.hasText(city)) {
            wrapper.eq(Job::getCity, city);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Job::getCategory, category);
        }
        if (StringUtils.hasText(jobType)) {
            wrapper.eq(Job::getJobType, jobType);
        }
        wrapper.orderByDesc(Job::getCreatedAt);

        Page<Job> pageParam = new Page<>(page, size);
        Page<Job> pageResult = jobMapper.selectPage(pageParam, wrapper);

        List<Job> jobs = pageResult.getRecords();
        Map<Long, CompanyProfile> companyMap = batchLoadCompanies(
                jobs.stream().map(Job::getCompanyId).collect(Collectors.toSet()));

        List<JobVO> voList = jobs.stream().map(job -> {
            JobVO vo = toJobVO(job);
            enrichWithCompany(vo, job.getCompanyId(), companyMap);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public JobVO getJobDetail(Long id) {
        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException("职位不存在");
        }
        JobVO vo = toJobVO(job);
        CompanyProfile cp = companyProfileMapper.selectById(job.getCompanyId());
        if (cp != null) {
            vo.setCompanyName(cp.getCompanyName());
            vo.setCompanyLogo(cp.getLogo());
            vo.setIndustry(cp.getIndustry());
        }
        return vo;
    }

    @Override
    public void createJob(Long companyId, JobDTO dto) {
        Job job = new Job();
        BeanUtils.copyProperties(dto, job);
        job.setCompanyId(companyId);
        job.setStatus(1); // 待审核
        jobMapper.insert(job);
    }

    @Override
    public void updateJob(Long id, JobDTO dto) {
        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException("职位不存在");
        }
        BeanUtils.copyProperties(dto, job);
        jobMapper.updateById(job);
    }

    @Override
    public void closeJob(Long id) {
        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException("职位不存在");
        }
        job.setStatus(3);
        jobMapper.updateById(job);
    }

    @Override
    public void deleteJob(Long id) {
        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException("职位不存在");
        }
        jobMapper.deleteById(id);
    }

    @Override
    public void auditJob(Long id, AuditDTO dto) {
        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException("职位不存在");
        }
        if (dto.getAuditStatus() == 1) {
            job.setStatus(2);
        } else if (dto.getAuditStatus() == 2) {
            job.setStatus(4);
        }
        job.setAuditRemark(dto.getAuditRemark());
        jobMapper.updateById(job);
    }

    @Override
    public List<JobVO> matchJobs(Long userId) {
        StudentProfile profile = studentProfileMapper.selectOne(
                new LambdaQueryWrapper<StudentProfile>().eq(StudentProfile::getUserId, userId));
        if (profile == null) {
            // 没有 profile 时返回普通列表
            List<Job> jobs = jobMapper.selectList(
                    new LambdaQueryWrapper<Job>().eq(Job::getStatus, 2).last("LIMIT 50"));
            Map<Long, CompanyProfile> cm = batchLoadCompanies(
                    jobs.stream().map(Job::getCompanyId).collect(Collectors.toSet()));
            return jobs.stream().map(job -> {
                JobVO vo = toJobVO(job);
                enrichWithCompany(vo, job.getCompanyId(), cm);
                vo.setMatchScore(0);
                return vo;
            }).collect(Collectors.toList());
        }

        List<Job> jobs = jobMapper.selectList(
                new LambdaQueryWrapper<Job>().eq(Job::getStatus, 2));
        Map<Long, CompanyProfile> companyMap = batchLoadCompanies(
                jobs.stream().map(Job::getCompanyId).collect(Collectors.toSet()));

        return jobs.stream().map(job -> {
            JobVO vo = toJobVO(job);
            enrichWithCompany(vo, job.getCompanyId(), companyMap);
            vo.setMatchScore(calculateMatchScore(profile, job));
            return vo;
        }).sorted(Comparator.comparingInt(JobVO::getMatchScore).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void recordBehavior(Long userId, Long jobId, Integer behaviorType) {
        // 同一用户同一岗位同类行为只记录一次（VIEW 可重复累加，APPLY 只记一次）
        if (behaviorType == 2) {
            Long cnt = userBehaviorMapper.selectCount(
                    new LambdaQueryWrapper<UserBehavior>()
                            .eq(UserBehavior::getUserId, userId)
                            .eq(UserBehavior::getJobId, jobId)
                            .eq(UserBehavior::getBehaviorType, 2));
            if (cnt > 0) return;
        }
        UserBehavior b = new UserBehavior();
        b.setUserId(userId);
        b.setJobId(jobId);
        b.setBehaviorType(behaviorType);
        userBehaviorMapper.insert(b);
    }

    @Override
    public List<JobVO> getRecommendedJobs(Long userId, int limit) {
        // 1. 获取用户历史行为
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .orderByDesc(UserBehavior::getCreatedAt)
                        .last("LIMIT 50"));

        Set<Long> viewedJobIds = behaviors.stream()
                .map(UserBehavior::getJobId).collect(Collectors.toSet());

        // 2. 从历史浏览岗位中提取偏好 category/city
        Set<Long> allIds = new HashSet<>(viewedJobIds);
        List<Job> viewedJobs = allIds.isEmpty() ? List.of() :
                jobMapper.selectList(new LambdaQueryWrapper<Job>().in(Job::getId, allIds));

        Map<String, Long> categoryFreq = viewedJobs.stream()
                .filter(j -> StringUtils.hasText(j.getCategory()))
                .collect(Collectors.groupingBy(Job::getCategory, Collectors.counting()));
        Map<String, Long> cityFreq = viewedJobs.stream()
                .filter(j -> StringUtils.hasText(j.getCity()))
                .collect(Collectors.groupingBy(Job::getCity, Collectors.counting()));

        // 3. 协同过滤：找与当前用户有共同浏览岗位的其他用户，取他们投递的岗位
        Set<Long> cfJobIds = new HashSet<>();
        if (!viewedJobIds.isEmpty()) {
            List<UserBehavior> similarUsersApply = userBehaviorMapper.selectList(
                    new LambdaQueryWrapper<UserBehavior>()
                            .in(UserBehavior::getJobId, viewedJobIds)
                            .ne(UserBehavior::getUserId, userId)
                            .eq(UserBehavior::getBehaviorType, 2)
                            .last("LIMIT 200"));
            Set<Long> similarUserIds = similarUsersApply.stream()
                    .map(UserBehavior::getUserId).collect(Collectors.toSet());
            if (!similarUserIds.isEmpty()) {
                List<UserBehavior> cfBehaviors = userBehaviorMapper.selectList(
                        new LambdaQueryWrapper<UserBehavior>()
                                .in(UserBehavior::getUserId, similarUserIds)
                                .eq(UserBehavior::getBehaviorType, 2)
                                .last("LIMIT 500"));
                cfBehaviors.stream()
                        .map(UserBehavior::getJobId)
                        .filter(id -> !viewedJobIds.contains(id))
                        .forEach(cfJobIds::add);
            }
        }

        // 4. 获取候选岗位（CF推荐 + 内容偏好匹配）
        List<Job> candidates = new ArrayList<>();
        if (!cfJobIds.isEmpty()) {
            candidates.addAll(jobMapper.selectList(
                    new LambdaQueryWrapper<Job>()
                            .eq(Job::getStatus, 2)
                            .in(Job::getId, cfJobIds)));
        }

        // 内容补充：按偏好 category/city 补齐
        String topCategory = categoryFreq.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        String topCity = cityFreq.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

        if (candidates.size() < limit) {
            LambdaQueryWrapper<Job> q = new LambdaQueryWrapper<Job>().eq(Job::getStatus, 2);
            if (StringUtils.hasText(topCategory)) q.eq(Job::getCategory, topCategory);
            if (!viewedJobIds.isEmpty()) q.notIn(Job::getId, viewedJobIds);
            q.last("LIMIT " + limit);
            List<Job> contentJobs = jobMapper.selectList(q);
            Set<Long> existing = candidates.stream().map(Job::getId).collect(Collectors.toSet());
            contentJobs.stream().filter(j -> !existing.contains(j.getId())).forEach(candidates::add);
        }

        // 5. 若仍不够，用学生 profile 的偏好城市/专业再补
        if (candidates.size() < limit) {
            StudentProfile profile = studentProfileMapper.selectOne(
                    new LambdaQueryWrapper<StudentProfile>().eq(StudentProfile::getUserId, userId));
            if (profile != null) {
                LambdaQueryWrapper<Job> q2 = new LambdaQueryWrapper<Job>().eq(Job::getStatus, 2);
                if (StringUtils.hasText(profile.getExpectedCity()))
                    q2.eq(Job::getCity, profile.getExpectedCity());
                if (!viewedJobIds.isEmpty()) q2.notIn(Job::getId, viewedJobIds);
                q2.last("LIMIT " + limit);
                Set<Long> existing2 = candidates.stream().map(Job::getId).collect(Collectors.toSet());
                jobMapper.selectList(q2).stream()
                        .filter(j -> !existing2.contains(j.getId()))
                        .forEach(candidates::add);
            }
        }

        // 6. 最终兜底：随机取最新发布的岗位
        if (candidates.isEmpty()) {
            candidates = jobMapper.selectList(
                    new LambdaQueryWrapper<Job>()
                            .eq(Job::getStatus, 2)
                            .orderByDesc(Job::getCreatedAt)
                            .last("LIMIT " + limit));
        }

        Map<Long, CompanyProfile> companyMap = batchLoadCompanies(
                candidates.stream().map(Job::getCompanyId).collect(Collectors.toSet()));

        return candidates.stream().limit(limit).map(job -> {
            JobVO vo = toJobVO(job);
            enrichWithCompany(vo, job.getCompanyId(), companyMap);
            return vo;
        }).collect(Collectors.toList());
    }

    // ---- helpers ----

    private Map<Long, CompanyProfile> batchLoadCompanies(Set<Long> companyIds) {
        if (companyIds.isEmpty()) return Map.of();
        List<CompanyProfile> companies = companyProfileMapper.selectList(
                new LambdaQueryWrapper<CompanyProfile>().in(CompanyProfile::getId, companyIds));
        return companies.stream()
                .collect(Collectors.toMap(CompanyProfile::getId, Function.identity(), (a, b) -> a));
    }

    private void enrichWithCompany(JobVO vo, Long companyId, Map<Long, CompanyProfile> companyMap) {
        CompanyProfile cp = companyMap.get(companyId);
        if (cp != null) {
            vo.setCompanyName(cp.getCompanyName());
            vo.setCompanyLogo(cp.getLogo());
            vo.setIndustry(cp.getIndustry());
        }
    }

    private int calculateMatchScore(StudentProfile profile, Job job) {
        int score = 0;

        // 城市匹配 30分
        if (StringUtils.hasText(profile.getExpectedCity()) && StringUtils.hasText(job.getCity())) {
            if (profile.getExpectedCity().contains(job.getCity())
                    || job.getCity().contains(profile.getExpectedCity())) {
                score += 30;
            }
        } else if (!StringUtils.hasText(job.getCity())) {
            score += 15;
        }

        // 学历匹配 25分
        if (!StringUtils.hasText(job.getEducationReq()) || "不限".equals(job.getEducationReq())) {
            score += 25;
        } else if (StringUtils.hasText(profile.getEducation())) {
            if (educationLevel(profile.getEducation()) >= educationLevel(job.getEducationReq())) {
                score += 25;
            }
        }

        // 专业匹配 25分
        if (!StringUtils.hasText(job.getMajorReq()) || "不限".equals(job.getMajorReq())) {
            score += 25;
        } else if (StringUtils.hasText(profile.getMajor())) {
            String major = profile.getMajor().toLowerCase();
            String majorReq = job.getMajorReq().toLowerCase();
            if (major.contains(majorReq) || majorReq.contains(major)) {
                score += 25;
            } else {
                // 部分匹配给10分
                score += 10;
            }
        }

        // 薪资匹配 20分
        if (job.getSalaryMin() == null && job.getSalaryMax() == null) {
            score += 10;
        } else if (profile.getExpectedSalaryMin() != null) {
            int jobMax = job.getSalaryMax() != null ? job.getSalaryMax() : Integer.MAX_VALUE;
            int jobMin = job.getSalaryMin() != null ? job.getSalaryMin() : 0;
            int stuMin = profile.getExpectedSalaryMin();
            int stuMax = profile.getExpectedSalaryMax() != null ? profile.getExpectedSalaryMax() : Integer.MAX_VALUE;
            if (stuMin <= jobMax && stuMax >= jobMin) {
                score += 20;
            }
        } else {
            score += 10;
        }

        return score;
    }

    private int educationLevel(String education) {
        if (education == null) return 0;
        switch (education) {
            case "博士": return 5;
            case "硕士": return 4;
            case "本科": return 3;
            case "大专": case "专科": return 2;
            default: return 1;
        }
    }

    private JobVO toJobVO(Job job) {
        JobVO vo = new JobVO();
        vo.setId(job.getId());
        vo.setCompanyId(job.getCompanyId());
        vo.setTitle(job.getTitle());
        vo.setDescription(job.getDescription());
        vo.setRequirements(job.getRequirements());
        vo.setJobType(job.getJobType());
        vo.setSalaryMin(job.getSalaryMin());
        vo.setSalaryMax(job.getSalaryMax());
        vo.setCity(job.getCity());
        vo.setAddress(job.getAddress());
        vo.setEducationReq(job.getEducationReq());
        vo.setMajorReq(job.getMajorReq());
        vo.setHeadcount(job.getHeadcount());
        vo.setCategory(job.getCategory());
        vo.setStatus(job.getStatus());
        vo.setAuditRemark(job.getAuditRemark());
        vo.setDeadline(job.getDeadline());
        vo.setCreatedAt(job.getCreatedAt());
        vo.setUpdatedAt(job.getUpdatedAt());
        return vo;
    }
}
