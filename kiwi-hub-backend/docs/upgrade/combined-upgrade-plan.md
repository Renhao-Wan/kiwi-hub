# KiwiHub 技术栈升级计划

## 一、项目现状分析

### 1.1 当前架构概览

| 服务名称 | 端口 | 数据库 | 消息队列 | 主要功能 |
|---------|------|--------|---------|---------|
| kiwi-user | 8070 | MongoDB + Redis | RabbitMQ | 用户认证、个人资料、关注关系 |
| kiwi-content | 8010 | MongoDB + Redis | RabbitMQ | 文章、评论、点赞互动 |
| kiwi-link | 8030 | Redis | 无 | 短链接服务 |

### 1.2 MongoDB 数据模型详情

#### 1.2.1 kiwi-user 服务

**User 集合**
```
{
  _id: String,
  username: String (唯一索引),
  email: String (唯一索引),
  password_hash: String,
  created_at: LocalDateTime,
  profile: {
    avatar_url: String,
    bio: String,
    tags: List<String>
  },
  social_stats: {
    article_count: int,
    following_count: int,
    follower_count: int
  }
}
```

**UserRelation 集合**
```
{
  _id: String,
  follower_id: String,
  following_id: String,
  created_at: LocalDateTime
}
// 复合索引: unique_relation (follower_id, following_id)
// 复合索引: idx_follower_time (follower_id, created_at)
// 复合索引: idx_following_time (following_id, created_at)
```

#### 1.2.2 kiwi-content 服务

**Article 集合**
```
{
  _id: String,
  author_id: String,
  title: String (全文索引),
  content: String (全文索引),
  content_type: String,
  oss_urls: List<String>,
  tags: List<String> (全文索引),
  created_at: LocalDateTime,
  updated_at: LocalDateTime (索引),
  stats: {
    view_count: int,
    like_count: int,
    comment_count: int
  }
}
```

**Comment 集合**
```
{
  _id: String,
  article_id: String,
  author_id: String,
  content: String,
  parent_id: String,
  root_id: String,
  created_at: LocalDateTime,
  status: int (0-正常, 1-已删除)
}
// 复合索引: article_id_author_id_index (article_id, author_id)
```

**ArticleLike 集合**
```
{
  _id: String,
  user_id: String,
  article_id: String,
  author_id: String,
  create_time: LocalDateTime
}
// 复合索引: idx_user_article_unique (user_id, article_id) 唯一
// 复合索引: idx_user_time (user_id, create_time)
```

### 1.3 RabbitMQ 集成点详情

#### 1.3.1 kiwi-user 服务

| 交换机 | 队列 | 路由键 | 生产者 | 消费者 | 功能 |
|-------|------|-------|--------|--------|------|
| kiwi-user-relation-exchange | kiwi-user-relation-queue | user.relation | UserServiceImpl | FollowEventListener | 关注/取关后更新粉丝统计 |
| kiwi-article-user-exchange | kiwi-article-user-queue | user.article | ArticleServiceImpl | ArticleEventListener | 发布/删除文章后更新用户文章数 |

**消息格式**:
```java
// 关注消息
Map.of(
  "followerId", userId,
  "followingId", followUserId,
  "followAction", "follow/unfollow"
)

// 文章消息
Map.of(
  "authorId", userId,
  "action", "publish/delete"
)
```

#### 1.3.2 kiwi-content 服务

| 交换机 | 队列 | 路由键 | 生产者 | 消费者 | 功能 |
|-------|------|-------|--------|--------|------|
| kiwi-article-interaction-exchange | kiwi-article-view-queue | article.view | ArticleServiceImpl | ArticleViewListener | 文章浏览计数 |
| kiwi-article-interaction-exchange | kiwi-article-like-queue | article.like | InteractionServiceImpl | ArticleLikeListener + ArticleViewListener | 点赞数据落库与计数 |
| kiwi-article-interaction-exchange | kiwi-article-comment-queue | article.comment | CommentServiceImpl | ArticleViewListener | 评论计数 |

**消息格式**:
```java
Map.of(
  "articleId", articleId,
  "userId", userId,
  "authorId", authorId,
  "action", "increase/decrease"
)
```

### 1.4 Redis 使用场景

| 服务 | Key 模式 | 数据结构 | 用途 |
|-----|---------|---------|------|
| kiwi-user | spring:session:* | Hash | Session 存储 |
| kiwi-content | kiwi:user_like:{articleId} | Set | 文章点赞用户集合 |
| kiwi-content | kiwi:like_count:{articleId} | String | 文章点赞计数缓存 |
| kiwi-link | kiwi:short_link:{code} | String | 短链接映射 |

### 1.5 潜在安全问题

| 问题类型 | 当前状态 | 风险等级 |
|---------|---------|---------|
| SQL/NoSQL 注入 | MongoDB 使用参数化查询，风险较低 | 低 |
| 数据一致性 | 异步消息可能导致最终一致性问题 | 中 |
| 认证授权 | Session 基于 Redis，无 JWT | 低 |
| 敏感数据 | 密码使用 BCrypt 加密 | 低 |
| 并发安全 | 依赖 MongoDB 原子操作 | 低 |

---

## 二、MySQL 整合策略

### 2.1 混合存储策略

KiwiHub 将采用 **MySQL + MongoDB 混合存储架构**，根据数据特性选择最优存储方案：

#### 2.1.1 MySQL 存储（MyBatis-Plus 3.5.7）
**存储场景**：结构化数据、强关联关系、需要事务支持、复杂查询、外键约束

