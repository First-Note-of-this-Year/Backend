package com.goormthon.backend.firstsori.domain.message.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

// 단일 메시지 상세 정보 응답 DTO
@Builder
@Schema(description = "메시지 상세 정보 응답")
public record MessageResponse(
        @Schema(description = "메시지 고유 ID")
        UUID messageId,

        @Schema(description = "메시지 발신자 닉네임")
        String sender,

        @Schema(description = "메시지 내용")
        String content,

        @Schema(description = "음악 고유 ID")
        UUID musicId,

        @Schema(description = "음악 제목")
        String songTitle,

        @Schema(description = "아티스트 이름")
        String artist,

        @Schema(description = "커버 이미지 URL")
        String coverImage,

        @Schema(description = "음악 URL")
        String songUrl,

        @Schema(description = "유튜브 URL")
        String youtubeUrl,

        @Schema(description = "iTunes URL")
        String itunesUrl,
        
        @Schema(description = "본인 message인지 여부")
        Boolean mine
) {
}
