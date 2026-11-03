-- [已废弃] 数据库初始化已改用 resources/mybatis.sql（库名 mybatis，登录表 tb_users）。
-- 本文件保留作历史参考，不再被应用启动流程执行。
-- 如需手动建旧的最小表结构：mysql -uroot -p jsj2434 < src/main/resources/schema.sql

CREATE TABLE IF NOT EXISTS users (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL,
    password VARCHAR(100) NOT NULL,
    email    VARCHAR(100) DEFAULT NULL,
    phone    VARCHAR(20)  DEFAULT NULL,
    address  VARCHAR(200) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 课程演示账号：admin / 123；另一条用于验证多用户查询。
INSERT IGNORE INTO users (username, password, email, phone, address)
VALUES ('admin', '123', 'admin@example.com', '13800000000', '浙江杭州'),
       ('alice', '123456', 'alice@example.com', '13900000000', '浙江杭州');
