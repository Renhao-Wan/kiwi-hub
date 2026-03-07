# Kiwi-Content 微服务 API 接口分析

本文档整理了 Kiwi-Content 微服务的所有业务接口，每个接口包含功能描述、业务逻辑、数据库使用情况、远程调用和数据一致性分析。

---

## 1. ArticleController（5个接口）

### 1.1 发布文章

- **接口路径**：`POST /articles`
- **功能描述**：用户发布新文章
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 插入文章元数据到 MySQL `article` 表（`ArticleEntityService.save`，即 `save(articleEntity)`）
  3. 插入文章统计数据到 MySQL `article_stats` 表（`ArticleStatsEntityService.save`）
  4. 插入文章内容到 MongoDB `article_content_cache` 集合（`ArticleContentRepository.save`）
  5. 异步通知用户服务增加文章数（调用 `UserServiceClient.updateArticleCount`）
- **使用的数据库**：
  - **MySQL**：
    - `article` 表：存储文章元数据（作者ID、标题、创建时间、更新时间、逻辑删除标记等）
    - `article_stats` 表：存储文章统计数据（文章ID、浏览量、点赞数、评论数等）
  - **MongoDB**：
    - `article_content_cache` 集合：存储文章内容（标题、内容、内容类型、OSS链接、标签等）
  - **Redis**：未使用
- **远程调用**：
  - **有远程调用**：调用 `UserServiceClient.updateArticleCount` 更新用户服务的文章数统计
    - 这是微服务间调用，使用 Feign 客户端
    - 目的：让用户服务能及时更新用户的文章数（虽然用户服务的统计更新是异步的）
- **数据一致性问题**：
  - **存在**：MySQL 和 MongoDB 之间存在数据一致性问题
    - 如果第 2 步或第 3 步失败，第 4 步可能不会执行
    - 如果第 4 步失败，第 2 步和第 3 步的记录可能需要补偿删除
    - 如果第 5 步远程调用失败，MySQL 中的记录会残留
  - **存在**：远程调用与本地操作之间的一致性问题
    - 如果第 5 步远程调用失败，MySQL 中的文章和统计数据会残留，但用户服务的文章数不会增加
  - 建议使用分布式事务或消息队列确保最终一致性

---

### 1.2 删除文章

- **接口路径**：`DELETE /articles`
- **功能描述**：用户删除自己的文章
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 从 MySQL `article` 表查询文章是否存在且属于该用户（`getById`）
  3. 如果文章不存在或无权删除，返回错误
  4. 逻辑删除文章（设置 `deleted = 1`）
  5. 删除文章统计数据（`ArticleStatsEntityService.remove`）
  6. 删除文章内容（`ArticleContentRepository.deleteByArticleId`）
  7. 异步通知用户服务减少文章数（调用 `UserServiceClient.updateArticleCount`）
- **使用的数据库**：
  - **MySQL**：
    - `article` 表：逻辑删除文章
    - `article_stats` 表：删除文章统计数据
  - **MongoDB**：
    - `article_content_cache` 集合：删除文章内容
  - **Redis**：未使用
- **远程调用**：
  - **有远程调用**：调用 `UserServiceClient.updateArticleCount` 更新用户服务的文章数统计
- **数据一致性问题**：
  - **存在**：MySQL 和 MongoDB 之间存在数据一致性问题
    - 如果第 6 步失败，MySQL 中的记录会被逻辑删除，但 MongoDB 中的内容会残留
    - 如果第 5 步失败，第 6 步可能不会执行，导致统计数据被删除但内容残留
  - **存在**：远程调用与本地操作之间的一致性问题
    - 如果第 7 步远程调用失败，MySQL 中的记录会被逻辑删除，但用户服务的文章数不会减少
  - 建议使用分布式事务或消息队列确保最终一致性

---

### 1.3 获取文章列表

- **接口路径**：`GET /articles/s`
- **功能描述**：分页获取文章列表
- **业务逻辑**：
  1. 接收页码和每页数量（默认第 1 页，每页 10 条）
  2. 使用 MyBatis-Plus 分页查询 MySQL `article` 表（`page`）
  3. 只查询未删除的文章（`deleted = 0`）
  4. 按 `updated_at` 降序排序
  5. 转换为 `ArticleListVO`（只包含文章ID、标题、更新时间）
  6. 返回分页结果