| 数据模型 | MySQL 表名 | 存储内容 | 理由 |
|---------|-----------|---------|------|
| User | `user` | 用户基础信息（id, username, email, password_hash, created_at） | 结构化数据，需要事务支持，关系查询频繁 |
| UserRelation | `user_relation` | 关注关系（follower_id, following_id, created_at） | 典型关系型数据，外键约束可保证数据完整性 |
| Article | `article` | 文章基础信息（id, author_id, title, created_at, updated_at） | 结构化数据，需要复杂查询和事务支持 |
| Comment | `comment` | 评论（id, article_id, author_id, content, parent_id, root_id, status, created_at） | 结构化数据，层级关系适合关系型数据库 |
| ArticleLike | `article_like` | 点赞记录（user_id, article_id, author_id, create_time） | 关系型数据，适合外键约束 |
| ArticleStats | `article_stats` | 文章统计（view_count, like_count, comment_count） | 关联查询频繁，需要事务更新 |
| UserStats | `user_stats` | 用户统计（article_count, following_count, follower_count） | 实时更新，需要事务一致性 |

#### 2.1.2 MongoDB 保留（Spring Data MongoDB）
**存储场景**：半结构化数据、文档型数据、分析型数据、全文索引、个性化配置

| 集合名称 | 存储内容 | 保留理由 |
|---------|---------|---------|
| `user_profiles` | 用户画像配置（avatar_url, bio, tags, 个性化推荐参数） | 半结构化，频繁扩展字段，个性化分析 |
| `user_operation_logs` | 用户操作日志（行为轨迹、分析数据） | 高写入，复杂聚合查询，分析用途 |
| `article_content_cache` | 文章内容（content, contentType, ossUrls, tags）及全文索引 | 内容存储，全文搜索，灵活Schema |
| `article_search_cache` | 文章搜索预热缓存、相关性评分 | 搜索性能优化，预计算数据 |
| `link_access_logs` | 短链接访问日志（聚合分析场景） | 复杂聚合查询，时序数据分析 |

### 2.2 MySQL 数据库 Schema 设计

#### 2.2.1 kiwi_user 数据库

```sql
-- 用户基础信息表（核心认证数据）
CREATE TABLE `user` (
  `id` VARCHAR(36) NOT NULL COMMENT '用户ID (UUID)',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希值',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';

-- 用户统计表（实时统计数据）
CREATE TABLE `user_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
  `article_count` INT NOT NULL DEFAULT 0 COMMENT '文章数',
  `following_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
  `follower_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_user_stats_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';

-- 注意：用户画像数据（avatar_url, bio, tags, 个性化配置）存储在 MongoDB 的 user_profiles 集合中
-- 此设计便于扩展个性化字段和进行复杂分析查询

-- 用户关注关系表
CREATE TABLE `user_relation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `follower_id` VARCHAR(36) NOT NULL COMMENT '关注者ID',
  `following_id` VARCHAR(36) NOT NULL COMMENT '被关注者ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_relation` (`follower_id`, `following_id`),
  KEY `idx_follower_created` (`follower_id`, `created_at`),
  KEY `idx_following_created` (`following_id`, `created_at`),
  CONSTRAINT `fk_relation_follower` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_relation_following` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';
```

#### 2.2.2 kiwi_content 数据库

```sql
-- 文章基础信息表（核心元数据）
CREATE TABLE `article` (
  `id` VARCHAR(36) NOT NULL COMMENT '文章ID (UUID)',
  `author_id` VARCHAR(36) NOT NULL COMMENT '作者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_author_updated` (`author_id`, `updated_at`),
  KEY `idx_updated_at` (`updated_at`),
  CONSTRAINT `fk_article_author` FOREIGN KEY (`author_id`) REFERENCES `kiwi_user`.`user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章基础信息表';

-- 文章统计表（实时互动数据）
CREATE TABLE `article_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `article_id` VARCHAR(36) NOT NULL COMMENT '文章ID',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_id` (`article_id`),
  CONSTRAINT `fk_article_stats_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章统计表';

-- 文章标签表（用于快速标签查询，完整标签数据存储在 MongoDB 的 article_content_cache 中）
CREATE TABLE `article_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `article_id` VARCHAR(36) NOT NULL COMMENT '文章ID',
  `tag` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_tag` (`tag`),
  UNIQUE KEY `uk_article_tag` (`article_id`, `tag`),
  CONSTRAINT `fk_article_tag_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签表';

-- 文章附件表（用于快速附件查询，完整附件数据存储在 MongoDB 的 article_content_cache 中）
CREATE TABLE `article_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `article_id` VARCHAR(36) NOT NULL COMMENT '文章ID',
  `oss_url` VARCHAR(500) NOT NULL COMMENT 'OSS存储URL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  CONSTRAINT `fk_attachment_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章附件表';

