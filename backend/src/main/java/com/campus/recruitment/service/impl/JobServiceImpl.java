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
import com.campus.recruitment.mapper.CompanyProfileMapper;
import com.campus.recruitment.mapper.JobMapper;
import com.campus.recruitment.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;
    private final CompanyProfileMapper companyProfileMapper;

    @Override
    public PageResult<JobVO> listJobs(int page, int size, String keyword, String city,
                                      String category, String jobType, Integer status, Long companyId) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();

        // 公开列表只展示已发布的职位
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

        // 批量查询关联的公司信息
        List<Job> jobs = pageResult.getRecords();
        Set<Long> companyIds = jobs.stream().map(Job::getCompanyId).collect(Collectors.toSet());
        Map<Long, CompanyProfile> companyMap = Map.of();
        if (!companyIds.isEmpty()) {
            List<CompanyProfile> companies = companyProfileMapper.selectList(
                    new LambdaQueryWrapper<CompanyProfile>().in(CompanyProfile::getUserId, companyIds));
            companyMap = companies.stream()
                    .collect(Collectors.toMap(CompanyProfile::getUserId, Function.identity(), (a, b) -> a));
        }

        Map<Long, CompanyProfile> finalCompanyMap = companyMap;
        List<JobVO> voList = jobs.stream().map(job -> {
            JobVO vo = toJobVO(job);
            CompanyProfile cp = finalCompanyMap.get(job.getCompanyId());
            if (cp != null) {
                vo.setCompanyName(cp.getCompanyName());
                vo.setCompanyLogo(cp.getLogo());
                vo.setIndustry(cp.getIndustry());
            }
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
        // 关联公司信息
        CompanyProfile cp = companyProfileMapper.selectOne(
                new LambdaQueryWrapper<CompanyProfile>().eq(CompanyProfile::getUserId, job.getCompanyId()));
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
        job.setStatus(3); // 3 = 已关闭
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
        // auditStatus 1=通过 -> job status 2=已发布; 2=拒绝 -> job status 4=已拒绝
        if (dto.getAuditStatus() == 1) {
            job.setStatus(2);
        } else if (dto.getAuditStatus() == 2) {
            job.setStatus(4);
        }
        job.setAuditRemark(dto.getAuditRemark());
        jobMapper.updateById(job);
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
