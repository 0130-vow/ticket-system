package com.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketCreateRequest {
    @NotBlank(message = "工单标题不能为空")
    private String title;
    
    @NotBlank(message = "问题描述不能为空")
    private String description;
    
    @NotBlank(message = "优先级不能为空")
    private String priority;
    
    @NotNull(message = "负责人不能为空")
    private Long assigneeId;
}
