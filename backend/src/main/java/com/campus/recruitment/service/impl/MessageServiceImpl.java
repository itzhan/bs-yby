package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.entity.Message;
import com.campus.recruitment.mapper.MessageMapper;
import com.campus.recruitment.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public PageResult<Message> listMessages(Long userId, int page, int size, String type, Boolean isRead) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);
        if (StringUtils.hasText(type)) {
            wrapper.eq(Message::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(Message::getIsRead, isRead);
        }
        wrapper.orderByDesc(Message::getCreatedAt);

        Page<Message> pageParam = new Page<>(page, size);
        Page<Message> pageResult = messageMapper.selectPage(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public void sendMessage(Long senderId, Long receiverId, String title, String content, String type, Long relatedId) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setRelatedId(relatedId);
        message.setIsRead(false);
        messageMapper.insert(message);
    }

    @Override
    public void markAsRead(Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        message.setIsRead(true);
        messageMapper.updateById(message);
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId)
               .eq(Message::getIsRead, false);

        Message update = new Message();
        update.setIsRead(true);
        messageMapper.update(update, wrapper);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, userId)
                        .eq(Message::getIsRead, false));
    }
}
