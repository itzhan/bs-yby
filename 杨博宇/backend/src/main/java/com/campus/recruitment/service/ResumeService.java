package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.ResumeDTO;
import com.campus.recruitment.entity.Resume;

public interface ResumeService {

    /**
     * 分页查询简历列表
     */
    PageResult<Resume> listResumes(int page, int size, Long userId);

    /**
     * 获取简历详情
     */
    Resume getResumeById(Long id);

    /**
     * 创建简历
     */
    void createResume(Long userId, ResumeDTO dto);

    /**
     * 更新简历
     */
    void updateResume(Long id, ResumeDTO dto);

    /**
     * 删除简历
     */
    void deleteResume(Long id);
}
