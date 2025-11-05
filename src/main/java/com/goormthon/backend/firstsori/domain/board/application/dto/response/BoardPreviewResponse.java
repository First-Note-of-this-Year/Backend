package com.goormthon.backend.firstsori.domain.board.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "보드 내 메시지 앨범 커버 미리보기 응답")
public class BoardPreviewResponse {

        @Schema(description = "메시지 ID", example = "ff54b002-7f36-496e-9d8e-cf7dc41556d7")
        private UUID messageId;

        @Schema(description = "음악 ID", example = "b3c22d97-8a45-4a1f-bb9e-6a5a5e3d1234")
        private UUID musicId;

        @Schema(description = "음악 앨범 커버 이미지 URL", example = "https://image.server.com/album/cover.jpg")
        private String coverImage;
}
