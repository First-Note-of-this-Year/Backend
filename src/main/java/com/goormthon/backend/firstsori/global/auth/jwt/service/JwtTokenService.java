package com.goormthon.backend.firstsori.global.auth.jwt.service;

import com.goormthon.backend.firstsori.global.auth.jwt.exception.JwtAuthenticationException;
import com.goormthon.backend.firstsori.global.auth.jwt.util.CookieUtil;
import com.goormthon.backend.firstsori.global.auth.jwt.util.JwtTokenExtractor;
import com.goormthon.backend.firstsori.global.auth.jwt.util.JwtTokenProvider;
import com.goormthon.backend.firstsori.global.auth.jwt.util.RedisAuthUtil;
import com.goormthon.backend.firstsori.global.auth.oauth2.domain.PrincipalDetails;
import com.goormthon.backend.firstsori.global.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtTokenService implements JwtTokenUseCase {

    private final JwtTokenProvider provider;
    private final JwtTokenExtractor extractor;

    /// 레디스
    private final RedisAuthUtil RedisAuthUtil;

    /// 쿠키
    private final CookieUtil cookieUtil;

    // 액세스 토큰 생성
    @Override
    public void createAccessToken(HttpServletResponse response, Authentication authentication) {
        // 토큰 발급
        String accessToken = provider.generateAccessToken(authentication);

        // 쿠키에 토큰 저장
        cookieUtil.setAccessCookie(accessToken, response);

    }

    // 리프레쉬 토큰 생성
    @Override
    public void createRefreshToken(HttpServletResponse response, Authentication authentication) {
        // 토큰 발급
        String refreshToken = provider.generateRefreshToken(authentication);

        // 인증 객체에서 정보 가져오기
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        // 해당 토큰을 레디스에 저장
        UUID userId = principalDetails.getId();
        RedisAuthUtil.saveRefreshToken(userId, refreshToken);

        // 토큰을 쿠키에 저장
        cookieUtil.setRefreshCookie(refreshToken, response);
    }

    // 재발급 하기
    @Override
    public void reissueByRefreshToken(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 리프레쉬 토큰 가져오기
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request)
                .orElseThrow(() -> new JwtAuthenticationException(ErrorCode.TOKEN_NOT_FOUND_COOKIE.getMessage()));

        // 쿠키 검증
        if (!extractor.validateToken(refreshToken)) {
            throw new JwtAuthenticationException(ErrorCode.TOKEN_INVALID.getMessage());
        };

        // 인증 객체에서 정보 가져오기
        Authentication authentication = extractor.getAuthentication(refreshToken);

        // 유저 인증 조회
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        // 유저와 리프레쉬 토큰이 일치하는지 체크한다.
        boolean checked = RedisAuthUtil.checkRefreshTokenAndUserId(refreshToken, principalDetails.getId());
        if (!checked) {
            throw new JwtAuthenticationException(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        // 쿠키에 다시 전송하기
        createAccessToken(response, authentication);

    }

    @Override
    public void logout(UUID userId, HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 리프레쉬 토큰 가져오기
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request)
                .orElseThrow(() -> new JwtAuthenticationException(ErrorCode.TOKEN_NOT_FOUND_COOKIE.getMessage()));

        // 리프레쉬와 유저가 맞는지 체크
        boolean checked = RedisAuthUtil.checkRefreshTokenAndUserId(refreshToken, userId);
        if (!checked) {
            throw new JwtAuthenticationException(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        // 리프레쉬 쿠키를 null 설정
        cookieUtil.deleteRefreshTokenCookie(response);

        // 액세스 쿠키를 null 설정
        cookieUtil.deleteAccessTokenCookie(response);

        // DB에서도 삭제
        RedisAuthUtil.deleteByRefreshToken(refreshToken);
    }

}
