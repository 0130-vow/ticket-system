-- 创建数据库
CREATE DATABASE IF NOT EXISTS ticket_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ticket_db;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'employee',
    department VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 工单表
CREATE TABLE IF NOT EXISTS ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    priority VARCHAR(10) NOT NULL DEFAULT 'P2',
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    creator_id BIGINT NOT NULL,
    assignee_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    closed_at DATETIME,
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_creator (creator_id),
    INDEX idx_assignee (assignee_id)
);

-- 回复表
CREATE TABLE IF NOT EXISTS ticket_reply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    is_internal TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ticket (ticket_id)
);

-- 插入测试数据 (密码使用BCrypt加密，admin123)
INSERT INTO sys_user (id, username, password, name, role, department) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin', 'IT部门'),
(2, 'zhangming', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张明', 'technician', 'IT运维'),
(3, 'liyun', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李云', 'technician', '前端技术'),
(4, 'wangfang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王芳', 'technician', '后端技术'),
(5, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '普通员工', 'employee', '销售部');

-- 插入测试工单
INSERT INTO ticket (id, title, description, priority, status, creator_id, assignee_id) VALUES
(1, '【OA系统无法登录】', '今天早上9点开始，OA系统无法正常登录，显示网络错误', 'P0', 'processing', 5, 2),
(2, '【打印机无法连接】', '3楼会议室打印机无法连接，影响会议资料打印', 'P2', 'pending', 5, NULL),
(3, '【邮箱收发异常】', '部分员工反映无法正常收发邮件', 'P1', 'resolved', 5, 3),
(4, '【系统响应缓慢】', 'ERP系统下午出现明显卡顿', 'P2', 'closed', 5, 4);