- **使用的数据库**：
  - **MySQL**：
    - `article` 表：查询文章元数据
  - **MongoDB**：未使用
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：只返回文章元数据，不包含内容和统计数据

---

### 1.4 获取当前用户文章列表

- **接口路径**：`GET /articles/me`
- **功能描述**：分页获取当前用户文章列表
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 接收页码和每页数量（默认第 1 页，每页 10 条）
  3. 使用 MyBatis-Plus 分页查询 MySQL `article` 表（`page`）
  4. 只查询未删除的文章，且作者ID等于当前用户ID
  5. 按 `updated_at` 降序排序
  6. 转换为 `ArticleListVO`
  7. 返回分页结果
- **使用的数据库**：
  - **MySQL**：
    - `article` 表：查询文章元数据
  - **MongoDB**：未使用
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：只返回文章元数据，不包含内容和统计数据

---

### 1.5 获取文章详情

- **接口路径**：`GET /articles/{articleId}`
- **功能描述**：根据文章ID获取文章详细信息
- **业务逻辑**：
  1. 从 MySQL `article` 表查询文章元数据（`getById`）
  2. 如果文章不存在或已删除，返回空对象
  3. 从 MongoDB `article_content_cache` 集合查询文章内容（`findByArticleId`）
  4. 如果内容不存在，返回空对象并记录警告日志
  5. 从 MySQL `article_stats` 表查询文章统计数据（`getOne`）
  6. 组装 `Article` 对象（包含元数据、内容、统计数据）
  7. 返回文章详情
- **使用的数据库**：
  - **MySQL**：
    - `article` 表：查询文章元数据
    - `article_stats` 表：查询统计数据
  - **MongoDB**：
    - `article_content_cache` 集合：查询文章内容
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - **存在**：MySQL 和 MongoDB 之间存在数据一致性问题
    - 如果第 3 步或第 5 步失败，返回的文章详情不完整（缺少内容或统计数据）
    - 如果返回的文章详情不完整，可能导致前端展示错误
  - 建议使用补偿机制或消息队列确保最终一致性
- **备注**：组合查询 MySQL 和 MongoDB 数据，返回完整的文章详情

---

## 2. ArticleSearchController（2个接口）

### 2.1 搜索文章

- **接口路径**：`POST /search/articles`
- **功能描述**：根据关键词搜索文章，支持全文搜索和正则搜索两种模式
- **业务逻辑**：
  1. 接收搜索条件（关键词、搜索模式、页码、每页数量、排序等）
  2. 使用 MongoDB 文本索引搜索 `article_content_cache` 集合（`searchByTextIndex`）
  3. 统计文本索引搜索结果数量（`countByTextIndex`）
  4. 批量获取文章统计信息（从 MySQL `article_stats` 表，使用 `listByIds`）
  5. 转换为 `ArticleSearchResultVO`（包含文章ID、标题、摘要、统计数据等）
  6. 添加搜索关键词高亮（在标题和摘要中高亮显示关键词）
  7. 返回搜索结果分页数据
- **使用的数据库**：
  - **MySQL**：
    - `article_stats` 表：批量查询文章统计数据
  - **MongoDB**：
    - `article_content_cache` 集合：全文搜索文章内容
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - **存在**：MySQL 和 MongoDB 之间存在数据一致性问题
    - 如果第 4 步查询统计数据失败，返回的文章列表中的统计数据为空或默认值
    - 如果搜索到的文章在 MySQL 中不存在统计数据，返回的统计信息不完整
  - 建议使用补偿机制或消息队列确保最终一致性
- **备注**：全文搜索基于 MongoDB 的文本索引，统计数据来自 MySQL

---

### 2.2 快速搜索文章

- **接口路径**：`GET /search/articles`
- **功能描述**：使用 GET 方式快速搜索文章
- **业务逻辑**：
  1. 接收搜索关键词、搜索模式（`text` 或 `regex`）、页码、每页数量、排序字段、排序方向、是否只搜索标题、标签过滤等参数
  2. 将参数转换为 `ArticleSearchDTO` 对象
  3. 调用 `searchArticles` 方法（与 POST 接口相同）
