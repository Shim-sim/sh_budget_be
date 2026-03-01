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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AssetRepository assetRepository;
    private final BookMemberRepository bookMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TransactionResponse createTransaction(Long memberId, TransactionCreateRequest request) {
        // 공통 검증
        validateBookMember(request.bookId(), memberId);
        validateAmount(request.amount());
        validateDate(request.date());

        // 타입별 생성 로직
        return switch (request.type()) {
            case INCOME -> createIncome(memberId, request);
            case EXPENSE -> createExpense(memberId, request);
            case TRANSFER -> createTransfer(memberId, request);
        };
    }

    private TransactionResponse createIncome(Long memberId, TransactionCreateRequest request) {
        // 검증
        if (request.assetId() == null) {
            throw new CustomException(ErrorCode.ASSET_REQUIRED_FOR_INCOME_EXPENSE);
        }

        Asset asset = findAssetByIdAndBookId(request.assetId(), request.bookId());

        // Transaction 생성
        Transaction transaction = Transaction.createIncome(
                request.bookId(),
                request.assetId(),
                request.categoryId(),
                request.amount(),
                request.date(),
                request.memo(),
                memberId
        );
        transactionRepository.save(transaction);

        // 자산 잔액 증가
        asset.increaseBalance(request.amount());

        return buildTransactionResponse(transaction);
    }

    private TransactionResponse createExpense(Long memberId, TransactionCreateRequest request) {
        // 검증
        if (request.assetId() == null) {
            throw new CustomException(ErrorCode.ASSET_REQUIRED_FOR_INCOME_EXPENSE);
        }

        Asset asset = findAssetByIdAndBookId(request.assetId(), request.bookId());

        // 잔액 부족 체크
        if (asset.getBalance() < request.amount()) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // Transaction 생성
        Transaction transaction = Transaction.createExpense(
                request.bookId(),
                request.assetId(),
                request.categoryId(),
                request.amount(),
                request.date(),
                request.memo(),
                memberId
        );
        transactionRepository.save(transaction);

        // 자산 잔액 감소
        asset.decreaseBalance(request.amount());

        return buildTransactionResponse(transaction);
    }

    private TransactionResponse createTransfer(Long memberId, TransactionCreateRequest request) {
        // 검증
        if (request.fromAssetId() == null || request.toAssetId() == null) {
            throw new CustomException(ErrorCode.ASSETS_REQUIRED_FOR_TRANSFER);
        }

        if (request.fromAssetId().equals(request.toAssetId())) {
            throw new CustomException(ErrorCode.SAME_ASSET_TRANSFER);
        }

        Asset fromAsset = findAssetByIdAndBookId(request.fromAssetId(), request.bookId());
        Asset toAsset = findAssetByIdAndBookId(request.toAssetId(), request.bookId());

        // 출발 자산 잔액 체크
        if (fromAsset.getBalance() < request.amount()) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // Transaction 생성
        Transaction transaction = Transaction.createTransfer(
                request.bookId(),
                request.fromAssetId(),
                request.toAssetId(),
                request.amount(),
                request.date(),
                request.memo(),
                memberId
        );
        transactionRepository.save(transaction);

        // 자산 잔액 이체
        fromAsset.decreaseBalance(request.amount());
        toAsset.increaseBalance(request.amount());

        return buildTransferResponse(transaction);
    }

    public List<TransactionResponse> getTransactionList(Long memberId, Long bookId, String month, TransactionType type) {
        validateBookMember(bookId, memberId);

        List<Transaction> transactions;

        if (month != null && !month.isBlank()) {
            // 월별 조회
            String[] parts = month.split("-");
            int year = Integer.parseInt(parts[0]);
            int monthValue = Integer.parseInt(parts[1]);

            if (type != null) {
                transactions = transactionRepository.findAllByBookIdAndTypeAndYearMonth(bookId, type, year, monthValue);
            } else {
                transactions = transactionRepository.findAllByBookIdAndYearMonth(bookId, year, monthValue);
            }
        } else if (type != null) {
            // 타입별 전체 조회
            transactions = transactionRepository.findAllByBookIdAndTypeOrderByDateDescCreatedAtDesc(bookId, type);
        } else {
            // 전체 조회
            transactions = transactionRepository.findAllByBookIdOrderByDateDescCreatedAtDesc(bookId);
        }

        return transactions.stream()
                .map(this::buildTransactionResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(Long memberId, Long bookId, Long transactionId) {
        validateBookMember(bookId, memberId);

        Transaction transaction = transactionRepository.findByIdAndBookId(transactionId, bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        return buildTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long memberId, Long bookId, Long transactionId,
                                                   TransactionUpdateRequest request) {
        validateBookMember(bookId, memberId);

        Transaction transaction = transactionRepository.findByIdAndBookId(transactionId, bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        // 금액 검증
        if (request.amount() != null) {
            validateAmount(request.amount());
        }

        // 날짜 검증
        if (request.date() != null) {
            validateDate(request.date());
        }

        // 기존 자산 잔액 복구
        restoreBalance(transaction);

        // 거래 정보 업데이트
        if (transaction.getType() == TransactionType.TRANSFER) {
            transaction.updateTransfer(
                    request.fromAssetId(),
                    request.toAssetId(),
                    request.amount(),
                    request.date(),
                    request.memo()
            );
        } else {
            transaction.updateTransaction(
                    request.assetId(),
                    request.categoryId(),
                    request.amount(),
                    request.date(),
                    request.memo()
            );
        }

        // 새로운 자산 잔액 적용
        applyBalance(transaction);

        return buildTransactionResponse(transaction);
    }

    @Transactional
    public void deleteTransaction(Long memberId, Long bookId, Long transactionId) {
        validateBookMember(bookId, memberId);

        Transaction transaction = transactionRepository.findByIdAndBookId(transactionId, bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        // 자산 잔액 복구
        restoreBalance(transaction);

        transactionRepository.delete(transaction);
    }

    // === Private Helper Methods ===

    private void validateBookMember(Long bookId, Long memberId) {
        if (!bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)) {
            throw new CustomException(ErrorCode.NOT_BOOK_MEMBER);
        }
    }

    private void validateAmount(Long amount) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_AMOUNT);
        }
    }

    private void validateDate(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new CustomException(ErrorCode.FUTURE_DATE_NOT_ALLOWED);
        }
    }

    private Asset findAssetByIdAndBookId(Long assetId, Long bookId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSET_NOT_FOUND));

        if (!asset.getBookId().equals(bookId)) {
            throw new CustomException(ErrorCode.ASSET_NOT_FOUND);
        }

        return asset;
    }

    private void restoreBalance(Transaction transaction) {
        switch (transaction.getType()) {
            case INCOME -> {
                Asset asset = assetRepository.findById(transaction.getAssetId()).orElseThrow();
                asset.decreaseBalance(transaction.getAmount());
            }
            case EXPENSE -> {
                Asset asset = assetRepository.findById(transaction.getAssetId()).orElseThrow();
                asset.increaseBalance(transaction.getAmount());
            }
            case TRANSFER -> {
                Asset fromAsset = assetRepository.findById(transaction.getFromAssetId()).orElseThrow();
                Asset toAsset = assetRepository.findById(transaction.getToAssetId()).orElseThrow();
                fromAsset.increaseBalance(transaction.getAmount());
                toAsset.decreaseBalance(transaction.getAmount());
            }
        }
    }

    private void applyBalance(Transaction transaction) {
        switch (transaction.getType()) {
            case INCOME -> {
                Asset asset = findAssetByIdAndBookId(transaction.getAssetId(), transaction.getBookId());
                asset.increaseBalance(transaction.getAmount());
            }
            case EXPENSE -> {
                Asset asset = findAssetByIdAndBookId(transaction.getAssetId(), transaction.getBookId());
                if (asset.getBalance() < transaction.getAmount()) {
                    throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
                }
                asset.decreaseBalance(transaction.getAmount());
            }
            case TRANSFER -> {
                Asset fromAsset = findAssetByIdAndBookId(transaction.getFromAssetId(), transaction.getBookId());
                Asset toAsset = findAssetByIdAndBookId(transaction.getToAssetId(), transaction.getBookId());

                if (fromAsset.getBalance() < transaction.getAmount()) {
                    throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
                }

                fromAsset.decreaseBalance(transaction.getAmount());
                toAsset.increaseBalance(transaction.getAmount());
            }
        }
    }

    private TransactionResponse buildTransactionResponse(Transaction transaction) {
        if (transaction.getType() == TransactionType.TRANSFER) {
            return buildTransferResponse(transaction);
        }

        String assetName = transaction.getAssetId() != null
                ? assetRepository.findById(transaction.getAssetId()).map(Asset::getName).orElse(null)
                : null;

        String createdByNickname = memberRepository.findById(transaction.getCreatedBy())
                .map(Member::getNickname)
                .orElse(null);

        return TransactionResponse.from(transaction, assetName, null, createdByNickname);
    }

    private TransactionResponse buildTransferResponse(Transaction transaction) {
        String fromAssetName = transaction.getFromAssetId() != null
                ? assetRepository.findById(transaction.getFromAssetId()).map(Asset::getName).orElse(null)
                : null;

        String toAssetName = transaction.getToAssetId() != null
                ? assetRepository.findById(transaction.getToAssetId()).map(Asset::getName).orElse(null)
                : null;

        String createdByNickname = memberRepository.findById(transaction.getCreatedBy())
                .map(Member::getNickname)
                .orElse(null);

        return TransactionResponse.fromTransfer(transaction, fromAssetName, toAssetName, createdByNickname);
    }
}
