package com.goormthon.backend.firstsori.domain.user.application.dto.response;

import com.goormthon.backend.firstsori.domain.user.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginCheckResponse {
    private boolean validUser;
    private boolean isOwner;
    private boolean invalidToken;

    public static LoginCheckResponse invalidToken() {
        return LoginCheckResponse.builder()
                .validUser(false)
                .isOwner(false)
                .invalidToken(true)
                .build();
    }

    public static LoginCheckResponse of(User user, boolean isOwner) {
        return LoginCheckResponse.builder()
                .validUser(true)
                .isOwner(isOwner)
                .invalidToken(false)
                .build();
    }
}
