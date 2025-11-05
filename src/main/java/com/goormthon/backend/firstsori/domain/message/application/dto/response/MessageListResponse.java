package com.goormthon.backend.firstsori.domain.message.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

// 메시지 목록 응답 DTO
@Builder
@Schema(description = "보드에 있는 메시지 목록 응답")
public record MessageListResponse(
        @Schema(description = "메시지 고유 ID")
        UUID messageId,

        @Schema(description = "메시지 발신자 닉네임")
        String sender,

        @Schema(description = "커버 이미지 URL")
        String coverImage,

        @Schema(description = "메시지 읽음 여부")
        boolean read
) {
}
