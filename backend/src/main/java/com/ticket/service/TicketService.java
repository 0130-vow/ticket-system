package com.ticket.service;

import com.ticket.dto.TicketCreateRequest;
import com.ticket.entity.Ticket;

import java.util.List;
import java.util.Map;

public interface TicketService {
    
    /**
     * 获取工单列表
     */
    List<Ticket> getTicketList(String status, String priority, String keyword);
    
    /**
     * 获取工单详情
     */
    Ticket getTicketById(Long id);
    
    /**
     * 创建工单
     */
    Ticket createTicket(TicketCreateRequest request, Long creatorId);
    
    /**
     * 更新工单状态
     */
    Ticket updateTicketStatus(Long id, String status, Long operatorId);
    
    /**
     * 获取看板数据
     */
    List<Ticket> getKanbanData();
    
    /**
     * 获取统计数据
     */
    Map<String, Object> getStatistics(Long userId, String role);
    
    /**
     * 获取用户的工单
     */
    List<Ticket> getUserTickets(Long userId, String type);
}
