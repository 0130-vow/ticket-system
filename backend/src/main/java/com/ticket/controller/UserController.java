package com.ticket.controller;

import com.ticket.entity.User;
import com.ticket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.getTechnicians();
        // 隐藏密码
        users.forEach(u -> u.setPassword(null));
        result.put("code", 200);
        result.put("data", users);
        return result;
    }
}
