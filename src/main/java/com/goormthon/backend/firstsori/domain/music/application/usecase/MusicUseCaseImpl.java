package com.goormthon.backend.firstsori.domain.music.application.usecase;

import com.goormthon.backend.firstsori.domain.music.application.dto.response.GetSearchResultResponse;
import com.goormthon.backend.firstsori.domain.music.application.dto.response.MusicChartResponse;
import com.goormthon.backend.firstsori.domain.music.domain.service.GetMusicSearchResultService;
import com.goormthon.backend.firstsori.domain.music.domain.service.GetPopularMusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MusicUseCaseImpl implements MusicUseCase {

    private final GetMusicSearchResultService getMusicSearchResultService;
    private final GetPopularMusicService getMusicChartService;


    @Override
    public GetSearchResultResponse getSearchResult(String keyword) {
        return new GetSearchResultResponse(getMusicSearchResultService.getSearchResult(keyword));
    }

    @Override
    public List<MusicChartResponse> getPopularMusic(int topN) {
        return getMusicChartService.getPopularMusic(topN);
    }
}
