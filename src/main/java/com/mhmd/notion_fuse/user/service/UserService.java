package com.mhmd.notion_fuse.user.service;

import com.mhmd.notion_fuse.user.dto.CreateUserRequest;
import com.mhmd.notion_fuse.user.dto.UserMapper;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequest request){
        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email Already Exists");
        }
        User user = UserMapper.toEntity(request);
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
