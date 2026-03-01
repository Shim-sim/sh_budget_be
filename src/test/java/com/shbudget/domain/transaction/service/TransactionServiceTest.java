package com.shbudget.domain.transaction.service;

import com.shbudget.domain.asset.entity.Asset;
import com.shbudget.domain.asset.repository.AssetRepository;
import com.shbudget.domain.book.repository.BookMemberRepository;
import com.shbudget.domain.member.entity.Member;
import com.shbudget.domain.member.repository.MemberRepository;
import com.shbudget.domain.transaction.dto.request.TransactionCreateRequest;
import com.shbudget.domain.transaction.dto.request.TransactionUpdateRequest;
import com.shbudget.domain.transaction.dto.response.TransactionResponse;
import com.shbudget.domain.transaction.entity.Transaction;
import com.shbudget.domain.transaction.entity.TransactionType;
import com.shbudget.domain.transaction.repository.TransactionRepository;
import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private BookMemberRepository bookMemberRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("수입 거래 생성 성공")
    void createIncome() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long assetId = 1L;
        TransactionCreateRequest request = new TransactionCreateRequest(
                bookId,
                TransactionType.INCOME,
                assetId,
                null,
                null,
                null,
                3000000L,
                LocalDate.now(),
                "월급"
        );

        Asset asset = Asset.builder()
                .id(assetId)
                .bookId(bookId)
                .name("월급통장")
                .balance(1000000L)
                .build();

        Transaction transaction = Transaction.createIncome(
                bookId, assetId, null, 3000000L, LocalDate.now(), "월급", memberId
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(
                Member.create("test@test.com", "테스터")
        ));

        // when
        TransactionResponse response = transactionService.createTransaction(memberId, request);

        // then
        assertThat(response.type()).isEqualTo(TransactionType.INCOME);
        assertThat(response.amount()).isEqualTo(3000000L);
        assertThat(asset.getBalance()).isEqualTo(4000000L);  // 1000000 + 3000000
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("지출 거래 생성 성공")
    void createExpense() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long assetId = 1L;
        TransactionCreateRequest request = new TransactionCreateRequest(
                bookId,
                TransactionType.EXPENSE,
                assetId,
                null,
                null,
                null,
                50000L,
                LocalDate.now(),
                "식비"
        );

        Asset asset = Asset.builder()
                .id(assetId)
                .bookId(bookId)
                .name("생활비통장")
                .balance(1000000L)
                .build();

        Transaction transaction = Transaction.createExpense(
                bookId, assetId, null, 50000L, LocalDate.now(), "식비", memberId
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(
                Member.create("test@test.com", "테스터")
        ));

        // when
        TransactionResponse response = transactionService.createTransaction(memberId, request);

        // then
        assertThat(response.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(response.amount()).isEqualTo(50000L);
        assertThat(asset.getBalance()).isEqualTo(950000L);  // 1000000 - 50000
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("이체 거래 생성 성공")
    void createTransfer() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long fromAssetId = 1L;
        Long toAssetId = 2L;
        TransactionCreateRequest request = new TransactionCreateRequest(
                bookId,
                TransactionType.TRANSFER,
                null,
                null,
                fromAssetId,
                toAssetId,
                100000L,
                LocalDate.now(),
                "적금 이체"
        );

        Asset fromAsset = Asset.builder()
                .id(fromAssetId)
                .bookId(bookId)
                .name("현금")
                .balance(500000L)
                .build();

        Asset toAsset = Asset.builder()
                .id(toAssetId)
                .bookId(bookId)
                .name("적금")
                .balance(1000000L)
                .build();

        Transaction transaction = Transaction.createTransfer(
                bookId, fromAssetId, toAssetId, 100000L, LocalDate.now(), "적금 이체", memberId
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(assetRepository.findById(fromAssetId)).thenReturn(Optional.of(fromAsset));
        when(assetRepository.findById(toAssetId)).thenReturn(Optional.of(toAsset));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(
                Member.create("test@test.com", "테스터")
        ));

        // when
        TransactionResponse response = transactionService.createTransaction(memberId, request);

        // then
        assertThat(response.type()).isEqualTo(TransactionType.TRANSFER);
        assertThat(response.amount()).isEqualTo(100000L);
        assertThat(fromAsset.getBalance()).isEqualTo(400000L);  // 500000 - 100000
        assertThat(toAsset.getBalance()).isEqualTo(1100000L);  // 1000000 + 100000
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("잔액 부족으로 지출 거래 생성 실패")
    void createExpense_InsufficientBalance() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long assetId = 1L;
        TransactionCreateRequest request = new TransactionCreateRequest(
                bookId,
                TransactionType.EXPENSE,
                assetId,
                null,
                null,
                null,
                2000000L,  // 잔액보다 큰 금액
                LocalDate.now(),
                "지출"
        );

        Asset asset = Asset.builder()
                .id(assetId)
                .bookId(bookId)
                .name("통장")
                .balance(1000000L)
                .build();

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        // when & then
        assertThatThrownBy(() -> transactionService.createTransaction(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_BALANCE);
    }

    @Test
    @DisplayName("같은 자산으로 이체 시도 시 실패")
    void createTransfer_SameAsset() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long assetId = 1L;
        TransactionCreateRequest request = new TransactionCreateRequest(
                bookId,
                TransactionType.TRANSFER,
                null,
                null,
                assetId,
                assetId,  // 같은 자산
                100000L,
                LocalDate.now(),
                "이체"
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> transactionService.createTransaction(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SAME_ASSET_TRANSFER);
    }

    @Test
    @DisplayName("미래 날짜 거래 생성 시도 시 실패")
    void createTransaction_FutureDate() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long assetId = 1L;
        TransactionCreateRequest request = new TransactionCreateRequest(
                bookId,
                TransactionType.INCOME,
                assetId,
                null,
                null,
                null,
                100000L,
                LocalDate.now().plusDays(1),  // 미래 날짜
                "미래 수입"
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> transactionService.createTransaction(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FUTURE_DATE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("거래 삭제 시 자산 잔액 복구")
    void deleteTransaction() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long transactionId = 1L;
        Long assetId = 1L;

        Asset asset = Asset.builder()
                .id(assetId)
                .bookId(bookId)
                .name("통장")
                .balance(900000L)  // 지출 후 잔액
                .build();

        Transaction transaction = Transaction.createExpense(
                bookId, assetId, null, 100000L, LocalDate.now(), "지출", memberId
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(transactionRepository.findByIdAndBookId(transactionId, bookId))
                .thenReturn(Optional.of(transaction));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        // when
        transactionService.deleteTransaction(memberId, bookId, transactionId);

        // then
        assertThat(asset.getBalance()).isEqualTo(1000000L);  // 900000 + 100000 복구
        verify(transactionRepository).delete(transaction);
    }

    @Test
    @DisplayName("가계부 멤버가 아닌 경우 거래 생성 실패")
    void createTransaction_NotBookMember() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        TransactionCreateRequest request = new TransactionCreateRequest(
                bookId,
                TransactionType.INCOME,
                1L,
                null,
                null,
                null,
                100000L,
                LocalDate.now(),
                "수입"
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> transactionService.createTransaction(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_MEMBER);
    }

    @Test
    @DisplayName("자산을 찾을 수 없는 경우 거래 생성 실패")
    void createTransaction_AssetNotFound() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long assetId = 999L;
        TransactionCreateRequest request = new TransactionCreateRequest(
                bookId,
                TransactionType.INCOME,
                assetId,
                null,
                null,
                null,
                100000L,
                LocalDate.now(),
                "수입"
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> transactionService.createTransaction(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSET_NOT_FOUND);
    }

    @Test
    @DisplayName("수입/지출 거래에서 자산 ID 누락 시 실패")
    void createTransaction_AssetIdRequired() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        TransactionCreateRequest request = new TransactionCreateRequest(
                bookId,
                TransactionType.INCOME,
                null,  // assetId 누락
                null,
                null,
                null,
                100000L,
                LocalDate.now(),
                "수입"
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> transactionService.createTransaction(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSET_REQUIRED_FOR_INCOME_EXPENSE);
    }

    @Test
    @DisplayName("이체 거래에서 자산 ID 누락 시 실패")
    void createTransfer_AssetsRequired() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        TransactionCreateRequest request = new TransactionCreateRequest(
                bookId,
                TransactionType.TRANSFER,
                null,
                null,
                null,  // fromAssetId 누락
                2L,
                100000L,
                LocalDate.now(),
                "이체"
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> transactionService.createTransaction(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSETS_REQUIRED_FOR_TRANSFER);
    }
}
