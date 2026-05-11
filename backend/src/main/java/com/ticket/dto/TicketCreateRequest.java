package com.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TicketCreateRequest {

    @NotBlank(message = "工单标题不能为空")
    @Size(min = 2, max = 50, message = "标题长度必须在2-50个字符之间")
    @Pattern(regexp = "^[^<>\"'&]*$", message = "标题包含非法字符")
    private String title;

    @NotBlank(message = "问题描述不能为空")
    @Size(min = 10, max = 2000, message = "问题描述长度必须在10-2000个字符之间")
    private String description;

    @NotBlank(message = "优先级不能为空")
    @Pattern(regexp = "^(P0|P1|P2|P3)$", message = "优先级必须是 P0、P1、P2 或 P3")
    private String priority;

    @NotNull(message = "负责人不能为空")
    private Long assigneeId;
}
