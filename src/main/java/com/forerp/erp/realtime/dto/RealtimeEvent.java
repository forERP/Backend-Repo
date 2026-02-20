package com.forerp.erp.realtime.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class RealtimeEvent {
    private String type;
    private Long storeId;
    private LocalDateTime occurredAt;
    private Map<String, Object> payload;
}

