 package com.goormthon.backend.firstsori.domain.board.domain.entity;

import com.goormthon.backend.firstsori.domain.message.domain.entity.Message;
import com.goormthon.backend.firstsori.domain.user.domain.entity.User;
import com.goormthon.backend.firstsori.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

 @Entity
 @Getter
 @NoArgsConstructor(access = AccessLevel.PROTECTED)
 @Table(name = "boards")
 @AllArgsConstructor
 @Builder
 @org.hibernate.annotations.DynamicUpdate
 public class Board extends BaseTimeEntity {

     @Id
     @GeneratedValue(generator = "UUID")
     @Column(name = "board_id", nullable = false)
     private UUID boardId;

     @OneToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id", nullable = false, unique = true)
     private User user;

     @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
     @Builder.Default
     private List<Message> messages = new ArrayList<>();

     @Column(name="nickname", nullable = false)
     private String nickname;

     @Column(name = "share_uri", nullable = false, unique = true, length = 12)
     private String shareUri;

    @Column(nullable = false)
    @Builder.Default
    private int messageCount = 0;

    public void incrementMessageCount(int redisCount) {
         this.messageCount += redisCount;
     }

    public void decrementMessageCount() {
         if (this.messageCount > 0) {
             this.messageCount--;
         }
     }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

 }
