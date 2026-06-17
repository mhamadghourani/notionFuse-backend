package com.mhmd.notion_fuse.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) { // Timeout of 2 seconds
                return ResponseEntity.ok("UP");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("DOWN");
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("DOWN");
    }
}
