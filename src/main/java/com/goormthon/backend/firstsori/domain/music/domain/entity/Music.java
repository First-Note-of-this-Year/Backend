package com.goormthon.backend.firstsori.domain.music.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "music",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_music_song_artist",
                        columnNames = {"song_name", "artist"}
                )
        }
)
public class Music {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "music_id", nullable = false)
    private UUID musicId;

    @Column(nullable = false)
    private String songName;

    @Column(nullable = false)
    private String artist;

    @Column(nullable = false)
    private String songUrl;

    @Column(nullable = true)
    private String albumImageUrl;

    @Column(nullable = true)
    private String youtubeUrl;
    
    @Column(nullable = true)
    private String itunesUrl;

    @Builder
    public Music(String songName, String artist, String albumImageUrl, String songUrl, String youtubeUrl, String itunesUrl){
        this.songName = songName;
        this.artist = artist;
        this.songUrl = songUrl;
        this.albumImageUrl = albumImageUrl;
        this.youtubeUrl = youtubeUrl;
        this.itunesUrl = itunesUrl;
    }

}
