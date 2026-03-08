-- ============================================================
-- 校园招聘系统 - 数据库初始化脚本 (DDL)
-- Database: campus_recruitment
-- MySQL 8.x / utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_recruitment
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_recruitment;
SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;

-- -----------------------------------------------------------
-- 1. sys_user  系统用户表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL                COMMENT '密码（BCrypt）',
    `nickname`   VARCHAR(50)  DEFAULT NULL             COMMENT '昵称',
    `avatar`     VARCHAR(500) DEFAULT NULL             COMMENT '头像URL',
    `email`      VARCHAR(100) DEFAULT NULL             COMMENT '邮箱',
    `phone`      VARCHAR(20)  DEFAULT NULL             COMMENT '手机号',
    `role`       VARCHAR(20)  NOT NULL                COMMENT '角色: ADMIN/STUDENT/COMPANY',
    `status`     TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 0=禁用 1=正常 2=待审核',
    `created_at` DATETIME     NOT NULL                COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL                COMMENT '更新时间',
    `deleted_at` DATETIME     DEFAULT NULL             COMMENT '逻辑删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_username` (`username`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- -----------------------------------------------------------
-- 2. student_profile  学生档案表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `student_profile`;
CREATE TABLE `student_profile` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`             BIGINT       NOT NULL                COMMENT '关联用户ID',
    `real_name`           VARCHAR(50)  DEFAULT NULL             COMMENT '真实姓名',
    `gender`              VARCHAR(10)  DEFAULT NULL             COMMENT '性别',
    `birth_date`          VARCHAR(20)  DEFAULT NULL             COMMENT '出生日期',
    `school`              VARCHAR(100) DEFAULT NULL             COMMENT '学校',
    `major`               VARCHAR(100) DEFAULT NULL             COMMENT '专业',
    `education`           VARCHAR(20)  DEFAULT NULL             COMMENT '学历',
    `graduation_year`     INT          DEFAULT NULL             COMMENT '毕业年份',
    `skills`              TEXT                                  COMMENT '技能标签',
    `job_intention`       VARCHAR(200) DEFAULT NULL             COMMENT '求职意向',
    `expected_salary_min` INT          DEFAULT NULL             COMMENT '期望薪资下限（元/月）',
    `expected_salary_max` INT          DEFAULT NULL             COMMENT '期望薪资上限（元/月）',
    `expected_city`       VARCHAR(100) DEFAULT NULL             COMMENT '期望工作城市',
    `self_introduction`   TEXT                                  COMMENT '自我介绍',
    `created_at`          DATETIME     DEFAULT NULL             COMMENT '创建时间',
    `updated_at`          DATETIME     DEFAULT NULL             COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生档案表';

-- -----------------------------------------------------------
-- 3. company_profile  企业档案表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `company_profile`;
CREATE TABLE `company_profile` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT       NOT NULL                COMMENT '关联用户ID',
    `company_name`   VARCHAR(100) DEFAULT NULL             COMMENT '公司名称',
    `logo`           VARCHAR(500) DEFAULT NULL             COMMENT '公司Logo',
    `industry`       VARCHAR(50)  DEFAULT NULL             COMMENT '行业',
    `scale`          VARCHAR(50)  DEFAULT NULL             COMMENT '公司规模',
    `address`        VARCHAR(200) DEFAULT NULL             COMMENT '公司地址',
    `city`           VARCHAR(50)  DEFAULT NULL             COMMENT '所在城市',
    `description`    TEXT                                  COMMENT '公司简介',
    `website`        VARCHAR(200) DEFAULT NULL             COMMENT '官网',
    `contact_person` VARCHAR(50)  DEFAULT NULL             COMMENT '联系人',
    `contact_phone`  VARCHAR(20)  DEFAULT NULL             COMMENT '联系电话',
    `contact_email`  VARCHAR(100) DEFAULT NULL             COMMENT '联系邮箱',
    `license_url`    VARCHAR(500) DEFAULT NULL             COMMENT '营业执照URL',
    `audit_status`   TINYINT      NOT NULL DEFAULT 0      COMMENT '审核状态: 0=待审核 1=已通过 2=已拒绝',
    `audit_remark`   VARCHAR(500) DEFAULT NULL             COMMENT '审核备注',
    `audit_time`     DATETIME     DEFAULT NULL             COMMENT '审核时间',
    `created_at`     DATETIME     DEFAULT NULL             COMMENT '创建时间',
    `updated_at`     DATETIME     DEFAULT NULL             COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    INDEX `idx_audit_status` (`audit_status`),
    CONSTRAINT `fk_company_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业档案表';

