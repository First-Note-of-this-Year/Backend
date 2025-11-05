package com.goormthon.backend.firstsori.domain.time.application.usecase;

import com.goormthon.backend.firstsori.domain.time.application.dto.response.GetServerTimeResponse;
import com.goormthon.backend.firstsori.domain.time.domain.service.GetServerTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeUseCaseImpl implements TimeUseCase {

    private final GetServerTimeService getServerTimeService;

    @Override
    public GetServerTimeResponse getServerTime() {
        return new GetServerTimeResponse(getServerTimeService.getServerTime());
    }
}

