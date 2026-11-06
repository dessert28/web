/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50561
 Source Host           : localhost:3306
 Source Schema         : javaweb

 Target Server Type    : MySQL
 Target Server Version : 50561
 File Encoding         : 65001

 Date: 06/11/2026 09:05:00
*/

-- ============================================================================
-- javaweb.sql —— jsj2434 课程的 javaweb 库建库脚本
--
-- 与 mybatis.sql 的区别：本脚本只建 javaweb 这一个库的 6 张表，是 jsj2434
-- 当前数据源（application.properties 里 jdbc:mysql://.../javaweb）的初始化脚本。
--
-- 导入方式（任选其一）：
--   命令行： mysql -uroot -proot --default-character-set=utf8mb4 < javaweb.sql
--   Navicat：打开本文件 → 执行
--
-- 特性：
--   1. 自带 CREATE DATABASE IF NOT EXISTS + USE，不依赖已选库，导入即建库；
--   2. 幂等：全部 CREATE TABLE IF NOT EXISTS，插入用 INSERT IGNORE，
--      重复执行不会报错、不会覆盖已修改的数据；
--   3. 表之间按依赖顺序创建：department → doctor_info → account / patient / visit_record；
--   4. 字符集统一 utf8mb4（MySQL 8 里 "utf8" 会被当成 utf8mb3，存不了 emoji 和生僻字）。
--
-- 如果需要回到 mybatis 库，改 jsj2434/src/main/resources/application.properties
-- 的 spring.datasource.url 即可，两个库互不影响。
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `javaweb`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;
USE `javaweb`;

-- ----------------------------
-- Table structure for department
-- 科室表：doctor_info.department_id 的外键目标
-- ----------------------------
CREATE TABLE IF NOT EXISTS `department`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_department_dept_name`(`dept_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT IGNORE INTO `department` VALUES (1, '内科', '1号楼2层', '0571-88287001');
INSERT IGNORE INTO `department` VALUES (2, '外科', '1号楼3层', '0571-88287002');
INSERT IGNORE INTO `department` VALUES (3, '儿科', '1号楼4层', '0571-88287003');
INSERT IGNORE INTO `department` VALUES (4, '眼科', '1号楼5层', '0571-88287004');
INSERT IGNORE INTO `department` VALUES (5, '妇科', '1号楼4层', '0571-88287005');
INSERT IGNORE INTO `department` VALUES (6, '骨科', '2号楼3层', '0571-88287006');

