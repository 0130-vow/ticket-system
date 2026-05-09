package com.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ticket.dto.ReplyRequest;
import com.ticket.entity.TicketReply;
import com.ticket.mapper.TicketReplyMapper;
import com.ticket.service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReplyServiceImpl implements ReplyService {

    @Autowired
    private TicketReplyMapper replyMapper;

    @Override
    public List<TicketReply> getRepliesByTicketId(Long ticketId) {
        return replyMapper.selectList(
            new LambdaQueryWrapper<TicketReply>()
                .eq(TicketReply::getTicketId, ticketId)
                .orderByAsc(TicketReply::getCreatedAt)
        );
    }

    @Override
    public TicketReply addReply(Long ticketId, ReplyRequest request, Long authorId, String role) {
        TicketReply reply = new TicketReply();
        reply.setTicketId(ticketId);
        reply.setContent(request.getContent());
        reply.setAuthorId(authorId);
        reply.setCreatedAt(LocalDateTime.now());
        
        // 普通员工不能添加内部备注
        if ("employee".equals(role)) {
            reply.setIsInternal(false);
        } else {
            reply.setIsInternal(request.getIsInternal());
        }
        
        replyMapper.insert(reply);
        return reply;
    }
}
