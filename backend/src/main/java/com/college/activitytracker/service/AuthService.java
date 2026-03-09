package com.college.activitytracker.service;

import com.college.activitytracker.dto.*;
import com.college.activitytracker.model.User;
import com.college.activitytracker.repository.UserRepository;
import com.college.activitytracker.security.JwtTokenProvider;
import com.college.activitytracker.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final BruteForceProtectionService bruteForceProtectionService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                      PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider,
                      BruteForceProtectionService bruteForceProtectionService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.bruteForceProtectionService = bruteForceProtectionService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsActive(true);

        user = userRepository.save(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                UserPrincipal.create(user),
                null,
                UserPrincipal.create(user).getAuthorities()
        );

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(accessTokenExpiration / 1000);
        response.setUser(mapToUserDTO(user));
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        // Check if account is locked
        if (bruteForceProtectionService.isAccountLocked(request.getEmail())) {
            LocalDateTime lockoutTime = bruteForceProtectionService.getLockoutTime(request.getEmail());
            throw new RuntimeException("Account is temporarily locked due to multiple failed login attempts. Try again after " + lockoutTime);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Record successful login
            bruteForceProtectionService.recordLoginSuccess(request.getEmail());

            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Update last login
            userRepository.findById(userPrincipal.getId()).ifPresent(user -> {
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
            });

            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AuthResponse response = new AuthResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(accessTokenExpiration / 1000);
            response.setUser(mapToUserDTO(user));
            return response;
        } catch (Exception e) {
            // Record failed login attempt
            bruteForceProtectionService.recordLoginFailure(request.getEmail());
            int remainingAttempts = bruteForceProtectionService.getRemainingAttempts(request.getEmail());
            
            if (remainingAttempts > 0) {
                throw new RuntimeException("Invalid credentials. " + remainingAttempts + " attempts remaining.");
            } else {
                throw new RuntimeException("Account locked due to multiple failed login attempts. Please try again in 15 minutes.");
            }
        }
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmailAndIsActiveTrue(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                UserPrincipal.create(user),
                null,
                UserPrincipal.create(user).getAuthorities()
        );

        String newAccessToken = tokenProvider.generateAccessToken(authentication);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(accessTokenExpiration / 1000);
        response.setUser(mapToUserDTO(user));
        return response;
    }

    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        return dto;
    }
}


