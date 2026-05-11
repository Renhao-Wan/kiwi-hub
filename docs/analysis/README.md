# KiwiHub 微服务 API 接口分析

本目录包含 KiwiHub 微服务的详细 API 接口分析文档。

## 目录结构

```
analysis/
├── kiwi-user-api-analysis.md      # Kiwi-User 微服务 API 分析
├── kiwi-content-api-analysis.md  # Kiwi-Content 微服务 API 分析
└── kiwi-link-api-analysis.md     # Kiwi-Link 微服务 API 分析
```

## 文档说明

### kiwi-user-api-analysis.md
**Kiwi-User 微服务 API 接口分析**

- **接口数量**：11 个
- **Controller**：
  - `UserAuthController`（4 个接口）：用户注册、登录、登出、注销账号
  - `UserController`（6 个接口）：获取用户信息、更新资料、关注/取消关注、关注列表、粉丝列表
  - `StatsController`（1 个接口）：更新用户文章数量

**数据库使用**：
- MySQL：`user`、`user_stats`、`user_relation` 表
- MongoDB：`user` 集合
- Redis：未使用

**核心特点**：
- 用户管理：注册、登录、认证
- 关注系统：关注/取消关注、关注列表、粉丝列表
- 统计更新：异步更新用户统计数据

---

### kiwi-content-api-analysis.md
**Kiwi-Content 微服务 API 接口分析**

- **接口数量**：12 个
- **Controller**：
  - `ArticleController`（5 个接口）：发布文章、删除文章、获取文章列表、获取用户文章列表、获取文章详情
  - `ArticleSearchController`（2 个接口）：搜索文章、快速搜索文章
  - `CommentController`（4 个接口）：发布评论、删除评论、获取一级评论、获取楼中楼回复
  - `InteractionController`（1 个接口）：点赞/取消点赞

**数据库使用**：
- MySQL：`article`、`article_stats`、`comment`、`article_like` 表
- MongoDB：`article_content_cache` 集合
- Redis：`USER_LIKE_KEY:{articleId}` 集合、`LIKE_COUNT_KEY:{articleId}` String

**核心特点**：
- 文章管理：发布、删除、查询、详情
- 搜索功能：全文搜索、关键词高亮
- 评论系统：一级评论、楼中楼回复
- 点赞互动：Redis 缓存 + 异步同步 MySQL

---

### kiwi-link-api-analysis.md
**Kiwi-Link 微服务 API 接口分析**

- **接口数量**：2 个
- **Controller**：
  - `LinkController`（2 个接口）：生成短链接、获取长链接

**数据库使用**：
- MySQL：`link` 表
- MongoDB：未使用
- Redis：未使用

**核心特点**：
- 短链接管理：根据文章 ID 生成短链接
- 重定向机制：短链接重定向到长链接
- 点击量统计：在重定向过程中统计点击量

---

## 分析维度

每个接口文档包含以下分析维度：

### 1. 接口信息
- **接口路径**：HTTP 方法 + URL
- **功能描述**：接口的作用和目的

### 2. 业务逻辑
- **步骤**：详细的业务处理流程
- **实现**：具体的代码实现方式

### 3. 数据库使用
- **MySQL**：使用的表和字段
- **MongoDB**：使用的集合和文档
- **Redis**：使用的缓存和计数器

### 4. 远程调用
- **微服务调用**：是否调用其他微服务
- **调用方式**：Feign、HTTP 等
- **调用目的**：为什么需要远程调用

### 5. 数据一致性问题
- **风险点**：存在哪些数据一致性风险
- **影响范围**：影响哪些数据
- **解决方案**：建议的解决方案

---

## 架构特点

### 数据分层
- **MySQL**：存储结构化数据（强一致性要求）
- **MongoDB**：存储半结构化数据（内容、全文索引）
- **Redis**：存储热点数据（点赞缓存）

### 异步处理
- 统计更新使用 `@Async` 注解
- 点赞同步使用异步方式
- 减少主线程阻塞

### 混合存储
- MySQL 负责核心业务数据
- MongoDB 负责非结构化内容
- Redis 负责缓存和计数

---

## 数据一致性风险

### 1. MySQL 和 MongoDB 之间
- **文章发布/删除**：元数据和内容可能不一致
- **用户注册/注销**：用户信息和资料文档可能不完整

### 2. MySQL 和 Redis 之间
- **点赞功能**：缓存和数据库可能不一致

### 3. 微服务之间
- **文章服务**：需要调用用户服务更新文章数
- **短链接服务**：可能需要调用用户服务检查文章

---

## 建议改进

### 数据一致性
1. 使用消息队列（RabbitMQ/Kafka）确保最终一致性
2. 分布式事务处理 MySQL 和 MongoDB 的操作
3. 补偿机制处理异步失败

### 性能优化
1. 用户信息查询使用 Redis 缓存
2. 点赞缓存设置过期时间
3. 评论列表增加缓存

### 安全性
1. 头像上传功能补充实现
2. 短链接代码加密
3. 文章 ID 访问权限验证

### 代码优化
1. 异常处理更加精细
2. 异步操作增加重试机制
3. 日志记录更加完善

---

## 版本历史

- **v1.0**（2026-03-07）：初始版本，包含所有 25 个接口的分析
- **v1.1**（2026-03-07）：拆分为三个独立的微服务分析文档

---

## 维护者

- KiwiHub 开发团队
- 代码库：https://github.com/your-repo/kiwihub
