package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.recruitment.dto.StudentProfileDTO;
import com.campus.recruitment.entity.StudentProfile;
import com.campus.recruitment.mapper.StudentProfileMapper;
import com.campus.recruitment.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentProfileMapper studentProfileMapper;

    @Override
    public StudentProfile getByUserId(Long userId) {
        return studentProfileMapper.selectOne(
                new LambdaQueryWrapper<StudentProfile>().eq(StudentProfile::getUserId, userId));
    }

    @Override
    public void saveOrUpdate(Long userId, StudentProfileDTO dto) {
        StudentProfile profile = getByUserId(userId);
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUserId(userId);
            BeanUtils.copyProperties(dto, profile);
            studentProfileMapper.insert(profile);
        } else {
            BeanUtils.copyProperties(dto, profile);
            studentProfileMapper.updateById(profile);
        }
    }
}
