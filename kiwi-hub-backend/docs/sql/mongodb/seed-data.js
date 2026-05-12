// KiwiHub MongoDB 测试数据种子文件
// 执行方式：mongosh kiwi_hub < seed-data.js
// 所有 _id 与 MySQL user.id / article.id 保持一致（Long 类型）
// Version: 1.1  Date: 2026-03-21

// ============================================================
// 1. users 集合（对应 kiwi-user 服务 User.java）
//    _id 与 MySQL user.id 一致（BIGINT 自增）
// ============================================================
db = db.getSiblingDB('kiwi-hub');

db.users.drop();
db.users.insertMany([
  {
    _id: NumberLong(1), username: 'alice_dev',
    profile: { avatar_url: 'https://cdn.example.com/avatars/1.jpg', bio: '专注 Java 后端与云原生，Spring 布道者。', tags: ['Java', 'Spring Boot', 'Redis', '云原生'] }
  },
  {
    _id: NumberLong(2), username: 'bob_coder',
    profile: { avatar_url: 'https://cdn.example.com/avatars/2.jpg', bio: '全栈工程师，热爱 DevOps 与容器化。', tags: ['Docker', 'Kubernetes', 'CI/CD', 'Go'] }
  },
  {
    _id: NumberLong(3), username: 'carol_writer',
    profile: { avatar_url: 'https://cdn.example.com/avatars/3.jpg', bio: '架构师，DDD 实践者，微服务布道者。', tags: ['架构设计', 'DDD', '微服务', '分布式'] }
  },
  {
    _id: NumberLong(4), username: 'dave_arch',
    profile: { avatar_url: 'https://cdn.example.com/avatars/4.jpg', bio: '高并发系统设计专家，10年后端经验。', tags: ['高并发', '系统设计', '消息队列', 'MySQL'] }
  },
  {
    _id: NumberLong(5), username: 'eve_frontend',
    profile: { avatar_url: 'https://cdn.example.com/avatars/5.jpg', bio: '前端工程师，Vue/React 双修，性能优化达人。', tags: ['Vue3', 'React', '前端性能', 'TypeScript'] }
  },
  {
    _id: NumberLong(6), username: 'frank_ops',
    profile: { avatar_url: 'https://cdn.example.com/avatars/6.jpg', bio: 'Linux 内核爱好者，SRE 工程师。', tags: ['Linux', 'SRE', '运维', '性能调优'] }
  },
  {
    _id: NumberLong(7), username: 'grace_ml',
    profile: { avatar_url: 'https://cdn.example.com/avatars/7.jpg', bio: 'AI/ML 工程师，专注大模型落地与部署。', tags: ['PyTorch', 'LLM', '机器学习', 'Python'] }
  },
  {
    _id: NumberLong(8), username: 'henry_dba',
    profile: { avatar_url: 'https://cdn.example.com/avatars/8.jpg', bio: 'DBA，MySQL/PostgreSQL 优化专家。', tags: ['MySQL', 'PostgreSQL', '数据库优化', '索引'] }
  },
  {
    _id: NumberLong(9), username: 'iris_sec',
    profile: { avatar_url: 'https://cdn.example.com/avatars/9.jpg', bio: '安全工程师，专注 Web 安全与渗透测试。', tags: ['安全', 'OAuth2', 'JWT', '渗透测试'] }
  },
  {
    _id: NumberLong(10), username: 'jack_mobile',
    profile: { avatar_url: 'https://cdn.example.com/avatars/10.jpg', bio: '移动端开发者，Flutter/React Native 双修。', tags: ['Flutter', 'React Native', 'iOS', 'Android'] }
  },
  {
    _id: NumberLong(11), username: 'kate_cloud',
    profile: { avatar_url: 'https://cdn.example.com/avatars/11.jpg', bio: '云架构师，AWS/阿里云认证专家。', tags: ['云计算', 'AWS', '阿里云', 'Serverless'] }
  },
  {
    _id: NumberLong(12), username: 'leo_devops',
    profile: { avatar_url: 'https://cdn.example.com/avatars/12.jpg', bio: 'DevOps 工程师，GitOps 实践者。', tags: ['DevOps', 'GitOps', 'ArgoCD', 'Terraform'] }
  }
]);

