package com.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ticket_tag")
public class Tag {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String color;

    private LocalDateTime createdAt;
}
