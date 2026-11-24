-- ============================================================
-- RBAC 认证三表：严格对齐老师给的 1117 样本
--   1117/sys_user.sql
--   1117/sys_role.sql
--   1117/sys_user_role.sql
--
-- 与样本保持一致的：
--   * 字段、类型、主键、唯一键、外键、utf8 字符集
--   * 账号 admin / doctor / patient，密码 1234 的 BCrypt 哈希
--   * 角色 ADMIN / DOCTOR / PATIENT 及三条绑定关系
--
-- 与样本只有两处必要差异，都不改变表语义：
--   1) DROP TABLE IF EXISTS -> CREATE TABLE IF NOT EXISTS
--      目标库 mybatis 与 jsj2434、hospital-registration 共用，
--      DROP 会在重复执行时清空数据，不符合「只新增、不破坏」的约束；
--   2) 跳过样本里 id=4、name='' 的空角色行
--      它是 Navicat 导出时的脏数据，留着会让角色查询多出一个空权限。
--
-- 密码说明：样本里三个哈希实测对应明文 1234（对 123 全部不匹配）。
-- 本脚本原样保留，登录请用 admin/1234、doctor/1234、patient/1234。
-- 如需改成 123，把下面三条 password 换成 123 的 BCrypt 即可。
-- ============================================================

SET NAMES utf8mb4;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username` (`username`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- 2. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name` (`name`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- 3. 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` bigint(20) NOT NULL DEFAULT 0,
  `role_id` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `role_id` (`role_id`) USING BTREE,
  CONSTRAINT `sys_user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `sys_user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- 4. 角色种子（样本 id=4 的空名角色已跳过）
INSERT IGNORE INTO `sys_role` (`id`, `name`) VALUES
  (1, 'ADMIN'),
  (2, 'DOCTOR'),
  (3, 'PATIENT');

-- 5. 账号种子（密码 1234 的 BCrypt，与样本逐字节一致）
INSERT IGNORE INTO `sys_user` (`id`, `username`, `password`, `enabled`) VALUES
  (1, 'admin',   '$2a$10$WduQXkbTNIHLPKiDeXayU.DJI9smdQDst0.HODz1FlUdHg3dWirXS', 1),
  (2, 'doctor',  '$2a$10$dvbBAoR4dJwgGdQJ0OSH/.3oXtz9xYM2Cg2y75koed/rRxa3TiFVm', 1),
  (3, 'patient', '$2a$10$I.5OOfuTYGUHnVBoxiCire9W0O2TwYEnfEf48XopWtXZIVqUkunc2', 1);

-- 6. 用户-角色绑定
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES
  (1, 1),
  (2, 2),
  (3, 3);

-- 7. 自检：应立即看到 3 行，密码哈希与样本一致，角色分别为 ADMIN/DOCTOR/PATIENT
SELECT u.`id`, u.`username`, u.`enabled`, GROUP_CONCAT(r.`name`) AS roles
FROM `sys_user` u
LEFT JOIN `sys_user_role` ur ON ur.`user_id` = u.`id`
LEFT JOIN `sys_role` r ON r.`id` = ur.`role_id`
GROUP BY u.`id`, u.`username`, u.`enabled`;
