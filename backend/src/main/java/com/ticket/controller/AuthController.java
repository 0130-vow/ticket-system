package com.ticket.controller;

import com.ticket.dto.LoginRequest;
import com.ticket.dto.LoginResponse;
import com.ticket.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            LoginResponse response = authService.login(request);
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", response);
        } catch (Exception e) {
            result.put("code", 401);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/validate")
    public Map<String, Object> validateToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> result = new HashMap<>();
        try {
            String token = authHeader.substring(7);
            boolean valid = authService.validateToken(token);
            if (valid) {
                LoginResponse.UserInfo userInfo = authService.getUserFromToken(token);
                result.put("code", 200);
                result.put("data", userInfo);
            } else {
                result.put("code", 401);
                result.put("message", "Token无效");
            }
        } catch (Exception e) {
            result.put("code", 401);
            result.put("message", "Token验证失败");
        }
        return result;
    }
}
