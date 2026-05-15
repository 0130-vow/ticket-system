package com.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("ticket_tag_relation")
public class TicketTagRelation {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long ticketId;

    private Long tagId;
}
