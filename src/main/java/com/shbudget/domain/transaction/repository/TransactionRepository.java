package com.shbudget.domain.transaction.repository;

import com.shbudget.domain.transaction.entity.Transaction;
import com.shbudget.domain.transaction.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 가계부별 거래 목록 조회
    List<Transaction> findAllByBookIdOrderByDateDescCreatedAtDesc(Long bookId);

    // 가계부 + 상세 조회
    Optional<Transaction> findByIdAndBookId(Long id, Long bookId);

    // 가계부 + 타입별 조회
    List<Transaction> findAllByBookIdAndTypeOrderByDateDescCreatedAtDesc(Long bookId, TransactionType type);

    // 가계부 + 월별 조회
    @Query("SELECT t FROM Transaction t WHERE t.bookId = :bookId " +
            "AND YEAR(t.date) = :year AND MONTH(t.date) = :month " +
            "ORDER BY t.date DESC, t.createdAt DESC")
    List<Transaction> findAllByBookIdAndYearMonth(
            @Param("bookId") Long bookId,
            @Param("year") int year,
            @Param("month") int month
    );

    // 가계부 + 월별 + 타입별 조회
    @Query("SELECT t FROM Transaction t WHERE t.bookId = :bookId " +
            "AND t.type = :type " +
            "AND YEAR(t.date) = :year AND MONTH(t.date) = :month " +
            "ORDER BY t.date DESC, t.createdAt DESC")
    List<Transaction> findAllByBookIdAndTypeAndYearMonth(
            @Param("bookId") Long bookId,
            @Param("type") TransactionType type,
            @Param("year") int year,
            @Param("month") int month
    );

    // 자산별 거래 존재 여부 (자산 삭제 검증용)
    boolean existsByAssetId(Long assetId);

    boolean existsByFromAssetId(Long fromAssetId);

    boolean existsByToAssetId(Long toAssetId);

    // 카테고리별 거래 존재 여부 (카테고리 삭제 검증용)
    boolean existsByCategoryId(Long categoryId);

    // 기간별 타입별 합계 (통계용)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.bookId = :bookId AND t.type = :type " +
            "AND t.date BETWEEN :startDate AND :endDate")
    Long sumAmountByBookIdAndTypeAndDateBetween(
            @Param("bookId") Long bookId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 카테고리별 지출 통계 (기간별)
    @Query("SELECT t.categoryId, COALESCE(SUM(t.amount), 0), COUNT(t) FROM Transaction t " +
            "WHERE t.bookId = :bookId AND t.type = :type " +
            "AND t.categoryId IS NOT NULL " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY t.categoryId")
    List<Object[]> sumAmountByCategoryAndDateBetween(
            @Param("bookId") Long bookId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 멤버별 타입별 합계 (기간별)
    @Query("SELECT t.createdBy, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.bookId = :bookId AND t.type = :type " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY t.createdBy")
    List<Object[]> sumAmountByMemberAndTypeAndDateBetween(
            @Param("bookId") Long bookId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
