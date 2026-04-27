package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.ApplicationDTO;
import com.campus.recruitment.dto.ApplicationVO;
import com.campus.recruitment.entity.*;
import com.campus.recruitment.mapper.*;
import com.campus.recruitment.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final JobMapper jobMapper;
    private final CompanyProfileMapper companyProfileMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final ResumeMapper resumeMapper;
    private final UserBehaviorMapper userBehaviorMapper;

    @Override
    public void apply(Long studentId, ApplicationDTO dto) {
        // 检查是否已投递该职位
        Long count = applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getStudentId, studentId)
                        .eq(Application::getJobId, dto.getJobId())
                        .ne(Application::getStatus, 5) // 排除已撤回
        );
        if (count > 0) {
            throw new BusinessException("您已投递过该职位，请勿重复投递");
        }

        // 检查职位是否处于已发布状态
        Job job = jobMapper.selectById(dto.getJobId());
        if (job == null) {
            throw new BusinessException("职位不存在");
        }
        if (job.getStatus() != 2) {
            throw new BusinessException("该职位当前不可投递");
        }

        // 处理简历：如果未传 resumeId，自动使用默认简历
        Long resumeId = dto.getResumeId();
        if (resumeId == null) {
            Resume defaultResume = resumeMapper.selectOne(
                    new LambdaQueryWrapper<Resume>()
                            .eq(Resume::getUserId, studentId)
                            .eq(Resume::getIsDefault, true)
                            .last("LIMIT 1"));
            if (defaultResume == null) {
                // 没有默认简历，取第一份
                defaultResume = resumeMapper.selectOne(
                        new LambdaQueryWrapper<Resume>()
                                .eq(Resume::getUserId, studentId)
                                .last("LIMIT 1"));
            }
            if (defaultResume == null) {
                throw new BusinessException("请先创建简历后再投递");
            }
            resumeId = defaultResume.getId();
        }

        Application application = new Application();
        application.setStudentId(studentId);
        application.setJobId(dto.getJobId());
        application.setResumeId(resumeId);
        application.setStatus(0); // 待查看
        applicationMapper.insert(application);

        // 记录投递行为（用于协同过滤推荐）
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(studentId);
        behavior.setJobId(dto.getJobId());
        behavior.setBehaviorType(2); // 2=投递
        userBehaviorMapper.insert(behavior);
    }

    @Override
    public PageResult<ApplicationVO> listByStudent(Long studentId, int page, int size, Integer status) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getStudentId, studentId);
        if (status != null) {
            wrapper.eq(Application::getStatus, status);
        }
        wrapper.orderByDesc(Application::getCreatedAt);
        return queryAndConvert(wrapper, page, size);
    }

    @Override
    public PageResult<ApplicationVO> listByJob(Long jobId, int page, int size, Integer status) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getJobId, jobId);
        if (status != null) {
            wrapper.eq(Application::getStatus, status);
        }
        wrapper.orderByDesc(Application::getCreatedAt);
        return queryAndConvert(wrapper, page, size);
    }

    @Override
    public PageResult<ApplicationVO> listAll(int page, int size, Integer status, String keyword) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Application::getStatus, status);
        }
        // keyword 需要跨表搜索，这里先查所有再在内存过滤（数据量可控）
        wrapper.orderByDesc(Application::getCreatedAt);

        if (StringUtils.hasText(keyword)) {
            // 先通过 keyword 查到匹配的 jobId 列表
            List<Job> matchedJobs = jobMapper.selectList(
                    new LambdaQueryWrapper<Job>().like(Job::getTitle, keyword));
            List<StudentProfile> matchedStudents = studentProfileMapper.selectList(
                    new LambdaQueryWrapper<StudentProfile>().like(StudentProfile::getRealName, keyword));
            Set<Long> jobIds = matchedJobs.stream().map(Job::getId).collect(Collectors.toSet());
            Set<Long> studentUserIds = matchedStudents.stream().map(StudentProfile::getUserId).collect(Collectors.toSet());

            if (jobIds.isEmpty() && studentUserIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0, page, size);
            }
            wrapper.and(w -> {
                if (!jobIds.isEmpty()) {
                    w.in(Application::getJobId, jobIds);
                }
                if (!studentUserIds.isEmpty()) {
                    if (!jobIds.isEmpty()) {
                        w.or();
                    }
                    w.in(Application::getStudentId, studentUserIds);
                }
            });
        }

        return queryAndConvert(wrapper, page, size);
    }

    @Override
    public void updateStatus(Long id, Integer status, String remark) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        application.setStatus(status);
        if (remark != null) {
            application.setRemark(remark);
        }
        applicationMapper.updateById(application);
    }

    @Override
    public void withdraw(Long id, Long studentId) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        if (!application.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作此申请");
        }
        if (application.getStatus() >= 3) {
            throw new BusinessException("当前状态不可撤回");
        }
        application.setStatus(5); // 已撤回
        applicationMapper.updateById(application);
    }

    /**
     * 通用分页查询并转换为 ApplicationVO（关联 jobTitle, companyName, studentName, resumeTitle）
     */
    private PageResult<ApplicationVO> queryAndConvert(LambdaQueryWrapper<Application> wrapper, int page, int size) {
        Page<Application> pageParam = new Page<>(page, size);
        Page<Application> pageResult = applicationMapper.selectPage(pageParam, wrapper);
        List<Application> records = pageResult.getRecords();

        if (records.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0, page, size);
        }

        // 收集关联 ID
        Set<Long> jobIds = records.stream().map(Application::getJobId).collect(Collectors.toSet());
        Set<Long> studentIds = records.stream().map(Application::getStudentId).collect(Collectors.toSet());
        Set<Long> resumeIds = records.stream().map(Application::getResumeId).filter(Objects::nonNull).collect(Collectors.toSet());

        // 批量查询关联数据
        Map<Long, Job> jobMap = jobMapper.selectBatchIds(jobIds).stream()
                .collect(Collectors.toMap(Job::getId, Function.identity()));

        // 通过 jobMap 中的 companyId 查询公司信息
        Set<Long> companyUserIds = jobMap.values().stream().map(Job::getCompanyId).collect(Collectors.toSet());
        Map<Long, CompanyProfile> companyMap = companyUserIds.isEmpty() ? Map.of() :
                companyProfileMapper.selectList(
                        new LambdaQueryWrapper<CompanyProfile>().in(CompanyProfile::getUserId, companyUserIds))
                        .stream().collect(Collectors.toMap(CompanyProfile::getUserId, Function.identity(), (a, b) -> a));

        // studentId 即 userId，通过 userId 查 studentProfile
        Map<Long, StudentProfile> studentMap = studentProfileMapper.selectList(
                new LambdaQueryWrapper<StudentProfile>().in(StudentProfile::getUserId, studentIds))
                .stream().collect(Collectors.toMap(StudentProfile::getUserId, Function.identity(), (a, b) -> a));

        Map<Long, Resume> resumeMap = resumeIds.isEmpty() ? Map.of() :
                resumeMapper.selectBatchIds(resumeIds).stream()
                        .collect(Collectors.toMap(Resume::getId, Function.identity()));

        // 转换
        List<ApplicationVO> voList = records.stream().map(app -> {
            ApplicationVO vo = new ApplicationVO();
            vo.setId(app.getId());
            vo.setStudentId(app.getStudentId());
            vo.setJobId(app.getJobId());
            vo.setResumeId(app.getResumeId());
            vo.setStatus(app.getStatus());
            vo.setRemark(app.getRemark());
            vo.setCreatedAt(app.getCreatedAt());
            vo.setUpdatedAt(app.getUpdatedAt());

            Job job = jobMap.get(app.getJobId());
            if (job != null) {
                vo.setJobTitle(job.getTitle());
                CompanyProfile cp = companyMap.get(job.getCompanyId());
                if (cp != null) {
                    vo.setCompanyName(cp.getCompanyName());
                }
            }

            StudentProfile sp = studentMap.get(app.getStudentId());
            if (sp != null) {
                vo.setStudentName(sp.getRealName());
            }

            if (app.getResumeId() != null) {
                Resume resume = resumeMap.get(app.getResumeId());
                if (resume != null) {
                    vo.setResumeTitle(resume.getTitle());
                }
            }

            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }
}
