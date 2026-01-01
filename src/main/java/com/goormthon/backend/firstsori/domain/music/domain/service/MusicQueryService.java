package com.goormthon.backend.firstsori.domain.music.domain.service;

import com.goormthon.backend.firstsori.domain.music.domain.entity.Music;
import com.goormthon.backend.firstsori.domain.music.domain.repository.MusicRepository;
import com.goormthon.backend.firstsori.global.common.exception.ErrorCode;
import com.goormthon.backend.firstsori.global.common.response.CustomException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MusicQueryService {

    private final MusicRepository musicRepository;

    @Transactional(readOnly = true)
    public Music findExisting(Music music) {
        return musicRepository
                .findBySongNameAndArtist(music.getSongName(), music.getArtist())
                .orElseThrow(() -> new CustomException(ErrorCode.MUSIC_CONSISTENCY_ERROR));
    }
}
