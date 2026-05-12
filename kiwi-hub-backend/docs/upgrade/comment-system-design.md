# 评论系统设计文档

> 版本：1.0 | 日期：2026-03-15 | 模块：`kiwi-content`

---

## 一、设计目标

实现类似微博/掘金的"楼中楼"评论结构：

- 文章下有若干**一级评论**（楼层）
- 每条一级评论下可以有**无限层级的回复**（楼中楼）
- 前端展示时，楼中楼内部**线性平铺**，通过 `@父评论作者` 的方式体现层级关系
- 支持软删除，删除后保留评论树结构完整性

---

## 二、数据库表设计

```sql
CREATE TABLE `comment` (
    `id`         BIGINT      NOT NULL COMMENT '评论ID（雪花算法，时间有序）',
    `article_id` VARCHAR(32) NOT NULL COMMENT '文章ID',
    `author_id`  VARCHAR(32) NOT NULL COMMENT '评论者ID',
    `content`    TEXT        NOT NULL COMMENT '评论内容',
    `parent_id`  BIGINT      NULL     COMMENT '父评论ID（为空表示根评论）',
    `root_id`    BIGINT      NULL     COMMENT '根评论ID（快速定位评论串根节点）',
    `status`     TINYINT     NOT NULL DEFAULT 0 COMMENT '0-正常，1-已删除',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_article_id` (`article_id`),
    INDEX `idx_root_id` (`root_id`),
    INDEX `idx_parent_id` (`parent_id`)
);
```

### 核心字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 雪花算法生成的 64 位长整型，时间有序，由 MyBatis-Plus `ASSIGN_ID` 生成 |
| `parent_id` | BIGINT | 直接父评论的 ID；**为 NULL 表示一级评论** |
| `root_id` | BIGINT | 整条评论串根节点的 ID；**一级评论的 root_id = 自身 id** |
| `status` | TINYINT | 软删除标志，删除后内容替换为占位文本，树结构保留 |

---

## 三、数据模型（邻接表 + 根节点冗余）

### 方案选型

| 方案 | 优点 | 缺点 |
|------|------|------|
| 纯邻接表 | 结构简单 | 查询某根节点下所有回复需递归，性能差 |
| 闭包表 | 查询灵活 | 写入复杂，存储开销大 |
| **邻接表 + root_id 冗余**（本项目） | 查询楼中楼只需一次 `WHERE root_id = ?`，性能好 | 写入时需维护 root_id |

`root_id` 是关键的冗余字段：同一条评论串下所有节点（无论嵌套多深）的 `root_id` 都指向最顶层的一级评论，从而将树形结构"打平"，一次查询即可取出整条楼中楼。

### 结构示意

```
评论A  (parent_id=NULL,  root_id=A)   ← 一级评论（楼层）
  └─ 评论B  (parent_id=A, root_id=A)  ← 回复A
       └─ 评论C  (parent_id=B, root_id=A)  ← 回复B（@B的作者）
            └─ 评论D  (parent_id=C, root_id=A)  ← 回复C（@C的作者）

评论E  (parent_id=NULL,  root_id=E)   ← 另一个一级评论
  └─ 评论F  (parent_id=E, root_id=E)