-- 评论表
CREATE TABLE `comment` (
  `id` VARCHAR(36) NOT NULL COMMENT '评论ID (UUID)',
  `article_id` VARCHAR(36) NOT NULL COMMENT '文章ID',
  `author_id` VARCHAR(36) NOT NULL COMMENT '评论者ID',
  `content` VARCHAR(2000) NOT NULL COMMENT '评论内容',
  `parent_id` VARCHAR(36) DEFAULT NULL COMMENT '父评论ID',
  `root_id` VARCHAR(36) DEFAULT NULL COMMENT '根评论ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-正常, 1-已删除',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_status` (`article_id`, `status`),
  KEY `idx_author_article` (`author_id`, `article_id`),
  KEY `idx_root_id` (`root_id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_comment_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_author` FOREIGN KEY (`author_id`) REFERENCES `kiwi_user`.`user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_root` FOREIGN KEY (`root_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 文章点赞表
CREATE TABLE `article_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` VARCHAR(36) NOT NULL COMMENT '点赞用户ID',
  `article_id` VARCHAR(36) NOT NULL COMMENT '文章ID',
  `author_id` VARCHAR(36) NOT NULL COMMENT '文章作者ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_article_id` (`article_id`),
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `kiwi_user`.`user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_like_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_like_author` FOREIGN KEY (`author_id`) REFERENCES `kiwi_user`.`user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';
```

### 2.3 数据迁移策略（无需历史数据迁移）

**重要说明**：本次升级**不需要迁移历史数据**，新系统将从零开始使用新的混合存储结构。历史数据可保留在MongoDB中供查询参考，新产生的数据将按新架构存储。

#### 2.3.1 实施步骤（代码改造为主）

```
阶段 1: 环境准备 (预计 1 天)
├── 1.1 安装配置 MySQL 8.0+（新数据库）
├── 1.2 创建 MySQL 数据库和表结构（Schema 2.2）
├── 1.3 保留 MongoDB 现有数据（不删除）
└── 1.4 配置双数据源连接

阶段 2: 代码改造 (预计 5 天)
├── 2.1 添加 MyBatis-Plus + MySQL 依赖
├── 2.2 创建 MySQL 实体类 + MyBatis-Plus Mapper
├── 2.3 创建 MongoDB 文档类（调整现有实体）
├── 2.4 重构 Service 层：拆分数据存储逻辑
├── 2.5 实现跨存储查询和数据聚合
└── 2.6 配置双数据源和事务管理

阶段 3: RabbitMQ 移除 (预计 2 天)
├── 3.1 删除 RabbitMQ 依赖和配置
├── 3.2 创建异步处理服务（CompletableFuture）
├── 3.3 改造消息生产者为直接调用
└── 3.4 移除消息消费者相关代码

阶段 4: 测试验证 (预计 2 天)
├── 4.1 单元测试（覆盖新数据访问逻辑）
├── 4.2 集成测试（验证双数据源协同）
├── 4.3 性能测试（对比混合存储性能）
└── 4.4 回归测试（确保现有功能正常）

阶段 5: 上线部署 (预计 1 天)
├── 5.1 灰度发布（新版本服务）
├── 5.2 监控验证（数据库连接、性能指标）
└── 5.3 回滚准备（快速回滚方案）
```

#### 2.3.2 数据一致性保证

由于采用混合存储架构且不迁移历史数据，需要确保以下一致性原则：

1. **新数据双写**：核心业务数据同时写入MySQL和MongoDB（如用户注册、文章发布）
2. **读取优先策略**：
   - 基础信息查询优先从MySQL读取
   - 内容、画像等从MongoDB读取
   - 缓存层统一使用Redis
3. **补偿机制**：
   - 跨存储写入失败时，记录日志并异步重试
   - 关键业务提供数据修复接口
4. **数据生命周期**：
   - 历史MongoDB数据只读，不更新
   - 新数据按新架构存储
   - 可逐步将热点历史数据迁移到新结构

#### 2.3.3 数据验证重点

| 检查项 | 验证方法 | 通过标准 |
|-------|---------|---------|
| 双数据源连接 | 连接测试 | MySQL 和 MongoDB 连接正常 |
| 新数据写入 | 插入测试数据 | 数据正确写入对应存储 |
| 跨存储查询 | 业务场景测试 | 能正确聚合MySQL和MongoDB数据 |
| 事务一致性 | 模拟失败场景 | 补偿机制能恢复数据一致性 |
| 性能基准 | 压测对比 | 混合存储性能不低于原MongoDB方案 |

### 2.4 代码修改点清单

#### 2.4.1 依赖修改

**根 pom.xml 添加**:
```xml
<!-- MyBatis-Plus 3.5.7 for MySQL -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**kiwi-user/pom.xml**:
```xml
<!-- MongoDB 依赖保留（用于用户画像、操作日志等场景） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>

<!-- MyBatis-Plus + MySQL 依赖添加 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- RabbitMQ 依赖移除（在 RabbitMQ 移除阶段执行） -->
<!--
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
    <version>${spring-boot.version}</version>
</dependency>
-->
```

**kiwi-content/pom.xml**: 同上

#### 2.4.2 Entity 类修改

##### MySQL 实体类（MyBatis-Plus 注解）

| 文件 | 修改类型 | 说明 |
|-----|---------|------|
| `kiwi-user/.../entity/UserEntity.java` | 新增 | MyBatis-Plus 实体，包含基础字段（id, username, email, password_hash, created_at） |
| `kiwi-user/.../entity/UserStatsEntity.java` | 新增 | 用户统计实体，与 UserEntity 一对一关联 |
| `kiwi-user/.../entity/UserRelationEntity.java` | 新增 | 用户关注关系实体，使用 @TableName 注解 |
| `kiwi-content/.../entity/ArticleEntity.java` | 新增 | 文章基础信息实体（id, author_id, title, created_at, updated_at） |
| `kiwi-content/.../entity/ArticleStatsEntity.java` | 新增 | 文章统计实体，与 ArticleEntity 一对一关联 |
| `kiwi-content/.../entity/CommentEntity.java` | 新增 | 评论实体，使用 MyBatis-Plus 逻辑删除注解 |
| `kiwi-content/.../entity/ArticleLikeEntity.java` | 新增 | 文章点赞实体，包含唯一约束 |

##### MongoDB 文档类（保留与调整）

| 文件 | 修改类型 | 说明 |
|-----|---------|------|
| `kiwi-user/.../document/UserProfileDocument.java` | 调整 | 用户画像文档，保留 avatar_url, bio, tags, 个性化配置字段 |
| `kiwi-user/.../document/UserOperationLogDocument.java` | 新增 | 用户操作日志文档，用于行为分析 |
| `kiwi-content/.../document/ArticleContentDocument.java` | 调整 | 文章内容文档，保留 content, contentType, ossUrls, tags, 全文索引 |
| `kiwi-content/.../document/ArticleSearchCacheDocument.java` | 新增 | 文章搜索缓存文档，存储预计算的相关性数据 |
| `kiwi-link/.../document/LinkAccessLogDocument.java` | 调整 | 短链接访问日志文档，用于复杂聚合查询 |

##### 原实体类处理

| 原文件 | 处理方式 | 说明 |
|-------|---------|------|
| `User.java` | 重命名并移至 `document/` 目录 | 保留为 MongoDB 文档，移除 MySQL 相关字段 |
| `UserProfile.java` | 删除 | 功能合并到 UserProfileDocument |
| `UserStats.java` | 删除 | 功能由 UserStatsEntity 替代 |
| `Article.java` | 重命名并移至 `document/` 目录 | 保留为 MongoDB 文档，移除 MySQL 相关字段 |
| `ArticleStats.java` | 删除 | 功能由 ArticleStatsEntity 替代 |
| `UserRelation.java` | 删除 | 功能由 UserRelationEntity 替代 |
| `Comment.java` | 删除 | 功能由 CommentEntity 替代 |
| `ArticleLike.java` | 删除 | 功能由 ArticleLikeEntity 替代 |

#### 2.4.3 Repository/Mapper 接口修改

##### MySQL Mapper 接口（MyBatis-Plus）

| 文件 | 原接口 | 新接口 |
|-----|-------|-------|
| `UserMapper.java` | `MongoRepository<User, String>` | `BaseMapper<UserEntity>` |
| `UserStatsMapper.java` | 无 | `BaseMapper<UserStatsEntity>` |
| `UserRelationMapper.java` | `MongoRepository<UserRelation, String>` | `BaseMapper<UserRelationEntity>` |
| `ArticleMapper.java` | `MongoRepository<Article, String>` | `BaseMapper<ArticleEntity>` |
| `ArticleStatsMapper.java` | 无 | `BaseMapper<ArticleStatsEntity>` |
| `CommentMapper.java` | `MongoRepository<Comment, String>` | `BaseMapper<CommentEntity>` |
| `ArticleLikeMapper.java` | `MongoRepository<ArticleLike, String>` | `BaseMapper<ArticleLikeEntity>` |

##### MongoDB Repository 接口（保留）

| 文件 | 接口类型 | 说明 |
|-----|---------|------|
| `UserProfileRepository.java` | `MongoRepository<UserProfileDocument, String>` | 用户画像数据访问 |
| `UserOperationLogRepository.java` | `MongoRepository<UserOperationLogDocument, String>` | 用户操作日志访问 |
| `ArticleContentRepository.java` | `MongoRepository<ArticleContentDocument, String>` | 文章内容数据访问 |
| `ArticleSearchCacheRepository.java` | `MongoRepository<ArticleSearchCacheDocument, String>` | 文章搜索缓存访问 |
| `LinkAccessLogRepository.java` | `MongoRepository<LinkAccessLogDocument, String>` | 短链接访问日志访问 |

#### 2.4.4 Service 层修改

| 文件 | 修改内容 |
|-----|---------|
| `UserServiceImpl.java` | 1. 注入 UserMapper、UserStatsMapper、UserRelationMapper<br>2. 注入 UserProfileRepository（MongoDB）<br>3. 拆分用户数据操作：基础信息存MySQL，画像数据存MongoDB<br>4. 跨存储事务使用补偿机制 |
| `ArticleServiceImpl.java` | 1. 注入 ArticleMapper、ArticleStatsMapper<br>2. 注入 ArticleContentRepository（MongoDB）<br>3. 文章发布：基础信息存MySQL，内容存MongoDB<br>4. 文章查询：联合查询MySQL和MongoDB数据 |
| `CommentServiceImpl.java` | 1. 注入 CommentMapper<br>2. 评论操作完全在MySQL，同步更新ArticleStats<br>3. 使用MyBatis-Plus事务注解 |
| `InteractionServiceImpl.java` | 1. 注入 ArticleLikeMapper、ArticleStatsMapper<br>2. 点赞逻辑：Redis缓存 + MySQL持久化<br>3. 异步更新文章统计 |
| `ArticleSearchServiceImpl.java` | 1. 保留MongoDB全文搜索功能<br>2. 结合MySQL数据丰富搜索结果<br>3. 使用ArticleSearchCacheRepository缓存搜索结果 |

#### 2.4.5 配置文件修改

**application-database.yml (新增，用于双数据源配置)**:
```yaml
# MySQL 数据源配置（MyBatis-Plus）
spring:
  datasource:
    mysql:
      url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/kiwi_user?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      username: ${MYSQL_USER:root}
      password: ${MYSQL_PASSWORD:root}
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        maximum-pool-size: ${MYSQL_POOL_SIZE:20}
        minimum-idle: ${MYSQL_POOL_MIN:5}
        idle-timeout: 300000
        connection-timeout: 20000
        max-lifetime: 1200000

# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: assign_uuid  # UUID主键策略
      logic-delete-field: deleted  # 逻辑删除字段
      logic-delete-value: 1  # 已删除值
      logic-not-delete-value: 0  # 未删除值
  mapper-locations: classpath*:/mapper/**/*.xml

# MongoDB 配置（保留）
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/kiwi_hub}
      database: kiwi_hub
      auto-index-creation: true

