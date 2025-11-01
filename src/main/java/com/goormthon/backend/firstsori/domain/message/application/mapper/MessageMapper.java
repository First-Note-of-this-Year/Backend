package com.goormthon.backend.firstsori.domain.message.application.mapper;

import com.goormthon.backend.firstsori.domain.board.application.dto.response.BoardPreviewResponse;
import com.goormthon.backend.firstsori.domain.board.domain.entity.Board;
import com.goormthon.backend.firstsori.domain.message.application.dto.request.SaveMessageRequest;
import com.goormthon.backend.firstsori.domain.message.application.dto.response.MessageListResponse;
import com.goormthon.backend.firstsori.domain.message.application.dto.response.MessageResponse;
import com.goormthon.backend.firstsori.domain.message.domain.entity.Message;
import com.goormthon.backend.firstsori.domain.music.domain.entity.Music;
import com.goormthon.backend.firstsori.global.common.response.page.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

public class MessageMapper {

    public static MessageResponse toMessageResponse(Message message) {
        if (message == null) {
            return null;
        }
        return MessageResponse.builder()
                .messageId(message.getMessageId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .songId(message.getMusic().getMusicId())
                .songName(message.getMusic().getSongName())
                .artist(message.getMusic().getArtist())
                .coverImageUrl(
                        message.getCustomImageUrl() != null
                                ? message.getCustomImageUrl()
                                : message.getMusic().getAlbumImageUrl()
                )
                .songUrl(message.getMusic().getSongUrl())
                .youtubeUrl(message.getMusic().getYoutubeUrl())
                .itunesUrl(message.getMusic().getItunesUrl())
                .mine(message.getMine())
                .build();
    }

    public static PageResponse<MessageListResponse> toMessageListResponse(Page<Message> messages) {
        List<MessageListResponse> content = messages.stream()
                .map(MessageMapper::toMessageListResponse)
                .collect(Collectors.toList());

        return PageResponse.<MessageListResponse>builder()
                .content(content)
                .pageNumber(messages.getNumber())
                .pageSize(messages.getSize())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .build();
    }

    public static Page<MessageListResponse> toMessageListResponse(Page<Message> messages, Pageable pageable) {
        return messages.map(MessageMapper::toMessageListResponse);
    }

    public static MessageListResponse toMessageListResponse(Message message) {
        if (message == null) {
            return null;
        }
        return MessageListResponse.builder()
                .messageId(message.getMessageId())
                .sender(message.getSenderName())
                .coverImageUrl(
                        message.getCustomImageUrl() != null
                                ? message.getCustomImageUrl()
                                : message.getMusic().getAlbumImageUrl()
                )
                .read(message.getRead())
                .build();
    }

    public static Message toMessageEntity(SaveMessageRequest request,Board board,Music music)
    {
        if (request == null) {
            return null;
        }
        return Message.builder()
                .board(board)
                .senderName(request.senderName())
                .content(request.content())
                .music(music)
                .read(false)
                .customImageUrl(null)
                .build();
    }

    public static BoardPreviewResponse toBoardPreviewResponse(Message message) {
        return BoardPreviewResponse.builder()
                .messageId(message.getMessageId())
                .musicId(message.getMusic().getMusicId())
                .musicCoverUrl(message.getMusic().getAlbumImageUrl())
                .build();
    }

    public static PageResponse<BoardPreviewResponse> toBoardPreviewPageResponse(Page<Message> messages) {
        List<BoardPreviewResponse> content = messages.stream()
                .map(MessageMapper::toBoardPreviewResponse)
                .collect(Collectors.toList());

        return PageResponse.<BoardPreviewResponse>builder()
                .content(content)
                .pageNumber(messages.getNumber())
                .pageSize(messages.getSize())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .build();
    }

}
