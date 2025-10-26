package com.goormthon.backend.firstsori.global.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;


    // 검색 횟수를 1 증가
    public Long incrementAndExpireCount(String countKey) {
        if (countKey == null) return 0L;

        try {
            Long count = redisTemplate.opsForValue().increment(countKey);
            log.info("Redis 키 '{}' 카운트 증가 완료. 최종 횟수: {}", countKey, count);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Redis 카운트 증가 중 오류 발생: key={}", countKey, e);
            return 0L;
        }
    }

    // 캐시 데이터를 저장하고 TTL을 설정
    public <T> void setValue(String key, T value, long timeoutSeconds) {
        if (key == null || value == null) return;
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeoutSeconds));
        } catch (Exception e) {
            log.error("Redis set 작업 중 오류 발생: key={}", key, e);
        }
    }


    // 캐시 데이터를 조회합니다.
    public <T> T getValue(String key) {
        if (key == null) return null;
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get 작업 중 오류 발생: key={}", key, e);
            return null;
        }
    }

    // 특정 키의 만료 시간을 갱신하거나 새로 설정합니다. (TTL 연장 용도)
    public boolean expire(String key, long timeoutSeconds) {
        if (key == null) {
            log.warn("Attempted to expire a null key.");
            return false;
        }
        try {
            Boolean result = redisTemplate.expire(key, Duration.ofSeconds(timeoutSeconds));

            if (Boolean.TRUE.equals(result)) {
                return true;
            } else {
                log.warn("Failed 않아to update TTL for Redis key '{}'. Key might not exist.", key);
                return false;
            }
        } catch (Exception e) {
            log.error("Error setting TTL for Redis key '{}'", key, e);
            return false;
        }
    }


    // 검색 결과 목록을 Redis Hash 구조에 저장하고 TTL을 설정
    public <T> void setHashList(String key, List<T> list, long timeoutSeconds) {
        if (key == null || list == null || list.isEmpty()) return;

        // 리스트를 Hash 형태로 변환 (필드: 인덱스, 값: 객체)
        Map<String, T> hashEntries = list.stream()
                .collect(Collectors.toMap(
                        item -> String.valueOf(list.indexOf(item)), // 필드 이름을 인덱스로 사용 (0, 1, 2, 3, 4)
                        item -> item // 값은 SongData 객체
                ));

        try {
            // Hash 전체 저장
            redisTemplate.opsForHash().putAll(key, hashEntries);

            // Hash 키에 TTL 설정
            redisTemplate.expire(key, Duration.ofSeconds(timeoutSeconds));
        } catch (Exception e) {
            log.error("Redis Hash 캐시 저장 중 오류 발생: key={}", key, e);
        }
    }

    // Redis Hash 구조에서 전체 검색 결과 목록을 조회
    public <T> List<T> getHashList(String key, Class<T> classType) {
        if (key == null) return List.of();


        try {
            // entries의 Value는 LinkedHashMap 형태
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

            if (entries.isEmpty()) return List.of();

            return entries.entrySet().stream()
                    // 1. 필드(key)를 정수(Integer)로 변환하여 정렬합니다. (순서 보장)
                    .sorted( (a, b) -> Integer.compare(Integer.parseInt((String)a.getKey()), Integer.parseInt((String)b.getKey())) )
                    // 2. 값(Value)을 추출하여 ObjectMapper로 T 타입으로 안전하게 변환
                    .map(entry -> objectMapper.convertValue(entry.getValue(), classType)) // ⭐ 안전한 타입 변환 적용
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Redis Hash 캐시 조회 중 오류 발생: key={}", key, e);
            return List.of();
        }
    }

    // 특정 키 삭제
    public void delete(String key) {
        if (key == null) return;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete 작업 중 오류 발생: key={}", key, e);
        }
    }
}
