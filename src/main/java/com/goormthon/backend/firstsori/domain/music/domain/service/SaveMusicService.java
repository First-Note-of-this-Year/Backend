package com.goormthon.backend.firstsori.domain.music.domain.service;

import com.goormthon.backend.firstsori.domain.music.domain.entity.Music;
import com.goormthon.backend.firstsori.domain.music.domain.repository.MusicRepository;
import com.goormthon.backend.firstsori.global.common.exception.ErrorCode;
import com.goormthon.backend.firstsori.global.common.response.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveMusicService {

    private final MusicRepository musicRepository;
    private final MusicEventProducer musicEventProducer;
    private final MusicAsyncService musicAsyncService;

    /**
     * music 저장 전용 트랜잭션
     * - 중복 시 DB 유니크 제약에 의해 실패
     * - 실패해도 상위 트랜잭션(message)은 영향받지 않도록 REQUIRES_NEW
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Music saveMusic(Music music) {
        Music targetMusic;

        try {
            targetMusic = musicRepository.saveAndFlush(music);
            log.info("DB에 새로운 음악 저장 완료: 곡='{}'", targetMusic.getSongName());

        } catch (DataIntegrityViolationException e) {
            targetMusic = musicRepository
                    .findBySongNameAndArtist(music.getSongName(), music.getArtist())
                    .orElseThrow(() -> new CustomException(ErrorCode.MUSIC_CONSISTENCY_ERROR));
            ;

            log.info("이미 존재하는 음악 사용: 곡='{}'", targetMusic.getSongName());
        }

        registerAfterCommit(targetMusic);

        return targetMusic;
    }

    // 2. 트랜잭션 커밋 성공 시 비동기 작업 등록
    // DB 변경사항이 Redis 및 Stream에 반영되기 전에 실패하는 것을 방지
    private void registerAfterCommit(Music music) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        log.info("✅ 트랜잭션 커밋 성공. 비동기 이벤트 및 캐시 TTL 연장 작업 시작.");
                        // 2.1. 검색 캐시 TTL 연장 (비동기)
                        musicAsyncService.extendSearchCacheTtl(music.getSongName());

                        // 2.2. 인기 차트 반영 이벤트 발행 (Stream)
                        musicEventProducer.publishMusicEvent(music.getSongName());
                    }
                }
        );
    }

}
