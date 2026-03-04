package com.shbudget.domain.pushsubscription.service;

import com.shbudget.domain.pushsubscription.dto.request.PushSubscribeRequest;
import com.shbudget.domain.pushsubscription.dto.request.PushUnsubscribeRequest;
import com.shbudget.domain.pushsubscription.entity.PushSubscription;
import com.shbudget.domain.pushsubscription.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Transactional
    public void subscribe(Long memberId, PushSubscribeRequest request) {
        pushSubscriptionRepository.findByMemberIdAndEndpoint(memberId, request.endpoint())
                .ifPresentOrElse(
                        existing -> existing.updateKeys(request.p256dh(), request.auth()),
                        () -> pushSubscriptionRepository.save(
                                PushSubscription.builder()
                                        .memberId(memberId)
                                        .endpoint(request.endpoint())
                                        .p256dh(request.p256dh())
                                        .auth(request.auth())
                                        .build()
                        )
                );
    }

    @Transactional
    public void unsubscribe(Long memberId, PushUnsubscribeRequest request) {
        pushSubscriptionRepository.deleteByMemberIdAndEndpoint(memberId, request.endpoint());
    }
}
