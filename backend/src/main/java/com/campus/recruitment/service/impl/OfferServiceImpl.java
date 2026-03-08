package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.OfferDTO;
import com.campus.recruitment.entity.Application;
import com.campus.recruitment.entity.Offer;
import com.campus.recruitment.mapper.ApplicationMapper;
import com.campus.recruitment.mapper.OfferMapper;
import com.campus.recruitment.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferMapper offerMapper;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional
    public void createOffer(OfferDTO dto) {
        Application application = applicationMapper.selectById(dto.getApplicationId());
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        Offer offer = new Offer();
        offer.setApplicationId(dto.getApplicationId());
        offer.setCompanyId(null); // 将在 controller 层设置
        offer.setStudentId(application.getStudentId());
        offer.setJobId(application.getJobId());
        offer.setSalary(dto.getSalary());
        offer.setStartDate(dto.getStartDate());
        offer.setDescription(dto.getDescription());
        offer.setDeadline(dto.getDeadline());
        offer.setStatus(0); // 待回复
        offerMapper.insert(offer);

        // 同步更新申请状态为已录用
        application.setStatus(3);
        applicationMapper.updateById(application);
    }

    @Override
    public PageResult<Offer> listByStudent(Long studentId, int page, int size) {
        LambdaQueryWrapper<Offer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Offer::getStudentId, studentId);
        wrapper.orderByDesc(Offer::getCreatedAt);

        Page<Offer> pageParam = new Page<>(page, size);
        Page<Offer> pageResult = offerMapper.selectPage(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public PageResult<Offer> listByCompany(Long companyId, int page, int size) {
        LambdaQueryWrapper<Offer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Offer::getCompanyId, companyId);
        wrapper.orderByDesc(Offer::getCreatedAt);

        Page<Offer> pageParam = new Page<>(page, size);
        Page<Offer> pageResult = offerMapper.selectPage(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public void respond(Long id, Integer status, Long studentId) {
        Offer offer = offerMapper.selectById(id);
        if (offer == null) {
            throw new BusinessException("Offer 不存在");
        }
        if (!offer.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作此 Offer");
        }
        if (offer.getStatus() != 0) {
            throw new BusinessException("该 Offer 已处理");
        }
        // status: 1=接受, 2=拒绝
        offer.setStatus(status);
        offerMapper.updateById(offer);
    }
}
