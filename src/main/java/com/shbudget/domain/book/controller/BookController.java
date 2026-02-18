package com.shbudget.domain.book.controller;

import com.shbudget.domain.book.dto.BookJoinRequest;
import com.shbudget.domain.book.dto.BookMemberResponse;
import com.shbudget.domain.book.dto.BookResponse;
import com.shbudget.domain.book.dto.BookUpdateRequest;
import com.shbudget.domain.book.service.BookMemberService;
import com.shbudget.domain.book.service.BookService;
import com.shbudget.global.common.ApiResult;
import com.shbudget.global.common.ResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Book", description = "가계부 관리 API")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookMemberService bookMemberService;

    @GetMapping("/my")
    @Operation(summary = "내 가계부 조회", description = "현재 사용자의 가계부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "가계부 없음")
    })
    public ResponseEntity<ApiResult<BookResponse>> getMyBook(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        BookResponse response = bookService.getMyBook(memberId);
        return ResponseEntity
                .status(ResponseStatus.SUCCESS.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "가계부 이름 수정", description = "가계부 이름을 수정합니다. (소유자만 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부 없음")
    })
    public ResponseEntity<ApiResult<BookResponse>> updateBook(
            @PathVariable Long id,
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody BookUpdateRequest request
    ) {
        BookResponse response = bookService.updateBook(id, memberId, request);
        return ResponseEntity
                .status(ResponseStatus.SUCCESS.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @PostMapping("/{id}/invite-code")
    @Operation(summary = "초대 코드 재생성", description = "가계부 초대 코드를 재생성합니다. (소유자만 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재생성 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부 없음")
    })
    public ResponseEntity<ApiResult<BookResponse>> regenerateInviteCode(
            @PathVariable Long id,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        BookResponse response = bookService.regenerateInviteCode(id, memberId);
        return ResponseEntity
                .status(ResponseStatus.SUCCESS.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "가계부 삭제", description = "가계부를 삭제합니다. (소유자만 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부 없음")
    })
    public ResponseEntity<ApiResult<Void>> deleteBook(
            @PathVariable Long id,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        bookService.deleteBook(id, memberId);
        return ResponseEntity
                .status(ResponseStatus.SUCCESS.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.SUCCESS, null));
    }

    @PostMapping("/join")
    @Operation(summary = "가계부 참여", description = "초대 코드를 사용하여 가계부에 참여합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "참여 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 초대 코드"),
            @ApiResponse(responseCode = "409", description = "이미 참여 중")
    })
    public ResponseEntity<ApiResult<BookMemberResponse>> joinBook(
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody BookJoinRequest request
    ) {
        BookMemberResponse response = bookMemberService.joinBook(memberId, request);
        return ResponseEntity
                .status(ResponseStatus.CREATED.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.CREATED, response));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "가계부 멤버 목록 조회", description = "가계부의 모든 멤버 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부 없음")
    })
    public ResponseEntity<ApiResult<List<BookMemberResponse>>> getBookMembers(
            @PathVariable Long id,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        List<BookMemberResponse> response = bookMemberService.getBookMembers(id, memberId);
        return ResponseEntity
                .status(ResponseStatus.SUCCESS.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "가계부 탈퇴/멤버 제거",
               description = "가계부에서 탈퇴하거나 다른 멤버를 제거합니다. 소유자는 탈퇴할 수 없으며, 소유자만 다른 멤버를 제거할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "소유자는 탈퇴 불가"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 멤버 없음")
    })
    public ResponseEntity<ApiResult<Void>> leaveOrRemoveMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestHeader("X-Member-Id") Long requesterId
    ) {
        bookMemberService.leaveOrRemoveMember(id, requesterId, memberId);
        return ResponseEntity
                .status(ResponseStatus.SUCCESS.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.SUCCESS, null));
    }
}
