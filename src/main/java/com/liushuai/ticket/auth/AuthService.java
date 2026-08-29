package com.liushuai.ticket.auth;

import com.liushuai.ticket.common.BusinessException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final String SESSION_KEY = "ticket:session:%s";
    private final UserRepository users;
    private final StringRedisTemplate redis;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository users, StringRedisTemplate redis) { this.users = users; this.redis = redis; }

    public LoginUser register(AuthRequest request) {
        String username = normalize(request.username());
        String password = request.password();
        validateLength(username, "账号");
        validateLength(password, "密码");
        if (users.findByUsername(username).isPresent()) {
            throw new BusinessException(409, "该账号已注册，请直接登录");
        }
        try {
            return users.insert(username, passwordEncoder.encode(password));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(409, "该账号已注册，请直接登录");
        }
    }

    public LoginSession login(AuthRequest request) {
        String username = normalize(request.username());
        String password = request.password();
        validateLength(username, "账号");
        validateLength(password, "密码");
        var user = users.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "账号未注册，请先注册"));
        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(SESSION_KEY.formatted(token), String.valueOf(user.id()), Duration.ofHours(24));
        return new LoginSession(user.id(), user.username(), user.createdAt(), token);
    }

    public long requireUserId(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) throw new BusinessException(401, "请先登录");
        String userId = redis.opsForValue().get(SESSION_KEY.formatted(accessToken));
        if (userId == null) throw new BusinessException(401, "登录已过期，请重新登录");
        return Long.parseLong(userId);
    }

    private String normalize(String username) { return username.trim(); }

    private void validateLength(String value, String fieldName) {
        if (value.length() < 6 || value.length() > 19) {
            throw new BusinessException(400, fieldName + "长度必须为 6-19 个字符");
        }
    }
}