// ============================================================
// 2. article_content_cache 集合（对应 ArticleContentDocument.java）
//    _id 即 articleId，与 MySQL article.id 一致（BIGINT 自增）
//    author_id 与 MySQL user.id 一致
// ============================================================
db.article_content_cache.drop();
db.article_content_cache.insertMany([
  {
    _id: NumberLong(1), author_id: NumberLong(1),
    title: 'Spring Boot 3 新特性全解析',
    content: 'Spring Boot 3 基于 Spring Framework 6，要求 Java 17+。核心新特性包括：AOT（Ahead-of-Time）编译支持，可将启动时间缩短 60% 以上；原生镜像（GraalVM Native Image）支持，内存占用降低 80%；Jakarta EE 10 迁移，所有 javax.* 包替换为 jakarta.*；虚拟线程（Project Loom）集成，通过 spring.threads.virtual.enabled=true 开启。本文将逐一深入讲解这些特性的原理与实战用法。',
    content_type: 'markdown', oss_urls: ['https://oss.example.com/articles/1/banner.png'],
    tags: ['Spring Boot', 'Java', 'AOT', '云原生'],
    created_at: ISODate('2025-02-01T09:00:00Z'), updated_at: ISODate('2025-02-01T09:00:00Z')
  },
  {
    _id: NumberLong(2), author_id: NumberLong(1),
    title: 'MyBatis-Plus 最佳实践指南',
    content: 'MyBatis-Plus 3.5.7 在 MyBatis 基础上提供了大量开箱即用的功能。本文重点介绍：BaseMapper 的 CRUD 方法使用规范；LambdaQueryWrapper 与 LambdaUpdateWrapper 的类型安全写法；分页插件 PaginationInnerInterceptor 的配置与使用；逻辑删除 @TableLogic 的原理与注意事项；乐观锁 @Version 的使用场景；以及自动填充 MetaObjectHandler 的实现方式。',
    content_type: 'markdown', oss_urls: [],
    tags: ['MyBatis-Plus', 'Java', 'ORM', '数据库'],
    created_at: ISODate('2025-02-10T10:00:00Z'), updated_at: ISODate('2025-02-10T10:00:00Z')
  },
  {
    _id: NumberLong(3), author_id: NumberLong(1),
    title: 'Redis 缓存设计模式',
    content: '缓存是提升系统性能的核心手段。本文系统梳理六种 Redis 缓存设计模式：Cache-Aside（旁路缓存）适合读多写少场景；Write-Through（写穿）保证强一致性；Write-Behind（写回）提升写性能；Read-Through（读穿）简化业务代码；Refresh-Ahead（预刷新）避免缓存击穿；以及 Singleflight 模式防止缓存雪崩。每种模式均附有 Spring Boot + Redisson 的完整代码示例。',
    content_type: 'markdown', oss_urls: ['https://oss.example.com/articles/3/cache-patterns.png'],
    tags: ['Redis', '缓存', '设计模式', 'Redisson'],
    created_at: ISODate('2025-02-20T11:00:00Z'), updated_at: ISODate('2025-02-20T11:00:00Z')
  },
  {
    _id: NumberLong(4), author_id: NumberLong(1),
    title: 'Java 17 虚拟线程实战',
    content: 'Project Loom 的虚拟线程（Virtual Threads）在 Java 21 正式 GA，Java 17 可通过预览特性体验。虚拟线程是 JVM 管理的轻量级线程，创建成本极低（约 1KB 内存），可轻松创建百万级并发。本文介绍：虚拟线程 vs 平台线程的本质区别；如何在 Spring Boot 3 中启用虚拟线程；虚拟线程的适用场景（IO 密集型）与不适用场景（CPU 密集型）；以及与 Kotlin 协程的对比分析。',
    content_type: 'markdown', oss_urls: [],
    tags: ['Java', '虚拟线程', '并发', 'Project Loom'],
    created_at: ISODate('2025-03-01T09:00:00Z'), updated_at: ISODate('2025-03-01T09:00:00Z')
  },
  {
    _id: NumberLong(5), author_id: NumberLong(2),
    title: 'Docker Compose 微服务编排实践',
    content: 'Docker Compose V2 带来了大量改进。本文以 KiwiHub 项目为例，演示如何用 Compose 编排包含 MySQL、Redis、MongoDB、Nacos 的完整微服务环境。重点讲解：healthcheck 配置确保服务启动顺序；depends_on 的 condition 模式；网络隔离与服务发现；volumes 持久化策略；以及 .env 文件管理多环境配置。附完整的 docker-compose.yml 示例。',
    content_type: 'markdown', oss_urls: ['https://oss.example.com/articles/5/compose-arch.png'],
    tags: ['Docker', 'Docker Compose', '微服务', 'DevOps'],
    created_at: ISODate('2025-02-03T10:00:00Z'), updated_at: ISODate('2025-02-03T10:00:00Z')
  },
  {
    _id: NumberLong(6), author_id: NumberLong(2),
    title: 'Kubernetes 入门到生产',
    content: 'Kubernetes 是容器编排的事实标准。本文从零开始，覆盖 K8s 核心概念：Pod、Deployment、Service、Ingress 的关系与使用；ConfigMap 与 Secret 的最佳实践；HPA 水平自动扩缩容配置；PersistentVolume 存储管理；以及 Helm Chart 打包与发布。最后以 KiwiHub 微服务为例，演示完整的生产级部署方案，包括滚动更新、健康检查和资源限制配置。',
    content_type: 'markdown', oss_urls: ['https://oss.example.com/articles/6/k8s-arch.png'],
    tags: ['Kubernetes', 'K8s', '容器', 'DevOps'],
    created_at: ISODate('2025-02-15T11:00:00Z'), updated_at: ISODate('2025-02-15T11:00:00Z')
  },
  {
    _id: NumberLong(7), author_id: NumberLong(2),
    title: 'CI/CD 流水线搭建指南',
    content: '高效的 CI/CD 流水线是工程效率的基石。本文以 GitHub Actions + Docker + Kubernetes 为技术栈，搭建完整的自动化流水线：代码提交触发单元测试与代码质量扫描（SonarQube）；测试通过后自动构建 Docker 镜像并推送至镜像仓库；通过 ArgoCD 实现 GitOps 风格的自动部署；以及蓝绿部署与金丝雀发布策略的实现。',
    content_type: 'markdown', oss_urls: [],
    tags: ['CI/CD', 'GitHub Actions', 'ArgoCD', 'GitOps'],
    created_at: ISODate('2025-02-25T12:00:00Z'), updated_at: ISODate('2025-02-25T12:00:00Z')
  },
  {
    _id: NumberLong(8), author_id: NumberLong(3),
    title: '微服务架构设计原则',
    content: '微服务不是银弹，但合理运用能显著提升系统的可维护性与可扩展性。本文系统梳理微服务设计的核心原则：单一职责原则（SRP）指导服务边界划分；围绕业务能力组织服务（Conway 定律）；去中心化数据管理（每个服务独立数据库）；设计容错性（Circuit Breaker、Bulkhead 模式）；以及服务间通信的选型（同步 REST/gRPC vs 异步消息队列）。结合 KiwiHub 实际案例深入分析。',
    content_type: 'markdown', oss_urls: ['https://oss.example.com/articles/8/microservice-principles.png'],
    tags: ['微服务', '架构设计', '分布式', 'Spring Cloud'],
    created_at: ISODate('2025-02-05T09:00:00Z'), updated_at: ISODate('2025-02-05T09:00:00Z')
  },
  {
    _id: NumberLong(9), author_id: NumberLong(3),
    title: 'DDD 领域驱动设计实践',
    content: 'DDD（Domain-Driven Design）是应对复杂业务系统的有效方法论。本文聚焦战术设计层面：实体（Entity）与值对象（Value Object）的区别与建模；聚合根（Aggregate Root）的设计原则与边界划分；领域事件（Domain Event）的发布与订阅；仓储（Repository）模式的实现；以及应用服务（Application Service）与领域服务（Domain Service）的职责边界。以电商订单系统为例贯穿全文。',
    content_type: 'markdown', oss_urls: [],
    tags: ['DDD', '领域驱动设计', '架构', '设计模式'],
    created_at: ISODate('2025-02-12T10:00:00Z'), updated_at: ISODate('2025-02-12T10:00:00Z')
  },
  {
    _id: NumberLong(10), author_id: NumberLong(3),
    title: '分布式事务解决方案对比',
    content: '分布式事务是微服务架构中最棘手的问题之一。本文对比四种主流方案：2PC（两阶段提交）的原理与性能瓶颈；TCC（Try-Confirm-Cancel）的代码侵入性与适用场景；Saga 模式（编排式 vs 协同式）的最终一致性保证；以及基于消息队列的本地消息表方案。重点分析 Seata 框架的 AT 模式实现原理，以及在 Spring Boot 中的集成方式，并给出各方案的选型建议。',
    content_type: 'markdown', oss_urls: ['https://oss.example.com/articles/10/distributed-tx.png'],
    tags: ['分布式事务', 'Seata', 'Saga', '微服务'],
    created_at: ISODate('2025-02-18T11:00:00Z'), updated_at: ISODate('2025-02-18T11:00:00Z')
  },
  {
    _id: NumberLong(11), author_id: NumberLong(3),
    title: 'API 网关设计与实现',
    content: 'API 网关是微服务架构的统一入口。本文以 Spring Cloud Gateway 为例，深入讲解：路由配置（基于路径、Header、权重的路由规则）；过滤器链（GlobalFilter vs GatewayFilter）的执行顺序；限流（Redis + 令牌桶算法）的实现；熔断降级（Resilience4j 集成）；以及认证鉴权（JWT 校验 + 用户上下文传递）的完整实现方案。',
    content_type: 'markdown', oss_urls: [],
    tags: ['API网关', 'Spring Cloud Gateway', '限流', '熔断'],
    created_at: ISODate('2025-02-28T12:00:00Z'), updated_at: ISODate('2025-02-28T12:00:00Z')
  },
  {
    _id: NumberLong(12), author_id: NumberLong(3),
    title: 'gRPC vs REST 深度对比',
    content: 'gRPC 和 REST 是微服务间通信的两大主流方案。本文从多个维度深度对比：协议层面（HTTP/2 vs HTTP/1.1，二进制 vs 文本）；性能测试数据（序列化速度、传输效率、吞吐量）；开发体验（IDL 优先 vs API 优先，代码生成 vs 手写）；生态支持（负载均衡、服务发现、监控）；以及适用场景分析。最后给出在 Spring Boot 中集成 gRPC 的完整示例。',
    content_type: 'markdown', oss_urls: [],
    tags: ['gRPC', 'REST', '微服务通信', 'Protobuf'],
    created_at: ISODate('2025-03-05T09:00:00Z'), updated_at: ISODate('2025-03-05T09:00:00Z')
  },
  {
    _id: NumberLong(13), author_id: NumberLong(4),
    title: '高并发系统设计实战',
    content: '高并发系统设计是后端工程师的核心能力。本文以秒杀系统为例，系统讲解：流量漏斗模型（CDN → 网关 → 应用 → 数据库）；数据库连接池（HikariCP）的精细化调优；Redis 集群的读写分离与数据分片；消息队列（RocketMQ）的削峰填谷；以及分布式锁（Redisson）防止超卖的完整实现。附压测数据：单机 QPS 从 500 提升至 8000 的优化过程。',
    content_type: 'markdown', oss_urls: ['https://oss.example.com/articles/13/high-concurrency.png', 'https://oss.example.com/articles/13/benchmark.png'],
    tags: ['高并发', '系统设计', 'Redis', '秒杀'],
    created_at: ISODate('2025-02-08T10:00:00Z'), updated_at: ISODate('2025-02-08T10:00:00Z')
  },
  {
    _id: NumberLong(14), author_id: NumberLong(4),
    title: '消息队列选型与实践',
    content: '消息队列是构建异步、解耦系统的核心组件。本文对比 Kafka、RocketMQ、RabbitMQ 三大主流消息队列：吞吐量与延迟的性能对比；消息可靠性保证（持久化、ACK 机制、死信队列）；顺序消息与事务消息的实现原理；消费者组与消息广播模式；以及在 Spring Boot 中的集成方式。最后给出不同业务场景下的选型建议。',
    content_type: 'markdown', oss_urls: [],
    tags: ['消息队列', 'Kafka', 'RocketMQ', '异步'],
    created_at: ISODate('2025-02-22T11:00:00Z'), updated_at: ISODate('2025-02-22T11:00:00Z')
  },
  {
    _id: NumberLong(15), author_id: NumberLong(5),
    title: 'Vue 3 Composition API 深度解析',
    content: 'Vue 3 的 Composition API 彻底改变了组件逻辑的组织方式。本文深入讲解：ref 与 reactive 的响应式原理（Proxy vs defineProperty）；computed 与 watch/watchEffect 的使用场景与性能差异；自定义 Composable 的设计模式（逻辑复用最佳实践）；provide/inject 的依赖注入模式；以及 `<script setup>` 语法糖的编译原理。附完整的 TypeScript 类型推导示例。',
    content_type: 'markdown', oss_urls: [],
    tags: ['Vue3', 'Composition API', 'TypeScript', '前端'],
    created_at: ISODate('2025-02-06T09:00:00Z'), updated_at: ISODate('2025-02-06T09:00:00Z')
  },
  {
    _id: NumberLong(16), author_id: NumberLong(5),
    title: 'React 18 并发特性实战',
    content: 'React 18 引入了并发渲染（Concurrent Rendering）机制，这是 React 架构的重大升级。本文详解：Concurrent Mode 的工作原理（Fiber 架构与时间切片）；useTransition 与 useDeferredValue 的使用场景；Suspense 在数据获取中的应用；自动批处理（Automatic Batching）的性能提升；以及 startTransition API 优化用户交互体验的实战案例。',
    content_type: 'markdown', oss_urls: [],
    tags: ['React', 'React18', '并发渲染', '前端性能'],
    created_at: ISODate('2025-02-16T10:00:00Z'), updated_at: ISODate('2025-02-16T10:00:00Z')
  },
  {
    _id: NumberLong(17), author_id: NumberLong(5),
    title: '前端性能优化实战手册',
    content: '前端性能优化是提升用户体验的关键。本文系统梳理优化手段：资源加载优化（代码分割、懒加载、预加载、HTTP/2 多路复用）；渲染性能优化（虚拟列表、避免重排重绘、CSS 动画 vs JS 动画）；缓存策略（Service Worker、HTTP 缓存头、CDN 配置）；以及性能监控（Core Web Vitals、Lighthouse、Performance API）。每个优化点均附有量化的性能提升数据。',
    content_type: 'markdown', oss_urls: ['https://oss.example.com/articles/17/perf-metrics.png'],
    tags: ['前端性能', 'Web优化', 'Core Web Vitals', 'Lighthouse'],
    created_at: ISODate('2025-02-26T11:00:00Z'), updated_at: ISODate('2025-02-26T11:00:00Z')
  },
  {
    _id: NumberLong(18), author_id: NumberLong(6),
    title: 'Linux 内核调优实战',
    content: 'Linux 内核参数调优是提升服务器性能的底层手段。本文针对高并发 Web 服务场景，讲解关键内核参数：网络栈优化（tcp_max_syn_backlog、somaxconn、tcp_tw_reuse）；文件描述符限制（ulimit、/proc/sys/fs/file-max）；内存管理（vm.swappiness、透明大页 THP）；以及 CPU 亲和性（taskset、numactl）配置。附完整的 /etc/sysctl.conf 生产配置模板。',
    content_type: 'markdown', oss_urls: [],
    tags: ['Linux', '内核调优', '性能优化', 'SRE'],
    created_at: ISODate('2025-02-09T10:00:00Z'), updated_at: ISODate('2025-02-09T10:00:00Z')
  },
  {
    _id: NumberLong(19), author_id: NumberLong(7),
    title: 'PyTorch 模型部署最佳实践',
    content: '将 PyTorch 模型从研究环境部署到生产是 MLOps 的核心挑战。本文覆盖完整部署链路：模型导出（TorchScript vs ONNX 的选择）；推理优化（量化 INT8、剪枝、TensorRT 加速）；服务化（TorchServe vs FastAPI + Triton Inference Server）；容器化部署（GPU Docker 镜像构建）；以及监控（推理延迟、吞吐量、模型漂移检测）。以图像分类模型为例演示完整流程。',
    content_type: 'markdown', oss_urls: ['https://oss.example.com/articles/19/mlops-pipeline.png'],
    tags: ['PyTorch', 'MLOps', '模型部署', 'TensorRT'],
    created_at: ISODate('2025-02-11T09:00:00Z'), updated_at: ISODate('2025-02-11T09:00:00Z')
  },
  {
    _id: NumberLong(20), author_id: NumberLong(7),
    title: 'LLM 微调技术全解析',
    content: '大语言模型（LLM）微调是让通用模型适配特定业务场景的关键技术。本文系统讲解：全量微调（Full Fine-tuning）的资源需求与适用场景；参数高效微调（PEFT）方法对比（LoRA、QLoRA、Prefix Tuning、Prompt Tuning）；指令微调（Instruction Tuning）的数据集构建规范；RLHF（基于人类反馈的强化学习）的实现流程；以及使用 Hugging Face Transformers + PEFT 库的完整代码示例。',
    content_type: 'markdown', oss_urls: [],
    tags: ['LLM', '微调', 'LoRA', 'PEFT', '大模型'],
    created_at: ISODate('2025-02-21T10:00:00Z'), updated_at: ISODate('2025-02-21T10:00:00Z')
  },
  {
    _id: NumberLong(21), author_id: NumberLong(8),
    title: 'MySQL 索引优化深度解析',
    content: 'MySQL 索引是查询性能优化的核心。本文深入讲解：B+ 树索引的存储结构与查询原理；聚簇索引 vs 非聚簇索引的区别与回表代价；联合索引的最左前缀原则与索引下推（ICP）；覆盖索引消除回表的设计技巧；EXPLAIN 执行计划的完整解读（type、key、rows、Extra 字段含义）；以及慢查询日志分析与 pt-query-digest 工具使用。附 20 个高频索引优化案例。',
    content_type: 'markdown', oss_urls: ['https://oss.example.com/articles/21/btree-index.png'],
    tags: ['MySQL', '索引优化', 'EXPLAIN', '数据库性能'],
    created_at: ISODate('2025-02-13T10:00:00Z'), updated_at: ISODate('2025-02-13T10:00:00Z')
  },
  {
    _id: NumberLong(22), author_id: NumberLong(9),
    title: 'OAuth2 与 JWT 安全实践',
    content: 'OAuth2 和 JWT 是现代 Web 应用认证授权的基石。本文深入讲解：OAuth2 四种授权模式（授权码、隐式、密码、客户端凭证）的适用场景；JWT 的结构（Header.Payload.Signature）与签名验证原理；Access Token 与 Refresh Token 的生命周期管理；Token 撤销（黑名单机制 vs 短期 Token）的实现方案；以及在 Spring Security 6 中集成 OAuth2 Resource Server 的完整配置。',
    content_type: 'markdown', oss_urls: [],
    tags: ['OAuth2', 'JWT', 'Spring Security', '安全'],
    created_at: ISODate('2025-02-14T09:00:00Z'), updated_at: ISODate('2025-02-14T09:00:00Z')
  },
  {
    _id: NumberLong(23), author_id: NumberLong(9),
    title: 'SQL 注入防御完全指南',
    content: 'SQL 注入是 OWASP Top 10 中长期存在的高危漏洞。本文系统讲解：SQL 注入的攻击原理与分类（联合注入、盲注、时间盲注、报错注入）；预编译语句（PreparedStatement）的防御原理；MyBatis 中 #{} vs ${} 的安全差异（${}的危险场景）；ORM 框架的安全配置；WAF 规则的局限性；以及使用 sqlmap 进行安全测试的方法。附真实漏洞案例分析。',
    content_type: 'markdown', oss_urls: [],
    tags: ['SQL注入', 'Web安全', 'MyBatis', 'OWASP'],
    created_at: ISODate('2025-02-24T10:00:00Z'), updated_at: ISODate('2025-02-24T10:00:00Z')
  },
  {
    _id: NumberLong(24), author_id: NumberLong(10),
    title: 'Flutter 跨平台开发实战',
    content: 'Flutter 凭借自绘引擎实现了真正的跨平台一致性。本文以一个完整的社交 App 为例，讲解：Widget 树与 Element 树的渲染原理；状态管理方案对比（Provider vs Riverpod vs BLoC）；与原生平台通信（Platform Channel）的实现；网络请求与 JSON 序列化（Dio + json_serializable）；以及 Flutter 3.x 的多平台支持（iOS、Android、Web、Desktop）与性能优化技巧。',
    content_type: 'markdown', oss_urls: [],
    tags: ['Flutter', 'Dart', '跨平台', '移动开发'],
    created_at: ISODate('2025-02-17T09:00:00Z'), updated_at: ISODate('2025-02-17T09:00:00Z')
  },
  {
    _id: NumberLong(25), author_id: NumberLong(10),
    title: 'React Native 性能优化指南',
    content: 'React Native 的 JS Bridge 是性能瓶颈的根源，新架构（JSI + Fabric + TurboModules）正在解决这一问题。本文讲解：新旧架构的原理对比；FlatList vs FlashList 的性能差异（实测数据）；避免不必要渲染（React.memo、useMemo、useCallback 的正确使用）；Hermes 引擎的启动优化；以及使用 Flipper + Perf Monitor 进行性能分析的完整流程。',
    content_type: 'markdown', oss_urls: [],
    tags: ['React Native', '性能优化', 'JSI', '移动开发'],
    created_at: ISODate('2025-02-27T10:00:00Z'), updated_at: ISODate('2025-02-27T10:00:00Z')
  },
  {
    _id: NumberLong(26), author_id: NumberLong(10),
    title: 'iOS 与 Android 差异化适配',
    content: '跨平台开发中，iOS 和 Android 的平台差异是不可回避的挑战。本文系统梳理：UI 设计规范差异（Material Design vs Human Interface Guidelines）；安全区域（Safe Area）与刘海屏适配；权限申请流程的差异化处理；推送通知（APNs vs FCM）的统一封装；以及文件系统、相机、蓝牙等原生能力的差异化适配方案。以 Flutter 和 React Native 双框架视角对比讲解。',
    content_type: 'markdown', oss_urls: [],
    tags: ['iOS', 'Android', '跨平台', '适配'],
    created_at: ISODate('2025-03-03T11:00:00Z'), updated_at: ISODate('2025-03-03T11:00:00Z')
  }
]);

