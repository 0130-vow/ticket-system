package com.ticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ticket.entity.Ticket;
import com.ticket.entity.TicketReply;
import com.ticket.mapper.TicketMapper;
import com.ticket.mapper.TicketReplyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin
public class TicketController {
    
    @Autowired
    private TicketMapper ticketMapper;
    
    @Autowired
    private TicketReplyMapper replyMapper;
    
    @GetMapping
    public List<Ticket> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword) {
        
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Ticket::getStatus, status)
               .eq(priority != null, Ticket::getPriority, priority)
               .like(keyword != null, Ticket::getTitle, keyword)
               .orderByDesc(Ticket::getCreatedAt);
        
        return ticketMapper.selectList(wrapper);
    }
    
    @GetMapping("/{id}")
    public Ticket detail(@PathVariable Long id) {
        return ticketMapper.selectById(id);
    }
    
    @PostMapping
    public Ticket create(@RequestBody Ticket ticket) {
        ticket.setStatus("pending");
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketMapper.insert(ticket);
        return ticket;
    }
    
    @PatchMapping("/{id}/status")
    public Ticket updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Ticket ticket = ticketMapper.selectById(id);
        String newStatus = body.get("status");
        
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        if ("resolved".equals(newStatus)) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if ("closed".equals(newStatus)) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        
        ticketMapper.updateById(ticket);
        return ticket;
    }
    
    @GetMapping("/{id}/replies")
    public List<TicketReply> getReplies(@PathVariable Long id) {
        return replyMapper.selectList(
            new LambdaQueryWrapper<TicketReply>()
                .eq(TicketReply::getTicketId, id)
                .orderByAsc(TicketReply::getCreatedAt)
        );
    }
    
    @PostMapping("/{id}/replies")
    public TicketReply addReply(@PathVariable Long id, @RequestBody TicketReply reply) {
        reply.setTicketId(id);
        reply.setCreatedAt(LocalDateTime.now());
        replyMapper.insert(reply);
        return reply;
    }
    
    @GetMapping("/kanban")
    public List<Ticket> kanban() {
        return ticketMapper.selectList(
            new LambdaQueryWrapper<Ticket>()
                .in(Ticket::getStatus, "pending", "processing", "resolved")
                .orderByDesc(Ticket::getCreatedAt)
        );
    }
}
