package com.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ticket.dto.PageResponse;
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
    public PageResponse<TicketReply> getRepliesByTicketId(Long ticketId, Integer page, Integer size) {
        // 默认值处理
        int currentPage = (page != null && page > 0) ? page : 1;
        int pageSize = (size != null && size > 0) ? size : 20;

        // 构建分页对象
        Page<TicketReply> pageParam = new Page<>(currentPage, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<TicketReply> wrapper = new LambdaQueryWrapper<TicketReply>()
                .eq(TicketReply::getTicketId, ticketId)
                .orderByAsc(TicketReply::getCreatedAt);

        // 执行分页查询
        Page<TicketReply> result = replyMapper.selectPage(pageParam, wrapper);

        // 构建返回结果
        return PageResponse.<TicketReply>builder()
                .records(result.getRecords())
                .total(result.getTotal())
                .page(currentPage)
                .size(pageSize)
                .pages((int) result.getPages())
                .hasNext(currentPage < result.getPages())
                .hasPrevious(currentPage > 1)
                .build();
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
