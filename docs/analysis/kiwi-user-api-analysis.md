# Kiwi-User 微服务 API 接口分析

本文档整理了 Kiwi-User 微服务的所有业务接口，每个接口包含功能描述、业务逻辑、数据库使用情况、远程调用和数据一致性分析。

---

## 1. UserAuthController（4个接口）

### 1.1 用户注册

- **接口路径**：`POST /users/register`
- **功能描述**：新用户注册，支持上传头像
- **业务逻辑**：
  1. 检查 MySQL 中是否已存在相同用户名或邮箱（调用 `UserEntityService.count`）
  2. 如果用户名或邮箱已存在，返回错误
  3. 如果不重复，插入用户到 MySQL `user` 表（`UserEntityService.save`）
  4. 插入用户统计数据到 MySQL `user_stats` 表（`UserStatsEntityService.save`）
  5. 如果提供了头像文件，保存头像到 OSS（代码中为空实现）
  6. 创建用户资料文档到 MongoDB `user` 集合（`userRepository.save`）
- **使用的数据库**：
  - **MySQL**：
    - `user` 表：存储用户基本信息（用户名、邮箱、密码哈希、创建时间等）
    - `user_stats` 表：存储用户统计数据（文章数、关注数、粉丝数等）
  - **MongoDB**：
    - `user` 集合：存储用户详细资料（个人简介、标签、头像URL等）
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - **存在**：MySQL 和 MongoDB 之间存在数据一致性问题
    - 如果第 5 步保存头像失败，MySQL 中的用户记录会残留，但 MongoDB 中的资料文档可能不完整
    - 建议使用分布式事务或消息队列确保最终一致性
- **备注**：代码中头像上传逻辑为空（TODO），需要补充 OSS 上传功能

---

### 1.2 用户登录

- **接口路径**：`POST /users/login`
- **功能描述**：用户登录系统
- **业务逻辑**：
  1. 根据用户名或邮箱查询 MySQL `user` 表（`UserEntityService.getOne`）
  2. 如果用户不存在，返回错误
  3. 验证密码（使用 `PasswordEncoder` 比对密码哈希）
  4. 登录成功后，将用户信息存入 HttpSession（用户名、ID）
  5. 将用户 ID 存入 ThreadLocal（`UserContext`）
- **使用的数据库**：
  - **MySQL**：
    - `user` 表：查询用户信息
- **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：使用 HttpSession 管理会话，ThreadLocal 存储用户上下文

---

### 1.3 用户登出

- **接口路径**：`POST /users/logout`
- **功能描述**：用户退出登录
- **业务逻辑**：
  1. 使 HttpSession 无效（`session.invalidate()`）
  2. 清理 ThreadLocal 中的用户上下文（`UserContext.clear()`）
- **使用的数据库**：
  - 无数据库操作
- **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：仅清理会话和上下文，不涉及数据库操作

---

### 1.4 注销账号

- **接口路径**：`POST /users/delete`
- **功能描述**：注销当前用户账号
- **业务逻辑**：
  1. 从 MySQL `user_stats` 表删除用户统计数据（`UserStatsEntityService.remove`）
  2. 从 MySQL `user` 表删除用户（`UserEntityService.removeById`）
  3. 从 MongoDB `user` 集合删除用户资料文档（`userRepository.deleteById`）
  4. 使 HttpSession 无效（`session.invalidate()`）
- **使用的数据库**：
  - **MySQL**：
    - `user_stats` 表：删除用户统计数据
    - `user` 表：删除用户记录（逻辑删除，`deleted` 字段设为 1）
  - **MongoDB**：
    - `user` 集合：删除用户资料文档
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - **存在**：MySQL 和 MongoDB 之间存在数据一致性问题
    - 如果第 3 步删除 MongoDB 失败，MySQL 中的用户记录会被保留（逻辑删除），但 MongoDB 中用户文档不存在
    - 如果第 1 步删除 `user_stats` 失败，但第 2 步删除了 `user`，会导致 `user_stats` 表中有孤儿记录
  - 建议使用分布式事务或消息队列确保最终一致性

---

## 2. UserController（6个接口）

### 2.1 获取当前用户信息

- **接口路径**：`GET /users/me`
- **功能描述**：获取当前登录用户的详细信息
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 从 MongoDB `user` 集合查询用户资料文档（`userRepository.findById`）
  3. 转换为 `UserDetailVO` 并返回
- **使用的数据库**：
  - **MongoDB**：
    - `user` 集合：查询用户详细资料
  - **MySQL**：未使用
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：仅查询 MongoDB，不涉及 MySQL

---

### 2.2 更新用户资料

- **接口路径**：`PUT /users/me/profile`
- **功能描述**：更新当前登录用户的个人资料
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 从 MongoDB `user` 集合查询用户文档（`userRepository.findById`）
  3. 更新用户资料字段（个人简介、标签等）（`mongoTemplate.updateFirst`）
- **使用的数据库**：
  - **MongoDB**：
    - `user` 集合：更新用户资料
  - **MySQL**：未使用
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：仅更新 MongoDB，不涉及 MySQL

---

### 2.3 关注用户

- **接口路径**：`POST /users/follow`
- **功能描述**：关注指定用户
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 从 MySQL `user` 表查询被关注用户是否存在（`UserEntityService.getById`）
  3. 如果用户不存在，返回错误
  4. 插入关注关系到 MySQL `user_relation` 表（`save`）
  5. 如果插入失败（主键冲突），说明已关注，返回错误
  6. 异步更新关注统计（调用 `StatsService.updateFollowStats`，使用 `@Async`）
- **使用的数据库**：
  - **MySQL**：
    - `user` 表：查询被关注用户是否存在
    - `user_relation` 表：插入关注关系
  - **MongoDB**：未使用
  - **Redis**：未使用
