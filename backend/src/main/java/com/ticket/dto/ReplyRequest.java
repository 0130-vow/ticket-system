package com.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReplyRequest {

    @NotBlank(message = "回复内容不能为空")
    @Size(min = 1, max = 2000, message = "回复内容长度不能超过2000个字符")
    private String content;

    private Boolean isInternal = false;
}
