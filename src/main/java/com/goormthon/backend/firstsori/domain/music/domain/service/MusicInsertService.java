package com.goormthon.backend.firstsori.domain.music.domain.service;

import com.goormthon.backend.firstsori.domain.music.domain.entity.Music;
import com.goormthon.backend.firstsori.domain.music.domain.repository.MusicRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
public class MusicInsertService {

    private final MusicRepository musicRepository;

    /**
     * music 저장 전용 트랜잭션
     * - 중복 시 DB 유니크 제약에 의해 실패
     * - 실패해도 상위 트랜잭션(message)은 영향받지 않도록 REQUIRES_NEW
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Music insert(Music music) {
        return musicRepository.saveAndFlush(music);
    }
}
