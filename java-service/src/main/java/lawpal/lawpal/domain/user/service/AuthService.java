package lawpal.lawpal.domain.user.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lawpal.lawpal.common.config.PasswordEncoder;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.common.exception.ExceptionEnum;
import lawpal.lawpal.common.repository.RefreshTokenRepository;
import lawpal.lawpal.domain.user.dto.request.LoginRequest;
import lawpal.lawpal.domain.user.entity.User;
import lawpal.lawpal.domain.user.repository.UserRepository;
import lawpal.lawpal.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RefreshTokenRepository refreshTokenRepository;


    @Transactional
    public void logout(String refreshToken, HttpServletResponse response) {
        User user = validateRefreshToken(refreshToken);

        try {
            //Redis에서 refreshToken 삭제
            redisTemplate.delete("refresh:" + user.getEmail());
        } catch (Exception e) {
            log.error("Redis 처리 실패", e);
        }

        // 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "") // 빈 문자열 사용
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }

    public String login(@Valid LoginRequest loginRequest) {
        User user = findByEmail(loginRequest.getEmail());
        validateUserNotDeleted(user);
        authenticateUser(user, loginRequest.getPassword());
        return generateAccessToken(user);
    }

    // 사용자 탈퇴 여부 확인
    public void validateUserNotDeleted(User user) {
        if (user.getDeletedAt() != null) {
            throw new BaseException(ExceptionEnum.ALREADY_DELETED);
        }
    }

    // 액세스 토큰 생성
    private String generateAccessToken(User user) {
        return jwtUtil.createToken(user.getId(), user.getUserRole());
    }

    // 비밀번호 인증
    public void authenticateUser(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BaseException(ExceptionEnum.EMAIL_PASSWORD_MISMATCH);
        }
    }

    // 이메일로 사용자 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ExceptionEnum.USER_NOT_FOUND));
    }

    private User validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshTokenRepository.isBlacklisted(refreshToken)
                || !jwtUtil.isTokenValid(refreshToken)) {
            throw new BaseException(ExceptionEnum.INVALID_REFRESH_TOKEN);
        }

        Claims claims = jwtUtil.extractClaims(refreshToken);
        UUID userId = UUID.fromString(claims.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ExceptionEnum.USER_NOT_FOUND));

        String storedToken = refreshTokenRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new BaseException(ExceptionEnum.INVALID_REFRESH_TOKEN));

        if (!storedToken.equals(refreshToken)) {
            throw new BaseException(ExceptionEnum.INVALID_REFRESH_TOKEN);
        }

        return user;
    }

}
