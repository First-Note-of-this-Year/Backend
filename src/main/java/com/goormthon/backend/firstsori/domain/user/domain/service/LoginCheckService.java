package com.goormthon.backend.firstsori.domain.user.domain.service;

import com.goormthon.backend.firstsori.domain.board.domain.repository.BoardRepository;
import com.goormthon.backend.firstsori.domain.user.application.dto.response.LoginCheckResponse;
import com.goormthon.backend.firstsori.domain.user.domain.entity.User;
import com.goormthon.backend.firstsori.global.auth.oauth2.domain.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginCheckService {

    private final BoardRepository boardRepository;

    public LoginCheckResponse checkLogin(PrincipalDetails principal, String shareUri) {
        if (principal == null || principal.getUser() == null) {
            return LoginCheckResponse.invalidToken();
        }

        User user = principal.getUser();

        boolean isOwner = boardRepository.findByShareUri(shareUri)
                .map(board -> board.getUser().equals(user))
                .orElse(false);

        return LoginCheckResponse.of(user, isOwner);
    }
}
