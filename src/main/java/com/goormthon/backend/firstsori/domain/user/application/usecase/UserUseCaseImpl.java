package com.goormthon.backend.firstsori.domain.user.application.usecase;

import com.goormthon.backend.firstsori.domain.user.domain.entity.User;
import com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Provider;
import com.goormthon.backend.firstsori.domain.user.domain.repository.UserRepository;
import com.goormthon.backend.firstsori.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserUseCaseImpl implements UserUseCase {

    private final UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void updateUser(User user) {
        var entity = userRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));
        entity.update(user.getNickname(), user.getEmail(), user.getProfileImage());
    }

    @Override
    public boolean checkExistingById(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public Optional<User> loadUserById(UUID userId) {
        return userRepository.findByUserId(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public Optional<User> loadUserBySocialAndSocialId(Provider social, String socialId) {
        return userRepository.findByProviderAndSocialId(social, socialId);
    }
}

