package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.AnnouncementDTO;
import com.campus.recruitment.entity.Announcement;
import com.campus.recruitment.mapper.AnnouncementMapper;
import com.campus.recruitment.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public PageResult<Announcement> listAnnouncements(int page, int size, String type, Integer status) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(Announcement::getType, type);
        }
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        // 置顶优先，再按创建时间倒序
        wrapper.orderByDesc(Announcement::getTopFlag)
               .orderByDesc(Announcement::getCreatedAt);

        Page<Announcement> pageParam = new Page<>(page, size);
        Page<Announcement> pageResult = announcementMapper.selectPage(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public Announcement getAnnouncementById(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        return announcement;
    }

    @Override
    public void createAnnouncement(Long publisherId, AnnouncementDTO dto) {
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(dto, announcement);
        announcement.setPublisherId(publisherId);
        if (announcement.getStatus() == null) {
            announcement.setStatus(1); // 默认已发布
        }
        if (announcement.getTopFlag() == null) {
            announcement.setTopFlag(false);
        }
        announcementMapper.insert(announcement);
    }

    @Override
    public void updateAnnouncement(Long id, AnnouncementDTO dto) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        BeanUtils.copyProperties(dto, announcement);
        announcementMapper.updateById(announcement);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        announcementMapper.deleteById(id);
    }
}
