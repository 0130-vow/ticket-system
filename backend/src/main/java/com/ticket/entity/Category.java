package com.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ticket_category")
public class Category {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private Long parentId;

    private Integer level;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createdAt;
}
