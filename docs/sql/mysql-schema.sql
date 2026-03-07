-- KiwiHub MySQL Schema
-- Generated for MyBatis-Plus entities
-- Version: 1.0
-- Date: 2026-03-06

-- Database: kiwi_hub
CREATE DATABASE IF NOT EXISTS `kiwi_hub` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `kiwi_hub`;

-- Table: user
CREATE TABLE IF NOT EXISTS `user` (
    `id` VARCHAR(32) NOT NULL COMMENT '用户ID（雪花算法）',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- Table: user_stats
CREATE TABLE IF NOT EXISTS `user_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `article_count` INT NOT NULL DEFAULT 0 COMMENT '文章数',
    `following_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
    `follower_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    CONSTRAINT `fk_user_stats_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';

-- Table: user_relation
CREATE TABLE IF NOT EXISTS `user_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `follower_id` VARCHAR(32) NOT NULL COMMENT '关注者ID',
    `following_id` VARCHAR(32) NOT NULL COMMENT '被关注者ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
    CONSTRAINT `fk_user_relation_follower` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_relation_following` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    INDEX `idx_follower_id` (`follower_id`),
    INDEX `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';

-- Table: article
CREATE TABLE IF NOT EXISTS `article` (
    `id` VARCHAR(32) NOT NULL COMMENT '文章ID（雪花算法）',
    `author_id` VARCHAR(32) NOT NULL COMMENT '作者ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
    `summary` VARCHAR(500) NULL COMMENT '文章摘要',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '文章状态：0-草稿，1-已发布，2-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_article_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- Table: article_stats
CREATE TABLE IF NOT EXISTS `article_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `article_id` VARCHAR(32) NOT NULL COMMENT '文章ID',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_id` (`article_id`),
    CONSTRAINT `fk_article_stats_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
    INDEX `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章统计表';

-- Table: comment
CREATE TABLE IF NOT EXISTS `comment` (
    `id` VARCHAR(32) NOT NULL COMMENT '评论ID（雪花算法）',
    `article_id` VARCHAR(32) NOT NULL COMMENT '文章ID',
    `author_id` VARCHAR(32) NOT NULL COMMENT '评论者ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` VARCHAR(32) NULL COMMENT '父评论ID（为空表示根评论）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '评论状态：0-删除，1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_comment_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE,
    INDEX `idx_article_id` (`article_id`),
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- Table: article_like
CREATE TABLE IF NOT EXISTS `article_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `article_id` VARCHAR(32) NOT NULL COMMENT '文章ID',
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
    CONSTRAINT `fk_article_like_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_article_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    INDEX `idx_article_id` (`article_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';

-- Initial data (optional)
-- INSERT INTO `user` (`id`, `username`, `email`, `password_hash`) VALUES (...);

-- Notes:
-- 1. All IDs using snowflake algorithm should be stored as VARCHAR(32)
-- 2. Timestamps are managed by MySQL default values
-- 3. Foreign keys enforce referential integrity
-- 4. Indexes are created for common query patterns