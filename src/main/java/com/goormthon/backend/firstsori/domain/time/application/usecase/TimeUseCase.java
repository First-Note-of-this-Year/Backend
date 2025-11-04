package com.goormthon.backend.firstsori.domain.time.application.usecase;

import com.goormthon.backend.firstsori.domain.time.application.dto.response.GetServerTimeResponse;

public interface TimeUseCase {

    GetServerTimeResponse getServerTime();

}

