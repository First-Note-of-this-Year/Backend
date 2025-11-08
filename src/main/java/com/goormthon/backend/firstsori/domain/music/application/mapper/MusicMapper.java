package com.goormthon.backend.firstsori.domain.music.application.mapper;

import com.goormthon.backend.firstsori.domain.music.application.dto.response.MusicChartResponse;
import com.goormthon.backend.firstsori.domain.music.application.dto.response.SongData;
import com.goormthon.backend.firstsori.domain.music.domain.entity.Music;

public class MusicMapper {

    public static Music toMusicEntity(String songName, String artist, String albumImageUrl, String songUrl, String youtubeUrl, String itunesUrl) {
        return Music.builder()
                .songName(songName)
                .artist(artist)
                .albumImageUrl(albumImageUrl)
                .songUrl(songUrl)
                .youtubeUrl(youtubeUrl)
                .itunesUrl(itunesUrl)
                .build();
    }

    public static MusicChartResponse toMusicChartResponse(Music music,double score) {
        return MusicChartResponse.builder()
                .musicId(music.getMusicId())
                .songTitle(music.getSongName())
                .artist(music.getArtist())
                .coverImage(music.getAlbumImageUrl())
                .songUrl(music.getSongUrl())
                .score(score)
                .build();
    }

    public static SongData toSongData(Music music) {
        return SongData.builder()
                .songTitle(music.getSongName())
                .artist(music.getArtist())
                .coverImage(music.getAlbumImageUrl())
                .songUrl(music.getSongUrl())
                .itunesUrl(music.getItunesUrl())
                .build();
    }
}
