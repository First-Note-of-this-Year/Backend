package com.goormthon.backend.firstsori.global.auth.oauth2.domain;

import com.goormthon.backend.firstsori.domain.user.domain.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
public class PrincipalDetails implements OAuth2User{

    private final User user;
    private final Map<String, Object> attributes;

    // 생성자
    public static PrincipalDetails of(User user, Map<String, Object> attributes) {
        return PrincipalDetails.builder()
                .user(user)
                .attributes(attributes)
                .build();
    }

    public static PrincipalDetails of(User user) {
        return PrincipalDetails.builder()
                .user(user)
                .attributes(null)
                .build();
    }

    /// 로직
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(this.user.getRole().getRole()));
    }

    @Override
    public String getName() {
        return user.getNickname();
    }

    public UUID getId() {
        return user.getUserId();
    }

    public String getEmail() {
        return user.getEmail();
    }

}
