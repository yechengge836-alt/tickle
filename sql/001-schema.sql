-- 让初始化脚本以 UTF-8 四字节字符集执行，支持中文和 Emoji。
SET NAMES utf8mb4;

-- 创建活动表，保存演出或活动的基本信息与库存。
CREATE TABLE activity (
  -- 自增主键。
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  -- 活动展示名称。
  name VARCHAR(100) NOT NULL,
  -- 初始化时配置的总票数。
  total_stock INT NOT NULL,
  -- 当前仍可售的票数。
  available_stock INT NOT NULL,
  -- 活动售票状态，例如 ON_SALE。
  status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE',
  -- 活动开始时间。
  start_at DATETIME NOT NULL,
  -- 活动结束时间。
  end_at DATETIME NOT NULL,
  -- 记录创建时间。
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  -- 防止库存被写成负数。
  CHECK (available_stock >= 0)
);

-- 创建订单表，保存每一次成功下单的记录。
CREATE TABLE ticket_order (
  -- 自增订单主键。
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  -- 前端生成的请求幂等标识。
  request_id VARCHAR(64) NOT NULL,
  -- 被购买的活动 ID。
  activity_id BIGINT NOT NULL,
  -- 已登录购票用户的 ID。
  user_id BIGINT NOT NULL,
  -- 本订单购买的票数。
  quantity INT NOT NULL,
  -- 订单处理状态，默认创建完成。
  status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
  -- 订单创建时间。
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  -- 同一请求只能对应一个订单。
  UNIQUE KEY uk_request_id (request_id),
  -- 同一用户对同一活动只能有一个订单。
  UNIQUE KEY uk_user_activity (user_id, activity_id),
  -- 确保订单引用存在的活动。
  CONSTRAINT fk_order_activity FOREIGN KEY (activity_id) REFERENCES activity(id)
);

-- 插入一个可用于演示和联调的活动。
INSERT INTO activity (name, total_stock, available_stock, status, start_at, end_at)
-- 设置活动名称、库存、售卖状态与开始结束时间。
VALUES ('2026 校园文化节开幕演出', 1000, 1000, 'ON_SALE', '2026-09-15 19:00:00', '2026-09-15 22:00:00');
