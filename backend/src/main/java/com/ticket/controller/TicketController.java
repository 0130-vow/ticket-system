package com.ticket.controller;

import com.ticket.dto.ReplyRequest;
import com.ticket.dto.TicketCreateRequest;
import com.ticket.entity.Ticket;
import com.ticket.entity.TicketReply;
import com.ticket.service.ReplyService;
import com.ticket.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin
public class TicketController {

    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private ReplyService replyService;

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
        if (ticket != null) {
            result.put("code", 200);
            result.put("data", ticket);
        } else {
            result.put("code", 404);
            result.put("message", "工单不存在");
        }
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
                    .toList();
        }
        
        result.put("code", 200);
        result.put("data", replies);
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
