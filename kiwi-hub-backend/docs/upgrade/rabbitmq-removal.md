# KiwiHub RabbitMQ 移除技术方案

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

## 二、RabbitMQ 移除策略

### 2.1 RabbitMQ 集成点识别

#### 2.1.1 完整集成点清单

| 服务 | 生产者类 | 方法 | 消费者类 | 消息类型 |
|-----|---------|------|---------|---------|
| kiwi-user | UserServiceImpl | follow() | FollowEventListener | 关注事件 |
| kiwi-user | UserServiceImpl | unfollow() | FollowEventListener | 取关事件 |
| kiwi-content | ArticleServiceImpl | publishArticle() | ArticleEventListener | 发布文章 |
| kiwi-content | ArticleServiceImpl | deleteArticle() | ArticleEventListener | 删除文章 |
| kiwi-content | InteractionServiceImpl | toggleLike() | ArticleLikeListener | 点赞事件 |
| kiwi-content | InteractionServiceImpl | toggleLike() | ArticleViewListener | 点赞计数 |

### 2.2 替代方案设计

#### 2.2.1 方案对比

| 方案 | 优点 | 缺点 | 适用场景 |
|-----|------|------|---------|
| **方案A: 同步调用** | 简单、事务一致性好 | 延迟增加 | 核心业务、需要强一致性 |
| **方案B: CompletableFuture 异步** | 不阻塞主线程、延迟低 | 线程池管理复杂 | 非核心业务、可接受最终一致 |
| **方案C: Spring Event** | 解耦、易测试 | 本地事件，不跨服务 | 服务内解耦 |
| **方案D: 数据库队列表** | 可靠、支持重试 | 性能开销 | 需要持久化的异步任务 |

#### 2.2.2 推荐方案

**采用 方案B (CompletableFuture) + 方案D (数据库队列表) 混合方案**:

- **统计数据更新**：使用 CompletableFuture 异步处理（低延迟）
- **关键业务操作**：使用数据库队列表（可靠性保证）

### 2.3 具体实现方案

#### 2.3.1 异步任务配置类

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

#### 2.3.2 统计服务类 (替代 RabbitMQ 消费者)

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

#### 2.3.3 修改后的 UserServiceImpl

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

#### 2.3.4 点赞服务改造

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

#### 2.3.5 修改后的 InteractionServiceImpl

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

### 2.4 代码修改点清单

#### 2.4.1 需要删除的文件

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

#### 2.4.2 需要修改的文件

| 服务 | 文件 | 修改内容 |
|-----|-----|---------|
| kiwi-user | `pom.xml` | 移除 spring-boot-starter-amqp 依赖 |
| kiwi-user | `service/impl/UserServiceImpl.java` | 移除 RabbitTemplate，注入 StatsService |
| kiwi-content | `pom.xml` | 移除 spring-boot-starter-amqp 依赖 |
| kiwi-content | `service/impl/ArticleServiceImpl.java` | 移除 RabbitTemplate，注入 StatsService |
| kiwi-content | `service/impl/InteractionServiceImpl.java` | 移除 RabbitTemplate，注入 LikeSyncService |

#### 2.4.3 需要新增的文件

| 服务 | 文件 | 说明 |
|-----|-----|------|
| kiwi-common | `config/AsyncConfig.java` | 异步线程池配置 |
| kiwi-user | `service/StatsService.java` | 统计服务接口 |
| kiwi-user | `service/impl/StatsServiceImpl.java` | 统计服务实现 |
| kiwi-content | `service/LikeSyncService.java` | 点赞同步服务接口 |
| kiwi-content | `service/impl/LikeSyncServiceImpl.java` | 点赞同步服务实现 |

### 2.5 系统安全问题

#### 2.5.1 异步处理安全措施

| 风险点 | 防护措施 |
|-------|---------|
| 线程安全 | 使用 @Transactional 保证事务隔离 |
| 数据竞争 | 数据库层面使用乐观锁/悲观锁 |
| 异常丢失 | 统一异常捕获 + 日志记录 |
| 线程池耗尽 | 拒绝策略：CallerRunsPolicy |
| 内存溢出 | 合理设置队列容量 |

#### 2.5.2 线程池监控

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

#### 2.5.3 失败重试机制

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

## 三、整体架构调整

