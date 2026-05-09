package com.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyRequest {
    @NotBlank(message = "回复内容不能为空")
    private String content;
    
    private Boolean isInternal = false;
}
