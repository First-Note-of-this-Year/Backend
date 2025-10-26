package com.goormthon.backend.firstsori.domain.music.domain.service;

import com.goormthon.backend.firstsori.domain.music.application.dto.response.SongData;
import com.goormthon.backend.firstsori.global.common.util.RedisCacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.goormthon.backend.firstsori.domain.music.domain.util.PrefixUtil.SEARCH_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicAsyncService {

    private final RedisCacheUtil redisCacheUtil;

    // TTL 상수 정의
    private static final int POPULAR_THRESHOLD = 10;
    private static final long DEFAULT_TTL_SECONDS = 3600;      // 1시간
    private static final long POPULAR_TTL_SECONDS = 86400;    // 24시간
    private static final long EXTENDED_TTL_SECONDS = 60 * 60 * 24 * 7; //7일


    // iTunes API 결과를 Redis에 비동기적으로 캐싱 (검색 목록 5개 저장)
    @Async("taskExecutor")
    public void saveAndCacheMusicData(String keyword, List<SongData> musicList, Long searchCount) {
        log.info("▶️ 비동기 검색 결과 캐싱 시작: keyword={}", keyword);
        try {
            String searchKey = SEARCH_PREFIX + keyword.toLowerCase();
            long ttl = getCacheTtlByCount(searchCount);

            // RedisCacheUtil을 사용하여 캐시 저장 및 TTL 설정
            redisCacheUtil.setHashList(searchKey, musicList, ttl);

            log.info("✅ 비동기 검색 결과 캐싱 완료: keyword={}, TTL={}초", keyword, ttl);

        } catch (Exception e) {
            log.error("❌ 비동기 캐싱 중 오류 발생: keyword={}", keyword, e);
        }
    }

    // 사용자가 특정 음악을 저장했을 때, 검색 결과 캐시의 TTL을 연장하는 메서드
    @Async("taskExecutor")
    public void extendSearchCacheTtl(String keyword) {
        String searchKey = SEARCH_PREFIX + keyword.toLowerCase();

        // Redis에 해당 키가 존재하면 TTL을 7일로 연장
        redisCacheUtil.expire(searchKey, EXTENDED_TTL_SECONDS);

        log.info("✅ 검색 캐시 TTL 연장 시도: keyword={}", keyword);
    }

    // 검색 횟수에 따른 TTL을 계산
    private long getCacheTtlByCount(Long searchCount) {
        if (searchCount != null && searchCount >= POPULAR_THRESHOLD) {
            return POPULAR_TTL_SECONDS; // 24시간
        }
        return DEFAULT_TTL_SECONDS; // 1시간
    }
}
