package com.goormthon.backend.firstsori.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @Async 어노테이션이 붙은 메서드를 발견했을 때,
 * 실제로 비동기(다른 스레드)에서 실행되도록 AOP 프록시를 생성
 * **/
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 코어 스레드 수: 기본으로 유지할 스레드 수
        executor.setCorePoolSize(5);
        // 최대 스레드 수: 요청 폭주시 최대 스레드 수
        executor.setMaxPoolSize(10);
        // 큐 용량: 요청이 대기할 수 있는 큐의 크기
        executor.setQueueCapacity(25);
        // 스레드 이름 접두사: 로그 추적을 용이하게 합니다.
        executor.setThreadNamePrefix("Async-Music-");
        executor.initialize();
        return executor;
    }
}
