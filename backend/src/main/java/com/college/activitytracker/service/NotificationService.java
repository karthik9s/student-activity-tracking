package com.college.activitytracker.service;

import com.college.activitytracker.dto.NotificationDTO;
import com.college.activitytracker.model.Notification;
import com.college.activitytracker.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    
    public NotificationService(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public NotificationDTO createNotification(NotificationDTO dto) {
        Notification notification = new Notification();
        notification.setUserId(dto.getUserId());
        notification.setType(dto.getType());
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setRead(false);
        notification.setRelatedEntityId(dto.getRelatedEntityId());
        notification.setRelatedEntityType(dto.getRelatedEntityType());
        notification.setCreatedAt(LocalDateTime.now());
        
        Notification saved = notificationRepository.save(notification);
        return toDTO(saved);
    }

    public void createLowAttendanceAlert(String studentId, String studentName, String studentEmail, 
                                        String subjectName, double attendancePercentage) {
        // Create in-app notification
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(studentId);
        notification.setType("LOW_ATTENDANCE");
        notification.setTitle("Low Attendance Alert");
        notification.setMessage(String.format("Your attendance in %s is %.1f%%, which is below the required 75%%.", 
                subjectName, attendancePercentage));
        notification.setRelatedEntityType("ATTENDANCE");
        
        createNotification(notification);
        
        // Send email notification
        String emailSubject = "Low Attendance Alert - " + subjectName;
        String emailBody = String.format(
                "Dear %s,\n\n" +
                "This is to inform you that your attendance in %s is %.1f%%, " +
                "which is below the required 75%% threshold.\n\n" +
                "Please ensure regular attendance to meet the minimum requirement.\n\n" +
                "Best regards,\n" +
                "Academic Department",
                studentName, subjectName, attendancePercentage
        );
        
        emailService.sendEmail(studentEmail, emailSubject, emailBody);
        
        // log removed
    }

    public void createPerformanceNotification(String studentId, String assessmentType, 
                                             String subjectName, double marks, String grade) {
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(studentId);
        notification.setType("PERFORMANCE_UPDATE");
        notification.setTitle("Performance Update");
        notification.setMessage(String.format("Your %s marks for %s have been updated: %.1f (%s)", 
                assessmentType, subjectName, marks, grade));
        notification.setRelatedEntityType("PERFORMANCE");
        
        createNotification(notification);
        
        // log removed
    }

    public void createAnnouncement(String userId, String title, String message) {
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(userId);
        notification.setType("ANNOUNCEMENT");
        notification.setTitle(title);
        notification.setMessage(message);
        
        createNotification(notification);
        
        // log removed
    }

    public List<NotificationDTO> getUserNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<NotificationDTO> getUserNotificationsPaginated(String userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::toDTO);
    }

    public List<NotificationDTO> getUnreadNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public NotificationDTO markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        Notification updated = notificationRepository.save(notification);
        return toDTO(updated);
    }

    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
        
        notificationRepository.saveAll(unreadNotifications);
        
        // log removed
    }

    private NotificationDTO toDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getRelatedEntityId(),
                notification.getRelatedEntityType(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}


