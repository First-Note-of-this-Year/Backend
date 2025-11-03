package com.goormthon.backend.firstsori.global.auth.jwt.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.goormthon.backend.firstsori.global.auth.jwt.util.TokenNameUtil.ACCESS_TOKEN_COOKIE_NAME;
import static com.goormthon.backend.firstsori.global.auth.jwt.util.TokenNameUtil.REFRESH_TOKEN_COOKIE_NAME;

@Slf4j
@Component
public class CookieUtil {

    @Value("${firstsori.jwt.access.expiration}")
    private Long accessTokenExpiration;

    @Value("${firstsori.jwt.refresh.expiration}")
    private Long refreshTokenExpiration;

    @Value("${firstsori.auth.jwt.secureOption}")
    private boolean secureOption;

    @Value("${firstsori.auth.jwt.sameSiteOption}")
    private String sameSiteOption;

    @Value("${firstsori.auth.jwt.cookiePathOption}")
    private String cookiePathOption;

    // 쿠키 저장
    public void setAccessCookie(String accessToken, HttpServletResponse response) {
        setCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTokenExpiration);
    }

    public void setRefreshCookie(String refreshToken, HttpServletResponse response) {
        setCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshTokenExpiration);
    }

    // 쿠키 조회
    public Optional<String> getAccessTokenFromCookie(HttpServletRequest request) {
        return getTokenFromCookie(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        return getTokenFromCookie(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    // 쿠키 삭제
    public void deleteAccessTokenCookie(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE_NAME);
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        deleteCookie(response, REFRESH_TOKEN_COOKIE_NAME);
    }



    // 공통 쿠키 저장 메서드
    private void setCookie(HttpServletResponse response, String cookieName, String tokenValue, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, tokenValue)
                .domain(".firstsori.site") // 모든 하위 도메인(subdomain)에서 쿠키 접근 허용
                .maxAge(maxAge)             // 쿠키의 유효 기간 설정 (초 단위)
                .path(cookiePathOption)     // 쿠키가 유효한 경로 설정 (일반적으로 '/')
                .httpOnly(true)             // 클라이언트 스크립트(JS)의 접근을 차단하여 XSS 공격 방지
                .secure(secureOption)       // HTTPS 연결에서만 쿠키 전송 허용 (Dev/Prod 환경에 따라 설정)
                .sameSite(sameSiteOption)   // CSRF 공격 방지를 위한 SameSite 정책 설정 (Strict, Lax, None)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }


    // 공통 쿠키 추출 메서드
    private Optional<String> getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return Optional.ofNullable(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }



    // 공통 쿠키 삭제 메서드
    private void deleteCookie(HttpServletResponse response, String cookieName) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .maxAge(0)
                .path(cookiePathOption)
                .secure(secureOption)
                .httpOnly(true)
                .sameSite(sameSiteOption)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
