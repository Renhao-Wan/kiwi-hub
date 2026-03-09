# KiwiHub Agent 指南

## 项目概述

KiwiHub 是基于 Spring Boot 3.3.4 的微服务社交平台，采用 **MySQL + MyBatis-Plus 3.5.7** 架构。基于 Spring Cloud Alibaba 构建，包含三个服务：`kiwi-user`、`kiwi-content` 和 `kiwi-link`。MongoDB 仅用于个性化推荐缓存、用户配置和文章搜索索引。

## 构建/测试命令

```bash
mvn clean install -DskipTests              # 构建所有模块
mvn clean install -pl kiwi-service/kiwi-user -am  # 构建指定模块
mvn test                                  # 运行所有测试
mvn test -Dtest=ArticleServiceTest -pl kiwi-service/kiwi-content  # 运行单个测试类
mvn spring-boot:run -pl kiwi-service/kiwi-user  # 本地运行服务
```

## 代码风格

### Import 顺序
java.* → jakarta.* → org.* → com.* → 静态导入

### 格式化
- 缩进：4 空格，禁止 Tab
- 行宽：最大 120 字符
- 大括号：K&R 风格，左括号不换行
- 空行：方法间 1 行，类间 2 行

### 命名约定
- 类：PascalCase (`ArticleServiceImpl`)
- 接口：PascalCase (`ArticleService`)
- 方法/变量：camelCase (`getArticleDetail`, `articleId`)
- 常量：SCREAMING_SNAKE_CASE (`USER_RELATION_EXCHANGE`)
- DTO/VO：XxDTO/XxVO (`PublishArticleDTO`)

### 错误处理
```java
return Result.success(data);              // 成功
return Result.fail().message("文章不存在");  // 失败
throw new ServiceException("用户不存在");  // 业务异常
```

## 架构模式

### Controller
```java
@Tag(name = "文章管理")
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/articles")
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping("{articleId}")
    public Result<Article> getArticleDetail(@PathVariable String articleId) {
        return Result.success(articleService.getArticleDetail(articleId));
    }
}
```

### Mapper (MyBatis-Plus BaseMapper)
```java
public interface ArticleMapper extends BaseMapper<Article> {
    @Select("SELECT * FROM article WHERE author_id = #{authorId}")
    List<Article> findByAuthorId(String authorId);
}
```

### CompletableFuture 异步处理
```java
return CompletableFuture.runAsync(() -> {
    // 异步逻辑
}, followTaskExecutor);
```

## 核心依赖
- Spring Boot 3.3.4
- MyBatis-Plus 3.5.7
- MySQL 8.3.0
- Redisson 3.30.0
- Lombok 1.18.42

### MyBatis-Plus 规范
参考 `./docs/specification/mybatis-plus-specification.md`
