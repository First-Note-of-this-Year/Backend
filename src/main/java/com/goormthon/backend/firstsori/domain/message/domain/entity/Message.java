package com.goormthon.backend.firstsori.domain.message.domain.entity;

import com.goormthon.backend.firstsori.domain.board.domain.entity.Board;
import com.goormthon.backend.firstsori.domain.music.domain.entity.Music;
import com.goormthon.backend.firstsori.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 이게 없으면 빈생성자를 무조건 만들어 둬야함 - JPA
@Table(name = "messages")
@SuperBuilder
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String content;

    @Column(nullable = true)
    private Boolean read = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "music_id", nullable = false)
    private Music music;

    @Column(nullable = true)
    private String customImageUrl;  // S3 이미지 URL


}
