package com.mhmd.notion_fuse.auth;

import com.mhmd.notion_fuse.auth.dto.AuthenticationRequest;
import com.mhmd.notion_fuse.auth.dto.AuthenticationResponse;
import com.mhmd.notion_fuse.auth.dto.RegisterRequest;
import com.mhmd.notion_fuse.security.jwt.JwtService;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService (
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),request.password()));

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(()->new RuntimeException("user not found"));

        var jwtToken = jwtService.generateToken(user.getEmail());
        return new AuthenticationResponse(jwtToken);
    }
    public AuthenticationResponse register(RegisterRequest request){
        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role("USER")
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user.getEmail());
        return new AuthenticationResponse(jwtToken);
    }
}
