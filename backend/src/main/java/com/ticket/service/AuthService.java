package com.ticket.service;

import com.ticket.dto.LoginRequest;
import com.ticket.dto.LoginResponse;

public interface AuthService {
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 验证 Token
     */
    boolean validateToken(String token);
    
    /**
     * 从 Token 获取用户信息
     */
    LoginResponse.UserInfo getUserFromToken(String token);
}
