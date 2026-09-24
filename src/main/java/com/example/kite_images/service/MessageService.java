package com.example.kite_images.service;

import com.example.kite_images.model.Message;
import com.example.kite_images.model.User;
import com.example.kite_images.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message sendMessage(User sender, User receiver, String text) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setText(text);
        return messageRepository.save(message);
    }

    public List<Message> getConversation(Long userId1, Long userId2) {
        return messageRepository.findConversation(userId1, userId2);
    }

    public List<Map<String, Object>> getConversations(Long userId) {
        List<Message> latestMessages = messageRepository.findLatestMessagesPerConversation(userId);
        List<Map<String, Object>> conversations = new ArrayList<>();

        for (Message msg : latestMessages) {
            User otherUser = msg.getSender().getId().equals(userId) ? msg.getReceiver() : msg.getSender();
            Map<String, Object> conv = new LinkedHashMap<>();
            conv.put("user", otherUser);
            conv.put("lastMessage", msg);
            conversations.add(conv);
        }

        return conversations;
    }

    public void markAsRead(Long senderId, Long receiverId) {
        List<Message> messages = messageRepository.findConversation(senderId, receiverId);
        for (Message msg : messages) {
            if (msg.getReceiver().getId().equals(receiverId) && !msg.isRead()) {
                msg.setRead(true);
                messageRepository.save(msg);
            }
        }
    }

    public int getUnreadCount(Long userId, Long otherUserId) {
        return messageRepository.countByReceiverIdAndSenderIdAndReadFalse(userId, otherUserId);
    }

    public Message getMessage(Long id) {
        return messageRepository.findById(id).orElse(null);
    }

    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }
}
