package com.ticket.service;

import com.ticket.entity.User;

import java.util.List;

public interface UserService {
    
    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);
    
    /**
     * 根据ID查找用户
     */
    User findById(Long id);
    
    /**
     * 获取所有用户列表
     */
    List<User> getAllUsers();
    
    /**
     * 获取技术负责人列表
     */
    List<User> getTechnicians();
}