-- ----------------------------
-- Table structure for doctor_info
-- 注意：没有 department 文本列，科室一律通过 department_id 关联 department 表。
-- jsj2434 的 DoctorMapper.xml 用 left join 把 dept_name 取回来当作 Doctor.department 展示。
-- ----------------------------
CREATE TABLE IF NOT EXISTS `doctor_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '医生ID，主键自增',
  `doctorName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '医生姓名',
  `gender` tinyint(4) NOT NULL DEFAULT 1 COMMENT '性别：0-女，1-男，2-其他',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '职称（主任医师/副主任医师/主治医师/住院医师）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `department_id` int(11) NOT NULL COMMENT '所属科室，外键 → department.id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `department_id`(`department_id`) USING BTREE,
  CONSTRAINT `doctor_info_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of doctor_info
-- 主键重排为 1..5；每行的 department_id 已按其真实科室映射：
--   张伟 心内科 → 内科(1)     李娜 儿科 → 儿科(3)     张宇 内科 → 内科(1)
--   朱俊 外科 → 外科(2)       李翔 内科 → 内科(1)
-- ----------------------------
INSERT IGNORE INTO `doctor_info` VALUES (1, '张伟', 1, '主任医师', '13800138001', 1);
INSERT IGNORE INTO `doctor_info` VALUES (2, '李娜', 0, '副主任医师', '13900139002', 3);
INSERT IGNORE INTO `doctor_info` VALUES (3, '张宇', 1, '主任医师', '13666668887', 1);
INSERT IGNORE INTO `doctor_info` VALUES (4, '朱俊', 1, '主治医师', '15888886666', 2);
INSERT IGNORE INTO `doctor_info` VALUES (5, '李翔', 1, '副主任医师', '13787876868', 1);

-- ----------------------------
-- Table structure for account
-- 医生登录账号：一个医生最多一个账号（doctor_id 唯一）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `account`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `doctor_id` int(11) NULL DEFAULT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `doctor_id`(`doctor_id`) USING BTREE,
  CONSTRAINT `account_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_info` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 105 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of account
-- 对应 doctor_info 的 1 / 2 / 3 / 4 号医生
-- ----------------------------
INSERT IGNORE INTO `account` VALUES (101, 1, 'zhangwei', 'pwd123');
INSERT IGNORE INTO `account` VALUES (102, 2, 'lina', 'pwd123');
INSERT IGNORE INTO `account` VALUES (103, 3, 'zhangyu', 'pwd123');
INSERT IGNORE INTO `account` VALUES (104, 4, 'zhujun', 'pwd123');

-- ----------------------------
-- Table structure for patient
-- ----------------------------
CREATE TABLE IF NOT EXISTS `patient`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `patient_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `birth_date` date NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 206 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of patient
-- ----------------------------
INSERT IGNORE INTO `patient` VALUES (201, '赵明', '男', '13800138001', '1990-04-10');
INSERT IGNORE INTO `patient` VALUES (202, '孙芳', '女', '13800138002', '2005-11-01');
INSERT IGNORE INTO `patient` VALUES (203, '周杰', '男', '13800138003', '2025-09-03');
INSERT IGNORE INTO `patient` VALUES (204, '吴静', '女', '13800138004', '1985-04-10');
INSERT IGNORE INTO `patient` VALUES (205, '郑爽', '女', '13800138005', '1970-04-30');

-- ----------------------------
-- Table structure for visit_record
-- 就诊记录：同时引用 doctor_info 与 patient
-- ----------------------------
CREATE TABLE IF NOT EXISTS `visit_record`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `doctor_id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `visit_date` date NULL DEFAULT NULL,
  `diagnosis` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `doctor_id`(`doctor_id`) USING BTREE,
  INDEX `patient_id`(`patient_id`) USING BTREE,
  CONSTRAINT `visit_record_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_info` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `visit_record_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of visit_record
-- ----------------------------
INSERT IGNORE INTO `visit_record` VALUES (1, 1, 201, '2026-04-09', '感冒');
INSERT IGNORE INTO `visit_record` VALUES (2, 2, 202, '2026-03-05', '偏头痛');
INSERT IGNORE INTO `visit_record` VALUES (3, 3, 203, '2026-03-31', '拉肚子');
INSERT IGNORE INTO `visit_record` VALUES (4, 1, 202, '2026-04-07', '流鼻涕');
INSERT IGNORE INTO `visit_record` VALUES (5, 1, 203, '2026-04-08', '打喷嚏');

-- ----------------------------
-- Table structure for tb_users
-- User 模块（UserMapper 注解 SQL）读写的用户表，对应 domain/Users.java。
-- Users 只有 username/password/email/phone/address 五个属性，与表的列不必一一对应；
-- 登录仍是硬编码 admin/123，本表只用于 /selectAllUsers 列表展示与两个查询接口。
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_users`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `rolename` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_users
-- 密码为课程明文演示值，不要在此基础上扩展真实用户体系
-- ----------------------------
INSERT IGNORE INTO `tb_users` VALUES (1, 'admin', '123', 'admin', '1', NULL);
INSERT IGNORE INTO `tb_users` VALUES (2, '15998815467', '123', 'ROLE_common', '2', NULL);
INSERT IGNORE INTO `tb_users` VALUES (15, '李四', '123', 'ROLE_vip', '2', NULL);
INSERT IGNORE INTO `tb_users` VALUES (20, 'shitou', '123', 'ROLE_common', '3', NULL);
INSERT IGNORE INTO `tb_users` VALUES (21, '张三', '123', 'ROLE_vip', '3', NULL);

SET FOREIGN_KEY_CHECKS = 1;
