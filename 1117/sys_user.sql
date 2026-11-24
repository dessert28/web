/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50561
 Source Host           : localhost:3306
 Source Schema         : mybatis

 Target Server Type    : MySQL
 Target Server Version : 50561
 File Encoding         : 65001

 Date: 20/10/2025 10:30:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$WduQXkbTNIHLPKiDeXayU.DJI9smdQDst0.HODz1FlUdHg3dWirXS', 1);
INSERT INTO `sys_user` VALUES (2, 'doctor', '$2a$10$dvbBAoR4dJwgGdQJ0OSH/.3oXtz9xYM2Cg2y75koed/rRxa3TiFVm', 1);
INSERT INTO `sys_user` VALUES (3, 'patient', '$2a$10$I.5OOfuTYGUHnVBoxiCire9W0O2TwYEnfEf48XopWtXZIVqUkunc2', 1);

SET FOREIGN_KEY_CHECKS = 1;
