# 第一阶段：构建/提取层 (Builder Stage)
# 使用 JRE 而不是 JDK，且使用 Alpine 版本，体积最小 (约 50MB-60MB)
FROM eclipse-temurin:17-jre-alpine AS builder

# 设置工作目录
WORKDIR /application

# 接收构建参数，指向你的 jar 包位置
ARG JAR_FILE=*.jar

# 将 jar 包复制进去并重命名
COPY ${JAR_FILE} application.jar

# 利用 Spring Boot 的 layertools 提取分层
# 这会将 jar 包拆解为依赖、Spring加载器、快照依赖、业务代码四部分
RUN java -Djarmode=layertools -jar application.jar extract

# ----------------------------------------------------------------

# 第二阶段：运行层 (Runtime Stage)
# 再次使用最小的基础镜像，丢弃第一阶段的构建工具，只保留提取后的文件
FROM eclipse-temurin:17-jre-alpine

WORKDIR /application

# 按照分层顺序复制文件 (顺序很重要！变动最少的放在最前面)
# 1. dependencies: 第三方依赖 (最不常变，Docker 会缓存这层)
COPY --from=builder /application/dependencies/ ./
# 2. spring-boot-loader: Spring 启动加载器
COPY --from=builder /application/spring-boot-loader/ ./
# 3. snapshot-dependencies: 内部快照依赖 (比如 common 模块)
COPY --from=builder /application/snapshot-dependencies/ ./
# 4. application: 业务代码 (最常变，只有这层会重新构建)
COPY --from=builder /application/application/ ./

# 设置时区为上海 (解决日志时间不对的问题)
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone

# 暴露端口 (仅作声明，实际要看 docker-compose)
EXPOSE 8010

# 核心启动命令
# 使用 Spring 的 JarLauncher 启动，而不是 java -jar
# 这样能自动加载分层文件
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]