package com.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ticket")
public class Ticket {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String title;
    
    private String description;
    
    private String priority;  // P0, P1, P2, P3
    
    private String status;    // pending, processing, resolved, closed
    
    private Long creatorId;
    
    private Long assigneeId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime resolvedAt;
    
    private LocalDateTime closedAt;
}
