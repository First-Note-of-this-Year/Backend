package com.goormthon.backend.firstsori.domain.time.domain.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GetServerTimeService {

    /**
     * 서버의 현재 시간을 반환합니다.
     * 향후 타임존 확장 시 이 메서드를 수정하여 타임존 기능을 추가할 수 있습니다.
     *
     * @return 서버의 현재 시간
     */
    public LocalDateTime getServerTime() {
        return LocalDateTime.now();
    }
}