- **使用的数据库**：
  - **MySQL**：
    - `article_stats` 表：批量查询文章统计数据
  - **MongoDB**：
    - `article_content_cache` 集合：全文搜索文章内容
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 与 `POST /search/articles` 接口相同
  - **存在**：MySQL 和 MongoDB 之间存在数据一致性问题
- **备注**：仅用于快速搜索，URL 长度受限（可能有 URL 长度限制问题）

---

## 3. CommentController（4个接口）

### 3.1 发布评论

- **接口路径**：`POST /comments`
- **功能描述**：发布新评论，支持一级评论和回复评论
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 如果 `parentId` 为空，创建一级评论
     - 插入评论到 MySQL `comment` 表（`commentEntityService.save`）
     - 插入后设置 `root_id = id`（更新记录）
  3. 如果 `parentId` 不为空，创建子评论
     - 从 MySQL `comment` 表查询父评论是否存在且未删除（`getById`）
     - 如果父评论不存在或已删除，返回错误
     - 设置 `parent_id = 父评论ID` 和 `root_id = 父评论的root_id`
     - 插入评论到 MySQL `comment` 表
  4. 更新文章评论计数（调用 `articleStatsEntityService.updateCommentCount`）
- **使用的数据库**：
  - **MySQL**：
    - `comment` 表：插入评论（一级或子评论）
  - **MongoDB**：未使用
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - **存在**：评论计数和评论数量之间的一致性问题
    - 如果第 4 步更新评论计数失败，MySQL `comment` 表中有新评论，但 `article_stats` 表中的 `comment_count` 不准确
  - 建议使用补偿机制或消息队列确保最终一致性

---

### 3.2 删除评论

- **接口路径**：`DELETE /comments/{commentId}`
- **功能描述**：删除自己的评论（软删除）
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 从 MySQL `comment` 表查询评论是否存在且属于该用户（`getById`）
  3. 更新评论状态（`status = 1` 表示已删除）
  4. 如果更新成功，更新文章评论计数（减 1）
- **使用的数据库**：
  - **MySQL**：
    - `comment` 表：软删除评论（更新 `status` 字段）
  - **MongoDB**：未使用
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - **存在**：评论计数和评论数量之间的一致性问题
    - 如果第 4 步更新评论计数失败，MySQL `comment` 表中的评论状态会被更新为已删除，但 `article_stats` 表中的 `comment_count` 不准确
  - 建议使用补偿机制或消息队列确保最终一致性

---

### 3.3 获取文章一级评论列表

- **接口路径**：`GET /comments/roots`
- **功能描述**：分页获取文章的一级评论列表
- **业务逻辑**：
  1. 接收文章ID、页码、每页数量
  2. 使用 MyBatis-Plus 分页查询 MySQL `comment` 表（`page`）
  3. 查询条件：文章ID、未删除状态（`status = 0`）、`id = root_id`（一级评论）
  4. 按 `created_at` 降序排序
  5. 转换为 `CommentVO` 并返回
- **使用的数据库**：
  - **MySQL**：
    - `comment` 表：查询一级评论
  - **MongoDB**：未使用
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：只返回一级评论，不包含子评论

---

### 3.4 获取楼中楼回复列表

- **接口路径**：`GET /comments/replies`
- **功能描述**：使用游标分页获取指定根评论下的所有回复
- **业务逻辑**：
  1. 接收查询条件（根评论ID、游标ID、每页数量等）
  2. 使用 MyBatis-Plus 查询 MySQL `comment` 表（`list`）
  3. 查询条件：`root_id = 指定根评论ID`、未删除状态、`id != root_id`（排除根评论本身）
  4. 如果提供了 `cursorId`，查询条件增加 `id > cursorId`（游标分页）
  5. 按 `id` 升序排序
  6. 返回评论列表和下一页游标ID
- **使用的数据库**：
  - **MySQL**：
    - `comment` 表：查询楼中楼回复
  - **MongoDB**：未使用
  - **Redis**：未使用
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - 不存在
- **备注**：使用游标分页，适合滚动加载场景

---

## 4. InteractionController（1个接口）

### 4.1 点赞/取消点赞

