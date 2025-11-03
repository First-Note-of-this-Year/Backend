package com.goormthon.backend.firstsori.domain.message.application.usecase;

import com.goormthon.backend.firstsori.domain.board.application.dto.response.BoardPreviewResponse;
import com.goormthon.backend.firstsori.domain.board.domain.entity.Board;
import com.goormthon.backend.firstsori.domain.board.domain.service.GetBoardService;
import com.goormthon.backend.firstsori.domain.message.application.dto.request.SaveMessageRequest;
import com.goormthon.backend.firstsori.domain.message.application.dto.response.MessageListResponse;
import com.goormthon.backend.firstsori.domain.message.application.dto.response.MessageResponse;
import com.goormthon.backend.firstsori.domain.message.application.mapper.MessageMapper;
import com.goormthon.backend.firstsori.domain.message.domain.entity.Message;
import com.goormthon.backend.firstsori.domain.message.domain.service.GetMessageService;
import com.goormthon.backend.firstsori.domain.message.domain.service.SaveMessageService;
import com.goormthon.backend.firstsori.domain.music.application.mapper.MusicMapper;
import com.goormthon.backend.firstsori.domain.music.domain.entity.Music;
import com.goormthon.backend.firstsori.domain.music.domain.service.SaveMusicService;
import com.goormthon.backend.firstsori.domain.user.domain.entity.User;
import com.goormthon.backend.firstsori.domain.user.domain.service.GetUserService;
import com.goormthon.backend.firstsori.global.common.exception.ErrorCode;
import com.goormthon.backend.firstsori.global.common.response.CustomException;
import com.goormthon.backend.firstsori.global.common.response.page.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageUseCaseImpl implements MessageUseCase {

    private final GetMessageService getMessageService;
    private final GetUserService getUserService;
    private final SaveMessageService saveMessageService;
    private final GetBoardService getBoardService;
    private final SaveMusicService saveMusicService;
    private final RedisTemplate<String, String> redisTemplate; // RedisTemplate 주입

    @Override
    public MessageResponse getMessage(UUID messageId) {

        Message message = getMessageService.getMessage(messageId);
        return MessageMapper.toMessageResponse(message);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<MessageListResponse> getMessages(UUID userId, Pageable pageable) {
        User user=Optional.ofNullable(getUserService.validateUserExists(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Optional.ofNullable(user.getBoard())
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        Page<Message> messages = getMessageService.getMessageList(userId,pageable);
        return MessageMapper.toMessageListResponse(messages);
    }

    @Override
    public void createMessage(SaveMessageRequest request) {

        Board board = getBoardService.getBoardBySharedId(request.shareUri());
        // 음악 정보 엔티티 생성
        Music music = MusicMapper.toMusicEntity(request.songTitle(), request.artist(), request.albumImageUrl(), request.songUrl(),request.itunesUrl(),request.youtubeUrl());
        Music persistedMusic=saveMusicService.saveMusic(music);

        // 메시지 엔티티 생성
        Message message = MessageMapper.toMessageEntity(request, board, persistedMusic);
        saveMessageService.saveMessage(message);

        // 보드의 메시지 카운트를 Redis에서 증가
        String redisKey = "board:messageCount:" + board.getBoardId().toString();
        redisTemplate.opsForValue().increment(redisKey);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<BoardPreviewResponse> getMessagesByBoardShareUri(String shareUri, Pageable pageable) {

        Board board = getBoardService.getBoardBySharedId(shareUri);

        Page<Message> messages = getMessageService.getMessageList(board.getUser().getUserId(), pageable);

        return MessageMapper.toBoardPreviewPageResponse(messages);
    }

}
