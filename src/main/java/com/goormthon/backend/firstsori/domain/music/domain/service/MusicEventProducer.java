package com.goormthon.backend.firstsori.domain.music.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MusicEventProducer {

    private static final String MUSIC_EVENT_STREAM = "chart:music_event_stream";
    private static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));
    private final StringRedisTemplate redisTemplate;

    /**
     * 곡 선택 시 이벤트 발행
     */
    public void publishMusicEvent(String songName) {

        String currentTime = ISO_8601_FORMATTER.format(Instant.now());

        // Redis Stream에 바로 넣을 key-value 형태
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("songName", songName);
        eventData.put("timestamp", currentTime);

        // JSON 변환 필요 없음 (Stream에 직접 map으로 저장)
        redisTemplate.opsForStream().add(MUSIC_EVENT_STREAM, eventData);
    }

}
