package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.AuditDTO;
import com.campus.recruitment.dto.JobFairDTO;
import com.campus.recruitment.dto.JobFairVO;
import com.campus.recruitment.entity.CompanyProfile;
import com.campus.recruitment.entity.JobFair;
import com.campus.recruitment.entity.JobFairBooking;
import com.campus.recruitment.mapper.CompanyProfileMapper;
import com.campus.recruitment.mapper.JobFairBookingMapper;
import com.campus.recruitment.mapper.JobFairMapper;
import com.campus.recruitment.service.JobFairService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobFairServiceImpl implements JobFairService {

    private final JobFairMapper jobFairMapper;
    private final JobFairBookingMapper jobFairBookingMapper;
    private final CompanyProfileMapper companyProfileMapper;

    @Override
    public PageResult<JobFairVO> listJobFairs(int page, int size, Integer status, Long companyId) {
        LambdaQueryWrapper<JobFair> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(JobFair::getStatus, status);
        }
        if (companyId != null) {
            wrapper.eq(JobFair::getCompanyId, companyId);
        }
        wrapper.orderByDesc(JobFair::getCreatedAt);

        Page<JobFair> pageParam = new Page<>(page, size);
        Page<JobFair> pageResult = jobFairMapper.selectPage(pageParam, wrapper);

        // 批量查询公司信息
        List<JobFair> fairs = pageResult.getRecords();
        Set<Long> companyIds = fairs.stream().map(JobFair::getCompanyId).collect(Collectors.toSet());
        Map<Long, CompanyProfile> companyMap = Map.of();
        if (!companyIds.isEmpty()) {
            companyMap = companyProfileMapper.selectList(
                    new LambdaQueryWrapper<CompanyProfile>().in(CompanyProfile::getUserId, companyIds))
                    .stream().collect(Collectors.toMap(CompanyProfile::getUserId, Function.identity(), (a, b) -> a));
        }

        Map<Long, CompanyProfile> finalCompanyMap = companyMap;
        List<JobFairVO> voList = fairs.stream().map(fair -> {
            JobFairVO vo = toJobFairVO(fair);
            CompanyProfile cp = finalCompanyMap.get(fair.getCompanyId());
            if (cp != null) {
                vo.setCompanyName(cp.getCompanyName());
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public JobFairVO getJobFairDetail(Long id) {
        JobFair fair = jobFairMapper.selectById(id);
        if (fair == null) {
            throw new BusinessException("招聘会不存在");
        }
        JobFairVO vo = toJobFairVO(fair);
        CompanyProfile cp = companyProfileMapper.selectOne(
                new LambdaQueryWrapper<CompanyProfile>().eq(CompanyProfile::getUserId, fair.getCompanyId()));
        if (cp != null) {
            vo.setCompanyName(cp.getCompanyName());
        }
        return vo;
    }

    @Override
    public void createJobFair(Long companyId, JobFairDTO dto) {
        JobFair fair = new JobFair();
        BeanUtils.copyProperties(dto, fair);
        fair.setCompanyId(companyId);
        fair.setCurrentCount(0);
        fair.setStatus(0); // 待审核
        jobFairMapper.insert(fair);
    }

    @Override
    public void updateJobFair(Long id, JobFairDTO dto) {
        JobFair fair = jobFairMapper.selectById(id);
        if (fair == null) {
            throw new BusinessException("招聘会不存在");
        }
        BeanUtils.copyProperties(dto, fair);
        jobFairMapper.updateById(fair);
    }

    @Override
    public void auditJobFair(Long id, AuditDTO dto) {
        JobFair fair = jobFairMapper.selectById(id);
        if (fair == null) {
            throw new BusinessException("招聘会不存在");
        }
        // auditStatus: 1=通过, 2=拒绝
        fair.setStatus(dto.getAuditStatus());
        fair.setAuditRemark(dto.getAuditRemark());
        jobFairMapper.updateById(fair);
    }

    @Override
    @Transactional
    public void bookJobFair(Long jobFairId, Long studentId) {
        JobFair fair = jobFairMapper.selectById(jobFairId);
        if (fair == null) {
            throw new BusinessException("招聘会不存在");
        }
        if (fair.getStatus() != 1) {
            throw new BusinessException("招聘会当前不可预约");
        }
        if (fair.getMaxCapacity() != null && fair.getCurrentCount() >= fair.getMaxCapacity()) {
            throw new BusinessException("招聘会名额已满");
        }

        // 检查是否已有预约记录（含已取消的）
        JobFairBooking existing = jobFairBookingMapper.selectOne(
                new LambdaQueryWrapper<JobFairBooking>()
                        .eq(JobFairBooking::getJobFairId, jobFairId)
                        .eq(JobFairBooking::getStudentId, studentId));
        if (existing != null) {
            if (existing.getStatus() == 0) {
                throw new BusinessException("您已预约该招聘会");
            }
            // 之前取消过，重新激活预约
            existing.setStatus(0);
            jobFairBookingMapper.updateById(existing);
        } else {
            JobFairBooking booking = new JobFairBooking();
            booking.setJobFairId(jobFairId);
            booking.setStudentId(studentId);
            booking.setStatus(0);
            jobFairBookingMapper.insert(booking);
        }

        // 更新当前人数
        fair.setCurrentCount(fair.getCurrentCount() + 1);
        jobFairMapper.updateById(fair);
    }

    @Override
    @Transactional
    public void cancelBooking(Long jobFairId, Long studentId) {
        JobFairBooking booking = jobFairBookingMapper.selectOne(
                new LambdaQueryWrapper<JobFairBooking>()
                        .eq(JobFairBooking::getJobFairId, jobFairId)
                        .eq(JobFairBooking::getStudentId, studentId)
                        .eq(JobFairBooking::getStatus, 0));
        if (booking == null) {
            throw new BusinessException("预约记录不存在");
        }
        booking.setStatus(1); // 已取消
        jobFairBookingMapper.updateById(booking);

        // 更新当前人数
        JobFair fair = jobFairMapper.selectById(jobFairId);
        if (fair != null && fair.getCurrentCount() > 0) {
            fair.setCurrentCount(fair.getCurrentCount() - 1);
            jobFairMapper.updateById(fair);
        }
    }

    @Override
    public PageResult<JobFairBooking> listBookings(Long jobFairId, int page, int size) {
        LambdaQueryWrapper<JobFairBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobFairBooking::getJobFairId, jobFairId);
        wrapper.orderByDesc(JobFairBooking::getCreatedAt);

        Page<JobFairBooking> pageParam = new Page<>(page, size);
        Page<JobFairBooking> pageResult = jobFairBookingMapper.selectPage(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public List<Long> getBookedFairIds(Long studentId) {
        LambdaQueryWrapper<JobFairBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobFairBooking::getStudentId, studentId)
               .eq(JobFairBooking::getStatus, 0)
               .select(JobFairBooking::getJobFairId);
        return jobFairBookingMapper.selectList(wrapper)
                .stream().map(JobFairBooking::getJobFairId).collect(Collectors.toList());
    }

    private JobFairVO toJobFairVO(JobFair fair) {
        JobFairVO vo = new JobFairVO();
        vo.setId(fair.getId());
        vo.setCompanyId(fair.getCompanyId());
        vo.setTitle(fair.getTitle());
        vo.setDescription(fair.getDescription());
        vo.setLocation(fair.getLocation());
        vo.setStartTime(fair.getStartTime());
        vo.setEndTime(fair.getEndTime());
        vo.setMaxCapacity(fair.getMaxCapacity());
        vo.setCurrentCount(fair.getCurrentCount());
        vo.setStatus(fair.getStatus());
        vo.setAuditRemark(fair.getAuditRemark());
        vo.setCreatedAt(fair.getCreatedAt());
        vo.setUpdatedAt(fair.getUpdatedAt());
        return vo;
    }
}
