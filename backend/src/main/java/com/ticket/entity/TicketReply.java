package com.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ticket_reply")
public class TicketReply {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long ticketId;
    
    private String content;
    
    private Long authorId;
    
    private Boolean isInternal;
    
    private LocalDateTime createdAt;
}
