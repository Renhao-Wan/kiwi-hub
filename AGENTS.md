# KiwiHub Agent 指南

## 项目概述

KiwiHub 是一个基于 Spring Boot 3.3.4 的微服务社交内容分享平台，采用**纯 NoSQL 架构**（MongoDB + Redis）。基于 Spring Cloud Alibaba 构建，包含三个服务：`kiwi-user`、`kiwi-content` 和 `kiwi-link`。

## 构建/检查/测试命令

```bash
# 构建所有模块
mvn clean install -DskipTests

# 构建指定模块
mvn clean install -pl kiwi-service/kiwi-user -am

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=ArticleServiceTest -pl kiwi-service/kiwi-content

# 运行单个测试方法
mvn test -Dtest=ArticleServiceTest#testPublishArticle -pl kiwi-service/kiwi-content

# 仅编译（快速检查）
mvn compile -DskipTests

# 打包（跳过测试）
mvn package -DskipTests

# 本地运行服务（在模块目录下）
mvn spring-boot:run -pl kiwi-service/kiwi-user

# 跳过代码检查工具（如已配置）
mvn clean install -DskipTests -Dcheckstyle.skip=true
```

## 项目结构

```
KiwiHub/
├── kiwi-common/           # 公共工具类、异常、结果封装
│   └── src/main/java/com/iot/common/
│       ├── constant/      # 消息常量、Session 常量
│       ├── context/       # UserContext (ThreadLocal 用户上下文)
│       ├── exception/     # ServiceException, GlobalExceptionHandler
│       └── result/        # Result, PageResult, ResultCodeEnum
├── kiwi-service/
│   ├── kiwi-user/         # 用户认证、个人资料、关注功能 (端口 8070)
│   ├── kiwi-content/      # 文章、评论、点赞互动 (端口 8010)
│   └── kiwi-link/         # 短链接服务 (端口 8030)
└── docs/                  # 部署文档
```

## 代码风格规范

### Import 导入顺序

```java
// 顺序：java.* -> jakarta.* -> org.* -> com.* -> 其他
// 静态导入放在最后
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.iot.common.result.Result;
import com.iot.kiwicontent.model.pojo.Article;

import static com.iot.kiwicontent.model.constant.RabbitConstant.*;
```

- 使用具体导入而非通配符导入：`import java.util.List` 而非 `import java.util.*`
- Lombok 常用导入：`lombok.Data`、`lombok.Builder`、`lombok.RequiredArgsConstructor`

### 格式化规范

- **缩进**：4 个空格（禁止使用 Tab）
- **行宽**：最大 120 字符
- **大括号**：K&R 风格，左大括号不换行
- **空行**：方法之间一个空行，类区域之间两个空行

```java
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    @Override
    public Article getArticleDetail(String articleId) {
        return articleRepository.findById(articleId)
                .orElse(Article.builder().build());
    }

    @Override
    public void publishArticle(String userId, PublishArticleDTO dto) {
        Article article = Article.builder()
                .authorId(userId)
                .title(dto.getTitle())
                .content(dto.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        articleRepository.save(article);
    }
}
```

### 类型使用规范

- **Java 版本**：Java 17（使用 record、模式匹配、文本块等新特性）
- **空值处理**：返回值使用 `Optional`，参数使用 `Objects.requireNonNull`
- **集合**：优先使用不可变集合：`List.of()`、`Map.of()` 或 stream 的 `.toList()`
- **日期时间**：使用 `LocalDateTime`

```java
// 推荐：返回值使用 Optional 处理可空值
public Optional<User> findById(String id) {
    return userRepository.findById(id);
}

// 推荐：使用 instanceof 模式匹配
if (userMap instanceof Map<?, ?> map) {
    Object id = map.get("id");
}

// 推荐：不可变集合与空安全处理
List<String> tags = CollectionUtils.isEmpty(dto.getTags()) 
        ? null 
        : dto.getTags();
```

### 命名约定

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 类 | PascalCase | `ArticleServiceImpl` |
| 接口 | PascalCase，不加前缀 | `ArticleService` |
| 方法 | camelCase，动词开头 | `getArticleDetail`、`publishArticle` |
| 变量 | camelCase | `articleId`、`userList` |
| 常量 | SCREAMING_SNAKE_CASE | `USER_RELATION_EXCHANGE` |
| 包名 | 全小写 | `com.iot.kiwicontent.service.impl` |
| DTO | XxDTO | `PublishArticleDTO`、`UserProfileDTO` |
| VO | XxVO | `ArticleListVO`、`UserCardVO` |
| POJO/实体 | PascalCase | `Article`、`User` |
| Repository | XxRepository | `ArticleRepository` |

### 注解顺序

