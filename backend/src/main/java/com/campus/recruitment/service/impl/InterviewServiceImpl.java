package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.InterviewDTO;
import com.campus.recruitment.entity.Application;
import com.campus.recruitment.entity.Interview;
import com.campus.recruitment.mapper.ApplicationMapper;
import com.campus.recruitment.mapper.InterviewMapper;
import com.campus.recruitment.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewMapper interviewMapper;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional
    public void createInterview(InterviewDTO dto) {
        Application application = applicationMapper.selectById(dto.getApplicationId());
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        Interview interview = new Interview();
        interview.setApplicationId(dto.getApplicationId());
        interview.setCompanyId(null); // 将在 controller 层从 job 中获取
        interview.setStudentId(application.getStudentId());
        interview.setJobId(application.getJobId());
        interview.setInterviewTime(dto.getInterviewTime());
        interview.setLocation(dto.getLocation());
        interview.setInterviewType(dto.getInterviewType());
        interview.setDescription(dto.getDescription());
        interview.setContact(dto.getContact());
        interview.setStatus(0); // 待确认

        interviewMapper.insert(interview);

        // 同步更新申请状态为面试中
        application.setStatus(2);
        applicationMapper.updateById(application);
    }

    @Override
    public PageResult<Interview> listByStudent(Long studentId, int page, int size) {
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interview::getStudentId, studentId);
        wrapper.orderByDesc(Interview::getInterviewTime);

        Page<Interview> pageParam = new Page<>(page, size);
        Page<Interview> pageResult = interviewMapper.selectPage(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public PageResult<Interview> listByCompany(Long companyId, int page, int size) {
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interview::getCompanyId, companyId);
        wrapper.orderByDesc(Interview::getInterviewTime);

        Page<Interview> pageParam = new Page<>(page, size);
        Page<Interview> pageResult = interviewMapper.selectPage(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            throw new BusinessException("面试记录不存在");
        }
        interview.setStatus(status);
        interviewMapper.updateById(interview);
    }
}
