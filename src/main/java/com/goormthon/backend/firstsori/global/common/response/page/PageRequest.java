package com.goormthon.backend.firstsori.global.common.response.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Schema(
        name = "[요청][페이지] 페이지네이션 기준 Request",
        description = "페이징 조회 시 사용하는 기본 요청 DTO입니다."
)
public class PageRequest {
    @Builder.Default
    @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
    private int pageNumber = 1;

    @Builder.Default
    @Schema(description = "페이지 크기", example = "20")
    private int pageSize = 10;
}
