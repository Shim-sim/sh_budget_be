package com.shbudget.domain.recurring.repository;

import com.shbudget.domain.recurring.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findAllByActiveTrue();

    List<RecurringTransaction> findAllByBookIdAndActiveTrue(Long bookId);

    List<RecurringTransaction> findAllByDayOfMonthAndActiveTrue(Integer dayOfMonth);

    /** 월말 처리: dayOfMonth가 targetDay 이상인 활성 반복 거래 조회 (31일 설정인데 해당 월이 28/30일인 경우) */
    List<RecurringTransaction> findAllByDayOfMonthGreaterThanEqualAndActiveTrue(Integer dayOfMonth);
}