- **接口路径**：`POST /interactions/like`
- **功能描述**：对指定文章进行点赞或取消点赞操作
- **业务逻辑**：
  1. 从 ThreadLocal 获取当前用户 ID（`UserContext.getUserId()`）
  2. 从 Redis `USER_LIKE_KEY:{articleId}` 集合中检查用户是否已点赞（`opsForSet().isMember`）
  3. 如果已点赞：
     - 从 Redis 集合中移除用户（`remove`）
     - Redis 计数减 1
     - 异步同步到数据库（调用 `LikeSyncService.syncUnlikeToDatabase`）
     - 返回 `false`（表示取消点赞成功）
  4. 如果未点赞：
     - 向 Redis 集合中添加用户（`add`）
     - Redis 计数加 1
     - 异步同步到数据库（调用 `LikeSyncService.syncLikeToDatabase`）
     - 返回 `true`（表示点赞成功）
- **使用的数据库**：
  - **MySQL**：
    - `article_like` 表：异步插入或删除点赞记录（通过 `LikeSyncService`）
    - `article_stats` 表：异步更新点赞计数（通过 `ArticleStatsEntityService`）
  - **MongoDB**：未使用
  - **Redis**：
    - `USER_LIKE_KEY:{articleId}` 集合：存储点赞用户列表
    - `LIKE_COUNT_KEY:{articleId}` String：存储点赞总数
- **远程调用**：
  - 未调用其他微服务
- **数据一致性问题**：
  - **存在**：Redis 缓存和 MySQL 数据库之间的一致性问题
    - Redis 中的点赞状态和计数是实时的，MySQL 是异步更新的
    - 如果异步更新失败，Redis 和 MySQL 的点赞状态不一致
  - **存在**：点赞记录和统计数据之间的一致性问题
    - 如果异步更新点赞记录失败，但 Redis 中的状态已改变，会导致数据不一致
  - 建议使用消息队列或补偿机制确保最终一致性

---

## 5. 数据库使用总结

### 5.1 数据库分布
- **MySQL**：
  - 文章管理：`article` 表（文章元数据）、`article_stats` 表（统计数据）、`comment` 表（评论）、`article_like` 表（点赞）
- **MongoDB**：
  - 内容存储：`article_content_cache` 集合（文章内容、全文索引）
- **Redis**：
  - 点赞缓存：`USER_LIKE_KEY:{articleId}` 集合、`LIKE_COUNT_KEY:{articleId}` String

### 5.2 核心数据模型
1. **ArticleEntity**：MySQL 文章表
2. **ArticleStatsEntity**：MySQL 文章统计表
3. **CommentEntity**：MySQL 评论表
4. **ArticleLikeEntity**：MySQL 点赞表
5. **ArticleContentDocument**：MongoDB 文章内容文档

---

## 6. 数据一致性风险

### 6.1 MySQL 和 MongoDB 之间
- **文章发布/删除**：存在数据一致性问题
  - 如果异步操作失败，导致数据不完整

### 6.2 MySQL 和 Redis 之间
- **点赞功能**：存在缓存和数据库之间的一致性问题
  - 异步更新可能失败

### 6.3 微服务之间
- **文章发布/删除**：需要调用用户服务更新文章数，存在远程调用失败的风险

---

## 7. 架构特点

### 7.1 混合存储架构
- **MySQL**：存储结构化数据（强一致性要求）
- **MongoDB**：存储半结构化数据（文章内容、全文索引）
- **Redis**：存储热点数据（点赞缓存）

### 7.2 异步处理
- 点赞同步、统计更新使用 `@Async` 注解
- 使用独立的线程池（`interactionExecutor`、`statsUpdateExecutor`）

### 7.3 分层架构
- Controller → Service → Repository
- Service 层处理业务逻辑
- Repository 层封装数据库操作

---

## 8. 关键设计模式

### 8.1 Redis 缓存
- 点赞状态存储在 Redis，提高响应速度
- Redis 和 MySQL 异步同步

### 8.2 游标分页
- 评论回复列表使用游标分页，适合滚动加载场景

### 8.3 全文索引
- MongoDB 文本索引支持高效全文搜索

---

## 9. 潜在优化点

### 9.1 数据一致性
- 使用消息队列确保异步操作最终一致性
- 分布式事务处理 MySQL 和 MongoDB 的操作

### 9.2 性能优化
- 点赞缓存可以设置过期时间
- 评论列表可以增加缓存

### 9.3 代码优化
- 异常处理可以更加精细
- 异步操作可以增加重试机制