```

前端渲染楼中楼时，查询 `root_id = A` 且 `parent_id != A` 的所有记录，线性平铺展示，通过 `parentAuthorName` 字段显示 `@谁`。

---

## 四、核心业务逻辑

### 4.1 发布评论

```
POST /comments
Body: { articleId, content, parentId? }
```

**一级评论**（`parentId` 为空）：

```
1. 构建 CommentEntity，save() 写库（雪花 ID 在 insert 前由 MyBatis-Plus 在应用层生成）
2. 将 root_id 回填为自身 id，updateById()
3. article_stats.comment_count + 1
```

> 雪花算法的 ID 在 insert 前即可确定，但当前实现仍沿用两次写库的方式回填 root_id。
> 可优化为：insert 前手动调用 `IdentifierGenerator` 预生成 ID，一次写库完成。

**子评论**（`parentId` 不为空）：

```
1. 查询父评论，校验其存在且未被删除
2. parent_id = 父评论.id，root_id = 父评论.root_id（继承，不论嵌套多深）
3. save() 一次写库完成
4. article_stats.comment_count + 1
```

### 4.2 删除评论（软删除）

```
DELETE /comments/{commentId}
```

- 通过 `UPDATE ... SET status=1 WHERE id=? AND author_id=? AND status=0` 原子操作，同时校验权限
- 不物理删除，保留树结构，前端展示"该评论已删除"
- `article_stats.comment_count - 1`

### 4.3 查询一级评论（偏移分页）

```
GET /comments/roots?articleId=&pageNum=&pageSize=
```

筛选条件：`article_id = ? AND status = 0 AND id = root_id`

排序：`ORDER BY created_at DESC`（最新楼层在前）

`id = root_id` 是识别一级评论的判断依据，使用 MyBatis-Plus `Page` 对象做标准偏移分页，返回 `PageResult` 包含总数、总页数等分页元数据。

### 4.4 查询回复列表（游标分页）

```
GET /comments/replies?articleId=&rootId=&parentId=&cursorId=&pageSize=
```

通用接口，通过 `parentId` 控制查询层级：

| 场景 | parentId 传值 |
|------|--------------|
| 查二级评论 | `rootId`（根评论自身 id） |
| 查三级回复 | 某条二级评论的 id |

每条回复都携带 `replyCount`，表示其直接子回复数，前端据此渲染"展开 N 条回复"入口。

筛选条件：`root_id = ? AND parent_id = ? AND status = 0`

排序：`ORDER BY id ASC`（雪花 ID 单调递增，等价于按发布时间正序）

**游标分页流程：**

```
首次请求（cursorId 不传）：
  WHERE root_id = ? AND parent_id = ? AND status = 0
  ORDER BY id ASC
  LIMIT pageSize
  → 返回第一批数据，响应携带 nextCursorId（最后一条的 id）

后续请求（带上 nextCursorId）：
  WHERE root_id = ? AND parent_id = ? AND status = 0
    AND id > cursorId
  ORDER BY id ASC
  LIMIT pageSize
  → 无更多数据时不返回 nextCursorId
```

**replyCount 的查询方式：**

查二级评论时，用一次 `IN` 查询批量取出所有二级评论的子回复，再在应用层 `groupingBy` 统计，避免 N+1 问题。

**三层结构示意：**

```
一级评论 A（楼层）
  ├─ 二级评论 B（parentId=A）  replyCount=2
  │    ├─ 三级回复 C（parentId=B，@B）   [点击展开后加载]
  │    └─ 三级回复 F（parentId=B，@B）
  └─ 二级评论 D（parentId=A）  replyCount=1
       └─ 三级回复 E（parentId=D，@D）   [点击展开后加载]
```

C 和 F 都回复 B，始终聚合在 B 下面，不会因为时间顺序被 D、E 打断。

---

## 五、接口汇总

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/comments` | 发布评论（一级或回复） |
| DELETE | `/comments/{commentId}` | 删除自己的评论 |
| GET | `/comments/roots` | 获取文章一级评论（偏移分页） |
| GET | `/comments/replies` | 查回复列表，parentId=rootId 查二级（携带 replyCount），parentId=二级id 查三级（游标分页） |

---

## 六、待完善项

| 问题 | 说明 |
|------|------|
| 用户信息未填充 | `CommentVO` 中 `authorName`、`authorAvatar`、`parentAuthorName` 目前为空，需通过 Feign 调用 `kiwi-user` 服务补全 |
| 评论点赞 | `CommentVO` 中 `likeCount`、`isLiked` 字段已预留，功能尚未实现 |
| 一级评论两次写库 | 雪花 ID 在 insert 前即可在应用层确定，可预生成后直接设置 `root_id = id`，避免回填的第二次 update |
| 评论计数一致性 | `comment_count` 更新失败时仅打印日志，不影响主流程，存在计数与实际数据短暂不一致的风险，可引入补偿机制 |
