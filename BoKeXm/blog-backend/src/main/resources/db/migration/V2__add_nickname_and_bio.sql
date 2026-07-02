-- =============================================
-- V2__add_nickname_and_bio.sql
-- 为用户表添加昵称和个人简介字段
-- =============================================

ALTER TABLE `user`
    ADD COLUMN `nickname` VARCHAR(64) NULL DEFAULT NULL COMMENT '昵称' AFTER `username`,
    ADD COLUMN `bio` VARCHAR(500) NULL DEFAULT NULL COMMENT '个人简介' AFTER `signature`;

-- 将现有 signature 数据同步到 bio（保持旧数据不丢失）
UPDATE `user` SET `bio` = `signature` WHERE `bio` IS NULL AND `signature` IS NOT NULL;
