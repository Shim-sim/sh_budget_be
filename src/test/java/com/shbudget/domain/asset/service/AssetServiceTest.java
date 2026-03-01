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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private BookMemberRepository bookMemberRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AssetService assetService;

    @Test
    @DisplayName("자산 생성 성공")
    void createAsset() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        AssetCreateRequest request = new AssetCreateRequest("월급 통장", 1000000L, null);

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(true);
        given(assetRepository.save(any(Asset.class))).willAnswer(invocation -> {
            Asset asset = invocation.getArgument(0);
            return Asset.create(bookId, asset.getName(), asset.getBalance());
        });

        // when
        AssetResponse response = assetService.createAsset(memberId, bookId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("월급 통장");
        assertThat(response.balance()).isEqualTo(1000000L);
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    @DisplayName("자산 생성 실패 - 가계부 멤버가 아님")
    void createAsset_notBookMember() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        AssetCreateRequest request = new AssetCreateRequest("월급 통장", 1000000L, null);

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> assetService.createAsset(memberId, bookId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_MEMBER);
    }

    @Test
    @DisplayName("자산 생성 성공 - 소유자 지정")
    void createAsset_withOwner() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long ownerMemberId = 2L;
        AssetCreateRequest request = new AssetCreateRequest("아내 통장", 500000L, ownerMemberId);

        Member owner = Member.create("owner@example.com", "아내");

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(true);
        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, ownerMemberId)).willReturn(true);
        given(assetRepository.save(any(Asset.class))).willAnswer(invocation -> {
            Asset asset = invocation.getArgument(0);
            return Asset.create(bookId, asset.getName(), asset.getBalance(), asset.getOwnerMemberId());
        });
        given(memberRepository.findById(ownerMemberId)).willReturn(Optional.of(owner));

        // when
        AssetResponse response = assetService.createAsset(memberId, bookId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.ownerMemberId()).isEqualTo(ownerMemberId);
        assertThat(response.ownerNickname()).isEqualTo("아내");
    }

    @Test
    @DisplayName("자산 목록 조회 성공")
    void getAssets() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Asset asset1 = Asset.create(bookId, "월급 통장", 1000000L);
        Asset asset2 = Asset.create(bookId, "생활비 카드", 500000L);

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(true);
        given(assetRepository.findAllByBookId(bookId)).willReturn(List.of(asset1, asset2));

        // when
        List<AssetResponse> responses = assetService.getAssets(memberId, bookId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("월급 통장");
        assertThat(responses.get(1).name()).isEqualTo("생활비 카드");
    }

    @Test
    @DisplayName("자산 상세 조회 성공")
    void getAsset() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long assetId = 1L;
        Asset asset = Asset.create(bookId, "월급 통장", 1000000L);

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(true);
        given(assetRepository.findByIdAndBookId(assetId, bookId)).willReturn(Optional.of(asset));

        // when
        AssetResponse response = assetService.getAsset(memberId, bookId, assetId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("월급 통장");
        assertThat(response.balance()).isEqualTo(1000000L);
    }

    @Test
    @DisplayName("자산 상세 조회 실패 - 자산 없음")
    void getAsset_notFound() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long assetId = 999L;

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(true);
        given(assetRepository.findByIdAndBookId(assetId, bookId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> assetService.getAsset(memberId, bookId, assetId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSET_NOT_FOUND);
    }

    @Test
    @DisplayName("자산 수정 성공")
    void updateAsset() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long assetId = 1L;
        AssetUpdateRequest request = new AssetUpdateRequest("수정된 통장", 2000000L, null);

        Asset asset = Asset.create(bookId, "월급 통장", 1000000L);

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(true);
        given(assetRepository.findByIdAndBookId(assetId, bookId)).willReturn(Optional.of(asset));

        // when
        AssetResponse response = assetService.updateAsset(memberId, bookId, assetId, request);

        // then
        assertThat(response.name()).isEqualTo("수정된 통장");
        assertThat(response.balance()).isEqualTo(2000000L);
    }

    @Test
    @DisplayName("자산 삭제 성공")
    void deleteAsset() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long assetId = 1L;
        Asset asset = Asset.create(bookId, "월급 통장", 1000000L);

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(true);
        given(assetRepository.findByIdAndBookId(assetId, bookId)).willReturn(Optional.of(asset));

        // when
        assetService.deleteAsset(memberId, bookId, assetId);

        // then
        verify(assetRepository).delete(asset);
    }

    @Test
    @DisplayName("총 자산 조회 성공")
    void getTotalAssets() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Asset asset1 = Asset.create(bookId, "월급 통장", 1000000L);
        Asset asset2 = Asset.create(bookId, "생활비 카드", 500000L);

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(true);
        given(assetRepository.sumBalanceByBookId(bookId)).willReturn(1500000L);
        given(assetRepository.findAllByBookId(bookId)).willReturn(List.of(asset1, asset2));

        // when
        AssetSummaryResponse response = assetService.getTotalAssets(memberId, bookId);

        // then
        assertThat(response.totalBalance()).isEqualTo(1500000L);
        assertThat(response.assetCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("총 자산 조회 성공 - 자산 없음")
    void getTotalAssets_empty() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(true);
        given(assetRepository.sumBalanceByBookId(bookId)).willReturn(null);
        given(assetRepository.findAllByBookId(bookId)).willReturn(List.of());

        // when
        AssetSummaryResponse response = assetService.getTotalAssets(memberId, bookId);

        // then
        assertThat(response.totalBalance()).isEqualTo(0L);
        assertThat(response.assetCount()).isEqualTo(0);
    }
}
