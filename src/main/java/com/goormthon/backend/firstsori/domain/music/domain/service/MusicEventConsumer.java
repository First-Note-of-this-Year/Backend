package com.goormthon.backend.firstsori.domain.music.domain.service;

import com.goormthon.backend.firstsori.global.common.exception.ErrorCode;
import com.goormthon.backend.firstsori.global.common.response.CustomException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicEventConsumer {

    private static final String MUSIC_EVENT_STREAM = "chart:music_event_stream";
    private static final String POPULAR_MUSIC_KEY = "chart:popular_music_sorted_set";

    private static final String GROUP_NAME = "music_event_group"; // 메세지를 처리하는 소비자 그룹 이름
    private static final String CONSUMER_NAME = "consumer_1"; // 그룹 내 comsumer를 구분하는 id (추후에 확장할 예정)

    private final RedisTemplate<String, String> stringRedisTemplate;

    @PostConstruct
    public void initGroup() {
        try {
            stringRedisTemplate.opsForStream().createGroup(MUSIC_EVENT_STREAM, GROUP_NAME);
        } catch (Exception e) {
            log.warn("Stream Group '{}'이 이미 존재합니다.", GROUP_NAME);
        }
    }

    // 인기차트
    @Scheduled(fixedDelay = 60000)
    public void consume() {

        // Group 설정
        Consumer consumer = Consumer.from(GROUP_NAME, CONSUMER_NAME);

        // Read 옵션 설정: 10개 카운트, 10초 블록 대기 (1분 fixedDelay 내에서 적절히 조정)
        StreamReadOptions readOptions = StreamReadOptions.empty().count(10).block(Duration.ofSeconds(10));

        // StreamOffset 설정: 그룹의 마지막 처리 오프셋 이후부터 읽기 위해 ">" (latest) 사용
        StreamOffset<String> streamOffset = StreamOffset.create(MUSIC_EVENT_STREAM, ReadOffset.lastConsumed());

        // Redis Stream에서 메시지 읽기 (read 대신 readGroup 사용)
        List<MapRecord<String, Object, Object>> messages = stringRedisTemplate.opsForStream().read(
                consumer,
                readOptions,
                streamOffset
        );
        // 메시지가 없으면 종료
        if (messages == null || messages.isEmpty()) return;

        for (MapRecord<String, Object, Object> message : messages) {
            try {
                Map<Object, Object> valueMap = message.getValue();

                // 메시지에서 songName 추출
                String songName = String.valueOf(valueMap.get("songName"));
                String timestamp = String.valueOf(valueMap.get("timestamp"));

                if (songName == null || songName.isBlank()) {
                    log.warn("잘못된 메시지: {}", message);
                    continue;
                }

                // ZSet에 해당 곡의 점수 1 증가 (인기 차트 반영)
                stringRedisTemplate.opsForZSet().incrementScore(POPULAR_MUSIC_KEY, songName, 1);  // ZSet에 musicId 점수 증

                // 메시지 처리 완료 후 ack 전송
                stringRedisTemplate.opsForStream().acknowledge(MUSIC_EVENT_STREAM, GROUP_NAME, message.getId());  // 스트림 메시지 ack
                log.info("✅ 소비 완료: {} ({})", songName, timestamp);

            } catch (Exception e) {
                log.error("❌ 스트림 소비 중 오류: {}", e.getMessage(), e);
                throw new CustomException(ErrorCode.STREAM_CONSUME_FAILED);

            }
        }

    }
}