### 3.1 更新后的技术栈（移除RabbitMQ）

| 组件 | 原技术 | 新技术 | 说明 |
|-----|-------|-------|------|
| 结构化数据存储 | MongoDB | MySQL 8.0 | 用户基础信息、关系、文章元数据、评论、点赞 |
| 非结构化数据存储 | MongoDB | MongoDB (保留) | 用户画像、操作日志、文章内容、搜索缓存 |
| 缓存 | Redis | Redis (不变) | 缓存和 Session、点赞状态 |
| 消息队列 | RabbitMQ | 移除 | 使用 CompletableFuture + 异步线程池替代 |
| MySQL ORM | Spring Data MongoDB | MyBatis-Plus 3.5.7 | 结构化数据访问层 |
| MongoDB ORM | Spring Data MongoDB | Spring Data MongoDB (保留) | 非结构化数据访问层 |
| 异步处理 | RabbitMQ | CompletableFuture + ThreadPoolTaskExecutor | 服务内异步调用 |
| 服务注册 | Nacos | Nacos (不变) | 服务发现 |
| 配置中心 | Nacos | Nacos (不变) | 配置管理 |

### 3.2 架构图（移除RabbitMQ后）

```
┌─────────────────────────────────────────────────────────────────┐
│                    KiwiHub Architecture (无RabbitMQ)              │
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
│  │ │ Service  │ │    │ │ Service  │ │    │ │ Service  │ │       │
│  │ └────┬─────┘ │    │ └────┬─────┘ │    │ └────┬─────┘ │       │
│  │      ▼       │    │      ▼       │    │      ▼       │       │
│  │┌────────────┐│    │┌────────────┐│    │┌────────────┐│       │
│  ││异步线程池   ││    ││异步线程池   ││    ││    Redis   ││       │
│  ││StatsService││    ││LikeSync    ││    ││  Template  ││       │
│  │└────┬───────┘│    │└────┬───────┘│    │└────┬───────┘│       │
│  └─────┼────────┘    └─────┼────────┘    └─────┼────────┘       │
│        │                   │                   │                │
└────────┼───────────────────┼───────────────────┼────────────────┘
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

### 3.3 微服务间通信优化

#### 3.3.1 服务调用方式

| 调用场景 | 原方式 | 新方式 |
|---------|-------|-------|
| 跨服务同步调用 | 无 (RabbitMQ 异步) | OpenFeign 同步调用 |
| 服务内异步处理 | RabbitMQ | CompletableFuture |
| 数据同步 | RabbitMQ 消息 | 异步方法调用 |

#### 3.3.2 OpenFeign 配置

**新增文件**: `kiwi-user/.../client/ContentServiceClient.java`

```java
@FeignClient(name = "kiwi-hub-content", path = "/articles")
public interface ContentServiceClient {

    @GetMapping("/{articleId}/author")
    Result<String> getArticleAuthor(@PathVariable("articleId") String articleId);
}
```

### 3.4 测试计划

#### 3.4.1 单元测试

| 测试类 | 测试内容 | 覆盖率目标 |
|-------|---------|-----------|
| UserServiceImplTest | 用户关注、取关逻辑 | ≥80% |
| ArticleServiceImplTest | 文章发布、删除逻辑 | ≥80% |
| InteractionServiceImplTest | 点赞切换逻辑 | ≥80% |
| StatsServiceImplTest | 统计更新逻辑 | ≥90% |
| LikeSyncServiceImplTest | 点赞同步逻辑 | ≥90% |

#### 3.4.2 集成测试

| 测试场景 | 验证点 |
|---------|-------|
| 关注用户 | 关系写入数据库，统计数据异步更新 |
| 点赞文章 | Redis 计数更新，MySQL 异步同步 |
| 异步任务执行 | 线程池正常调度，无任务丢失 |

#### 3.4.3 性能测试

| 测试场景 | 目标指标 |
|---------|---------|
| 关注操作 (200 QPS) | 响应时间 P99 < 300ms |
| 点赞操作 (500 QPS) | 响应时间 P99 < 100ms |
| 异步任务处理 | 线程池队列积压 < 50% |

### 3.5 部署和回滚计划

#### 3.5.1 部署流程

```
阶段 1: 服务准备
├── 1.1 移除 RabbitMQ 依赖
├── 1.2 添加异步处理配置
├── 1.3 更新服务代码
└── 1.4 编译打包验证

