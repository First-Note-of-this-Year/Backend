package com.goormthon.backend.firstsori.domain.time.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "[GET] 서버 시간 조회 response",
        description = "서버 시간 조회 응답")
public record GetServerTimeResponse(

        @Schema(description = "서버 시간")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime serverTime
) {
}

