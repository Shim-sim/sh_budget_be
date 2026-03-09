package com.shbudget.domain.recurring.scheduler;

import com.shbudget.domain.member.entity.Member;
import com.shbudget.domain.member.repository.MemberRepository;
import com.shbudget.domain.pushsubscription.entity.PushSubscription;
import com.shbudget.domain.pushsubscription.repository.PushSubscriptionRepository;
import com.shbudget.domain.pushsubscription.service.WebPushService;
import com.shbudget.domain.recurring.entity.RecurringTransaction;
import com.shbudget.domain.recurring.repository.RecurringTransactionRepository;
import com.shbudget.domain.transaction.dto.request.TransactionCreateRequest;
import com.shbudget.domain.transaction.entity.TransactionType;
import com.shbudget.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final RecurringTransactionRepository recurringRepository;
    private final TransactionService transactionService;
    private final MemberRepository memberRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final WebPushService webPushService;

    /**
     * 매월 1일 21:00 실행 - 활성화된 모든 반복 거래를 해당 월 설정일로 자동 생성
     * 월말 처리: 설정일이 해당 월의 마지막 날보다 크면 마지막 날로 등록
     * (예: 31일 설정 → 2월은 28/29일, 4월은 30일로 등록)
     */
    @Scheduled(cron = "0 0 21 1 * *", zone = "Asia/Seoul")
    public void executeRecurringTransactions() {
        LocalDate today = LocalDate.now();
        int lastDayOfMonth = YearMonth.from(today).lengthOfMonth();

        // 활성화된 모든 반복 거래 조회
        List<RecurringTransaction> targets = recurringRepository.findAllByActiveTrue();

        log.info("[반복 거래] 실행 시작 - 월: {}, 대상: {}건", today.getMonth(), targets.size());

        for (RecurringTransaction recurring : targets) {
            try {
                // 설정일이 해당 월 마지막 날보다 크면 마지막 날로 조정
                int actualDay = Math.min(recurring.getDayOfMonth(), lastDayOfMonth);
                LocalDate transactionDate = today.withDayOfMonth(actualDay);

                TransactionCreateRequest request = new TransactionCreateRequest(
                        recurring.getBookId(),
                        recurring.getType(),
                        recurring.getAssetId(),
                        recurring.getCategoryId(),
                        recurring.getFromAssetId(),
                        recurring.getToAssetId(),
                        recurring.getAmount(),
                        transactionDate,
                        recurring.getMemo()
                );
                transactionService.createTransaction(recurring.getCreatedBy(), request);
                log.info("[반복 거래] 생성 완료 - id: {}, type: {}, amount: {}, date: {}",
                        recurring.getId(), recurring.getType(), recurring.getAmount(), transactionDate);
            } catch (Exception e) {
                log.error("[반복 거래] 생성 실패 - id: {}, error: {}", recurring.getId(), e.getMessage());
            }
        }
    }

    /**
     * 매일 21:00 실행 - 이틀 후 예정된 반복 거래에 대해 알림 발송
     * 월말 처리 동일 적용
     */
    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
    public void notifyUpcomingRecurringTransactions() {
        LocalDate twoDaysLater = LocalDate.now().plusDays(2);
        int targetDay = twoDaysLater.getDayOfMonth();
        int lastDayOfMonth = YearMonth.from(twoDaysLater).lengthOfMonth();

        List<RecurringTransaction> targets = new ArrayList<>(
                recurringRepository.findAllByDayOfMonthAndActiveTrue(targetDay)
        );

        // 월말 처리
        if (targetDay == lastDayOfMonth && lastDayOfMonth < 31) {
            List<RecurringTransaction> overflows =
                    recurringRepository.findAllByDayOfMonthGreaterThanEqualAndActiveTrue(lastDayOfMonth + 1);
            targets.addAll(overflows);
        }

        log.info("[반복 알림] 발송 시작 - 예정일: {}, 대상: {}건", twoDaysLater, targets.size());

        for (RecurringTransaction recurring : targets) {
            try {
                String typeLabel = switch (recurring.getType()) {
                    case INCOME -> "수입";
                    case EXPENSE -> "지출";
                    case TRANSFER -> "이체";
                };

                String memoText = (recurring.getMemo() != null && !recurring.getMemo().isBlank())
                        ? recurring.getMemo() + " "
                        : "";

                String body = String.format("2일 후 %s%s %,d원 예정입니다. 앱을 확인해주세요!",
                        memoText, typeLabel, recurring.getAmount());

                // 등록자 본인에게 알림
                List<PushSubscription> subscriptions =
                        pushSubscriptionRepository.findAllByMemberIdIn(List.of(recurring.getCreatedBy()));
                for (PushSubscription subscription : subscriptions) {
                    webPushService.sendPush(subscription, "반복 거래 알림", body);
                }

                log.info("[반복 알림] 발송 완료 - id: {}, memberId: {}", recurring.getId(), recurring.getCreatedBy());
            } catch (Exception e) {
                log.error("[반복 알림] 발송 실패 - id: {}, error: {}", recurring.getId(), e.getMessage());
            }
        }
    }
}
