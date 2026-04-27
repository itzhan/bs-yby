package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.entity.Message;

public interface MessageService {

    /**
     * 分页查询消息列表
     */
    PageResult<Message> listMessages(Long userId, int page, int size, String type, Boolean isRead);

    /**
     * 发送消息
     */
    void sendMessage(Long senderId, Long receiverId, String title, String content, String type, Long relatedId);

    /**
     * 标记单条消息为已读
     */
    void markAsRead(Long id);

    /**
     * 标记所有消息为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 获取未读消息数量
     */
    long getUnreadCount(Long userId);
}
