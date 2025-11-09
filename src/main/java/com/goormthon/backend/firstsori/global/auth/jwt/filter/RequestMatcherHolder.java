package com.goormthon.backend.firstsori.global.auth.jwt.filter;

import com.goormthon.backend.firstsori.domain.user.domain.entity.enums.Role;
import io.micrometer.common.lang.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpMethod.*;

@Component
public class RequestMatcherHolder {

    private static final List<RequestInfo> REQUEST_INFO_LIST = List.of(

            // 공통
            new RequestInfo(OPTIONS, "/**", null),
            new RequestInfo(GET, "/", null),
            new RequestInfo(GET, "/login", null),
            new RequestInfo(POST, "/api/v1/auth/dev-login", null),
            new RequestInfo(GET, "/api/v1/time", null)

            // auth
            new RequestInfo(POST, "/api/v1/oauth2/**", null),
            new RequestInfo(GET, "/api/v1/oauth2/**", null),
            new RequestInfo(GET, "/oauth2/authorization/**", null),   // Spring Security 기본 엔드포인트
            new RequestInfo(GET, "/login/oauth2/code/**", null),       // callback URL

            // 유저 관련
            new RequestInfo(POST, "/api/v1/auth/reissue", null),
            new RequestInfo(POST, "/api/v1/auth/logout", Role.USER),

            // 메세지 전송
            new RequestInfo(POST, "/api/v1/message", null),

            // board shared
            new RequestInfo(GET, "/api/v1/board/share/**", null),

            // static resources
            new RequestInfo(GET, "/docs/**", null),
            new RequestInfo(GET, "/*.ico", null),
            new RequestInfo(GET, "/resources/**", null),
            new RequestInfo(GET, "/index.html", null),
            new RequestInfo(GET, "/error", null),

            // Swagger UI 및 API 문서 관련 요청
            new RequestInfo(GET, "/v3/api-docs/**", null),
            new RequestInfo(GET, "/swagger-ui/**", null),
            new RequestInfo(GET, "/swagger-resources/**", null),
            new RequestInfo(GET, "/webjars/**", null),
            new RequestInfo(GET, "/swagger-ui.html", null)

    );




    private final ConcurrentHashMap<String, RequestMatcher> reqMatcherCacheMap = new ConcurrentHashMap<>();

    /**
     * 최소 권한이 주어진 요청에 대한 RequestMatcher 반환
     * @param minRole 최소 권한 (Nullable)
     * @return 생성된 RequestMatcher
     */
    public RequestMatcher getRequestMatchersByMinRole(@Nullable Role minRole) {
        var key = getKeyByRole(minRole);
        return reqMatcherCacheMap.computeIfAbsent(key, k ->
                new OrRequestMatcher(REQUEST_INFO_LIST.stream()
                        .filter(reqInfo -> isAccessible(reqInfo.minRole(), minRole))
                        .map(reqInfo -> new AntPathRequestMatcher(reqInfo.pattern(),
                                reqInfo.method().name()))
                        .toArray(AntPathRequestMatcher[]::new)));
    }

    private boolean isAccessible(@Nullable Role requiredRole, @Nullable Role currentRole) {
        if (requiredRole == null) return true; // 누구나 접근 가능
        if (currentRole == null) return false; // 권한 없음
        return currentRole.ordinal() >= requiredRole.ordinal(); // ADMIN이면 MEMBER 포함
    }

    private String getKeyByRole(@Nullable Role minRole) {
        return minRole == null ? "VISITOR" : minRole.name();
    }

    private record RequestInfo(HttpMethod method, String pattern, Role minRole) {}

}