-- -----------------------------------------------------------
-- 4. job  职位表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `company_id`    BIGINT       NOT NULL                COMMENT '所属企业ID',
    `title`         VARCHAR(100) NOT NULL                COMMENT '职位名称',
    `description`   TEXT                                  COMMENT '职位描述',
    `requirements`  TEXT                                  COMMENT '任职要求',
    `job_type`      VARCHAR(20)  DEFAULT NULL             COMMENT '职位类型: 全职/实习',
    `salary_min`    INT          DEFAULT NULL             COMMENT '薪资下限（元/月）',
    `salary_max`    INT          DEFAULT NULL             COMMENT '薪资上限（元/月）',
    `city`          VARCHAR(50)  DEFAULT NULL             COMMENT '工作城市',
    `address`       VARCHAR(200) DEFAULT NULL             COMMENT '工作地址',
    `education_req` VARCHAR(20)  DEFAULT NULL             COMMENT '学历要求',
    `major_req`     VARCHAR(100) DEFAULT NULL             COMMENT '专业要求',
    `headcount`     INT          DEFAULT NULL             COMMENT '招聘人数',
    `category`      VARCHAR(50)  DEFAULT NULL             COMMENT '职位类别',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 0=草稿 1=待审核 2=已发布 3=已关闭 4=已拒绝',
    `audit_remark`  VARCHAR(500) DEFAULT NULL             COMMENT '审核备注',
    `deadline`      DATE         DEFAULT NULL             COMMENT '截止日期',
    `created_at`    DATETIME     DEFAULT NULL             COMMENT '创建时间',
    `updated_at`    DATETIME     DEFAULT NULL             COMMENT '更新时间',
    `deleted_at`    DATETIME     DEFAULT NULL             COMMENT '逻辑删除时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_company` (`company_id`),
    INDEX `idx_category` (`category`),
    CONSTRAINT `fk_job_company` FOREIGN KEY (`company_id`) REFERENCES `company_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位表';

-- -----------------------------------------------------------
-- 5. resume  简历表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`              BIGINT       NOT NULL                COMMENT '所属用户ID',
    `title`                VARCHAR(100) DEFAULT NULL             COMMENT '简历标题',
    `education_experience` TEXT                                  COMMENT '教育经历（JSON）',
    `work_experience`      TEXT                                  COMMENT '工作/实习经历（JSON）',
    `project_experience`   TEXT                                  COMMENT '项目经历（JSON）',
    `skills`               TEXT                                  COMMENT '技能特长',
    `awards`               TEXT                                  COMMENT '获奖情况',
    `self_evaluation`      TEXT                                  COMMENT '自我评价',
    `is_default`           TINYINT      NOT NULL DEFAULT 0      COMMENT '是否默认简历: 0=否 1=是',
    `status`               TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 0=私密 1=公开',
    `created_at`           DATETIME     DEFAULT NULL             COMMENT '创建时间',
    `updated_at`           DATETIME     DEFAULT NULL             COMMENT '更新时间',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_resume_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';

-- -----------------------------------------------------------
-- 6. application  投递申请表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `application`;
CREATE TABLE `application` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `student_id` BIGINT       NOT NULL                COMMENT '学生用户ID',
    `job_id`     BIGINT       NOT NULL                COMMENT '职位ID',
    `resume_id`  BIGINT       NOT NULL                COMMENT '简历ID',
    `status`     TINYINT      NOT NULL DEFAULT 0      COMMENT '状态: 0=待查看 1=已查看 2=面试中 3=已录用 4=已拒绝 5=已撤回',
    `remark`     VARCHAR(500) DEFAULT NULL             COMMENT '备注',
    `created_at` DATETIME     DEFAULT NULL             COMMENT '创建时间',
    `updated_at` DATETIME     DEFAULT NULL             COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_job` (`student_id`, `job_id`),
    CONSTRAINT `fk_app_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_app_job` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`),
    CONSTRAINT `fk_app_resume` FOREIGN KEY (`resume_id`) REFERENCES `resume` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投递申请表';

-- -----------------------------------------------------------
-- 7. interview  面试表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `interview`;
CREATE TABLE `interview` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `application_id` BIGINT       NOT NULL                COMMENT '申请ID',
    `company_id`     BIGINT       NOT NULL                COMMENT '企业ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '学生用户ID',
    `job_id`         BIGINT       NOT NULL                COMMENT '职位ID',
    `interview_time` DATETIME     DEFAULT NULL             COMMENT '面试时间',
    `location`       VARCHAR(200) DEFAULT NULL             COMMENT '面试地点',
    `interview_type` VARCHAR(20)  DEFAULT NULL             COMMENT '面试形式: 线上/线下/电话',
    `description`    TEXT                                  COMMENT '面试说明',
    `contact`        VARCHAR(100) DEFAULT NULL             COMMENT '联系方式',
    `status`         TINYINT      NOT NULL DEFAULT 0      COMMENT '状态: 0=待确认 1=已接受 2=已拒绝 3=已完成',
    `created_at`     DATETIME     DEFAULT NULL             COMMENT '创建时间',
    `updated_at`     DATETIME     DEFAULT NULL             COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试表';