- **远程调用**：
  - **未调用**：`StatsService.updateFollowStats` 是本地服务，不是远程调用
- **数据一致性问题**：
  - **可能存在**：关注关系和统计数据之间的一致性问题
    - 关注关系插入成功后，异步更新统计可能失败（第 6 步），导致 `user_relation` 表中有记录，但 `user_stats` 表中的 `following_count` 不准确
    - 建议使用补偿机制或消息队列确保最终一致性

---

### 2.4 取消关注

- **接口路径**：`DELETE /users/follow`
- **功能描述**：取消关注指定用户
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 从 MySQL `user_relation` 表删除关注关系（`remove`，使用 LambdaQueryWrapper）
  3. 异步更新统计（调用 `StatsService.updateFollowStats`）
- **使用的数据库**：
  - **MySQL**：
    - `user_relation` 表：删除关注关系
  - **MongoDB**：未使用
  - **Redis**：未使用
- **远程调用**：
  - **未调用**：`StatsService.updateFollowStats` 是本地服务
- **数据一致性问题**：
  - **可能存在**：关注关系和统计数据之间的一致性问题
    - 关注关系删除成功后，异步更新统计可能失败，导致 `user_relation` 表中没有记录，但 `user_stats` 表中的 `following_count` 不准确
    - 建议使用补偿机制或消息队列确保最终一致性

---

### 2.5 获取关注列表

- **接口路径**：`GET /users/following`
- **功能描述**：分页获取当前用户的关注列表
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 使用 MyBatis-Plus 分页查询 MySQL `user_relation` 表（`page`）
  3. 转换为 Spring Data Page 格式（保持兼容）
  4. 批量查询用户基本信息（`userRepository.findAllById`，从 MongoDB）
  5. 组装 `UserCardVO` 并返回
- **使用的数据库**：
  - **MySQL**：
    - `user_relation` 表：查询关注关系
  - **MongoDB**：
    - `user` 集合：批量查询用户基本信息
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：MySQL 查询关系关系，MongoDB 查询用户基本信息

---

### 2.6 获取粉丝列表

- **接口路径**：`GET /users/followers`
- **功能描述**：分页获取当前用户的粉丝列表
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 使用 MyBatis-Plus 分页查询 MySQL `user_relation` 表（`page`）
  3. 转换为 Spring Data Page 格式（保持兼容）
  4. 批量查询用户基本信息（`userRepository.findAllById`，从 MongoDB）
  5. 组装 `UserCardVO` 并返回
- **使用的数据库**：
  - **MySQL**：
    - `user_relation` 表：查询粉丝关系（查询条件为 `followingId = userId`）
  - **MongoDB**：
    - `user` 集合：批量查询用户基本信息
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：MySQL 查询粉丝关系，MongoDB 查询用户基本信息

---

## 3. StatsController（1个接口）

### 3.1 更新文章数量

- **接口路径**：`POST /stats/article/count/{userId}/{delta}`
- **功能描述**：更新指定用户的文章数量统计
- **业务逻辑**：
  1. 从路径参数获取 `userId` 和 `delta`
  2. 异步调用 `StatsService.updateArticleCount`（使用 `@Async`）
- **使用的数据库**：
  - **MySQL**：
    - `user_stats` 表：异步更新 `article_count` 字段（通过 `LambdaUpdateWrapper` 和 `setSql`）
  - **MongoDB**：未使用
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：异步更新统计，使用 `@Async` 注解，不会阻塞主线程

---

## 4. 数据库使用总结

### 数据库分布
- **MySQL**：
  - 用户管理：`user` 表（用户基本信息）、`user_stats` 表（用户统计数据）、`user_relation` 表（关注关系）
- **MongoDB**：
  - 用户资料：`user` 集合（用户详细资料）
- **Redis**：
  - 未使用（统计更新是异步的，但不使用 Redis 缓存）

### 核心数据模型
1. **UserEntity**：MySQL 用户表
2. **UserStatsEntity**：MySQL 用户统计表
3. **UserRelationEntity**：MySQL 用户关系表
4. **User (Document)**：MongoDB 用户文档

---

## 5. 数据一致性风险

### 5.1 MySQL 和 MongoDB 之间
- **注册/注销接口**：存在数据一致性问题
  - 如果异步操作失败，导致数据不完整

### 5.2 MySQL 内部
- **关注/取消关注**：关注关系和统计数据可能不一致
  - 异步更新统计可能失败

---

## 6. 架构特点

### 6.1 数据分层
- **MySQL**：存储结构化数据（强一致性要求）
- **MongoDB**：存储半结构化数据（用户资料）
- **异步处理**：统计更新使用 `@Async`，不阻塞主线程

### 6.2 服务职责
- **UserAuthController**：认证相关（注册、登录、登出、注销）
- **UserController**：用户信息管理（查询、更新、关注关系）
- **StatsController**：统计更新（文章数更新）

---

## 7. 关键设计模式

### 7.1 ThreadLocal
- 每个请求线程独立存储用户上下文
- 通过 `UserContext.getUserId()` 获取当前用户ID

### 7.2 异步处理
- 统计更新使用 `@Async` 注解
- 使用独立的线程池（`statsUpdateExecutor`）

### 7.3 分层架构
- Controller → Service → Repository
- Service 层处理业务逻辑
- Repository 层封装数据库操作

---

## 8. 潜在优化点

### 8.1 数据一致性
- 使用消息队列确保统计更新最终一致性
- 分布式事务处理 MySQL 和 MongoDB 的操作

### 8.2 性能优化
- 用户信息查询可以使用 Redis 缓存
- 批量查询时考虑分片

### 8.3 代码优化
- 头像上传功能需要补充实现
- 异常处理可以更加精细
