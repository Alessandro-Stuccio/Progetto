package com.project.tesi.service.impl;

import com.project.tesi.enums.MessageStatus;
import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;
import com.project.tesi.model.User;
import com.project.tesi.repository.MessageRepository;
import com.project.tesi.service.MessageService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Message saveMessage(Chat chat, User sender, String content) {
        boolean sentByUser1 = chat.getUser1().getId().equals(sender.getId());
        Message message = Message.builder()
                .chat(chat)
                .sentByUser1(sentByUser1)
                .content(content)
                .timeStamp(LocalDateTime.now())
                .build();
        return messageRepository.save(message);
    }

    @Override
    public List<Message> getMessages(Long chatId, int page, int size) {
        return messageRepository.findMessagesByChatId(chatId, PageRequest.of(page, size));
    }

    @Override
    public void markAsDelivered(Long chatId, Long userId) {
        messageRepository.markMessagesAsDelivered(chatId, userId, MessageStatus.SENT, MessageStatus.DELIVERED);
    }

    @Override
    public void markAsRead(Long chatId, Long userId) {
        messageRepository.markMessagesAsRead(chatId, userId, MessageStatus.READ);
    }

    @Override
    public int getTotalUnreadCount(Long userId) {
        return messageRepository.countTotalUnreadMessagesByUserId(userId, MessageStatus.READ);
    }

    @Override
    public Message getLastMessage(Long chatId) {
        return messageRepository.findLastMessageByChatId(chatId);
    }

    @Override
    public int getUnreadCount(Long chatId, Long userId) {
        return messageRepository.countUnreadMessagesByChatIdAndUserId(chatId, userId, MessageStatus.READ);
    }
}
