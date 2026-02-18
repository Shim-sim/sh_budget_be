package com.shbudget.domain.member.service;

import com.shbudget.domain.book.service.BookService;
import com.shbudget.domain.member.dto.request.MemberCreateRequest;
import com.shbudget.domain.member.dto.request.MemberUpdateRequest;
import com.shbudget.domain.member.dto.response.MemberResponse;
import com.shbudget.domain.member.entity.Member;
import com.shbudget.domain.member.repository.MemberRepository;
import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final BookService bookService;

    @Transactional
    public MemberResponse createMember(MemberCreateRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.create(request.email(), request.nickname());
        Member savedMember = memberRepository.save(member);

        String bookName = savedMember.getNickname() + "의 가계부";
        bookService.createBookForMember(savedMember.getId(), bookName);

        return MemberResponse.from(savedMember);
    }

    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateMember(Long id, MemberUpdateRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateProfile(request.nickname(), request.profileImageUrl());

        return MemberResponse.from(member);
    }
}
