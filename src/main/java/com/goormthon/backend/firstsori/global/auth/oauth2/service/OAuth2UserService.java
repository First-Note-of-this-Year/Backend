package com.goormthon.backend.firstsori.global.auth.oauth2.service;

import com.goormthon.backend.firstsori.domain.user.application.usecase.UserUseCase;
import com.goormthon.backend.firstsori.domain.user.domain.entity.User;
import com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Provider;
import com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Role;
import com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Status;
import com.goormthon.backend.firstsori.global.auth.oauth2.domain.OAuth2UserInfo;
import com.goormthon.backend.firstsori.global.auth.oauth2.domain.PrincipalDetails;
import com.goormthon.backend.firstsori.global.auth.oauth2.domain.kakao.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserUseCase userUseCase;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 유저 정보(attributes) 가져오기
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

        // resistrationId 가져오기
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // userNameAttributeName 가져오기
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        log.info(registrationId + ": " + userNameAttributeName);

        // OAuth2를 바탕으로 정보 생성
        /// Provider 에 따른 유저 생성
        OAuth2UserInfo userInfo = createOAuth2User(registrationId, oAuth2UserAttributes);
        Provider social = Provider.valueOf(userInfo.getProvider());
        Optional<User> existUser = userUseCase.loadUserBySocialAndSocialId(social, userInfo.getProviderId());

        // 존재한다면 로그인
        if (existUser.isPresent()) {
            ///  이미 존재하는 유저를 반환한다./
            log.info("이미 로그인한 유저입니다.");
            return PrincipalDetails.of(existUser.get(), oAuth2UserAttributes);
        } else {
            // 이전 로그인 기록 없는 새 유저
            ///  정보를 통해 임시 저장한 뒤, 개인정보 추가하도록 한다.

            // 존재하지 않는 경우 신규 유저 생성
            User newUser = User.builder()
                    .email(userInfo.getEmail())
                    .nickname(userInfo.getUserName())
                    .profileImage(userInfo.getImageUrl())
                    .role(Role.USER)
                    .status(Status.ACTIVE)
                    .provider(social)
                    .socialId(userInfo.getProviderId())
                    .build();

            User savedUser = userUseCase.saveUser(newUser);
            return PrincipalDetails.of(savedUser, oAuth2UserAttributes);
        }
    }

    private OAuth2UserInfo createOAuth2User(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toUpperCase()) {
            case "KAKAO":
                return new KakaoUserInfo(attributes);
            default:
                throw new IllegalArgumentException("Unknown provider: " + registrationId);
        }
    }
}
