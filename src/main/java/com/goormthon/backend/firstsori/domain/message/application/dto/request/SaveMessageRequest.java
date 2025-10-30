package com.goormthon.backend.firstsori.domain.message.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "메시지 저장 요청 DTO")
public record SaveMessageRequest(
        @Schema(description = "Board의 shareUri", example = "P5ZPBUXLNLYw")
        @NotBlank
        String shareUri,

        @Schema(description = "닉네임", example = "홍길동", maxLength = 30)
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 30, message = "닉네임은 30자 이하로 입력해주세요.")
        String senderName,

        @Schema(description = "내용", example = "새해 복 많이 많으세요:)", maxLength = 500)
        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 500, message = "내용은 500자 이하로 입력해주세요.")
        String content,

        @Schema(description = "노래 제목", example = "이루리", maxLength = 100)
        @NotBlank(message = "노래 제목은 필수입니다.")
        @Size(max = 100, message = "노래 제목은 100자 이하로 입력해주세요.")
        String songTitle,

        @Schema(description = "아티스트 이름", example = "우주소녀", maxLength = 50)
        @NotBlank(message = "아티스트는 필수입니다.")
        @Size(max = 50, message = "아티스트 이름은 50자 이하로 입력해주세요.")
        String artist,

        @Schema(description = "앨범 이미지 URL", example = "https://is1-ssl.mzstatic.com/image/thumb/Music118/v4/45/ca/4f/45ca4f34-7daf-7199-c71d-ae56cc26105d/Rise_From_The_Ashes_cover.jpg/500x500bb.jpg")
        @NotNull(message = "앨범 이미지 URL은 필수입니다.")
        String albumImageUrl,

        @Schema(description = "노래 URL", example = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/96/5e/3d/965e3deb-ab0b-4bcd-07a8-a5eb08e149f8/mzaf_18013741809430634334.plus.aac.p.m4a")
        @NotNull(message = "노래 URL은 필수입니다.")
        String songUrl
) {
}
