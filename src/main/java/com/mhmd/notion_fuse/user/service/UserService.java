package com.mhmd.notion_fuse.user.service;

import com.mhmd.notion_fuse.user.dto.CreateUserRequest;
import com.mhmd.notion_fuse.user.dto.UserMapper;
import com.mhmd.notion_fuse.user.dto.UserResponse;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getMyProfile(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("user not found"));
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName() != null ? user.getName() : "New User");
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setPlan(user.getPlan() != null ? user.getPlan() : "FREE");
        return response;
    }

    public User createUser(CreateUserRequest request){
        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email Already Exists");
        }
        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPlan("FREE");
        user.setRole("USER");
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User Not Found "));
    }
    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User Not Found"));
    }
}
