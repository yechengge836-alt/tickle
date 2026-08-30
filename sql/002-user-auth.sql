-- 让用户表初始化脚本以 UTF-8 四字节字符集执行。
SET NAMES utf8mb4;

-- 创建平台用户表；IF NOT EXISTS 让脚本可重复执行。
CREATE TABLE IF NOT EXISTS platform_user (
  -- 自增用户主键。
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  -- 登录账号，最长 19 个字符。
  username VARCHAR(19) NOT NULL,
  -- BCrypt 密码哈希，禁止存储密码明文。
  password_hash VARCHAR(100) NOT NULL,
  -- 注册时间。
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  -- 数据库层阻止同名账号重复注册。
  UNIQUE KEY uk_platform_user_username (username),
  -- 数据库层再次限制账号长度为 6 到 19。
  CONSTRAINT ck_platform_user_username_length CHECK (CHAR_LENGTH(username) BETWEEN 6 AND 19)
);
