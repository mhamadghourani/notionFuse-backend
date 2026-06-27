package com.mhmd.notion_fuse.auth;

import com.mhmd.notion_fuse.auth.dto.AuthenticationRequest;
import com.mhmd.notion_fuse.auth.dto.AuthenticationResponse;
import com.mhmd.notion_fuse.auth.dto.AuthMessageResponse;
import com.mhmd.notion_fuse.auth.dto.ForgotPasswordRequest;
import com.mhmd.notion_fuse.auth.dto.RegisterRequest;
import com.mhmd.notion_fuse.auth.dto.ResetPasswordRequest;
import com.mhmd.notion_fuse.auth.entity.AuthToken;
import com.mhmd.notion_fuse.auth.entity.AuthTokenType;
import com.mhmd.notion_fuse.auth.repository.AuthTokenRepository;
import com.mhmd.notion_fuse.auth.service.EmailService;
import com.mhmd.notion_fuse.security.jwt.JwtService;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService (
            UserRepository userRepository,
            AuthTokenRepository authTokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.emailService = emailService;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(()->new RuntimeException("user not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("please verify your email before logging in");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),request.password()));

        var jwtToken = jwtService.generateToken(user.getEmail());
        return new AuthenticationResponse(jwtToken);
    }



    @Transactional
    public AuthMessageResponse register(RegisterRequest request){
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("email already registered");
        }

        validatePassword(request.password());

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role("USER")
                .enabled(false) // Note: user is disabled until they verify!
                .build();
        userRepository.save(user);

        var token = createToken(user, AuthTokenType.EMAIL_VERIFICATION, LocalDateTime.now().plusHours(24));

        try {
            emailService.sendVerificationEmail(user.getEmail(), token.getToken());
        } catch (Exception e) {
            System.err.println("🚨 CRITICAL: Failed to send verification email: " + e.getMessage());
        }

        return new AuthMessageResponse("Registration successful. Please check your email to verify your account.");
    }

    @Transactional
    public AuthMessageResponse verifyEmail(String token) {
        var authToken = authTokenRepository.findByTokenAndType(token, AuthTokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid verification token"));

        var user = authToken.getUser();

        if (authToken.isUsed() && user.isEnabled()) {
            return new AuthMessageResponse("Email is already verified. You can now log in.");
        }

        if (authToken.isUsed() || authToken.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "verification token is invalid or expired");
        }

        user.setEnabled(true);
        authToken.markUsed();
        userRepository.save(user);
        authTokenRepository.save(authToken);

        return new AuthMessageResponse("Email verified successfully. You can now log in.");
    }

    @Transactional
    public AuthMessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            invalidateUnusedTokens(user, AuthTokenType.PASSWORD_RESET);
            var token = createToken(user, AuthTokenType.PASSWORD_RESET, LocalDateTime.now().plusHours(1));
            emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
        });

        return new AuthMessageResponse("If an account exists for that email, a password reset link has been sent.");
    }

    @Transactional
    public AuthMessageResponse resetPassword(ResetPasswordRequest request) {
        var authToken = authTokenRepository.findByTokenAndType(request.token(), AuthTokenType.PASSWORD_RESET)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid password reset token"));

        if (authToken.isUsed() || authToken.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password reset token is invalid or expired");
        }

        validatePassword(request.password());

        var user = authToken.getUser();
        user.setPassword(passwordEncoder.encode(request.password()));
        authToken.markUsed();
        userRepository.save(user);
        authTokenRepository.save(authToken);

        return new AuthMessageResponse("Password reset successfully. You can now log in.");
    }

    private AuthToken createToken(User user, AuthTokenType type, LocalDateTime expiresAt) {
        invalidateUnusedTokens(user, type);

        var authToken = AuthToken.builder()
                .token(UUID.randomUUID().toString())
                .type(type)
                .user(user)
                .expiresAt(expiresAt)
                .build();

        return authTokenRepository.save(authToken);
    }

    private void invalidateUnusedTokens(User user, AuthTokenType type) {
        authTokenRepository.findByUserAndTypeAndUsedAtIsNull(user, type)
                .forEach(token -> {
                    token.markUsed();
                    authTokenRepository.save(token);
                });
    }

    private void validatePassword(String password) {
        if (password == null
                || password.length() < 8
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters and include one uppercase letter and one special character"
            );
        }
    }
}
