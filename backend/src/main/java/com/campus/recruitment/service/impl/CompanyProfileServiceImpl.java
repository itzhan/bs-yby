package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.AuditDTO;
import com.campus.recruitment.dto.CompanyProfileDTO;
import com.campus.recruitment.entity.CompanyProfile;
import com.campus.recruitment.mapper.CompanyProfileMapper;
import com.campus.recruitment.service.CompanyProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompanyProfileServiceImpl implements CompanyProfileService {

    private final CompanyProfileMapper companyProfileMapper;

    @Override
    public CompanyProfile getByUserId(Long userId) {
        return companyProfileMapper.selectOne(
                new LambdaQueryWrapper<CompanyProfile>().eq(CompanyProfile::getUserId, userId));
    }

    @Override
    public CompanyProfile getById(Long id) {
        return companyProfileMapper.selectById(id);
    }

    @Override
    public void saveOrUpdate(Long userId, CompanyProfileDTO dto) {
        CompanyProfile profile = getByUserId(userId);
        if (profile == null) {
            profile = new CompanyProfile();
            profile.setUserId(userId);
            BeanUtils.copyProperties(dto, profile);
            profile.setAuditStatus(0); // 待审核
            companyProfileMapper.insert(profile);
        } else {
            BeanUtils.copyProperties(dto, profile);
            companyProfileMapper.updateById(profile);
        }
    }

    @Override
    public PageResult<CompanyProfile> listCompanies(int page, int size, Integer auditStatus, String keyword) {
        LambdaQueryWrapper<CompanyProfile> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) {
            wrapper.eq(CompanyProfile::getAuditStatus, auditStatus);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(CompanyProfile::getCompanyName, keyword)
                    .or().like(CompanyProfile::getIndustry, keyword)
                    .or().like(CompanyProfile::getCity, keyword));
        }
        wrapper.orderByDesc(CompanyProfile::getCreatedAt);

        Page<CompanyProfile> pageParam = new Page<>(page, size);
        Page<CompanyProfile> pageResult = companyProfileMapper.selectPage(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public void audit(Long id, AuditDTO dto) {
        CompanyProfile profile = companyProfileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException("企业档案不存在");
        }
        profile.setAuditStatus(dto.getAuditStatus());
        profile.setAuditRemark(dto.getAuditRemark());
        profile.setAuditTime(LocalDateTime.now());
        companyProfileMapper.updateById(profile);
    }
}
