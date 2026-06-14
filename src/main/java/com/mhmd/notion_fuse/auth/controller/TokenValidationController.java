package com.mhmd.notion_fuse.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenValidationController {

    @GetMapping("/api/v1/user/validate")
    public ResponseEntity<?> validateToken() {
        return ResponseEntity.ok().build();
    }
}
