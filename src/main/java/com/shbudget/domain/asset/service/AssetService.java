package com.shbudget.domain.asset.service;

import com.shbudget.domain.asset.dto.AssetCreateRequest;
import com.shbudget.domain.asset.dto.AssetResponse;
import com.shbudget.domain.asset.dto.AssetSummaryResponse;
import com.shbudget.domain.asset.dto.AssetUpdateRequest;
import com.shbudget.domain.asset.entity.Asset;
import com.shbudget.domain.asset.repository.AssetRepository;
import com.shbudget.domain.book.repository.BookMemberRepository;
import com.shbudget.domain.member.entity.Member;
import com.shbudget.domain.member.repository.MemberRepository;
import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;
    private final BookMemberRepository bookMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public AssetResponse createAsset(Long memberId, Long bookId, AssetCreateRequest request) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        // ownerMemberId 검증 (있는 경우)
        if (request.ownerMemberId() != null) {
            validateBookMember(bookId, request.ownerMemberId());
        }

        Asset asset = Asset.create(bookId, request.name(), request.balance(), request.ownerMemberId());
        Asset savedAsset = assetRepository.save(asset);

        return buildAssetResponse(savedAsset);
    }

    public List<AssetResponse> getAssets(Long memberId, Long bookId) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        List<Asset> assets = assetRepository.findAllByBookId(bookId);
        return assets.stream()
                .map(this::buildAssetResponse)
                .toList();
    }

    public AssetResponse getAsset(Long memberId, Long bookId, Long assetId) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        Asset asset = assetRepository.findByIdAndBookId(assetId, bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSET_NOT_FOUND));

        return buildAssetResponse(asset);
    }

    @Transactional
    public AssetResponse updateAsset(Long memberId, Long bookId, Long assetId, AssetUpdateRequest request) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        Asset asset = assetRepository.findByIdAndBookId(assetId, bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSET_NOT_FOUND));

        // ownerMemberId 검증 (있는 경우)
        if (request.ownerMemberId() != null) {
            validateBookMember(bookId, request.ownerMemberId());
        }

        asset.updateAsset(request.name(), request.balance(), request.ownerMemberId());

        return buildAssetResponse(asset);
    }

    @Transactional
    public void deleteAsset(Long memberId, Long bookId, Long assetId) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        Asset asset = assetRepository.findByIdAndBookId(assetId, bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSET_NOT_FOUND));

        // TODO: Transaction 도메인 구현 후 거래 내역 체크 추가
        // if (transactionRepository.existsByAssetId(assetId)) {
        //     throw new CustomException(ErrorCode.ASSET_HAS_TRANSACTIONS);
        // }

        assetRepository.delete(asset);
    }

    public AssetSummaryResponse getTotalAssets(Long memberId, Long bookId) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        Long totalBalance = assetRepository.sumBalanceByBookId(bookId);
        Integer assetCount = assetRepository.findAllByBookId(bookId).size();

        return AssetSummaryResponse.of(totalBalance, assetCount);
    }

    private void validateBookMember(Long bookId, Long memberId) {
        if (!bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)) {
            throw new CustomException(ErrorCode.NOT_BOOK_MEMBER);
        }
    }

    private AssetResponse buildAssetResponse(Asset asset) {
        if (asset.getOwnerMemberId() == null) {
            return AssetResponse.from(asset);
        }

        String ownerNickname = memberRepository.findById(asset.getOwnerMemberId())
                .map(Member::getNickname)
                .orElse(null);

        return AssetResponse.from(asset, ownerNickname);
    }
}
