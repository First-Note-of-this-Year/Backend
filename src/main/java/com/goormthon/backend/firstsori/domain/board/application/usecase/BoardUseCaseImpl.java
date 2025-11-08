package com.goormthon.backend.firstsori.domain.board.application.usecase;

import com.goormthon.backend.firstsori.domain.board.application.dto.request.CreateBoardRequest;
import com.goormthon.backend.firstsori.domain.board.application.dto.request.UpdateBoardRequest;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.CreateBoardResponse;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.GetShareUriResponse;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.BoardInfoResponse;
import com.goormthon.backend.firstsori.domain.board.application.dto.response.UpdateBoardResponse;
import com.goormthon.backend.firstsori.domain.board.application.mapper.BoardMapper;
import com.goormthon.backend.firstsori.domain.board.domain.entity.Board;
import com.goormthon.backend.firstsori.domain.board.domain.repository.BoardRepository;
import com.goormthon.backend.firstsori.domain.board.domain.service.GetBoardService;
import com.goormthon.backend.firstsori.domain.board.domain.service.S3UploadService;
import com.goormthon.backend.firstsori.domain.user.application.usecase.UserUseCase;
import com.goormthon.backend.firstsori.domain.user.domain.entity.User;
import com.goormthon.backend.firstsori.domain.user.domain.repository.UserRepository;
import com.goormthon.backend.firstsori.global.auth.jwt.util.JwtTokenExtractor;
import com.goormthon.backend.firstsori.domain.user.domain.service.GetUserService;
import com.goormthon.backend.firstsori.global.common.exception.ErrorCode;
import com.goormthon.backend.firstsori.global.common.response.CustomException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager; 
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardUseCaseImpl implements BoardUseCase {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final S3UploadService s3UploadService;
    private final GetBoardService getBoardService;

    @PersistenceContext
    private final EntityManager entityManager;

    @Transactional
    @Override
    public CreateBoardResponse createBoard(CreateBoardRequest request, User user) {

        String nickname = request.getNickname();

        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        Board existingBoard = boardRepository.findByUser(user).orElse(null);
        if (existingBoard != null) {
            // 이미 존재하면 기존 보드 반환
            return new CreateBoardResponse(existingBoard.getBoardId(), user.getUserId(),existingBoard.getNickname(), existingBoard.getShareUri());
        }

        // 공유 URI 중복만 방지 (닉네임은 중복 허용)
        String shareUri = generateUniqueShareUri();

        Board board = Board.builder()
                .user(user)
                .nickname(nickname)
                .shareUri(shareUri)
                .build();

        Board saved = boardRepository.save(board);

        return CreateBoardResponse.builder()
                .boardId(saved.getBoardId())
                .userId(saved.getUser().getUserId())
                .nickname(saved.getNickname())
                .shareUri(saved.getShareUri())
                .build();
    }

    private String generateUniqueShareUri() {
        String candidate;
        do {
            candidate = RandomStringUtils.randomAlphanumeric(12);
        } while (boardRepository.existsByShareUri(candidate));
        return candidate;
    }

    private String extractTokenFromBearer(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }

    @Transactional(readOnly = true)
    @Override
    public GetShareUriResponse getShareUriByUser(User user) {

        Board board = boardRepository.findByUser(user)
                .orElse(null);

        if (board == null) {
            return GetShareUriResponse.builder()
                    .boardId(null)
                    .shareUri(null)
                    .build();
        }

        return GetShareUriResponse.builder()
                .boardId(board.getBoardId())
                .shareUri(board.getShareUri())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public BoardInfoResponse getBoardInfo(String shareUri) {
        Board board = getBoardService.getBoardBySharedId(shareUri);

        return BoardMapper.toBoardInfoResponse(board.getNickname(), board.getUser().getProfileImage(), board.getMessageCount());
    }

    @Transactional
    @Override
    public UpdateBoardResponse updateBoard(UpdateBoardRequest request, User user) {
        // 1. 사용자의 보드 조회
        Board board = boardRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        User finalUser = user;
        
        // 2. 사용자 프로필 이미지 반영 (이미지 요청이 있는 경우에만 실행)
        if (request.getProfileImage() != null) { 
            // A. 기존 이미지 삭제
            if (user.getProfileImage() != null) {
                try {
                    s3UploadService.deleteImage(user.getProfileImage());
                } catch (CustomException e) {
                    log.warn("기존 프로필 이미지 S3 삭제 실패: {}", user.getProfileImage(), e);
                }
            }

            // B. 새 이미지 S3에 업로드 및 C. User 엔티티에 새 URL 저장
            String newProfileImageUrl = s3UploadService.uploadImage(request.getProfileImage());
            user.update(null, null, newProfileImageUrl);
            finalUser = userRepository.saveAndFlush(user); // User 엔티티 변경사항 DB 반영 및 finalUser 갱신
        }

        // 3. 닉네임 업데이트 (닉네임 요청이 있는 경우에만 실행)
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            board.updateNickname(request.getNickname());
        
            // ⭐ 변경 1: flush를 통해 DB에 변경사항을 강제 반영합니다.
            boardRepository.save(board); 
            boardRepository.flush();
        
            // ⭐ 변경 2: EntityManager.refresh()를 사용하여 DB의 최신값을 메모리 객체로 강제 로드합니다.
            // board 객체의 닉네임이 최신 값으로 갱신됩니다.
            entityManager.refresh(board); 
        }
        
        // 4. 응답 생성
        // 닉네임은 board 엔티티에서 최신 값이 반영된 상태입니다.
        // 프로필 이미지는 finalUser 엔티티에서 최신 값이 반영된 상태입니다.

        return UpdateBoardResponse.builder()
                .boardId(board.getBoardId())
                .userId(finalUser.getUserId())
                .nickname(board.getNickname())
                .profileImage(finalUser.getProfileImage())
                .shareUri(board.getShareUri())
                .build();
    }
}