阶段 2: 灰度发布
├── 2.1 部署 kiwi-user (新版)
├── 2.2 部署 kiwi-content (新版)
├── 2.3 验证异步处理功能
└── 2.4 逐步切分流量

阶段 3: 监控验证
├── 3.1 监控线程池状态
├── 3.2 监控异步任务成功率
├── 3.3 监控服务响应时间
└── 3.4 监控错误率
```

#### 3.5.2 回滚方案

| 场景 | 回滚操作 | 预计时间 |
|-----|---------|---------|
| 异步任务失败率高 | 回滚到 RabbitMQ 版本 | 5 分钟 |
| 线程池耗尽 | 调整配置或回滚 | 3 分钟 |
| 性能严重下降 | 回滚全部服务 | 10 分钟 |

### 3.6 潜在风险和缓解措施

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| 异步任务丢失 | 高 | 中 | 重试机制 + 失败记录 + 监控告警 |
| 线程池耗尽 | 高 | 低 | 合理配置 + 拒绝策略 + 监控 |
| 数据不一致 | 中 | 中 | 事务管理 + 补偿机制 |
| 性能下降 | 中 | 低 | 压测验证 + 线程池调优 |

---

## 四、实施时间表

| 阶段 | 任务 | 预计时间 | 负责人 |
|-----|------|---------|-------|
| **准备阶段** | 方案评审、环境准备 | 1 天 | 架构师 |
| **代码改造** | 异步服务开发、代码重构 | 3 天 | 后端开发 |
| **测试阶段** | 单元测试、集成测试、性能测试 | 2 天 | 测试团队 |
| **部署阶段** | 灰度发布、监控验证 | 1 天 | 运维团队 |
| **总计** | - | **7 天** | - |

---

## 五、附录

### 5.1 配置文件模板

**application-async.yml**（异步处理配置）:
```yaml
# 异步线程池配置
async:
  thread-pool:
    stats-update:
      core-pool-size: 4
      max-pool-size: 16
      queue-capacity: 1000
      keep-alive-seconds: 60
      thread-name-prefix: stats-update-
    interaction:
      core-pool-size: 8
      max-pool-size: 32
      queue-capacity: 2000
      keep-alive-seconds: 60
      thread-name-prefix: interaction-

# Spring 异步配置
spring:
  task:
    execution:
      pool:
        stats-update:
          core-size: ${async.thread-pool.stats-update.core-pool-size}
          max-size: ${async.thread-pool.stats-update.max-pool-size}
          queue-capacity: ${async.thread-pool.stats-update.queue-capacity}
          keep-alive: ${async.thread-pool.stats-update.keep-alive-seconds}s
          thread-name-prefix: ${async.thread-pool.stats-update.thread-name-prefix}
        interaction:
          core-size: ${async.thread-pool.interaction.core-pool-size}
          max-size: ${async.thread-pool.interaction.max-pool-size}
          queue-capacity: ${async.thread-pool.interaction.queue-capacity}
          keep-alive: ${async.thread-pool.interaction.keep-alive-seconds}s
          thread-name-prefix: ${async.thread-pool.interaction.thread-name-prefix}
```

### 5.2 监控指标

| 指标 | 告警阈值 | 说明 |
|-----|---------|------|
| 线程池活跃数 | > 80% 最大线程数 | 可能需要扩容 |
| 线程池队列大小 | > 80% 队列容量 | 任务积压严重 |
| 异步任务失败率 | > 5% | 需要排查原因 |
| 异步任务平均耗时 | > 5s | 任务处理慢 |
| 线程池拒绝任务数 | > 0 | 线程池已满 |

### 5.3 参考文档

1. Spring 异步处理: https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling-annotation-support-async
2. CompletableFuture 指南: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html
3. ThreadPoolTaskExecutor 配置: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/concurrent/ThreadPoolTaskExecutor.html
4. Spring Retry 重试机制: https://docs.spring.io/spring-batch/docs/current/reference/html/retry.html

---

**文档版本**: v2.0  
**创建日期**: 2026-03-05  
**最后更新**: 2026-03-05  
**作者**: KiwiHub Team  
**重要更新**: 移除 RabbitMQ，采用 CompletableFuture + 异步线程池方案