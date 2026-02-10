package com.smartparking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartparking.dto.NotificationRequest;
import com.smartparking.dto.NotificationResponse;
import com.smartparking.model.Notification;
import com.smartparking.repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public NotificationResponse sendNotification(NotificationRequest request) {
        Notification notification = new Notification(
                request.getUserId(),
                request.getType(),
                request.getMessage()
        );
        
        if (request.getBookingId() != null) {
            notification.setBookingId(request.getBookingId());
        }
        
        notification = notificationRepository.save(notification);
        return convertToResponse(notification);
    }

    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findNotificationsByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findUnreadNotificationsByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadNotifications(userId);
    }

    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);
        
        return convertToResponse(notification);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadNotificationsByUserId(userId);
        unreadNotifications.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    public void deleteOldNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        notifications.stream()
                .filter(n -> n.getCreatedAt().isBefore(thirtyDaysAgo))
                .forEach(n -> notificationRepository.deleteById(n.getId()));
    }

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getUserId()
        );
        response.setBookingId(notification.getBookingId());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());
        response.setReadAt(notification.getReadAt());
        return response;
    }
}
