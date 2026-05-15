package com.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ticket.dto.PageResponse;
import com.ticket.dto.TicketCreateRequest;
import com.ticket.entity.Ticket;
import com.ticket.mapper.TicketMapper;
import com.ticket.service.TagService;
import com.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private TagService tagService;

    @Override
    public PageResponse<Ticket> getTicketList(String status, String priority, String keyword,
                                               Long creatorId, Long assigneeId, Long categoryId,
                                               String startDate, String endDate,
                                               Integer page, Integer size) {
        // 默认值处理
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        if (size > 50) size = 50;

        // 构建查询条件
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null && !status.isEmpty(), Ticket::getStatus, status)
               .eq(priority != null && !priority.isEmpty(), Ticket::getPriority, priority)
               .like(keyword != null && !keyword.isEmpty(), Ticket::getTitle, keyword)
               .eq(creatorId != null, Ticket::getCreatorId, creatorId)
               .eq(assigneeId != null, Ticket::getAssigneeId, assigneeId)
               .eq(categoryId != null, Ticket::getCategoryId, categoryId)
               .ge(startDate != null && !startDate.isEmpty(), Ticket::getCreatedAt, startDate + " 00:00:00")
               .le(endDate != null && !endDate.isEmpty(), Ticket::getCreatedAt, endDate + " 23:59:59")
               .orderByDesc(Ticket::getCreatedAt);

        // 执行分页查询
        Page<Ticket> pageParam = new Page<>(page, size);
        Page<Ticket> result = ticketMapper.selectPage(pageParam, wrapper);

        // 构建分页响应
        return PageResponse.<Ticket>builder()
                .records(result.getRecords())
                .total(result.getTotal())
                .page((int) result.getCurrent())
                .size((int) result.getSize())
                .pages((int) result.getPages())
                .hasNext(result.getCurrent() < result.getPages())
                .hasPrevious(result.getCurrent() > 1)
                .build();
    }

    @Override
    public List<Ticket> getTicketList(String status, String priority, String keyword) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null && !status.isEmpty(), Ticket::getStatus, status)
               .eq(priority != null && !priority.isEmpty(), Ticket::getPriority, priority)
               .like(keyword != null && !keyword.isEmpty(), Ticket::getTitle, keyword)
               .orderByDesc(Ticket::getCreatedAt);
        return ticketMapper.selectList(wrapper);
    }

    @Override
    public Ticket getTicketById(Long id) {
        return ticketMapper.selectById(id);
    }

    @Override
    public Ticket createTicket(TicketCreateRequest request, Long creatorId) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setStatus("pending");
        ticket.setCreatorId(creatorId);
        ticket.setAssigneeId(request.getAssigneeId());
        ticket.setCategoryId(request.getCategoryId());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticketMapper.insert(ticket);

        // 设置标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tagService.setTicketTags(ticket.getId(), request.getTagIds());
        }

        return ticket;
    }

    @Override
    public Ticket updateTicketStatus(Long id, String status, Long operatorId) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new RuntimeException("工单不存在");
        }
        
        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        if ("resolved".equals(status)) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if ("closed".equals(status)) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        
        ticketMapper.updateById(ticket);
        return ticket;
    }

    @Override
    public List<Ticket> getKanbanData() {
        return ticketMapper.selectList(
            new LambdaQueryWrapper<Ticket>()
                .in(Ticket::getStatus, Arrays.asList("pending", "processing", "resolved"))
                .orderByAsc(Ticket::getPriority)
                .orderByDesc(Ticket::getCreatedAt)
        );
    }

    @Override
    public Map<String, Object> getStatistics(Long userId, String role) {
        Map<String, Object> stats = new HashMap<>();
        
        // 状态分布
        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("pending", countByStatus("pending"));
        statusDistribution.put("processing", countByStatus("processing"));
        statusDistribution.put("resolved", countByStatus("resolved"));
        statusDistribution.put("closed", countByStatus("closed"));
        stats.put("statusDistribution", statusDistribution);
        
        // 核心指标
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("avgResponseTime", calculateAvgResponseTime());
        metrics.put("avgResolveTime", calculateAvgResolveTime());
        metrics.put("resolveRate", calculateResolveRate());
        metrics.put("overdueRate", calculateOverdueRate());
        stats.put("metrics", metrics);
        
        // 用户相关工单
        if (userId != null) {
            stats.put("myTickets", getTicketsByAssignee(userId));
            stats.put("createdTickets", getTicketsByCreator(userId));
        }
        
        // 负责人工作量统计
        stats.put("assigneeStats", getAssigneeStats());
        
        return stats;
    }

    @Override
    public List<Ticket> getUserTickets(Long userId, String type) {
        if ("assignee".equals(type)) {
            return getTicketsByAssignee(userId);
        } else {
            return getTicketsByCreator(userId);
        }
    }
    
    // 私有方法
    
    private Long countByStatus(String status) {
        return ticketMapper.selectCount(
            new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getStatus, status)
        );
    }
    
    private List<Ticket> getTicketsByAssignee(Long userId) {
        return ticketMapper.selectList(
            new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getAssigneeId, userId)
                .in(Ticket::getStatus, Arrays.asList("pending", "processing"))
                .orderByDesc(Ticket::getCreatedAt)
        );
    }
    
    private List<Ticket> getTicketsByCreator(Long userId) {
        return ticketMapper.selectList(
            new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getCreatorId, userId)
                .orderByDesc(Ticket::getCreatedAt)
        );
    }
    
    private Double calculateAvgResponseTime() {
        // 简化实现：返回模拟数据
        // 实际应该计算从创建到首次回复的平均时间
        return 45.0; // 分钟
    }
    
    private Double calculateAvgResolveTime() {
        // 简化实现：返回模拟数据
        return 8.0; // 小时
    }
    
    private Double calculateResolveRate() {
        Long total = ticketMapper.selectCount(null);
        if (total == 0) return 0.0;
        Long resolved = countByStatus("resolved") + countByStatus("closed");
        return (double) resolved / total * 100;
    }
    
    private Double calculateOverdueRate() {
        // 简化实现：返回模拟数据
        return 3.5; // 百分比
    }
    
    private List<Map<String, Object>> getAssigneeStats() {
        // 简化实现：返回模拟数据
        List<Map<String, Object>> stats = new ArrayList<>();
        
        Map<String, Object> stat1 = new HashMap<>();
        stat1.put("name", "张明");
        stat1.put("total", 15);
        stat1.put("resolved", 12);
        stat1.put("pending", 3);
        stats.add(stat1);
        
        Map<String, Object> stat2 = new HashMap<>();
        stat2.put("name", "李云");
        stat2.put("total", 10);
        stat2.put("resolved", 8);
        stat2.put("pending", 2);
        stats.add(stat2);
        
        return stats;
    }
}