# 多数据源配置（可选，如需明确指定）
# spring:
#   datasource:
#     mysql-primary:
#       jdbc-url: ...
#     mongodb-primary:
#       uri: ...
```

### 2.5 性能考虑

#### 2.5.1 MongoDB vs MySQL 性能对比

| 操作类型 | MongoDB | MySQL | 说明 |
|---------|---------|-------|------|
| 单条插入 | 快 | 中等 | MongoDB 无 Schema 验证 |
| 批量插入 | 快 | 中等 | MongoDB 批量写入优化好 |
| 主键查询 | 快 | 快 | 相当 |
| 复杂关联查询 | 慢 | 快 | MySQL JOIN 性能优 |
| 全文检索 | 中等 | 快 | MySQL 8.0 ngram 分词 |
| 聚合统计 | 中等 | 快 | MySQL 聚合函数优化好 |
| 事务支持 | 4.0+ 支持 | 原生支持 | MySQL 事务更成熟 |

#### 2.5.2 性能优化策略

**数据库层面**:
1. 合理设计索引，避免全表扫描
2. 使用覆盖索引减少回表
3. 分页查询使用延迟关联优化
4. 大表考虑分区或分表

**应用层面**:
1. 使用 Redis 缓存热点数据
2. 批量操作代替循环单条操作
 3. 使用 MyBatis-Plus 关联查询或自定义 SQL 解决 N+1 问题
4. 异步处理非核心逻辑

**示例优化**:
```java
// 优化前：循环查询
List<User> users = userIds.stream()
    .map(id -> userRepository.findById(id).orElse(null))
    .toList();

