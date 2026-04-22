package com.mhmd.notion_fuse.user;

import com.mhmd.notion_fuse.user.dto.UserResponse;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import com.mhmd.notion_fuse.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should return UserResponse when user exists")
    void getMyProfile_Success(){
        User mockUser = User.builder()
                .id(1L)
                .name("Moe")
                .email("moe@example.com")
                .role("USER")
                .plan("FREE")
                .build();
        when(userRepository.findByEmail("moe@example.com"))
                .thenReturn(Optional.of(mockUser));

        UserResponse response = userService.getMyProfile("moe@example.com");
        assertNotNull(response);
        assertEquals("moe@example.com",response.getEmail());
        verify(userRepository, times(1)).findByEmail("moe@example.com");
    }
    @Test
    @DisplayName("Should throw exception if user not found")
    void getMyProfile_UserNotFound(){
        when(userRepository.findByEmail("stranger@example.com"))
                .thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, ()->{
            userService.getMyProfile("stranger@example.com");
        });
    }
}
