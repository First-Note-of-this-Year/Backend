package com.goormthon.backend.firstsori.domain.music.application.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SpotifySearchApiResponse {
    private Tracks tracks;

    @Data
    public static class Tracks {
        private List<Item> items;
    }

    @Data
    public static class Item {
        private String id;
        private String name;
        private List<Artist> artists;
        private ExternalUrls external_urls;
        private String preview_url;
        private Album album;
    }

    @Data
    public static class Album {
        private List<Image> images;
    }

    @Data
    public static class Image {
        private String url;
        private Integer width;
        private Integer height;
    }

    @Data
    public static class Artist {
        private String name;
    }

    @Data
    public static class ExternalUrls {
        private String spotify;
    }
}

