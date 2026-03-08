package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.OfferDTO;
import com.campus.recruitment.entity.Offer;

public interface OfferService {

    /**
     * 创建 Offer
     */
    void createOffer(OfferDTO dto);

    /**
     * 学生查询自己的 Offer 列表
     */
    PageResult<Offer> listByStudent(Long studentId, int page, int size);

    /**
     * 企业查询自己发出的 Offer 列表
     */
    PageResult<Offer> listByCompany(Long companyId, int page, int size);

    /**
     * 学生回应 Offer（接受/拒绝）
     */
    void respond(Long id, Integer status, Long studentId);
}
