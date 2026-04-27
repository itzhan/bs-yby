package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.AnnouncementDTO;
import com.campus.recruitment.entity.Announcement;

public interface AnnouncementService {

    /**
     * 分页查询公告列表
     */
    PageResult<Announcement> listAnnouncements(int page, int size, String type, Integer status);

    /**
     * 获取公告详情
     */
    Announcement getAnnouncementById(Long id);

    /**
     * 创建公告
     */
    void createAnnouncement(Long publisherId, AnnouncementDTO dto);

    /**
     * 更新公告
     */
    void updateAnnouncement(Long id, AnnouncementDTO dto);

    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);
}
