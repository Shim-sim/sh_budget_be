package com.shbudget.domain.pushsubscription.repository;

import com.shbudget.domain.pushsubscription.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findAllByMemberIdIn(List<Long> memberIds);

    Optional<PushSubscription> findByMemberIdAndEndpoint(Long memberId, String endpoint);

    void deleteByMemberIdAndEndpoint(Long memberId, String endpoint);

    void deleteAllByMemberId(Long memberId);
}
