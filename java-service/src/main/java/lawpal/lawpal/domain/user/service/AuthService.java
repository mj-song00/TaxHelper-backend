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
import java.util.concurrent.TimeUnit;

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

    // 리프레시 토큰 쿠키 설정
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge((long) 7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 사용자 탈퇴 여부 확인
    public void validateUserNotDeleted(User user) {
        if (user.getDeletedAt() != null) {
            throw new BaseException(ExceptionEnum.ALREADY_DELETED);
        }
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken(String email) {
        User user = findByEmail(email);
        return jwtUtil.createRefreshToken(user.getId());
    }

    public void saveRefreshToken(String email, String refreshToken) {
        String key = "refresh:" + email;
        redisTemplate.opsForValue().set(key, refreshToken, 7, TimeUnit.DAYS);
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

    //AccessToken 재발급
    @Transactional
    public String refreshAccessToken(String refreshToken, HttpServletResponse response) {
        User user = validateRefreshToken(refreshToken);
        long expiration = jwtUtil.getRemainingExpiration(refreshToken);

        try {
            refreshTokenRepository.addBlacklist(refreshToken, expiration);

            String newRefreshToken = generateRefreshToken(user.getEmail());
            saveRefreshToken(user.getEmail(), newRefreshToken);
            setRefreshTokenCookie(response, newRefreshToken);
        } catch (Exception e) {
            log.error("Redis 처리 실패", e);
        }

        return generateAccessToken(user);

    }
}