// ============================================================
// 3. comments 集合（对应 Comment.java）
//    article_id/author_id 与 MySQL 保持一致（Long）
//    _id 使用 ObjectId（MongoDB 自身主键，与 MySQL comment.id 雪花算法无关）
// ============================================================
db.comments.drop();
db.comments.insertMany([
  { _id: ObjectId(), article_id: NumberLong(1),  author_id: NumberLong(2), content: 'Spring Boot 3 的 AOT 编译确实提升了不少启动速度，实测快了 3 倍！', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-02T10:30:00Z') },
  { _id: ObjectId(), article_id: NumberLong(1),  author_id: NumberLong(3), content: '请问虚拟线程和协程有什么本质区别？', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-03T11:00:00Z') },
  { _id: ObjectId(), article_id: NumberLong(1),  author_id: NumberLong(1), content: '@carol_writer 虚拟线程是 JVM 层面的，协程是语言层面的，本质上都是轻量级并发。', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-03T11:30:00Z') },
  { _id: ObjectId(), article_id: NumberLong(1),  author_id: NumberLong(4), content: '@alice_dev 感谢解答，那 Kotlin 协程和虚拟线程可以混用吗？', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-03T12:00:00Z') },
  { _id: ObjectId(), article_id: NumberLong(8),  author_id: NumberLong(1), content: '微服务拆分粒度这个问题困扰我很久了，文章讲得很清楚！', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-06T09:30:00Z') },
  { _id: ObjectId(), article_id: NumberLong(8),  author_id: NumberLong(4), content: '建议补充一下服务网格（Service Mesh）的内容。', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-07T10:00:00Z') },
  { _id: ObjectId(), article_id: NumberLong(8),  author_id: NumberLong(3), content: '@dave_arch 好的，下一篇会专门写 Istio 实践。', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-07T10:30:00Z') },
  { _id: ObjectId(), article_id: NumberLong(8),  author_id: NumberLong(5), content: '@carol_writer 期待！Istio 的学习曲线太陡了。', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-07T11:00:00Z') },
  { _id: ObjectId(), article_id: NumberLong(13), author_id: NumberLong(1), content: '高并发下的数据库连接池配置这块讲得非常实用！', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-09T09:30:00Z') },
  { _id: ObjectId(), article_id: NumberLong(13), author_id: NumberLong(3), content: '请问 HikariCP 和 Druid 在高并发场景下哪个更好？', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-10T10:00:00Z') },
  { _id: ObjectId(), article_id: NumberLong(13), author_id: NumberLong(4), content: '@carol_writer HikariCP 性能更好，Druid 监控功能更强，看需求选择。', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-10T10:30:00Z') },
  { _id: ObjectId(), article_id: NumberLong(13), author_id: NumberLong(2), content: '@dave_arch 我们生产用的 HikariCP，确实稳定。', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-10T11:00:00Z') },
  { _id: ObjectId(), article_id: NumberLong(21), author_id: NumberLong(1), content: 'EXPLAIN 分析这块讲得很详细，收藏了！', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-14T09:30:00Z') },
  { _id: ObjectId(), article_id: NumberLong(21), author_id: NumberLong(3), content: '联合索引的最左前缀原则这里有个小错误，应该是...', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-15T10:00:00Z') },
  { _id: ObjectId(), article_id: NumberLong(21), author_id: NumberLong(8), content: '@carol_writer 感谢指正，已更新文章！', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-15T10:30:00Z') },
  { _id: ObjectId(), article_id: NumberLong(21), author_id: NumberLong(5), content: '@henry_dba 这种互动很棒，技术社区就该这样。', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-15T11:00:00Z') },
  { _id: ObjectId(), article_id: NumberLong(10), author_id: NumberLong(1), content: 'Seata 的 AT 模式在实际项目中踩了不少坑，文章总结得很好。', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-19T09:30:00Z') },
  { _id: ObjectId(), article_id: NumberLong(10), author_id: NumberLong(2), content: 'TCC 模式的代码侵入性太强了，有没有更优雅的方案？', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-20T10:00:00Z') },
  { _id: ObjectId(), article_id: NumberLong(10), author_id: NumberLong(3), content: '@bob_coder 可以考虑 Saga 模式，配合消息队列实现最终一致性。', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-20T10:30:00Z') },
  { _id: ObjectId(), article_id: NumberLong(10), author_id: NumberLong(7), content: '@carol_writer Saga 模式的补偿事务设计是个难点，期待专题文章。', parent_id: null, root_id: null, status: 0, created_at: ISODate('2025-02-20T11:00:00Z') }
]);

// ============================================================
// 4. article_likes 集合（对应 ArticleLike.java）
//    user_id/article_id/author_id 均与 MySQL article_like 表一致（Long）
// ============================================================
db.article_likes.drop();
db.article_likes.insertMany([
  { _id: ObjectId(), user_id: NumberLong(2),  article_id: NumberLong(1),  author_id: NumberLong(1), create_time: ISODate('2025-02-02T10:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(3),  article_id: NumberLong(1),  author_id: NumberLong(1), create_time: ISODate('2025-02-03T11:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(4),  article_id: NumberLong(1),  author_id: NumberLong(1), create_time: ISODate('2025-02-04T12:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(5),  article_id: NumberLong(1),  author_id: NumberLong(1), create_time: ISODate('2025-02-05T13:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(1),  article_id: NumberLong(8),  author_id: NumberLong(3), create_time: ISODate('2025-02-06T09:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(2),  article_id: NumberLong(8),  author_id: NumberLong(3), create_time: ISODate('2025-02-07T10:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(4),  article_id: NumberLong(8),  author_id: NumberLong(3), create_time: ISODate('2025-02-08T11:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(1),  article_id: NumberLong(13), author_id: NumberLong(4), create_time: ISODate('2025-02-09T09:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(3),  article_id: NumberLong(13), author_id: NumberLong(4), create_time: ISODate('2025-02-10T10:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(5),  article_id: NumberLong(13), author_id: NumberLong(4), create_time: ISODate('2025-02-11T11:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(1),  article_id: NumberLong(21), author_id: NumberLong(8), create_time: ISODate('2025-02-14T09:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(2),  article_id: NumberLong(21), author_id: NumberLong(8), create_time: ISODate('2025-02-15T10:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(3),  article_id: NumberLong(21), author_id: NumberLong(8), create_time: ISODate('2025-02-16T11:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(1),  article_id: NumberLong(10), author_id: NumberLong(3), create_time: ISODate('2025-02-19T09:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(2),  article_id: NumberLong(10), author_id: NumberLong(3), create_time: ISODate('2025-02-20T10:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(6),  article_id: NumberLong(19), author_id: NumberLong(7), create_time: ISODate('2025-02-12T10:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(7),  article_id: NumberLong(2),  author_id: NumberLong(1), create_time: ISODate('2025-02-11T09:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(8),  article_id: NumberLong(3),  author_id: NumberLong(1), create_time: ISODate('2025-02-21T10:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(9),  article_id: NumberLong(5),  author_id: NumberLong(2), create_time: ISODate('2025-02-04T11:00:00Z') },
  { _id: ObjectId(), user_id: NumberLong(10), article_id: NumberLong(6),  author_id: NumberLong(2), create_time: ISODate('2025-02-16T12:00:00Z') }
]);

// ============================================================
// 5. user_relations 集合（对应 UserRelation.java）
//    follower_id/following_id 与 MySQL user_relation 表一致（Long）
// ============================================================
db.user_relations.drop();
db.user_relations.insertMany([
  { _id: ObjectId(), follower_id: NumberLong(2),  following_id: NumberLong(1),  created_at: ISODate('2025-01-15T10:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(3),  following_id: NumberLong(1),  created_at: ISODate('2025-01-16T11:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(4),  following_id: NumberLong(1),  created_at: ISODate('2025-01-17T12:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(5),  following_id: NumberLong(1),  created_at: ISODate('2025-01-18T13:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(6),  following_id: NumberLong(1),  created_at: ISODate('2025-01-19T14:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(1),  following_id: NumberLong(3),  created_at: ISODate('2025-01-20T09:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(1),  following_id: NumberLong(5),  created_at: ISODate('2025-01-21T10:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(1),  following_id: NumberLong(7),  created_at: ISODate('2025-01-22T11:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(2),  following_id: NumberLong(3),  created_at: ISODate('2025-01-23T12:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(2),  following_id: NumberLong(5),  created_at: ISODate('2025-01-24T13:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(2),  following_id: NumberLong(7),  created_at: ISODate('2025-01-25T14:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(2),  following_id: NumberLong(9),  created_at: ISODate('2025-01-26T15:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(3),  following_id: NumberLong(2),  created_at: ISODate('2025-01-27T09:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(7),  following_id: NumberLong(3),  created_at: ISODate('2025-02-01T10:00:00Z') },
  { _id: ObjectId(), follower_id: NumberLong(10), following_id: NumberLong(3),  created_at: ISODate('2025-02-02T11:00:00Z') }
]);

// ============================================================
// 创建索引（与 Java 实体类注解保持一致）
// ============================================================
db.users.createIndex({ username: 1 }, { unique: true, name: 'idx_username_unique' });
db.article_content_cache.createIndex({ author_id: 1 }, { name: 'idx_author_id' });
db.article_content_cache.createIndex(
  { title: 'text', content: 'text', tags: 'text' },
  { weights: { title: 10, content: 3, tags: 5 }, name: 'ArticleContentDocument_TextIndex' }
);
db.comments.createIndex({ article_id: 1, author_id: 1 }, { name: 'article_id_author_id_index' });
db.comments.createIndex({ root_id: 1 }, { name: 'idx_root_id' });
db.article_likes.createIndex({ user_id: 1, article_id: 1 }, { unique: true, name: 'idx_user_article_unique' });
db.article_likes.createIndex({ user_id: 1, create_time: -1 }, { name: 'idx_user_time' });
db.user_relations.createIndex({ follower_id: 1, following_id: 1 }, { unique: true, name: 'unique_relation' });
db.user_relations.createIndex({ follower_id: 1, created_at: -1 }, { name: 'idx_follower_time' });
db.user_relations.createIndex({ following_id: 1, created_at: -1 }, { name: 'idx_following_time' });

print('MongoDB 种子数据导入完成');
print('users: '                  + db.users.countDocuments());
print('article_content_cache: '  + db.article_content_cache.countDocuments());
print('comments: '               + db.comments.countDocuments());
print('article_likes: '          + db.article_likes.countDocuments());
print('user_relations: '         + db.user_relations.countDocuments());