```java
// 类级别注解
@Tag(name = "文章管理")
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/articles")
public class ArticleController {

    // 字段级别注解（Lombok 生成构造器）
    private final ArticleService articleService;

    // 方法级别注解
    @Operation(summary = "发布文章")
    @PostMapping
    public Result<Object> publishArticle(@RequestBody @Validated PublishArticleDTO dto) {
        // ...
    }
}
```

### 错误处理

1. **所有 API 响应使用 `Result<T>`**：

```java
// 成功响应
return Result.success(data);
return Result.success();

// 失败响应
return Result.fail().message("文章不存在");
return Result.result(ResultCodeEnum.UNAUTHORIZED);

// 带数据的失败响应
return Result.<Object>fail().message("操作失败").data(errorInfo);
```

2. **业务错误抛出 `ServiceException`**：

```java
if (user == null) {
    throw new ServiceException("用户不存在");
}
```

3. **GlobalExceptionHandler 处理规则**：
   - `MethodArgumentNotValidException` -> 400 Bad Request
   - `ServiceException` -> 200 响应码，业务失败
   - `UnauthorizedRequestException` -> 401 Unauthorized
   - `ConnectException` -> 503 Service Unavailable

### 注释规范

```java
/**
 * 文章服务实现类
 * @author wan
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    /**
     * 发表文章
     *
     * @param userId 用户ID
     * @param dto 发表文章参数
     */
    @Override
    public void publishArticle(String userId, PublishArticleDTO dto) {
        // TODO: RabbitMQ 通知粉丝
    }
}
```

- 类注释：Javadoc 格式，包含 `@author`
- 公共方法：Javadoc 格式，包含 `@param`、`@return`
- 使用 `// TODO:` 标记未完成工作
- 使用 `// NOTE:` 标记重要说明

## 架构模式

### 分层架构

```
Controller -> Service -> Repository -> MongoDB/Redis
                |
                v
           RabbitMQ (异步消息)
```

### Controller 模式

```java
@Tag(name = "文章管理")
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/articles")
public class ArticleController {
    private final ArticleService articleService;

    @Operation(summary = "获取文章详情")
    @GetMapping("{articleId}")
    public Result<Article> getArticleDetail(@PathVariable String articleId) {
        return Result.success(articleService.getArticleDetail(articleId));
    }
}
```

### Service 模式

- 接口定义：`XxxService` 放在 `service/` 包
- 实现类：`XxxServiceImpl` 放在 `service/impl/` 包
- 使用 `@RequiredArgsConstructor` 进行构造器注入

### Repository 模式

```java
public interface ArticleRepository extends MongoRepository<Article, String> {
    @Query(value = "{'authorId': ?0}", sort = "{ 'updatedAt': -1 }")
    Page<Article> findByAuthorId(String userId, Pageable pageable);
    
    boolean existsByIdAndAuthorId(String articleId, String userId);
}
```

### UserContext (ThreadLocal 用户上下文)

```java
// 在 Filter 中设置用户
UserContext.setUserId(userId);

// 在 Service/Controller 中获取用户
String userId = UserContext.getUserId();

// 必须在 finally 块中清理
try {
    filterChain.doFilter(request, response);
} finally {
    UserContext.clear();
}
```

### RabbitMQ 消息模式

```java
// 生产者发送消息
rabbitTemplate.convertAndSend(
    RabbitConstant.ARTICLE_USER_EXCHANGE,
    routingKey,
    Map.of("authorId", userId, "action", "publish")
);

// 消费者监听消息
@RabbitListener(queues = RabbitConstant.USER_RELATION_QUEUE)
public void handleFollowEvent(Map<String, String> message) {
    String action = message.get(ParameterConstant.FOLLOW_ACTION);
    // 处理消息
}
```

## 核心依赖

- Spring Boot 3.3.4
- Spring Cloud Alibaba 2023.0.3.2
- MongoDB (Spring Data MongoDB)
- Redis (Spring Data Redis + Redisson)
- RabbitMQ (Spring AMQP)
- Lombok 1.18.42
- Knife4j (OpenAPI 3.0 接口文档)
- Hutool 5.8.36（工具库）
- Guava 33.5.0（布隆过滤器、缓存）

## 常用代码片段

### 分页查询

```java
Pageable pageable = PageRequest.of(pageNum - 1, pageSize,
        Sort.by(Sort.Direction.DESC, "updatedAt"));
Page<Article> page = articleRepository.findAll(pageable);
return PageResult.restPage(page);
```

### Builder 模式（实体类）

```java
Article article = Article.builder()
        .authorId(userId)
        .title(dto.getTitle())
        .createdAt(LocalDateTime.now())
        .build();
```

### DTO 参数校验

```java
@Data
public class PublishArticleDTO {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;
}
```

### 空安全集合处理

```java
List<String> tags = CollectionUtils.isEmpty(dto.getTags()) 
        ? null 
        : dto.getTags();
```
