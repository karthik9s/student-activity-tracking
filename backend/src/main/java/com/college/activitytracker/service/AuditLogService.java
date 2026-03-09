package com.college.activitytracker.service;

import com.college.activitytracker.dto.AuditLogDTO;
import com.college.activitytracker.model.AuditLog;
import com.college.activitytracker.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logOperation(String userId, String action, String entityType, String entityId,
                            Map<String, Object> oldValue, Map<String, Object> newValue,
                            String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setOldValue(oldValue);
            auditLog.setNewValue(newValue);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setTimestamp(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
            // log removed
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
            // Don't throw exception to avoid breaking the main operation
        }
    }

    @Transactional
    public void logCreate(String userId, String entityType, String entityId, 
                         Map<String, Object> newValue) {
        logOperation(userId, "CREATE", entityType, entityId, null, newValue, null, null);
    }

    @Transactional
    public void logUpdate(String userId, String entityType, String entityId,
                         Map<String, Object> oldValue, Map<String, Object> newValue) {
        logOperation(userId, "UPDATE", entityType, entityId, oldValue, newValue, null, null);
    }

    @Transactional
    public void logDelete(String userId, String entityType, String entityId,
                         Map<String, Object> oldValue) {
        logOperation(userId, "DELETE", entityType, entityId, oldValue, null, null, null);
    }

    public Page<AuditLogDTO> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByUserId(String userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable).map(this::toDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable).map(this::toDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable).map(this::toDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable).map(this::toDTO);
    }

    private AuditLogDTO toDTO(AuditLog auditLog) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(auditLog.getId());
        dto.setUserId(auditLog.getUserId());
        dto.setAction(auditLog.getAction());
        dto.setEntityType(auditLog.getEntityType());
        dto.setEntityId(auditLog.getEntityId());
        dto.setOldValue(auditLog.getOldValue());
        dto.setNewValue(auditLog.getNewValue());
        dto.setIpAddress(auditLog.getIpAddress());
        dto.setUserAgent(auditLog.getUserAgent());
        dto.setTimestamp(auditLog.getTimestamp());
        return dto;
    }
}


