package com.goormthon.backend.firstsori.domain.music.domain.util;

import com.goormthon.backend.firstsori.domain.music.application.dto.response.SpotifySearchApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class SpotifyApiClient {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final WebClient webClient = WebClient.builder().baseUrl("https://api.spotify.com/v1").build();


    public SpotifySearchApiResponse searchMusicRaw(String keyword) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("spotify")
                .principal("spotify-client") // 임의의 principal
                .build();

        OAuth2AuthorizedClient authorizedClient =
                authorizedClientManager.authorize(authorizeRequest);

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        // Spotify API 호출 + 응답 DTO 매핑
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", keyword)
                        .queryParam("type", "track")
                        .queryParam("limit", 40)
                        .build())
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(SpotifySearchApiResponse.class)
                .block();

    }
}

