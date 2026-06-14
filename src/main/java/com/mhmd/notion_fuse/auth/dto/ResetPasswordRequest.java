package com.mhmd.notion_fuse.auth.dto;

public record ResetPasswordRequest(String token, String password) {
}
