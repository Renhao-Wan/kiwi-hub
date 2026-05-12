# Kiwi-Hub 容器化部署指南 (基于 Nginx 分支)

本文档详细介绍了如何在资源受限的环境（如 2vCPU/2GB 内存服务器）下，通过 **Docker + Nginx + Nacos** 部署 Kiwi-Hub 微服务系统。

## 1. 架构说明

### 1.1 架构设计
本项目分为 `main` 和 `nginx` 两个分支，本指南适用于 **nginx 分支**。
*   **Main 分支**：使用 Spring Cloud Gateway，适合标准服务器配置。
*   **Nginx 分支**：移除 Java 网关，改用 **Nginx** 作为反向代理和统一入口。在低配服务器上，该方案可节省约 300MB-500MB 的内存资源。

### 1.2 服务清单
| 服务名称 | 说明 |
| :--- | :--- |
| **kiwi-user** | 用户认证、信息管理 |
| **kiwi-content** | 帖子、评论、点赞互动 |
| **kiwi-link** | 短链接生成与解析 |
| **Nacos**  | 注册中心与配置中心 (Standalone) |
| **Nginx**  | 网关入口、文档聚合 |

### 1.3 基础设施要求
*   **操作系统**：CentOS 7+, Ubuntu 20.04+ (本文以 CentOS 为例)
*   **容器环境**：Docker Engine 20+, Docker Compose v2+
*   **外部依赖 (SaaS推荐)**：
    *   Redis (推荐 Upstash)
    *   MongoDB (推荐 MongoDB Atlas)
    *   RabbitMQ (推荐 CloudAMQP)
    *   *注：为降低本地负载，建议使用云端托管的中间件免费实例。*

---

## 2. 服务器环境初始化

在 2GB 内存的服务器上运行 Java 微服务集群，**必须**配置虚拟内存（Swap）及内核参数。

### 2.1 安装 Docker 环境
```bash
# 安装 Docker
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io
sudo systemctl start docker && sudo systemctl enable docker

# 安装 Docker Compose
curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```

### 2.2 配置虚拟内存 (Swap)
建议创建至少 2GB 的 Swap 空间以防止 OOM。

```bash
# 创建并启用 Swap
dd if=/dev/zero of=/swapfile bs=1M count=2048
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile

# 持久化配置
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 调整内核参数 (积极使用 Swap)
sysctl vm.swappiness=100
echo 'vm.swappiness=100' >> /etc/sysctl.conf
```

---

## 3. 项目目录与配置

### 3.1 目录结构规划
建议在服务器 `/opt/kiwihub` 目录下组织文件：

```text
/opt/kiwihub/
├── docker-compose.yml       # 容器编排文件
├── logs/                    # 业务日志挂载点
├── services/                # 微服务构建目录
│   ├── user/    (含 Dockerfile, kiwi-user.jar)
│   ├── content/ (含 Dockerfile, kiwi-content.jar)
│   └── link/    (含 Dockerfile, kiwi-link.jar)
├── nginx/
│   ├── conf.d/              # Nginx 配置文件
│   ├── logs/
│   └── html/                # 接口文档导航页
└── data/
    └── nacos/               # Nacos 数据持久化
```

### 3.2 镜像构建 (Dockerfile)
本项目采用 Spring Boot 分层构建 (Layered Jar) 方案，配合 Alpine 基础镜像以最小化体积。请在各服务目录下使用以下 Dockerfile：

