package com.goormthon.backend.firstsori.domain.user.domain.entity;

import com.goormthon.backend.firstsori.domain.board.domain.entity.Board;
import com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Provider;
import com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Role;
import com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Status;
import com.goormthon.backend.firstsori.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@AllArgsConstructor
@SuperBuilder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = true)
    private String email;

    @Column(name = "name", nullable = true)
    private String nickname;

    @Column(nullable = true)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String socialId;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Board board;

    public void update(String nickname, String email, String profileImage) {
        if (nickname != null) this.nickname = nickname;
        if (email != null) this.email = email;
        if (profileImage != null) this.profileImage = profileImage;
    }
}