-- -----------------------------------------------------------
-- 8. offer  录用通知表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `offer`;
CREATE TABLE `offer` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `application_id` BIGINT       NOT NULL                COMMENT '申请ID',
    `company_id`     BIGINT       NOT NULL                COMMENT '企业ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '学生用户ID',
    `job_id`         BIGINT       NOT NULL                COMMENT '职位ID',
    `salary`         INT          DEFAULT NULL             COMMENT '薪资（元/月）',
    `start_date`     DATE         DEFAULT NULL             COMMENT '入职日期',
    `description`    TEXT                                  COMMENT 'Offer详情',
    `deadline`       DATE         DEFAULT NULL             COMMENT '回复截止日期',
    `status`         TINYINT      NOT NULL DEFAULT 0      COMMENT '状态: 0=待回复 1=已接受 2=已拒绝 3=已过期',
    `created_at`     DATETIME     DEFAULT NULL             COMMENT '创建时间',
    `updated_at`     DATETIME     DEFAULT NULL             COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='录用通知表';

-- -----------------------------------------------------------
-- 9. job_fair  招聘会表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `job_fair`;
CREATE TABLE `job_fair` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `company_id`    BIGINT       NOT NULL                COMMENT '举办企业ID',
    `title`         VARCHAR(200) DEFAULT NULL             COMMENT '招聘会标题',
    `description`   TEXT                                  COMMENT '招聘会描述',
    `location`      VARCHAR(200) DEFAULT NULL             COMMENT '举办地点',
    `start_time`    DATETIME     DEFAULT NULL             COMMENT '开始时间',
    `end_time`      DATETIME     DEFAULT NULL             COMMENT '结束时间',
    `max_capacity`  INT          DEFAULT NULL             COMMENT '最大容纳人数',
    `current_count` INT          NOT NULL DEFAULT 0      COMMENT '当前报名人数',
    `status`        TINYINT      NOT NULL DEFAULT 0      COMMENT '状态: 0=待审核 1=已通过 2=已拒绝 3=已完成 4=已取消',
    `audit_remark`  VARCHAR(500) DEFAULT NULL             COMMENT '审核备注',
    `created_at`    DATETIME     DEFAULT NULL             COMMENT '创建时间',
    `updated_at`    DATETIME     DEFAULT NULL             COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招聘会表';

-- -----------------------------------------------------------
-- 10. job_fair_booking  招聘会预约表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `job_fair_booking`;
CREATE TABLE `job_fair_booking` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `job_fair_id` BIGINT   NOT NULL                COMMENT '招聘会ID',
    `student_id`  BIGINT   NOT NULL                COMMENT '学生用户ID',
    `status`      TINYINT  NOT NULL DEFAULT 0      COMMENT '状态: 0=已预约 1=已取消 2=已签到',
    `created_at`  DATETIME DEFAULT NULL             COMMENT '创建时间',
    `updated_at`  DATETIME DEFAULT NULL             COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fair_student` (`job_fair_id`, `student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招聘会预约表';

-- -----------------------------------------------------------
-- 11. announcement  公告表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`        VARCHAR(200) DEFAULT NULL             COMMENT '公告标题',
    `content`      TEXT                                  COMMENT '公告内容',
    `type`         VARCHAR(20)  DEFAULT NULL             COMMENT '类型: system/recruitment/activity',
    `publisher_id` BIGINT       DEFAULT NULL             COMMENT '发布人ID',
    `status`       TINYINT      NOT NULL DEFAULT 0      COMMENT '状态: 0=草稿 1=已发布 2=已归档',
    `top_flag`     TINYINT      NOT NULL DEFAULT 0      COMMENT '是否置顶: 0=否 1=是',
    `created_at`   DATETIME     DEFAULT NULL             COMMENT '创建时间',
    `updated_at`   DATETIME     DEFAULT NULL             COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- -----------------------------------------------------------
-- 12. message  站内消息表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sender_id`   BIGINT       DEFAULT NULL             COMMENT '发送人ID（NULL=系统消息）',
    `receiver_id` BIGINT       NOT NULL                COMMENT '接收人ID',
    `title`       VARCHAR(200) DEFAULT NULL             COMMENT '消息标题',
    `content`     TEXT                                  COMMENT '消息内容',
    `type`        VARCHAR(20)  DEFAULT NULL             COMMENT '类型: system/application/interview/offer',
    `is_read`     TINYINT      NOT NULL DEFAULT 0      COMMENT '是否已读: 0=未读 1=已读',
    `related_id`  BIGINT       DEFAULT NULL             COMMENT '关联业务ID',
    `created_at`  DATETIME     DEFAULT NULL             COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_receiver` (`receiver_id`),
    INDEX `idx_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息表';

-- -----------------------------------------------------------
-- 13. operation_log  操作日志表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT       DEFAULT NULL             COMMENT '操作人ID',
    `username`   VARCHAR(50)  DEFAULT NULL             COMMENT '操作人用户名',
    `operation`  VARCHAR(200) DEFAULT NULL             COMMENT '操作描述',
    `method`     VARCHAR(200) DEFAULT NULL             COMMENT '请求方法',
    `params`     TEXT                                  COMMENT '请求参数',
    `ip`         VARCHAR(50)  DEFAULT NULL             COMMENT '操作IP',
    `result`     TEXT                                  COMMENT '操作结果',
    `duration`   BIGINT       DEFAULT NULL             COMMENT '耗时（毫秒）',
    `created_at` DATETIME     DEFAULT NULL             COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
