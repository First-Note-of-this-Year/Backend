package com.goormthon.backend.firstsori.domain.user.presentation;

import com.goormthon.backend.firstsori.domain.board.application.dto.request.CreateBoardRequest;
import com.goormthon.backend.firstsori.domain.board.application.usecase.BoardUseCase;
import com.goormthon.backend.firstsori.domain.user.application.usecase.UserUseCase;
import com.goormthon.backend.firstsori.domain.user.domain.entity.User;
import com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Provider;
import com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Status;
import com.goormthon.backend.firstsori.domain.user.presentation.spec.AuthControllerSpec;
import com.goormthon.backend.firstsori.global.auth.jwt.service.JwtTokenUseCase;
import com.goormthon.backend.firstsori.global.auth.oauth2.domain.PrincipalDetails;
import com.goormthon.backend.firstsori.global.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.UUID;

import static com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Role.ADMIN;

@Slf4j
@RequestMapping("/api/v1/auth")
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthControllerSpec {

    private final JwtTokenUseCase tokenService;
    private final UserUseCase userService;
    private final BoardUseCase boardService;

    @PostMapping("/reissue")
    public ApiResponse<String> reissue(HttpServletRequest request, HttpServletResponse response) {

        /// 재발급 하기
        tokenService.reissueByRefreshToken(request, response);

        return ApiResponse.ok("재발급 완료");
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request, HttpServletResponse response) {

        // 유저
        UUID userId = principalDetails.getUser().getUserId();

        // 리프레쉬 토큰 삭제하기
        tokenService.logout(userId, request, response);

        return ApiResponse.ok("로그아웃 완료");
    }

    @PostMapping("/dev-login")
    public ApiResponse<String> devLogin(HttpServletResponse httpServletResponse) {

        // 1. 테스트용 유저 생성 또는 저장
        User dev = createDev();
        User user = userService.saveUser(dev);
        boardService.createBoard(new CreateBoardRequest("개발자"), user);

        // 2. PrincipalDetails 생성
        PrincipalDetails principalDetails = PrincipalDetails.of(user);

        // 3. Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails, null, principalDetails.getAuthorities()
        );

        // 4. SecurityContext에 인증 정보 등록
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 5. 토큰 생성 및 응답 헤더에 추가
        tokenService.createAccessToken(httpServletResponse, authentication);
        tokenService.createRefreshToken(httpServletResponse, authentication);

        return ApiResponse.created();
    }

    /// 임시 유저 생성
    private User createDev() {
        return User.builder()
                .email("firstSori_kakao@example.com")
                .nickname("어드민")
                .profileImage("https://firstsori-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png")
                .role(ADMIN)
                .status(Status.ACTIVE)
                .provider(Provider.ETC)
                .socialId("dev-kakao-id")
                .board(null)
                .build();

    }

}
