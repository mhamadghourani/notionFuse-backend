package com.mhmd.notion_fuse.user.controller;

import com.mhmd.notion_fuse.user.dto.*;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import com.mhmd.notion_fuse.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService,UserRepository userRepository){
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Principal principal){
        return ResponseEntity.ok(userService.getMyProfile(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request){
        User newUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(newUser));
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email){
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestBody UpdateProfileRequest request) {
        String email = currentUser.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found in database"));
        userService.updateName(user.getId(), request.getName());
        return ResponseEntity.ok("Profile updated successfully");
    }
    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestBody UpdatePasswordRequest request
    ){
        String email = currentUser.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User not found in database"));
        userService.updatePassword(user.getId(),request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");
    }
}
