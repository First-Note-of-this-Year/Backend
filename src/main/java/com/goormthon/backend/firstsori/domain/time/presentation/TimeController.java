package com.goormthon.backend.firstsori.domain.time.presentation;

import com.goormthon.backend.firstsori.domain.time.application.dto.response.GetServerTimeResponse;
import com.goormthon.backend.firstsori.domain.time.application.usecase.TimeUseCase;
import com.goormthon.backend.firstsori.domain.time.presentation.spec.TimeControllerSpec;
import com.goormthon.backend.firstsori.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/time")
@RequiredArgsConstructor
public class TimeController implements TimeControllerSpec {

    private final TimeUseCase timeUseCase;

    @Override
    public ApiResponse<GetServerTimeResponse> getServerTime() {
        GetServerTimeResponse response = timeUseCase.getServerTime();
        return ApiResponse.ok(response);
    }
}

