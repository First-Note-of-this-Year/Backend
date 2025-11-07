package com.goormthon.backend.firstsori.domain.music.domain.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormthon.backend.firstsori.domain.music.domain.entity.Music;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItunesService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    final String ITUNES_API_PATH = "/search";

    // iTunes Search API를 호출하여 음악 메타데이터를 검색
    public Mono<List<Music>> searchMusic(String term) {
        log.info("iTunes API 호출 시작: term={}", term);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ITUNES_API_PATH)
                        .queryParam("term", term)
                        .queryParam("country", "kr") // 한국 설정
                        .queryParam("media", "music")
                        .queryParam("limit", 5) // 5개만 검색
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseItunesResponse) // JSON 문자열 파싱
                .onErrorResume(e -> {
                    log.error("iTunes API 호출 실패 또는 파싱 오류", e);
                    return Mono.just(Collections.emptyList());
                });
    }

    // iTunes 응답 JSON 문자열을 List<Music> 엔티티로 변환
    private Mono<List<Music>> parseItunesResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode results = root.get("results");

            if (results == null || !results.isArray()) {
                return Mono.just(Collections.emptyList());
            }

            List<Music> musicList = StreamSupport.stream(results.spliterator(), false)
                    .filter(node -> "song".equals(node.path("kind").asText())) // 노래 타입만 필터링
                    .map(this::mapJsonNodeToMusic)
                    .collect(Collectors.toList());

            return Mono.just(musicList);
        } catch (Exception e) {
            log.error("iTunes 응답 파싱 중 오류 발생", e);
            return Mono.error(new RuntimeException("iTunes 응답 파싱 실패", e));
        }
    }

    // 개별 JSON 노드를 Music 엔티티로 매핑
    private Music mapJsonNodeToMusic(JsonNode node) {
       log.info(node.toString());
        String artistName = node.path("artistName").asText();
        String trackName = node.path("trackName").asText();
        // iTunes detail page URL is typically under 'trackViewUrl'
        String itunesUrl = node.path("trackViewUrl").asText();

        return Music.builder()
                .songName(trackName)
                .artist(artistName)
                .songUrl(node.path("previewUrl") != null ? node.path("previewUrl").asText() : null)
                .albumImageUrl(node.path("artworkUrl100").asText().replace("100x100", "500x500")) // 고화질로 변환 시도
                .itunesUrl(itunesUrl)
                .build();
    }


}

