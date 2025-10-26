package com.goormthon.backend.firstsori.domain.music.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "music")
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

    @Builder
    public Music(String songName, String artist, String albumImageUrl, String songUrl){
        this.songName = songName;
        this.artist = artist;
        this.songUrl = songUrl;
        this.albumImageUrl = albumImageUrl;
    }

}
