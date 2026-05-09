package com.ticket.dto;

import com.ticket.entity.Ticket;
import com.ticket.entity.TicketReply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponse {
    private Ticket ticket;
    private List<TicketReply> replies;
    private Map<Long, UserBrief> userMap;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBrief {
        private Long id;
        private String name;
        private String role;
        private String department;
    }
}
