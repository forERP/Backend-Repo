package com.forerp.erp.realtime.controller;

import com.forerp.erp.realtime.service.RealtimeEventService;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "실시간 이벤트", description = "SSE 실시간 이벤트 스트림")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RealtimeEventController {

    private final RealtimeEventService realtimeEventService;

    @Operation(summary = "실시간 이벤트 스트림 구독", description = "Server-Sent Events (SSE) 스트림을 구독합니다.")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "매장 ID (선택, 미입력 시 인증 사용자의 매장 기준)") @RequestParam(required = false) Long storeId
    ) {
        Long actorStoreId = actor != null && actor.getStore() != null ? actor.getStore().getId() : null;
        Long resolvedStoreId = storeId == null ? actorStoreId : storeId;
        return realtimeEventService.subscribe(resolvedStoreId);
    }
}