package com.mhmd.notion_fuse.user.dto;

import com.mhmd.notion_fuse.user.entity.User;

public class UserMapper {

    public static User toEntity(CreateUserRequest request){
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        return user;
    }

    public static UserResponse toResponse(User user){

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setPlan(user.getPlan());


        // Map the UserTier enum to a String ("NORMAL" or "SPECIAL") for the frontend
        if (user.getTier() != null) {
            response.setTier(user.getTier().name());
        }
        System.out.println("DEBUG: User tier from database is: " + user.getTier());
        return response;
    }
}
