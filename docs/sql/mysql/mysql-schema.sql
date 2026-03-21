-- KiwiHub MySQL Schema
-- 以 Java 实体类字段为准，无外键约束
-- Version: 1.2
-- Date: 2026-03-21

-- Database: kiwi_hub
CREATE DATABASE IF NOT EXISTS `kiwi_hub` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `kiwi_hub`;

-- Table: user
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID（自增主键）',
    `username`      VARCHAR(50)  NOT NULL COMMENT '用户名',
    `email`         VARCHAR(100) NOT NULL COMMENT '邮箱',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt）',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- Table: user_stats
CREATE TABLE IF NOT EXISTS `user_stats` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `user_id`         BIGINT   NOT NULL COMMENT '用户ID',
    `article_count`   INT      NOT NULL DEFAULT 0 COMMENT '文章数',
    `following_count` INT      NOT NULL DEFAULT 0 COMMENT '关注数',
    `follower_count`  INT      NOT NULL DEFAULT 0 COMMENT '粉丝数',
    `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';

-- Table: user_relation
CREATE TABLE IF NOT EXISTS `user_relation` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `follower_id`  BIGINT   NOT NULL COMMENT '关注者ID',
    `following_id` BIGINT   NOT NULL COMMENT '被关注者ID',
    `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
    INDEX `idx_follower_id` (`follower_id`),
    INDEX `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';

-- Table: article
-- 注意：summary/status 字段已移除（不在实体类中）；文章内容存储于 MongoDB
CREATE TABLE IF NOT EXISTS `article` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文章ID（自增主键）',
    `author_id`  BIGINT       NOT NULL COMMENT '作者ID',
    `title`      VARCHAR(200) NOT NULL COMMENT '文章标题',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- Table: article_stats
CREATE TABLE IF NOT EXISTS `article_stats` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `article_id`    BIGINT   NOT NULL COMMENT '文章ID',
    `view_count`    INT      NOT NULL DEFAULT 0 COMMENT '浏览量',
    `like_count`    INT      NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT      NOT NULL DEFAULT 0 COMMENT '评论数',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_id` (`article_id`),
    INDEX `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章统计表';

-- Table: article_like
-- author_id 为冗余字段，方便快速统计作者维度数据
CREATE TABLE IF NOT EXISTS `article_like` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `user_id`    BIGINT   NOT NULL COMMENT '点赞用户ID',
    `article_id` BIGINT   NOT NULL COMMENT '文章ID',
    `author_id`  BIGINT   NOT NULL COMMENT '文章作者ID（冗余字段）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    INDEX `idx_article_id` (`article_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';

-- Table: comment
-- root_id 用于快速定位评论串根节点；status: 0-正常，1-已删除；主键使用雪花算法保证有序插入
CREATE TABLE IF NOT EXISTS `comment` (
    `id`         BIGINT   NOT NULL COMMENT '评论ID（雪花算法，时间有序）',
    `article_id` BIGINT   NOT NULL COMMENT '文章ID',
    `author_id`  BIGINT   NOT NULL COMMENT '评论者ID',
    `content`    TEXT     NOT NULL COMMENT '评论内容',
    `parent_id`  BIGINT   NULL     COMMENT '父评论ID（为空表示根评论）',
    `root_id`    BIGINT   NULL     COMMENT '根评论ID（快速定位评论串根节点）',
    `status`     TINYINT  NOT NULL DEFAULT 0 COMMENT '评论状态：0-正常，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    INDEX `idx_article_id` (`article_id`),
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_root_id` (`root_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
