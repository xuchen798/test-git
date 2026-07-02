-- =============================================
-- 博客系统数据库初始化脚本
-- Flyway 版本迁移 V1
-- 字符集: utf8mb4
-- =============================================

-- 创建数据库 (如果不存在)
CREATE DATABASE IF NOT EXISTS blog_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE blog_system;

-- =============================================
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `signature` varchar(255) DEFAULT NULL COMMENT '个人签名',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1正常 0禁用',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色 0普通用户 1管理员',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 分类表
-- =============================================
CREATE TABLE IF NOT EXISTS `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- =============================================
-- 文章表
-- =============================================
CREATE TABLE IF NOT EXISTS `article` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '作者ID',
  `title` varchar(200) NOT NULL COMMENT '文章标题',
  `content` longtext NOT NULL COMMENT '文章内容',
  `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要',
  `category_ids` varchar(255) DEFAULT NULL COMMENT '分类ID，逗号分隔',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签，逗号分隔',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '阅读数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `favorite_count` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1已发布 0草稿',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_view_count` (`view_count`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- =============================================
-- 评论表
-- =============================================
CREATE TABLE IF NOT EXISTS `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父评论ID，0表示顶级评论',
  `reply_user_id` bigint DEFAULT NULL COMMENT '被回复用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- =============================================
-- 文章点赞表
-- =============================================
CREATE TABLE IF NOT EXISTS `article_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞表';

-- =============================================
-- 收藏表
-- =============================================
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- =============================================
-- 搜索历史表
-- =============================================
CREATE TABLE IF NOT EXISTS `search_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `keyword` varchar(100) NOT NULL COMMENT '搜索关键词',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史表';

-- =============================================
-- 系统日志表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `operation` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
  `params` text COMMENT '请求参数',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1成功 0失败',
  `time` bigint DEFAULT '0' COMMENT '耗时ms',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- =============================================
-- Flyway 版本记录表 (由 Flyway 自动创建)
-- =============================================

-- =============================================
-- 初始化数据
-- =============================================

-- 管理员账户: admin / 123456
-- BCrypt加密后的密码: $2a$10$wcvibaCd0Ise5j2CZQ2uY.JfNQAokUTbZhMamEH8ket6S9dplBWrS
INSERT INTO `user` (`username`, `password`, `email`, `signature`, `role`) VALUES
('admin', '$2a$10$wcvibaCd0Ise5j2CZQ2uY.JfNQAokUTbZhMamEH8ket6S9dplBWrS', 'admin@blog.com', '博客管理员', 1);

-- 测试用户: test / 123456
INSERT INTO `user` (`username`, `password`, `email`, `signature`, `role`) VALUES
('test', '$2a$10$wcvibaCd0Ise5j2CZQ2uY.JfNQAokUTbZhMamEH8ket6S9dplBWrS', 'test@blog.com', '爱学习的测试用户', 0);

-- 分类数据
INSERT INTO `category` (`name`, `sort`) VALUES
('Java', 1),
('Spring Boot', 2),
('Vue', 3),
('MySQL', 4),
('Redis', 5),
('前端开发', 6),
('后端开发', 7),
('算法', 8);

-- 测试文章数据
INSERT INTO `article` (`user_id`, `title`, `content`, `summary`, `category_ids`, `tags`, `view_count`, `like_count`, `status`) VALUES
(2, 'Spring Boot 入门指南', '# Spring Boot 入门\n\nSpring Boot是由Pivotal团队提供的全新框架，其设计目的是用来简化新Spring应用的初始搭建以及开发过程。\n\n## 特性\n\n1. 快速开发\n2. 自动配置\n3. 独立运行\n4. 无代码生成和XML配置', 'Spring Boot入门指南，介绍快速开发Spring应用的方法', '1,2', 'Spring Boot,Java,框架', 120, 15, 1),
(2, 'Vue.js 基础教程', '# Vue.js 基础\n\nVue.js是一套用于构建用户界面的渐进式JavaScript框架。\n\n## 核心特性\n\n- 响应式数据绑定\n- 组件化开发\n- 虚拟DOM', 'Vue.js基础教程，学习响应式数据绑定和组件化开发', '3,6', 'Vue.js,前端,JavaScript', 256, 32, 1),
(2, 'MySQL 性能优化实战', '# MySQL性能优化\n\nMySQL是最流行的关系型数据库之一，优化SQL查询性能至关重要。\n\n## 优化方向\n\n1. 索引优化\n2. SQL语句优化\n3. 配置优化', 'MySQL性能优化实战，从索引到SQL全面提升查询性能', '4', 'MySQL,数据库,优化', 189, 28, 1),
(2, 'Redis 缓存策略详解', '# Redis缓存策略\n\nRedis是一个开源的内存数据结构存储系统，常用作缓存和消息中间件。\n\n## 常见问题\n\n- 缓存穿透\n- 缓存击穿\n- 缓存雪崩', 'Redis缓存策略详解，防止穿透、击穿和雪崩', '5', 'Redis,缓存,性能', 312, 45, 1),
(1, '博客系统架构设计', '# 博客系统架构\n\n本文介绍一个完整博客系统的架构设计思路。\n\n## 技术栈\n\n- 前端: Vue + ElementUI\n- 后端: Spring Boot + MyBatis-Plus\n- 数据库: MySQL + Redis', '博客系统架构设计，前后端分离全栈项目', '2,7', '架构,博客,全栈', 88, 12, 1);
