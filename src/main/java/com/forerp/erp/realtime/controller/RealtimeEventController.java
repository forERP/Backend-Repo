package com.forerp.erp.realtime.controller;

import com.forerp.erp.realtime.service.RealtimeEventService;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Realtime Events", description = "SSE 실시간 이벤트 스트림")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class RealtimeEventController {

    private final RealtimeEventService realtimeEventService;

    @Operation(summary = "실시간 이벤트 스트림 구독")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @AuthenticationPrincipal User actor,
            @RequestParam(required = false) Long storeId
    ) {
        Long actorStoreId = actor != null && actor.getStore() != null ? actor.getStore().getId() : null;
        Long resolvedStoreId = storeId == null ? actorStoreId : storeId;
        return realtimeEventService.subscribe(resolvedStoreId);
    }
}

