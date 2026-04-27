package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.ResumeDTO;
import com.campus.recruitment.entity.Resume;
import com.campus.recruitment.mapper.ResumeMapper;
import com.campus.recruitment.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeMapper resumeMapper;

    @Override
    public PageResult<Resume> listResumes(int page, int size, Long userId) {
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Resume::getUserId, userId);
        }
        wrapper.orderByDesc(Resume::getCreatedAt);

        Page<Resume> pageParam = new Page<>(page, size);
        Page<Resume> pageResult = resumeMapper.selectPage(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public Resume getResumeById(Long id) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        return resume;
    }

    @Override
    public void createResume(Long userId, ResumeDTO dto) {
        Resume resume = new Resume();
        BeanUtils.copyProperties(dto, resume);
        resume.setUserId(userId);
        resumeMapper.insert(resume);
    }

    @Override
    public void updateResume(Long id, ResumeDTO dto) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        BeanUtils.copyProperties(dto, resume);
        resumeMapper.updateById(resume);
    }

    @Override
    public void deleteResume(Long id) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        resumeMapper.deleteById(id);
    }
}
