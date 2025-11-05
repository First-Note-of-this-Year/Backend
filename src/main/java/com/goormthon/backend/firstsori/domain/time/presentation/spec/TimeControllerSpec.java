package com.goormthon.backend.firstsori.domain.time.presentation.spec;

import com.goormthon.backend.firstsori.domain.time.application.dto.response.GetServerTimeResponse;
import com.goormthon.backend.firstsori.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "시간 관련 API", description = "서버 시간 조회 기능을 제공하는 API 입니다.")
public interface TimeControllerSpec {

    @Operation(
            summary = "서버 시간 조회 API",
            description = "현재 서버의 시간을 반환합니다."
    )
    @GetMapping
    ApiResponse<GetServerTimeResponse> getServerTime();
}

