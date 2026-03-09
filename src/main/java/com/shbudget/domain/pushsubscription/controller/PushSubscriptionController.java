package com.shbudget.domain.pushsubscription.controller;

import com.shbudget.domain.pushsubscription.dto.request.PushSubscribeRequest;
import com.shbudget.domain.pushsubscription.dto.request.PushUnsubscribeRequest;
import com.shbudget.domain.pushsubscription.service.PushSubscriptionService;
import com.shbudget.global.auth.CurrentMemberId;
import com.shbudget.global.common.ApiResult;
import com.shbudget.global.common.ResponseStatus;
import com.shbudget.global.config.VapidConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Push", description = "푸시 알림 API")
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushSubscriptionController {

    private final PushSubscriptionService pushSubscriptionService;
    private final VapidConfig vapidConfig;

    @PostMapping("/subscribe")
    @Operation(summary = "푸시 구독 등록", description = "웹 푸시 알림 구독을 등록합니다.")
    public ResponseEntity<ApiResult<Void>> subscribe(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody PushSubscribeRequest request
    ) {
        pushSubscriptionService.subscribe(memberId, request);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, null));
    }

    @PostMapping("/unsubscribe")
    @Operation(summary = "푸시 구독 해제", description = "웹 푸시 알림 구독을 해제합니다.")
    public ResponseEntity<ApiResult<Void>> unsubscribe(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody PushUnsubscribeRequest request
    ) {
        pushSubscriptionService.unsubscribe(memberId, request);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, null));
    }

    @GetMapping("/vapid-key")
    @Operation(summary = "VAPID 공개키 조회", description = "푸시 구독에 필요한 VAPID 공개키를 반환합니다.")
    public ResponseEntity<ApiResult<String>> getVapidKey() {
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, vapidConfig.getPublicKey()));
    }
}
