package lawpal.lawpal.filter;


import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lawpal.lawpal.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Slf4j
@Component
public class JwtFilter  implements Filter {
    private final JwtUtil jwtUtil;
    private static final List<String> SWAGGER_WHITELIST = List.of(
            "/swagger-ui", "/swagger-ui.html", "/v3/api-docs",
            "/api-docs", "/v3/api-docs/swagger-config", "/swagger-ui/**"
    );


    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        // 초기화 로직이 필요한 경우 구현
//    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH");
            httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }



        String url = httpRequest.getRequestURI();

        // Swagger 경로는 JWT 검증 없이 통과
        if (SWAGGER_WHITELIST.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        /**
         * 회원가입, 로그인, 코인 시세 api는 jwt 토큰 불필요
         */
        if (url.startsWith("/api/v1/users/sign-up") || url.startsWith("/api/v1/users/auth/sign-in")
                || (url.startsWith("/swagger-ui") || url.startsWith("/v3/api-docs"))
                || (url.startsWith("/api/v1/users/auth/refresh-token"))
                || (url.startsWith("/api/v1/users/auth/logout"))
                || (url.startsWith("/api/v2/admin/sign-up"))
                || (url.startsWith("/api/v2/admin/sign-in"))
                || (url.startsWith("/api/v1/coin/price"))
        ) {
            chain.doFilter(request, response);
            return;
        }

        // 정적 리소스에 대한 요청은 JWT 검증을 생략
        if (url.startsWith("/images/") || url.startsWith("/css/") || url.startsWith("/js/") || url.startsWith("/webjars/")) {
            chain.doFilter(request, response);
            return;
        }


        // 헤더에서 Authorization 토큰을 가져옵니다.
        String authorizationHeader = httpRequest.getHeader("Authorization");


        // refreshToken 재발급 API인 경우 쿠키에서 refreshToken 꺼내기
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            if (httpRequest.getCookies() != null) {
                for (Cookie cookie : httpRequest.getCookies()) {
                    if ("refreshToken".equals(cookie.getName())) {
                        authorizationHeader = "Bearer " + cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 토큰이 필요합니다.");
            return;
        }

        // 헤더에서 JWT 추출
        String token = jwtUtil.getJwtFromHeader(httpRequest);
        try {
            Claims claims = jwtUtil.extractClaims(token);
            // 토큰을 검증하고, 유효한 경우 필터 체인을 타고 다음 필터로 이동합니다.
            if (jwtUtil.validateToken(token)) {
                // 필요하다면 요청에 사용자 정보를 추가할 수 있습니다.
                httpRequest.setAttribute("id", UUID.fromString(claims.getSubject()));
                httpRequest.setAttribute("username", (claims.get("username", String.class)));
                httpRequest.setAttribute("role", claims.get("role", String.class));
//                httpRequest.setAttribute("isActive", claims.get("isActive",Boolean.class));
//                httpRequest.setAttribute("isSecession", claims.get("isSecession", Boolean.class));

                chain.doFilter(request, response);
            } else {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 토큰이 유효하지 않습니다.");
            }
        } catch (Exception e) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 토큰 검증 중 오류가 발생했습니다.");
        }
    }
}
