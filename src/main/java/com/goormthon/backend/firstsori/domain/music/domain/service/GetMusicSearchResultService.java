package com.goormthon.backend.firstsori.domain.music.domain.service;


import com.goormthon.backend.firstsori.domain.music.application.dto.response.SongData;
import com.goormthon.backend.firstsori.domain.music.application.mapper.MusicMapper;
import com.goormthon.backend.firstsori.domain.music.domain.entity.Music;
import com.goormthon.backend.firstsori.domain.music.domain.util.ItunesService;
import com.goormthon.backend.firstsori.global.common.response.CustomException;
import com.goormthon.backend.firstsori.global.common.util.RedisCacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.goormthon.backend.firstsori.domain.music.domain.util.PrefixUtil.*;
import static com.goormthon.backend.firstsori.global.common.exception.ErrorCode.SPOTIFY_API_CALL_FAILED;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class GetMusicSearchResultService {

    private final RedisCacheUtil redisCacheUtil;
    private final ItunesService itunesService;
    private final MusicAsyncService musicAsyncService;

    /**
     *  키워드로 itunes 검색 및 캐싱 작업 메서드
     *
     *   키워드 검색횟수 TTL (countKey) :  7일
     *   키워드 검색결과 TTL (searchKey) : 기본 1시간 캐싱
     *   10회 이상 검색된 키워드의 검색결과 TTL (trackKey) : 24시간
    */
    public List<SongData> getSearchResult(String keyword) {
        String searchKey = SEARCH_PREFIX + keyword.toLowerCase();
        String countKey = COUNT_PREFIX + keyword.toLowerCase();

        // 검색 횟수 증가 및 TTL 획득
        Long searchCount = redisCacheUtil.incrementAndExpireCount(countKey);
        log.info("[Count Update] 검색 횟수 키 '{}'가 {}로 갱신되었습니다.", countKey, searchCount);
        
        // 캐시 조회
        List<SongData> cachedResult = redisCacheUtil.getHashList(searchKey, SongData.class);

        // 캐시 히트 시: 로그 남기고 즉시 반환
        if (cachedResult != null && !cachedResult.isEmpty()) {
            log.info("[Cache Hit] 음악 검색 결과가 캐시에서 조회되었습니다. 키워드: {}, 아이템 수: {}", keyword, cachedResult.size());
            return cachedResult;
        }

        // 캐시 Miss 시, iTunes API 호출 시작
        log.info("[Cache Miss] iTunes API 호출을 시작합니다. 키워드: {}", keyword);
        List<Music> musicEntities = getItunesService(keyword);

        // 응답 데이터 변환
        List<SongData> resultToClient = musicEntities.stream()
                .map(MusicMapper::toSongData)
                .collect(Collectors.toList());

        // 비동기 저장 및 캐싱 호출 (클라이언트에게 반환할 데이터를 Redis에 비동기적으로 저장)
        if (!resultToClient.isEmpty()) {
            musicAsyncService.saveAndCacheMusicData(keyword, resultToClient, searchCount);
            log.info("[Async Start] 검색 결과 {}개를 Redis에 비동기 캐싱 요청.", resultToClient.size());
        }
        // 클라이언트에게 결과 즉시 반환
        return resultToClient;
    }

    // itunes service 호출
    private List<Music> getItunesService(String keyword) {
        List<Music> response;
        try {
            response = itunesService.searchMusic(keyword).block();
        } catch (Exception e) {
            throw new CustomException(SPOTIFY_API_CALL_FAILED);
        }
        return response;
    }

}