```dockerfile
# Stage 1: Builder
FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /application
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /application
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

### 3.3 Nginx 网关配置
创建配置文件 `/opt/kiwihub/nginx/conf.d/kiwi.conf`。该配置实现了流量转发及 Knife4j 文档的聚合。

```nginx
server {
    listen 80;
    server_name localhost; # 请修改为实际域名或IP

    # 前端导航页
    location / {
        root   /usr/share/nginx/html;
        index  index.html;
        try_files $uri $uri/ /index.html;
    }

    # Swagger/Knife4j 静态资源路由分发
    location ~ ^/(webjars|v3|swagger-resources) {
        if ($http_referer ~* "/users/") { proxy_pass http://kiwi-user:8070; }
        if ($http_referer ~* "/links/") { proxy_pass http://kiwi-link:8030; }
        proxy_pass http://kiwi-content:8010;
    }

    # 业务接口转发
    location /users/ {
        rewrite ^/users/(.*)$ /$1 break;
        proxy_pass http://kiwi-user:8070;
        proxy_set_header Host $host;
    }
    location /links/ {
        rewrite ^/links/(.*)$ /$1 break;
        proxy_pass http://kiwi-link:8030;
        proxy_set_header Host $host;
    }
    location /content/ {
        rewrite ^/content/(.*)$ /$1 break;
        proxy_pass http://kiwi-content:8010;
        proxy_set_header Host $host;
    }
}
```

---

## 4. 启动与编排

### 4.1 Docker Compose 配置
在 `/opt/kiwihub/docker-compose.yml` 中定义服务。注意内存限制 (`deploy.resources.limits`) 是生产环境稳定的关键。

```yaml
version: '3.8'

networks:
  kiwi-net:
    driver: bridge

services:
  # --- Nacos ---
  nacos:
    image: nacos/nacos-server:v2.4.1
    container_name: nacos-standalone
    restart: always
    environment:
      - MODE=standalone
      - NACOS_AUTH_ENABLE=true
      - NACOS_CORE_AUTH_SERVER_IDENTITY_KEY=kiwi_server_id
      - NACOS_CORE_AUTH_SERVER_IDENTITY_VALUE=kiwi_server_val
      - NACOS_CORE_AUTH_PLUGIN_NACOS_TOKEN_SECRET_KEY=VGhpc0lzQVNlY3JldEtleUZvck5hY29zVG9rZW5WYWxpZGF0aW9u
      - JVM_XMS=200m
      - JVM_XMX=200m
    ports:
      - "8848:8848"
      - "9848:9848"
    volumes:
      - ./nacos/logs:/home/nacos/logs
      - /opt/kiwihub/data/nacos:/home/nacos/data
    deploy:
      resources:
        limits:
          memory: 512M
    networks:
      - kiwi-net

  # --- Nginx ---
  nginx:
    image: nginx:alpine
    container_name: kiwi-nginx
    restart: always
    ports:
      - "80:80"
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d
      - ./nginx/html:/usr/share/nginx/html
      - ./nginx/logs:/var/log/nginx
    deploy:
      resources:
        limits:
          memory: 128M
    networks:
      - kiwi-net

  # --- User Service (其他服务类似) ---
  kiwi-user:
    image: kiwi-user:v1
    container_name: kiwi-user
    restart: always
    environment:
      - NACOS_SERVER_ADDR=nacos:8848
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=nacos:8848
      # Redis 配置 (示例)
      - SPRING_DATA_REDIS_URL=rediss://default:pwd@host:port
      - JVM_TOOL_OPTIONS=-Xms128m -Xmx256m
      - TZ=Asia/Shanghai
    deploy:
      resources:
        limits:
          memory: 350M
    depends_on:
      nacos:
        condition: service_healthy
    networks:
      - kiwi-net
    # Content 和 Link 服务配置请参考上述模板进行复制修改
```

### 4.2 部署步骤

1.  **准备配置**：启动 Nacos (`docker-compose up -d nacos`)，登录控制台导入各微服务的 `dev.yaml` 配置文件，并确保 MongoDB/RabbitMQ/Redis 连接地址正确。
2.  **构建镜像**：
    ```bash
    cd /opt/kiwihub/services/user && docker build -t kiwi-user:v1 .
    # 对 content 和 link 服务执行相同操作
    ```
3.  **启动集群**：
    ```bash
    cd /opt/kiwihub && docker-compose up -d
    ```
4.  **验证状态**：
    ```bash
    docker-compose ps
    ```
    确保所有容器状态为 `Up` 且无退出代码 (Exit)。

---

## 5. 验证与文档

部署成功后，访问服务器 IP 或域名。

1.  **API 导航**：首页将展示微服务文档入口（需在 `nginx/html` 下放置索引页）。
2.  **接口调试**：点击各服务链接，可直接进入 Knife4j 调试界面。Nginx 将自动根据 Referer 头处理静态资源请求，无需跨域配置。

### 常见问题排查
*   **容器频繁退出 (Exit 137)**：通常为 OOM (Out Of Memory)。请检查 Swap 是否启用，或尝试在 `docker-compose.yml` 中适当调低 JVM 参数。
*   **Nacos 连接失败**：确认 `NACOS_SERVER_ADDR` 环境变量正确，且 9848 端口在容器网络中可达。

---