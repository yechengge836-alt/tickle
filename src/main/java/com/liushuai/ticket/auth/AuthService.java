// 声明认证业务服务所在的包。
package com.liushuai.ticket.auth;

// 导入业务异常类型。
import com.liushuai.ticket.common.BusinessException;
// 导入 Redis 会话过期时间使用的类型。
import java.time.Duration;
// 导入生成随机访问令牌的工具。
import java.util.UUID;
// 导入唯一约束冲突异常。
import org.springframework.dao.DataIntegrityViolationException;
// 导入 BCrypt 密码哈希工具。
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// 导入 Redis 字符串操作模板。
import org.springframework.data.redis.core.StringRedisTemplate;
// 导入 Spring 服务层注解。
import org.springframework.stereotype.Service;

// 注册为认证服务，集中处理注册、登录与会话校验。
@Service
public class AuthService {
    // 定义 Redis 会话键的格式，令牌是唯一部分。
    private static final String SESSION_KEY = "ticket:session:%s";
    // 保存用户数据库仓储。
    private final UserRepository users;
    // 保存 Redis 会话存储工具。
    private final StringRedisTemplate redis;
    // 使用 BCrypt 对密码进行不可逆加盐哈希。
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 注入用户仓储和会话缓存。
    public AuthService(UserRepository users, StringRedisTemplate redis) {
        // 保存用户仓储。
        this.users = users;
        // 保存 Redis 模板。
        this.redis = redis;
    }

    // 创建一个新的平台账号。
    public LoginUser register(AuthRequest request) {
        // 去除账号两端空格，保证注册和登录使用同一格式。
        String username = normalize(request.username());
        // 读取密码原文，仅用于校验和立即哈希。
        String password = request.password();
        // 在服务层再次校验账号长度，防止非 Web 调用绕过注解。
        validateLength(username, "账号");
        // 在服务层再次校验密码长度。
        validateLength(password, "密码");
        // 先快速检查账号是否已存在。
        if (users.findByUsername(username).isPresent()) {
            // 返回可展示的冲突提示。
            throw new BusinessException(409, "该账号已注册，请直接登录");
        }
        // 尝试写入数据库，仍以唯一约束作为并发条件下的最终保护。
        try {
            // 只保存 BCrypt 生成的哈希，不保存明文密码。
            return users.insert(username, passwordEncoder.encode(password));
        } catch (DataIntegrityViolationException exception) {
            // 两个请求并发注册同一账号时统一返回冲突提示。
            throw new BusinessException(409, "该账号已注册，请直接登录");
        }
    }

    // 校验账号密码并创建一天有效期的登录会话。
    public LoginSession login(AuthRequest request) {
        // 使用与注册一致的账号规范化规则。
        String username = normalize(request.username());
        // 读取用户提交的密码。
        String password = request.password();
        // 校验账号长度。
        validateLength(username, "账号");
        // 校验密码长度。
        validateLength(password, "密码");
        // 查找账号，不存在时提示先注册。
        var user = users.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "账号未注册，请先注册"));
        // 用 BCrypt 比较密码与数据库哈希。
        if (!passwordEncoder.matches(password, user.passwordHash())) {
            // 不区分账号或密码的具体错误，减少账号枚举风险。
            throw new BusinessException(401, "账号或密码错误");
        }
        // 生成不可预测的访问令牌。
        String token = UUID.randomUUID().toString();
        // 将令牌映射到用户 ID，并在 24 小时后自动过期。
        redis.opsForValue().set(SESSION_KEY.formatted(token), String.valueOf(user.id()), Duration.ofHours(24));
        // 返回用户资料与令牌给前端保存。
        return new LoginSession(user.id(), user.username(), user.createdAt(), token);
    }

    // 根据请求头令牌解析已登录用户的 ID。
    public long requireUserId(String accessToken) {
        // 缺少令牌时拒绝匿名下单。
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(401, "请先登录");
        }
        // 从 Redis 中读取令牌对应的用户 ID。
        String userId = redis.opsForValue().get(SESSION_KEY.formatted(accessToken));
        // 缓存不存在说明令牌已失效或过期。
        if (userId == null) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
        // 将缓存中的文本 ID 转为 long 类型。
        return Long.parseLong(userId);
    }

    // 统一移除账号首尾空格。
    private String normalize(String username) {
        // 返回规范化账号。
        return username.trim();
    }

    // 校验任意认证字段的长度范围。
    private void validateLength(String value, String fieldName) {
        // 拒绝短于 6 或长于 19 个字符的值。
        if (value.length() < 6 || value.length() > 19) {
            // 构造包含字段名称的清晰业务提示。
            throw new BusinessException(400, fieldName + "长度必须为 6-19 个字符");
        }
    }
}
