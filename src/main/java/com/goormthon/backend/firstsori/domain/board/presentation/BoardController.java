package com.goormthon.backend.firstsori.domain.board.presentation;

import com.goormthon.backend.firstsori.domain.board.application.dto.response.BoardInfoResponse;
import com.goormthon.backend.firstsori.domain.board.application.dto.request.CreateBoardRequest;
import com.goormthon.backend.firstsori.domain.board.application.dto.request.UpdateBoardRequest;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.BoardPreviewResponse;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.CreateBoardResponse;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.GetShareUriResponse;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.UpdateBoardResponse;
import com.goormthon.backend.firstsori.domain.board.application.usecase.BoardUseCase;
import com.goormthon.backend.firstsori.domain.board.domain.util.OffsetBasedPageRequest;
import com.goormthon.backend.firstsori.domain.board.presentation.spec.BoardControllerSpec;
import com.goormthon.backend.firstsori.domain.message.application.dto.response.MessageListResponse;
import com.goormthon.backend.firstsori.domain.message.application.dto.response.MessageResponse;
import com.goormthon.backend.firstsori.domain.message.application.usecase.MessageUseCase;
import com.goormthon.backend.firstsori.global.auth.oauth2.domain.PrincipalDetails;
import com.goormthon.backend.firstsori.global.common.response.ApiResponse;
import com.goormthon.backend.firstsori.global.common.response.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
public class BoardController implements BoardControllerSpec {

    private final MessageUseCase messageUseCase;
    private final BoardUseCase boardUseCase;

    // 개별 메세지 조회
    @GetMapping("/{messageId}")
    public ApiResponse<MessageResponse> getMessage(@PathVariable UUID messageId) {
        MessageResponse messageResponse = messageUseCase.getMessage(messageId);
        return ApiResponse.ok(messageResponse);
    }

    // 전체 메세지 리스트 정보 조회
    @GetMapping
    public ApiResponse<PageResponse<MessageListResponse>> getMessages(
            @AuthenticationPrincipal PrincipalDetails user,
            @RequestParam(defaultValue = "0") int pageNumber) {
        int pageSize = calculatePageSize(pageNumber);
        int offset = calculateOffset(pageNumber);
        Pageable pageable = createPageableWithOffset(offset, pageSize);
        PageResponse<MessageListResponse> messageListResponses = messageUseCase.getMessages(user.getId(), pageable);
        return ApiResponse.ok(messageListResponses);
    }

    // 외부인 접속 시 전체 메세지 앨범 커버만 조회
    @GetMapping("/share/{shareUri}")
    public ApiResponse<PageResponse<BoardPreviewResponse>> getMessagesFromNonOwner(
            @PathVariable String shareUri,
            @RequestParam(defaultValue = "0") int pageNumber) {
        int pageSize = calculatePageSize(pageNumber);
        int offset = calculateOffset(pageNumber);
        Pageable pageable = createPageableWithOffset(offset, pageSize);
        PageResponse<BoardPreviewResponse> response = messageUseCase.getMessagesByBoardShareUri(shareUri, pageable);
        return ApiResponse.ok(response);
    }

    // 보드 생성
    @PostMapping("/create")
    public ApiResponse<CreateBoardResponse> createBoard(
            @RequestBody CreateBoardRequest request,
            @AuthenticationPrincipal PrincipalDetails user
    ) {
        CreateBoardResponse response = boardUseCase.createBoard(request, user.getUser());
        return ApiResponse.ok(response);
    }

    // 보드 공유 URI 조회
    @GetMapping("/share")
    public ApiResponse<GetShareUriResponse> getShareUri(
            @AuthenticationPrincipal PrincipalDetails user
    ) {
        GetShareUriResponse response = boardUseCase.getShareUriByUser(user.getUser());
        return ApiResponse.ok(response);
    }


    // 보드 정보 반환
    @GetMapping("/info/{shareUri}")
    public ApiResponse<BoardInfoResponse> getBoardInfo(@PathVariable String shareUri) {
        BoardInfoResponse boardInfo=boardUseCase.getBoardInfo(shareUri);
        return ApiResponse.ok(boardInfo);
    }

    // 보드 수정 (닉네임, 프로필 이미지)
    @PatchMapping("/update")
    public ApiResponse<UpdateBoardResponse> updateBoard(
        @RequestPart(value = "nickname", required = false) String nickname,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
        @AuthenticationPrincipal PrincipalDetails user
    ) {
        UpdateBoardRequest request = new UpdateBoardRequest(nickname, profileImage);
        UpdateBoardResponse response = boardUseCase.updateBoard(request, user.getUser());
        return ApiResponse.ok(response);
    }

    /**
     * 페이지 번호에 따라 페이지 크기를 계산합니다.
     * 0페이지: 10개, 1페이지부터: 11개씩
     */
    private int calculatePageSize(int pageNumber) {
        return pageNumber == 0 ? 10 : 11;
    }

    /**
     * 페이지 번호에 따라 올바른 offset을 계산합니다.
     * 0페이지: offset = 0
     * 1페이지: offset = 10
     * 2페이지 이상: offset = 10 + (pageNumber - 1) * 11
     */
    private int calculateOffset(int pageNumber) {
        if (pageNumber == 0) {
            return 0;
        }
        return 10 + (pageNumber - 1) * 11;
    }

    /**
     * offset과 pageSize를 사용하여 Pageable을 생성합니다.
     * 커스텀 OffsetBasedPageRequest를 사용하여 정확한 offset을 보장합니다.
     */
    private Pageable createPageableWithOffset(int offset, int pageSize) {
        return new OffsetBasedPageRequest(offset, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

}
