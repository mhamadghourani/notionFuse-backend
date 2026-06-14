package com.mhmd.notion_fuse.auth;

import com.mhmd.notion_fuse.auth.dto.AuthenticationRequest;
import com.mhmd.notion_fuse.auth.dto.AuthenticationResponse;
import com.mhmd.notion_fuse.auth.dto.AuthMessageResponse;
import com.mhmd.notion_fuse.auth.dto.ForgotPasswordRequest;
import com.mhmd.notion_fuse.auth.dto.RegisterRequest;
import com.mhmd.notion_fuse.auth.dto.ResetPasswordRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    public AuthController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
    @PostMapping("/register")
    public ResponseEntity<AuthMessageResponse> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<AuthMessageResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authenticationService.verifyEmail(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthMessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authenticationService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthMessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authenticationService.resetPassword(request));
    }
}
