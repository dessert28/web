-- ============================================================
-- 实验二：患者挂号 —— 数据库升级脚本
-- 目标库：mybatis（与 jsj2434 共用，不新建库）
-- 特性：幂等，可重复执行；只新增 tb_appointment，并给 doctor_info 补两列
-- 用法：mysql -uroot -p mybatis < db/experiment2-appointment.sql
-- 注意：本脚本不 DROP 任何表，也不动 mybatis.sql 里已有的其余 37 张表
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 预约挂号表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_appointment` (
  `id`             int(11)      NOT NULL AUTO_INCREMENT COMMENT '预约流水号',
  `doctor_id`      int(11)      NOT NULL COMMENT '医生ID，关联 doctor_info.id',
  `user_id`        int(11)      NOT NULL COMMENT '挂号人，关联 tb_users.id',
  `patient_name`   varchar(50)  NOT NULL COMMENT '就诊人姓名',
  `patient_gender` varchar(10)  NULL DEFAULT NULL COMMENT '就诊人性别：男/女/其他',
  `patient_phone`  varchar(20)  NOT NULL COMMENT '联系电话',
  `patient_id_card` varchar(30) NULL DEFAULT NULL COMMENT '身份证号',
  `visit_date`     date         NOT NULL COMMENT '预约就诊日期',
  `time_slot`      varchar(20)  NOT NULL COMMENT '就诊时段',
  `status`         tinyint(4)   NOT NULL DEFAULT 0 COMMENT '状态：0-待就诊，1-已取消',
  `remark`         varchar(200) NULL DEFAULT NULL COMMENT '备注',
  `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_doctor_date` (`doctor_id`, `visit_date`) USING BTREE,
  INDEX `idx_user` (`user_id`) USING BTREE,
  CONSTRAINT `tb_appointment_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_info` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact COMMENT = '预约挂号记录';

-- ------------------------------------------------------------
-- 2. doctor_info 补列：specialty（专业专长）、password（医生登录密码）
--    MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，用 information_schema 判断后动态执行
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS `add_column_if_absent`;
DELIMITER $$
CREATE PROCEDURE `add_column_if_absent`(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_ddl TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_ddl);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

CALL `add_column_if_absent`('doctor_info', 'specialty', '`specialty` varchar(200) NULL DEFAULT NULL COMMENT ''专业专长'' AFTER `title`');
CALL `add_column_if_absent`('doctor_info', 'password',  '`password` varchar(255) NULL DEFAULT NULL COMMENT ''医生登录密码（课程演示用明文）'' AFTER `phone`');

DROP PROCEDURE IF EXISTS `add_column_if_absent`;

-- ------------------------------------------------------------
-- 3. 医生登录密码：未设置的一律置为 123（课程演示）
-- ------------------------------------------------------------
UPDATE `doctor_info` SET `password` = '123' WHERE `password` IS NULL OR `password` = '';

-- ------------------------------------------------------------
-- 4. 医生专业专长种子数据（按 doctor_info.id）
-- ------------------------------------------------------------
UPDATE `doctor_info` SET `specialty` = '冠心病、高血压、心律失常的诊断与介入治疗' WHERE `id` = 1  AND (`specialty` IS NULL OR `specialty` = '');
UPDATE `doctor_info` SET `specialty` = '小儿呼吸道感染、哮喘、儿童生长发育评估'   WHERE `id` = 2  AND (`specialty` IS NULL OR `specialty` = '');
UPDATE `doctor_info` SET `specialty` = '慢性胃炎、消化性溃疡、肝胆疾病的诊治'       WHERE `id` = 3  AND (`specialty` IS NULL OR `specialty` = '');
UPDATE `doctor_info` SET `specialty` = '普外微创手术、疝气与胆囊疾病的外科治疗'     WHERE `id` = 6  AND (`specialty` IS NULL OR `specialty` = '');
UPDATE `doctor_info` SET `specialty` = '脑血管病、头痛与眩晕、周围神经病变'         WHERE `id` = 7  AND (`specialty` IS NULL OR `specialty` = '');
UPDATE `doctor_info` SET `specialty` = '湿疹、痤疮、真菌感染等常见皮肤病'           WHERE `id` = 8  AND (`specialty` IS NULL OR `specialty` = '');
UPDATE `doctor_info` SET `specialty` = '新生儿保健、儿童营养与消化道疾病'           WHERE `id` = 9  AND (`specialty` IS NULL OR `specialty` = '');
UPDATE `doctor_info` SET `specialty` = '小儿常见病、儿童疫苗接种咨询'               WHERE `id` = 10 AND (`specialty` IS NULL OR `specialty` = '');

-- ------------------------------------------------------------
-- 5. 自查
-- ------------------------------------------------------------
SELECT 'tb_appointment' AS object_name, COUNT(*) AS row_count FROM `tb_appointment`
UNION ALL
SELECT 'doctor_info', COUNT(*) FROM `doctor_info`;
