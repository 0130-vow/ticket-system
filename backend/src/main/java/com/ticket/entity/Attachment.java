package com.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ticket_attachment")
public class Attachment {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long ticketId;

    private Long replyId;

    private String originalName;

    private String storedName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private Long uploaderId;

    private LocalDateTime createdAt;
}
