package com.goormthon.backend.firstsori.domain.board.application.mapper;

import com.goormthon.backend.firstsori.domain.board.application.dto.response.BoardInfoResponse;

import java.time.LocalDateTime;

public class BoardMapper {

    public static BoardInfoResponse toBoardInfoResponse(String nickname,String profileImage,Integer messageCount) {
        return BoardInfoResponse.builder()
                .nickname(nickname)
                .profileImage(profileImage)
                .messageCount(messageCount)
                .serverTime(LocalDateTime.now())
                .build();
    }
}
