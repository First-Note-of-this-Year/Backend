package com.goormthon.backend.firstsori.domain.board.presentation.spec;

import com.goormthon.backend.firstsori.domain.board.application.dto.request.UpdateBoardRequest;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.BoardInfoResponse;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.BoardPreviewResponse;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.UpdateBoardResponse;
import com.goormthon.backend.firstsori.domain.message.application.dto.response.MessageListResponse;
import com.goormthon.backend.firstsori.domain.message.application.dto.response.MessageResponse;
import com.goormthon.backend.firstsori.global.auth.oauth2.domain.PrincipalDetails;
import com.goormthon.backend.firstsori.global.common.response.page.PageResponse;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.GetShareUriResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Board API", description = "메세지 게시판 관련 API")
public interface BoardControllerSpec {

    @Operation(summary = "메세지 상세 정보 조회", description = "단일 메세지의 상세 내용을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "메세지 ID를 찾을 수 없음")
    })
    @GetMapping("/{messageId}")
    com.goormthon.backend.firstsori.global.common.response.ApiResponse<MessageResponse> getMessage(
            @Parameter(
                    description = "조회할 메세지 ID",
                    example = "ff54b002-7f36-496e-9d8e-cf7dc41556d7"
            )
            @PathVariable UUID messageId    );

    @Operation(
            summary = "전체 메세지 목록 조회",
            description = "사용자 ID에 해당하는 게시판의 메세지 목록을 페이징하여 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json"
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 게시판을 찾을 수 없음"
            )
    })
    @GetMapping
    com.goormthon.backend.firstsori.global.common.response.ApiResponse<PageResponse<MessageListResponse>> getMessages(
            @AuthenticationPrincipal PrincipalDetails user,
            @Parameter(
                    description = "페이지 번호 (0부터 시작, 0페이지는 10개, 1페이지부터는 11개씩 반환)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int pageNumber
    );

    @Operation(
            summary = "보드 정보 반환",
            description = "인증된 사용자의 보드 정보(닉네임, 프로필 사진, 총 메시지 수 등)를 반환합니다. 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 부재 또는 유효하지 않은 토큰)"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/info")
    com.goormthon.backend.firstsori.global.common.response.ApiResponse<BoardInfoResponse> getBoardInfo(
            @PathVariable String shareUri
    );

    @Operation(
            summary = "보드 생성",
            description = "닉네임과 Authorization 헤더의 JWT 토큰으로 보드를 생성합니다."
    )
    @PostMapping("/create")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    implementation = com.goormthon.backend.firstsori.domain.board.application.dto.response.CreateBoardResponse.class
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 부재 또는 유효하지 않은 토큰)")
    })
    com.goormthon.backend.firstsori.global.common.response.ApiResponse<com.goormthon.backend.firstsori.domain.board.application.dto.response.CreateBoardResponse> createBoard(
            @RequestBody com.goormthon.backend.firstsori.domain.board.application.dto.request.CreateBoardRequest request,
            @AuthenticationPrincipal PrincipalDetails user
    );

    @Operation(
            summary = "보드 공유 URI 조회",
            description = "JWT 토큰으로 인증된 사용자의 보드 공유 URI를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.goormthon.backend.firstsori.domain.board.application.dto.response.GetShareUriResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "토큰이 없거나 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "보드를 찾을 수 없음"
            )
    })
    @GetMapping("/share")
    com.goormthon.backend.firstsori.global.common.response.ApiResponse<GetShareUriResponse> getShareUri(
            @Parameter(description = "인증 토큰이 담긴 Authorization 헤더. 예: Bearer eyJ...")
            @AuthenticationPrincipal PrincipalDetails user


            );

    @Operation(
            summary = "외부 사용자가 보드 메세지 조회",
            description = "공유 URI를 통해 외부 사용자가 접근할 때, 해당 보드의 전체 메시지 앨범 커버 정보를 페이지네이션과 함께 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    implementation = com.goormthon.backend.firstsori.global.common.response.page.PageResponse.class
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공유 URI에 해당하는 보드를 찾을 수 없음"
            )
    })
    @GetMapping("/share/{shareUri}")
    com.goormthon.backend.firstsori.global.common.response.ApiResponse<
            PageResponse<BoardPreviewResponse>
            > getMessagesFromNonOwner(
            @Parameter(
                    description = "공유 URI",
                    example = "a1b2c3d4e5f6"
            )
            @PathVariable String shareUri,
            @Parameter(
                    description = "페이지 번호 (0부터 시작, 0페이지는 10개, 1페이지부터는 11개씩 반환)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int pageNumber
    );

    @Operation(
            summary = "프로필 수정",
            description = "JWT 토큰으로 인증된 사용자의 보드 닉네임과 프로필 이미지를 수정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 성공",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    implementation = com.goormthon.backend.firstsori.domain.board.application.dto.response.UpdateBoardResponse.class
                            )
                    )),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 부재 또는 유효하지 않은 토큰)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음")
    })
    @PatchMapping("/update")
    com.goormthon.backend.firstsori.global.common.response.ApiResponse<UpdateBoardResponse> updateBoard(
            @RequestBody UpdateBoardRequest request,
            @AuthenticationPrincipal PrincipalDetails user
    );

}
