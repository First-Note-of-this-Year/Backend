package com.goormthon.backend.firstsori.global.auth.oauth2.handler;

import com.goormthon.backend.firstsori.global.auth.jwt.service.JwtTokenUseCase;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenUseCase tokenService;

    @Value("${spring.front.host}")
    public String REDIRECT_PATH;

    /*
        기존에 존재하는 유저의 경우, 토큰 발급을 진행합니다.
        리다이렉트 시킵니다.
     */

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        /// AccessToken과 Refresh 토큰 생성

        tokenService.createAccessToken(response, authentication);
        tokenService.createRefreshToken(response, authentication);

        /// 시큐리티 홀더에 해당 멤버 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("시큐리티에 저장된 인증 정보 :{}", authentication.getPrincipal().toString());

        // 쿠키와 함께 리다이렉트 (프론트 홈 주소)
        String redirectPath = REDIRECT_PATH + "/login-success"; // 백엔드쪽 콜백-리다이렉트 주소 충돌 없도록 별도 엔드포인트로 분리
        getRedirectStrategy().sendRedirect(request, response, redirectPath);
    }
}
