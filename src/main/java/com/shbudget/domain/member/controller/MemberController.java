package com.shbudget.domain.member.controller;

import com.shbudget.domain.member.dto.request.MemberCreateRequest;
import com.shbudget.domain.member.dto.request.MemberUpdateRequest;
import com.shbudget.domain.member.dto.response.MemberResponse;
import com.shbudget.domain.member.service.MemberService;
import com.shbudget.global.common.ApiResult;
import com.shbudget.global.common.ResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관리 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원 가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력"),
            @ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    public ResponseEntity<ApiResult<MemberResponse>> createMember(
            @Valid @RequestBody MemberCreateRequest request
    ) {
        MemberResponse response = memberService.createMember(request);
        return ResponseEntity
                .status(ResponseStatus.CREATED.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.CREATED, response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "회원 조회", description = "ID로 회원을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<MemberResponse>> getMember(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Long id
    ) {
        MemberResponse response = memberService.findById(id);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "회원 정보 수정", description = "회원의 프로필 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<MemberResponse>> updateMember(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        MemberResponse response = memberService.updateMember(id, request);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

}