// 优化后：批量查询
List<User> users = userRepository.findAllById(userIds);
```

### 2.6 安全考虑

| 安全措施 | 实现方式 |
|---------|---------|
| SQL 注入防护 | 使用 MyBatis-Plus 参数化查询（#{}语法），禁用原生 SQL 拼接 |
| 连接加密 | SSL/TLS 配置，`useSSL=true` |
| 访问控制 | 数据库用户权限最小化原则 |
| 敏感数据 | 密码继续使用 BCrypt 加密 |
| 审计日志 | 开启 MySQL 审计日志 |
| 备份策略 | 定期全量备份 + binlog 增量备份 |

---

## 三、RabbitMQ 移除策略

### 3.1 RabbitMQ 集成点识别

#### 3.1.1 完整集成点清单

| 服务 | 生产者类 | 方法 | 消费者类 | 消息类型 |
|-----|---------|------|---------|---------|
| kiwi-user | UserServiceImpl | follow() | FollowEventListener | 关注事件 |
| kiwi-user | UserServiceImpl | unfollow() | FollowEventListener | 取关事件 |
| kiwi-content | ArticleServiceImpl | publishArticle() | ArticleEventListener | 发布文章 |
| kiwi-content | ArticleServiceImpl | deleteArticle() | ArticleEventListener | 删除文章 |
| kiwi-content | InteractionServiceImpl | toggleLike() | ArticleLikeListener | 点赞事件 |
| kiwi-content | InteractionServiceImpl | toggleLike() | ArticleViewListener | 点赞计数 |

### 3.2 替代方案设计

#### 3.2.1 方案对比

| 方案 | 优点 | 缺点 | 适用场景 |
|-----|------|------|---------|
| **方案A: 同步调用** | 简单、事务一致性好 | 延迟增加 | 核心业务、需要强一致性 |
| **方案B: CompletableFuture 异步** | 不阻塞主线程、延迟低 | 线程池管理复杂 | 非核心业务、可接受最终一致 |
| **方案C: Spring Event** | 解耦、易测试 | 本地事件，不跨服务 | 服务内解耦 |
| **方案D: 数据库队列表** | 可靠、支持重试 | 性能开销 | 需要持久化的异步任务 |

#### 3.2.2 推荐方案

**采用 方案B (CompletableFuture) + 方案D (数据库队列表) 混合方案**:

- **统计数据更新**：使用 CompletableFuture 异步处理（低延迟）
- **关键业务操作**：使用数据库队列表（可靠性保证）

### 3.3 具体实现方案

#### 3.3.1 异步任务配置类

**新增文件**: `kiwi-common/.../config/AsyncConfig.java`

```java
package com.iot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("statsUpdateExecutor")
    public Executor statsUpdateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("stats-update-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("interactionExecutor")
    public Executor interactionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(2000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("interaction-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

#### 3.3.2 统计服务类 (替代 RabbitMQ 消费者)

**新增文件**: `kiwi-user/.../service/StatsService.java`

```java
package com.iot.kiwiuser.service;

public interface StatsService {
    
    void updateFollowStats(String followerId, String followingId, int delta);
    
    void updateArticleCount(String authorId, int delta);
}
```

**新增文件**: `kiwi-user/.../service/impl/StatsServiceImpl.java`

```java
package com.iot.kiwiuser.service.impl;

import com.iot.kiwiuser.repository.UserRepository;
import com.iot.kiwiuser.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final UserRepository userRepository;

    @Async("statsUpdateExecutor")
    @Transactional
    @Override
    public void updateFollowStats(String followerId, String followingId, int delta) {
        try {
            userRepository.updateFollowingCount(followerId, delta);
            userRepository.updateFollowerCount(followingId, delta);
            log.debug("更新关注统计成功: follower={}, following={}, delta={}", 
                followerId, followingId, delta);
        } catch (Exception e) {
            log.error("更新关注统计失败: follower={}, following={}, delta={}", 
                followerId, followingId, delta, e);
        }
    }

    @Async("statsUpdateExecutor")
    @Transactional
    @Override
    public void updateArticleCount(String authorId, int delta) {
        try {
            userRepository.updateArticleCount(authorId, delta);
            log.debug("更新文章数成功: authorId={}, delta={}", authorId, delta);
        } catch (Exception e) {
            log.error("更新文章数失败: authorId={}, delta={}", authorId, delta, e);
        }
    }
}
```

#### 3.3.3 修改后的 UserServiceImpl

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRelationRepository userRelationRepository;
    private final StatsService statsService;  // 替代 RabbitTemplate

    @Override
    public Result<Object> follow(String userId, String followUserId) {
        // ... 原有逻辑保持不变 ...
        
        // 异步更新统计 (替代 RabbitMQ)
        statsService.updateFollowStats(userId, followUserId, 1);
        
        return Result.success().message("关注成功");
    }

    @Override
    public Result<Object> unfollow(String userId, String followUserId) {
        // ... 原有逻辑保持不变 ...
        
        // 异步更新统计 (替代 RabbitMQ)
        statsService.updateFollowStats(userId, followUserId, -1);
        
        return Result.success().message("取消关注成功");
    }
}
```

#### 3.3.4 点赞服务改造

**新增文件**: `kiwi-content/.../service/LikeSyncService.java`

```java
package com.iot.kiwicontent.service;

public interface LikeSyncService {
    
    void syncLikeToDatabase(String userId, String articleId, String authorId);
    
    void syncUnlikeToDatabase(String userId, String articleId);
}
```

**新增文件**: `kiwi-content/.../service/impl/LikeSyncServiceImpl.java`

```java
package com.iot.kiwicontent.service.impl;

import com.iot.kiwicontent.model.constant.RedisConstant;
import com.iot.kiwicontent.model.pojo.Article;
import com.iot.kiwicontent.model.pojo.ArticleLike;
import com.iot.kiwicontent.repository.ArticleLikeRepository;
import com.iot.kiwicontent.repository.ArticleRepository;
import com.iot.kiwicontent.service.LikeSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeSyncServiceImpl implements LikeSyncService {

    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleRepository articleRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Async("interactionExecutor")
    @Transactional
    @Override
    public void syncLikeToDatabase(String userId, String articleId, String authorId) {
        try {
            ArticleLike like = new ArticleLike(userId, articleId, authorId);
            articleLikeRepository.save(like);
            articleRepository.incrementLikeCount(articleId);
            log.info("点赞数据同步完成: u={} -> a={}", userId, articleId);
        } catch (DataIntegrityViolationException e) {
            log.warn("重复点赞，已忽略: u={}, a={}", userId, articleId);
        } catch (Exception e) {
            log.error("点赞同步失败: u={}, a={}", userId, articleId, e);
        }
    }

    @Async("interactionExecutor")
    @Transactional
    @Override
    public void syncUnlikeToDatabase(String userId, String articleId) {
        try {
            long deleted = articleLikeRepository.deleteByUserIdAndArticleId(userId, articleId);
            if (deleted > 0) {
                articleRepository.decrementLikeCount(articleId);
                log.info("取消点赞同步完成: u={} -> a={}", userId, articleId);
            }
        } catch (Exception e) {
            log.error("取消点赞同步失败: u={}, a={}", userId, articleId, e);
        }
    }
}
```

#### 3.3.5 修改后的 InteractionServiceImpl

```java
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final StringRedisTemplate redisTemplate;
    private final LikeSyncService likeSyncService;  // 替代 RabbitTemplate

    @Override
    public boolean toggleLike(String userId, String articleId, String authorId) {
        String userLikeKey = RedisConstant.USER_LIKE_KEY + articleId;
        String countKey = RedisConstant.LIKE_COUNT_KEY + articleId;

        Boolean isLiked = redisTemplate.opsForSet().isMember(userLikeKey, userId);

        if (Boolean.TRUE.equals(isLiked)) {
            redisTemplate.opsForSet().remove(userLikeKey, userId);
            redisTemplate.opsForValue().decrement(countKey);
            
            // 异步同步到数据库 (替代 RabbitMQ)
            likeSyncService.syncUnlikeToDatabase(userId, articleId);
            return false;
        } else {
            redisTemplate.opsForSet().add(userLikeKey, userId);
            redisTemplate.opsForValue().increment(countKey);
            
            // 异步同步到数据库 (替代 RabbitMQ)
            likeSyncService.syncLikeToDatabase(userId, articleId, authorId);
            return true;
        }
    }
}
```

### 3.4 代码修改点清单

#### 3.4.1 需要删除的文件

| 服务 | 文件路径 |
|-----|---------|
| kiwi-user | `model/constant/RabbitConstant.java` |
| kiwi-user | `rabbitmq/consume/config/UserRabbitConfig.java` |
| kiwi-user | `rabbitmq/consume/config/ArticleRabbitConfig.java` |
| kiwi-user | `rabbitmq/consume/config/ReturnCallbackConfig.java` |
| kiwi-user | `rabbitmq/consume/config/MessageRecovererConfig.java` |
| kiwi-user | `rabbitmq/consume/listener/FollowEventListener.java` |
| kiwi-user | `rabbitmq/consume/listener/ArticleEventListener.java` |
| kiwi-user | `config/RabbitMQMessageConverterConfig.java` |
| kiwi-content | `model/constant/RabbitConstant.java` |
| kiwi-content | `consume/config/ArticleRabbitConfig.java` |
| kiwi-content | `consume/config/ReturnCallbackConfig.java` |
| kiwi-content | `consume/config/MessageRecovererConfig.java` |
| kiwi-content | `consume/listener/ArticleLikeListener.java` |
| kiwi-content | `consume/listener/ArticleViewListener.java` |
| kiwi-content | `config/RabbitMQMessageConverterConfig.java` |

#### 3.4.2 需要修改的文件

| 服务 | 文件 | 修改内容 |
|-----|-----|---------|
| kiwi-user | `pom.xml` | 移除 spring-boot-starter-amqp 依赖 |
| kiwi-user | `service/impl/UserServiceImpl.java` | 移除 RabbitTemplate，注入 StatsService |
| kiwi-content | `pom.xml` | 移除 spring-boot-starter-amqp 依赖 |
| kiwi-content | `service/impl/ArticleServiceImpl.java` | 移除 RabbitTemplate，注入 StatsService |
| kiwi-content | `service/impl/InteractionServiceImpl.java` | 移除 RabbitTemplate，注入 LikeSyncService |

#### 3.4.3 需要新增的文件

| 服务 | 文件 | 说明 |
|-----|-----|------|
| kiwi-common | `config/AsyncConfig.java` | 异步线程池配置 |
| kiwi-user | `service/StatsService.java` | 统计服务接口 |
| kiwi-user | `service/impl/StatsServiceImpl.java` | 统计服务实现 |
| kiwi-content | `service/LikeSyncService.java` | 点赞同步服务接口 |
| kiwi-content | `service/impl/LikeSyncServiceImpl.java` | 点赞同步服务实现 |

### 3.5 系统安全问题

#### 3.5.1 异步处理安全措施

| 风险点 | 防护措施 |
|-------|---------|
| 线程安全 | 使用 @Transactional 保证事务隔离 |
| 数据竞争 | 数据库层面使用乐观锁/悲观锁 |
| 异常丢失 | 统一异常捕获 + 日志记录 |
| 线程池耗尽 | 拒绝策略：CallerRunsPolicy |
| 内存溢出 | 合理设置队列容量 |

#### 3.5.2 线程池监控

```java
@Configuration
public class ThreadPoolMonitorConfig {

    @Bean
    public MBeanExporter threadPoolMBeanExporter(AsyncConfig asyncConfig) {
        MBeanExporter exporter = new MBeanExporter();
        // 暴露线程池指标到 JMX
        return exporter;
    }
}
```

#### 3.5.3 失败重试机制

```java
@Retryable(
    value = {Exception.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public void syncLikeToDatabase(String userId, String articleId, String authorId) {
    // 业务逻辑
}

@Recover
public void recoverSyncLike(Exception e, String userId, String articleId, String authorId) {
    log.error("点赞同步最终失败，记录到死信表: u={}, a={}", userId, articleId);
    // 写入失败记录表，后续人工处理
    failedTaskRepository.save(new FailedTask(userId, articleId, "LIKE_SYNC"));
}
```

---

## 四、整体架构调整

### 4.1 更新后的技术栈（混合存储架构）

| 组件 | 原技术 | 新技术 | 说明 |
|-----|-------|-------|------|
| 结构化数据存储 | MongoDB | MySQL 8.0 | 用户基础信息、关系、文章元数据、评论、点赞 |
| 非结构化数据存储 | MongoDB | MongoDB (保留) | 用户画像、操作日志、文章内容、搜索缓存 |
| 缓存 | Redis | Redis (不变) | 缓存和 Session、点赞状态 |
| 消息队列 | RabbitMQ | 移除 | 使用 CompletableFuture + 异步线程池替代 |
| MySQL ORM | Spring Data MongoDB | MyBatis-Plus 3.5.7 | 结构化数据访问层 |
| MongoDB ORM | Spring Data MongoDB | Spring Data MongoDB (保留) | 非结构化数据访问层 |
| 服务注册 | Nacos | Nacos (不变) | 服务发现 |
| 配置中心 | Nacos | Nacos (不变) | 配置管理 |

### 4.2 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         KiwiHub Architecture                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐       │
│  │  kiwi-user   │    │kiwi-content  │    │  kiwi-link   │       │
│  │   (8070)     │    │   (8010)     │    │   (8030)     │       │
│  │              │    │              │    │              │       │
│  │ ┌──────────┐ │    │ ┌──────────┐ │    │ ┌──────────┐ │       │
│  │ │Controller│ │    │ │Controller│ │    │ │Controller│ │       │
│  │ └────┬─────┘ │    │ └────┬─────┘ │    │ └────┬─────┘ │       │
│  │      ▼       │    │      ▼       │    │      ▼       │       │
│  │ ┌──────────┐ │    │ ┌──────────┐ │    │ ┌──────────┐ │       │
│  │ │ Service  │◄├────┼─┤ Service  │ │    │ │ Service  │ │       │
│  │ └────┬─────┘ │    │ └────┬─────┘ │    │ └────┬─────┘ │       │
│  │      ▼       │    │      ▼       │    │      ▼       │       │
│  │ ┌──────────┐ │    │ ┌──────────┐ │    │ ┌──────────┐ │       │
  │ │ │ MySQL    │ │    │ │ MySQL    │ │    │ │   Redis  │ │       │
  │ │ │Mapper+   │ │    │ │Mapper+   │ │    │ │Template  │ │
  │ │ │MongoRepo │ │    │ │MongoRepo │ │    │ │          │ │       │
│  │ └────┬─────┘ │    │ └────┬─────┘ │    │ └────┬─────┘ │       │
│  └──────┼───────┘    └──────┼───────┘    └──────┼───────┘       │
│         │                   │                   │                │
└─────────┼───────────────────┼───────────────────┼────────────────┘
          │                   │                   │
           ▼                   ▼                   ▼
     ┌──────────┐        ┌──────────┐        ┌──────────┐
     │  MySQL   │        │  Redis   │        │  Redis   │
     │ kiwi_user│        │  (Cache) │        │ (Short   │
     │kiwi_     │        │          │        │  Link)   │
     │ content  │        │          │        │          │
     └──────────┘        └──────────┘        └──────────┘
           │
           ▼
     ┌──────────┐
     │ MongoDB  │
     │(保留数据)│
     │ user_    │
     │ profiles │
     │ article_ │
     │ content  │
     └──────────┘
```

### 4.3 微服务间通信优化

#### 4.3.1 服务调用方式

| 调用场景 | 原方式 | 新方式 |
|---------|-------|-------|
| 跨服务同步调用 | 无 (RabbitMQ 异步) | OpenFeign 同步调用 |
| 服务内异步处理 | RabbitMQ | CompletableFuture |
| 数据同步 | RabbitMQ 消息 | 异步方法调用 |

#### 4.3.2 OpenFeign 配置

**新增文件**: `kiwi-user/.../client/ContentServiceClient.java`

```java
@FeignClient(name = "kiwi-hub-content", path = "/articles")
public interface ContentServiceClient {

    @GetMapping("/{articleId}/author")
    Result<String> getArticleAuthor(@PathVariable("articleId") String articleId);
}
```

### 4.4 测试计划

#### 4.4.1 单元测试

| 测试类 | 测试内容 | 覆盖率目标 |
|-------|---------|-----------|
| UserServiceImplTest | 用户关注、取关逻辑 | ≥80% |
| ArticleServiceImplTest | 文章发布、删除逻辑 | ≥80% |
| InteractionServiceImplTest | 点赞切换逻辑 | ≥80% |
| CommentServiceImplTest | 评论发布、删除逻辑 | ≥80% |
| StatsServiceImplTest | 统计更新逻辑 | ≥90% |
| LikeSyncServiceImplTest | 点赞同步逻辑 | ≥90% |

#### 4.4.2 集成测试

| 测试场景 | 验证点 |
|---------|-------|
| 用户注册登录 | 数据正确写入 MySQL，Session 写入 Redis |
| 发布文章 | 文章写入 MySQL，用户文章数更新 |
| 关注用户 | 关系写入 MySQL，统计数据异步更新 |
| 点赞文章 | Redis 计数更新，MySQL 异步同步 |
| 评论文章 | 评论写入 MySQL，计数更新 |

#### 4.4.3 性能测试

| 测试场景 | 目标指标 |
|---------|---------|
| 用户登录 (1000 QPS) | 响应时间 P99 < 200ms |
| 发布文章 (100 QPS) | 响应时间 P99 < 500ms |
| 点赞操作 (500 QPS) | 响应时间 P99 < 100ms |
| 关注操作 (200 QPS) | 响应时间 P99 < 300ms |

### 4.5 部署和回滚计划

#### 4.5.1 部署流程

```
阶段 1: 数据库部署
├── 1.1 部署 MySQL 集群
├── 1.2 创建数据库和表结构
├── 1.3 数据迁移和验证
└── 1.4 配置主从同步

阶段 2: 服务灰度发布
├── 2.1 部署 kiwi-link (无变更)
├── 2.2 部署 kiwi-user (新版)
├── 2.3 部署 kiwi-content (新版)
└── 2.4 流量切分验证

阶段 3: 监控验证
├── 3.1 监控数据库连接数
├── 3.2 监控错误率
├── 3.3 监控响应时间
└── 3.4 监控线程池状态
```

#### 4.5.2 回滚方案

| 场景 | 回滚操作 | 预计时间 |
|-----|---------|---------|
| 数据库连接异常 | 回滚到 MongoDB 配置 | 5 分钟 |
| 服务启动失败 | 回滚到上一版本镜像 | 3 分钟 |
| 性能严重下降 | 回滚全部服务 | 10 分钟 |
| 数据不一致 | 停止服务，数据修复 | 30 分钟+ |

### 4.6 潜在风险和缓解措施

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| 数据迁移丢失 | 高 | 低 | 全量备份 + 增量同步 + 多轮验证 |
| 性能下降 | 中 | 中 | 压测验证 + 索引优化 + 缓存策略 |
| 异步任务失败 | 中 | 中 | 重试机制 + 失败记录 + 监控告警 |
| 线程池耗尽 | 高 | 低 | 合理配置 + 拒绝策略 + 监控 |
| 外键约束错误 | 中 | 低 | 数据清洗 + 级联删除 |
| 全文检索性能 | 中 | 中 | ngram 分词 + 索引优化 |

---

## 五、实施时间表

| 阶段 | 任务 | 预计时间 | 负责人 |
|-----|------|---------|-------|
| **准备阶段** | 环境搭建、方案评审 | 2 天 | 架构师 |
| **MySQL 迁移** | 数据库设计、数据迁移、代码改造 | 5 天 | 后端开发 |
| **RabbitMQ 移除** | 异步服务开发、代码重构 | 3 天 | 后端开发 |
| **测试阶段** | 单元测试、集成测试、性能测试 | 3 天 | 测试团队 |
| **部署阶段** | 灰度发布、监控验证 | 2 天 | 运维团队 |
| **总计** | - | **15 天** | - |

---

## 六、附录

### 6.1 配置文件模板

**application-database.yml**（混合存储配置）:
```yaml
# MySQL 数据源配置（MyBatis-Plus）
spring:
  datasource:
    mysql:
      url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/kiwi_user?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      username: ${MYSQL_USER:root}
      password: ${MYSQL_PASSWORD:root}
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        maximum-pool-size: ${MYSQL_POOL_SIZE:20}
        minimum-idle: ${MYSQL_POOL_MIN:5}
        idle-timeout: 300000
        connection-timeout: 20000
        max-lifetime: 1200000

# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开发环境开启SQL日志
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: assign_uuid  # UUID主键策略
      logic-delete-field: deleted  # 逻辑删除字段
      logic-delete-value: 1  # 已删除值
      logic-not-delete-value: 0  # 未删除值
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.iot.kiwiuser.entity,com.iot.kiwicontent.entity

# MongoDB 配置（保留）
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/kiwi_hub}
      database: kiwi_hub
      auto-index-creation: true

# 多数据源配置示例（如需明确数据源名称）
# spring:
#   datasource:
#     mysql-primary:
#       jdbc-url: ${spring.datasource.mysql.url}
#       username: ${spring.datasource.mysql.username}
#       password: ${spring.datasource.mysql.password}
#       driver-class-name: com.mysql.cj.jdbc.Driver
#     mongodb-primary:
#       uri: ${spring.data.mongodb.uri}
```

### 6.2 监控指标

| 指标 | 告警阈值 | 说明 |
|-----|---------|------|
| MySQL 连接数 | > 80% 最大连接数 | 连接池可能耗尽 |
| 慢查询 | > 1s | 需要优化 SQL |
| 线程池活跃数 | > 80% 最大线程数 | 可能需要扩容 |
| 异步任务失败率 | > 5% | 需要排查原因 |
| 服务响应时间 | P99 > 1s | 性能告警 |
| 错误率 | > 1% | 业务告警 |

### 6.3 参考文档

1. MyBatis-Plus 官方文档: https://baomidou.com/
2. Spring Data MongoDB 官方文档: https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/
3. MySQL 8.0 参考手册: https://dev.mysql.com/doc/refman/8.0/en/
4. Spring 异步处理: https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling-annotation-support-async
5. Spring Boot 多数据源配置: https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.datasource

---

**文档版本**: v2.0  
**创建日期**: 2026-03-05  
**最后更新**: 2026-03-05  
**作者**: KiwiHub Team  
**重要更新**: 调整为 MySQL + MongoDB 混合存储架构，ORM 框架改为 MyBatis-Plus 3.5.7
