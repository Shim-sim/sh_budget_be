package com.shbudget.domain.pushsubscription.service;

import com.shbudget.domain.book.entity.BookMember;
import com.shbudget.domain.book.repository.BookMemberRepository;
import com.shbudget.domain.pushsubscription.entity.PushSubscription;
import com.shbudget.domain.pushsubscription.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final BookMemberRepository bookMemberRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final WebPushService webPushService;

    public void notifyBookMembers(Long bookId, Long actorMemberId, String title, String body) {
        List<Long> memberIds = bookMemberRepository.findAllByBookId(bookId).stream()
                .map(BookMember::getMemberId)
                .filter(id -> !id.equals(actorMemberId))
                .toList();

        if (memberIds.isEmpty()) {
            return;
        }

        List<PushSubscription> subscriptions = pushSubscriptionRepository.findAllByMemberIdIn(memberIds);
        for (PushSubscription subscription : subscriptions) {
            webPushService.sendPush(subscription, title, body);
        }
    }
}
