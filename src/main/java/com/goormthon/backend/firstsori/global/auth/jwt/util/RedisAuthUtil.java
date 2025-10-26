package com.goormthon.backend.firstsori.global.auth.jwt.util;

import com.goormthon.backend.firstsori.global.auth.jwt.domain.JwtToken;
import com.goormthon.backend.firstsori.global.auth.jwt.domain.JwtTokenRedisRepository;
import com.goormthon.backend.firstsori.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisAuthUtil {

    @Value("${firstsori.jwt.refresh.expiration}")
    private Long refreshTokenExpiration;

    private final JwtTokenRedisRepository repository;


    /// 리프레쉬 토큰 저장
    public void saveRefreshToken(UUID userId, String refreshToken) {

        // TTL 시점 세팅
        LocalDateTime plusSeconds = LocalDateTime.now().plusSeconds(refreshTokenExpiration);
        Long expiredAt = plusSeconds.atZone(ZoneId.systemDefault()).toEpochSecond();

        // 토큰, userId, 만료시점을 JwtToken에 담아서 Redis에 저장
        JwtToken jwtToken = JwtToken.of(userId, refreshToken, expiredAt);
        repository.save(jwtToken);

    }


    /// 리프레쉬 토큰 조회
    public String getRefreshTokenByUserId(UUID userId) {

        /// 유저 Id로 리프레쉬 토큰 조회
        JwtToken jwtToken = repository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));

        return jwtToken.getRefreshToken();
    }

    public String getRefreshTokenById(String refreshToken) {

        /// 리프레쉬 토큰으로 유저 조회
        JwtToken jwtToken = repository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));

        return jwtToken.getRefreshToken();
    }

    /// 존재 여부 체크
    public boolean checkRefreshTokenAndUserId(String refreshToken, UUID userId) {
        return repository.findByRefreshToken(refreshToken)
                .map(token -> token.getUserId().equals(userId))
                .orElse(false);
    }

    /// 리프레쉬 토큰 삭제하기
    public void deleteByRefreshToken(String refreshToken) {

        repository.deleteByRefreshToken(refreshToken);
    }

}
