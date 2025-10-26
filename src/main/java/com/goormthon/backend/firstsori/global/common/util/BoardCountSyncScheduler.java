package com.goormthon.backend.firstsori.global.common.util;

import com.goormthon.backend.firstsori.domain.board.domain.entity.Board;
import com.goormthon.backend.firstsori.domain.board.domain.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoardCountSyncScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final BoardRepository boardRepository;

    /**
     * Redis의 보드 메시지 카운트와 DB를 동기화하는 스케줄러 메서드
     * 30초마다 실행됩니다.
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void syncBoardMessageCounts() {

        List<Board> allBoards = boardRepository.findAll();

        for (Board board : allBoards) {
            String redisKey = "board:messageCount:" + board.getBoardId().toString();
            try {
                // Redis에서 원자적으로 값을 읽고 0으로 초기화
                String countString = redisTemplate.opsForValue().getAndSet(redisKey, "0");

                if (countString != null) {
                    Integer redisCount = Integer.parseInt(countString);

                    if (redisCount > 0) {
                        // DB에 메시지 카운트 증분 반영
                        boardRepository.incrementMessageCountById(board.getBoardId(), redisCount);
                    }
                }
            } catch (Exception e) {
                // 예외 발생 시 Redis 키는 삭제하지 않고 로그에 기록만 함
//                log.error("Board ID: {} 동기화 중 오류 발생 - Redis 카운트 유지", board.getBoardId(), e);
            }
        }
    }
}
