package com.college.activitytracker.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceProtectionService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    public void recordLoginFailure(String email) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(email, k -> new LoginAttempt());
        
        synchronized (attempt) {
            // Reset if lockout period has passed
            if (attempt.lockedUntil != null && LocalDateTime.now().isAfter(attempt.lockedUntil)) {
                attempt.failedAttempts = 0;
                attempt.lockedUntil = null;
            }
            
            attempt.failedAttempts++;
            attempt.lastAttempt = LocalDateTime.now();
            
            if (attempt.failedAttempts >= MAX_ATTEMPTS) {
                attempt.lockedUntil = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
                // log removed
            }
        }
    }

    public void recordLoginSuccess(String email) {
        loginAttempts.remove(email);
    }

    public boolean isAccountLocked(String email) {
        LoginAttempt attempt = loginAttempts.get(email);
        if (attempt == null) {
            return false;
        }
        
        synchronized (attempt) {
            if (attempt.lockedUntil == null) {
                return false;
            }
            
            if (LocalDateTime.now().isAfter(attempt.lockedUntil)) {
                // Lockout period has passed
                attempt.failedAttempts = 0;
                attempt.lockedUntil = null;
                return false;
            }
            
            return true;
        }
    }

    public int getRemainingAttempts(String email) {
        LoginAttempt attempt = loginAttempts.get(email);
        if (attempt == null) {
            return MAX_ATTEMPTS;
        }
        
        synchronized (attempt) {
            if (attempt.lockedUntil != null && LocalDateTime.now().isBefore(attempt.lockedUntil)) {
                return 0;
            }
            return Math.max(0, MAX_ATTEMPTS - attempt.failedAttempts);
        }
    }

    public LocalDateTime getLockoutTime(String email) {
        LoginAttempt attempt = loginAttempts.get(email);
        if (attempt == null) {
            return null;
        }
        
        synchronized (attempt) {
            if (attempt.lockedUntil != null && LocalDateTime.now().isBefore(attempt.lockedUntil)) {
                return attempt.lockedUntil;
            }
            return null;
        }
    }

    private static class LoginAttempt {
        int failedAttempts = 0;
        LocalDateTime lastAttempt;
        LocalDateTime lockedUntil;
    }
}


