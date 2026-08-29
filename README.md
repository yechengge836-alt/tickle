# 校园文化节分布式票务平台（MVP）

这是一个针对简历项目复现的 Java 后端 MVP。它覆盖活动查询、Redis 库存缓存、Lua 原子扣减、订单幂等、防重复购票、MySQL 持久化和 Kafka 异步事件消费。

## 架构

```text
Client
  │ POST /api/v1/ticket-orders
  ▼
Spring Boot ── Lua 原子扣减 ── Redis（余票 + 请求幂等键）
  │
  ├── MySQL（活动库存 + 订单，事务持久化）
  └── AFTER_COMMIT 事件 ── Kafka ── 通知消费者（示例）
```

库存扣减使用 Lua 脚本，让“检查库存、扣减库存、标记请求处理中”在 Redis 内原子完成，避免并发请求把余票扣成负数。MySQL 更新同样带有 `available_stock >= quantity` 条件，作为最终库存防线。若数据库事务失败，服务会补偿 Redis 库存。

## 启动

前置条件：JDK 17、Maven 3.9+、Docker Desktop。

```bash
docker compose up -d
mvn spring-boot:run
```

初始活动 ID 为 `1`，初始库存为 `1000`。

## 接口

查询活动：

```http
GET /api/v1/activities/1
GET /api/v1/activities/1/stock
```

创建订单：

```http
POST /api/v1/ticket-orders
Content-Type: application/json

{
  "activityId": 1,
  "userId": 10001,
  "quantity": 1,
  "requestId": "a8ac3388-63e6-4c51-a98c-a0e69d90c001"
}
```

`requestId` 是客户端幂等键：同一请求重复提交会返回原订单；同一用户对同一活动重复购票会被拒绝。

## 当前边界与下一步

这是教学型 MVP，不含登录鉴权、支付、超时取消、库存预热管理后台、Kafka 重试/DLQ 和分布式事务 Outbox。生产化时，建议优先增加 Outbox 表、消费者幂等记录、限流与压测脚本。
# Ticket Platform

## 前端展示页

项目包含一个面向用户的活动购票展示页，位于 `frontend/`。它会通过 Vite 开发代理访问当前 Spring Boot 服务的 `4154` 端口，因此不需要额外配置跨域。

先启动基础服务和后端，再在另一个 PowerShell 窗口运行：

```powershell
cd frontend
pnpm install
pnpm dev
```

浏览器访问 `http://127.0.0.1:5173`。页面支持：

- 加载活动详情与实时余票；
- 输入用户 ID、调整数量并提交购票请求；
- 下单成功后自动刷新库存；
- 当后端未启动时展示可交互的演示数据。

## 登录与注册

页面右上角提供“登录 / 注册”。账号和密码均限制为 6–19 个字符；注册成功后需要使用该账号登录，购票时会自动使用已登录用户。

密码仅以 BCrypt 摘要形式保存在 MySQL。后端启动时会自动创建 `platform_user` 表；如果希望在 MySQL Workbench 中立即同步，也可以执行 `sql/002-user-auth.sql`。

接口：

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
