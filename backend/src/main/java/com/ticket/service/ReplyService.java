package com.ticket.service;

import com.ticket.dto.ReplyRequest;
import com.ticket.entity.TicketReply;

import java.util.List;

public interface ReplyService {
    
    /**
     * 获取工单回复列表
     */
    List<TicketReply> getRepliesByTicketId(Long ticketId);
    
    /**
     * 添加回复
     */
    TicketReply addReply(Long ticketId, ReplyRequest request, Long authorId, String role);
}
