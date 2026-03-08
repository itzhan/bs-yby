package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.AuditDTO;
import com.campus.recruitment.dto.CompanyProfileDTO;
import com.campus.recruitment.entity.CompanyProfile;

public interface CompanyProfileService {

    /**
     * 根据用户 ID 获取企业档案
     */
    CompanyProfile getByUserId(Long userId);

    /**
     * 根据企业档案 ID 获取详情
     */
    CompanyProfile getById(Long id);

    /**
     * 新增或更新企业档案
     */
    void saveOrUpdate(Long userId, CompanyProfileDTO dto);

    /**
     * 分页查询企业列表
     */
    PageResult<CompanyProfile> listCompanies(int page, int size, Integer auditStatus, String keyword);

    /**
     * 企业审核
     */
    void audit(Long id, AuditDTO dto);
}
