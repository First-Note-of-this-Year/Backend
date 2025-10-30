package com.goormthon.backend.firstsori.domain.music.domain.service;

import com.goormthon.backend.firstsori.domain.music.domain.entity.Music;
import com.goormthon.backend.firstsori.domain.music.domain.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SaveMusicService {

    private final MusicRepository musicRepository;
    private final MusicEventProducer musicEventProducer;
    private final MusicAsyncService musicAsyncService;

    public Music saveMusic(Music music) {
        Optional<Music> existsMusic = musicRepository.findBySongNameAndArtist(music.getSongName(), music.getArtist());
        Music targetMusic;

        if (existsMusic.isEmpty()) {
            targetMusic = musicRepository.save(music);
            log.info("DB에 새로운 음악 저장 완료: 곡='{}'", targetMusic.getSongName());

        } else {
            targetMusic = existsMusic.get();
            log.info("음악이 이미 존재함: 곡='{}'",  targetMusic.getSongName());

        }

        // 2. 트랜잭션 커밋 성공 시 비동기 작업 등록
        // DB 변경사항이 Redis 및 Stream에 반영되기 전에 실패하는 것을 방지
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                log.info("✅ 트랜잭션 커밋 성공. 비동기 이벤트 및 캐시 TTL 연장 작업 시작.");

                // 2.1. 검색 캐시 TTL 연장 (비동기)
                musicAsyncService.extendSearchCacheTtl(targetMusic.getSongName());

                // 2.2. 인기 차트 반영 이벤트 발행 (Stream)
                musicEventProducer.publishMusicEvent(targetMusic.getSongName());
            }
        });

        // 3. 결과 반환 (트랜잭션 커밋은 이 메서드 종료 후 Spring에 의해 실행)
        return targetMusic;
    }



}
