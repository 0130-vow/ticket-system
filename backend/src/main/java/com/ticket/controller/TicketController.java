package com.ticket.controller;

import com.ticket.dto.ReplyRequest;
import com.ticket.dto.TicketCreateRequest;
import com.ticket.dto.TicketDetailResponse;
import com.ticket.entity.Ticket;
import com.ticket.entity.TicketReply;
import com.ticket.entity.User;
import com.ticket.service.ReplyService;
import com.ticket.service.TicketService;
import com.ticket.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin
public class TicketController {

    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private ReplyService replyService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        List<Ticket> tickets = ticketService.getTicketList(status, priority, keyword);
        result.put("code", 200);
        result.put("data", tickets);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Ticket ticket = ticketService.getTicketById(id);
        
        if (ticket == null) {
            result.put("code", 404);
            result.put("message", "工单不存在");
            return result;
        }
        
        // 获取相关用户信息
        Set<Long> userIds = new HashSet<>();
        userIds.add(ticket.getCreatorId());
        if (ticket.getAssigneeId() != null) {
            userIds.add(ticket.getAssigneeId());
        }
        
        // 构建用户信息映射
        Map<Long, TicketDetailResponse.UserBrief> userMap = new HashMap<>();
        for (Long userId : userIds) {
            User user = userService.findById(userId);
            if (user != null) {
                userMap.put(userId, TicketDetailResponse.UserBrief.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .role(user.getRole())
                        .department(user.getDepartment())
                        .build());
            }
        }
        
        result.put("code", 200);
        result.put("data", ticket);
        result.put("userMap", userMap);
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody TicketCreateRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Ticket ticket = ticketService.createTicket(request, userId);
            result.put("code", 200);
            result.put("message", "工单创建成功");
            result.put("data", ticket);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PatchMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String role = (String) httpRequest.getAttribute("role");
            String newStatus = body.get("status");
            
            // 权限验证
            if ("employee".equals(role) && !"closed".equals(newStatus)) {
                result.put("code", 403);
                result.put("message", "普通员工只能关闭工单");
                return result;
            }
            
            Ticket ticket = ticketService.updateTicketStatus(id, newStatus, userId);
            result.put("code", 200);
            result.put("message", "状态更新成功");
            result.put("data", ticket);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/{id}/replies")
    public Map<String, Object> getReplies(@PathVariable Long id, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        String role = (String) httpRequest.getAttribute("role");
        List<TicketReply> replies = replyService.getRepliesByTicketId(id);
        
        // 普通员工看不到内部备注
        if ("employee".equals(role)) {
            replies = replies.stream()
                    .filter(r -> !r.getIsInternal())
                    .collect(Collectors.toList());
        }
        
        // 获取回复用户信息
        Set<Long> authorIds = replies.stream()
                .map(TicketReply::getAuthorId)
                .collect(Collectors.toSet());
        
        Map<Long, TicketDetailResponse.UserBrief> userMap = new HashMap<>();
        for (Long authorId : authorIds) {
            User user = userService.findById(authorId);
            if (user != null) {
                userMap.put(authorId, TicketDetailResponse.UserBrief.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .role(user.getRole())
                        .department(user.getDepartment())
                        .build());
            }
        }
        
        result.put("code", 200);
        result.put("data", replies);
        result.put("userMap", userMap);
        return result;
    }

    @PostMapping("/{id}/replies")
    public Map<String, Object> addReply(@PathVariable Long id, @Valid @RequestBody ReplyRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String role = (String) httpRequest.getAttribute("role");
            TicketReply reply = replyService.addReply(id, request, userId, role);
            result.put("code", 200);
            result.put("message", "回复成功");
            result.put("data", reply);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/kanban")
    public Map<String, Object> kanban() {
        Map<String, Object> result = new HashMap<>();
        List<Ticket> tickets = ticketService.getKanbanData();
        result.put("code", 200);
        result.put("data", tickets);
        return result;
    }
}
