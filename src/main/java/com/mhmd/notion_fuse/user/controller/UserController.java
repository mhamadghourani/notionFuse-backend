package com.mhmd.notion_fuse.user.controller;

import com.mhmd.notion_fuse.user.dto.CreateUserRequest;
import com.mhmd.notion_fuse.user.dto.UserMapper;
import com.mhmd.notion_fuse.user.dto.UserResponse;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import com.mhmd.notion_fuse.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
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
}
