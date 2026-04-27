package com.campus.recruitment.service;

import com.campus.recruitment.dto.StudentProfileDTO;
import com.campus.recruitment.entity.StudentProfile;

public interface StudentProfileService {

    /**
     * 根据用户 ID 获取学生档案
     */
    StudentProfile getByUserId(Long userId);

    /**
     * 新增或更新学生档案
     */
    void saveOrUpdate(Long userId, StudentProfileDTO dto);
}
