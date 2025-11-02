package com.goormthon.backend.firstsori.domain.music.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

// 음악 메타데이터 DTO
@Schema(description = "음악 메타데이터 응답")
@Builder
public record SongData(

//        @Schema(description = "곡 고유 ID")
//        String songId,

        @Schema(description = "곡 제목")
        String songTitle,

        @Schema(description = "아티스트명 (여러 명일 경우 콤마 구분)")
        String artist,

        @Schema(description = "커버 이미지 URL (앨범 이미지)")
        String coverImage,

        @Schema(description = "iTunes URL")
        String itunesUrl,

        @Schema(description = "youtube URL")
        String youtubeUrl

//        @Schema(description = "미리 듣기 URL (30초 미리듣기 등)")
//        String prestreamingUrl
) {
}
