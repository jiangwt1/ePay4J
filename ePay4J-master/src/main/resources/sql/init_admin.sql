-- 管理员用户表
CREATE TABLE IF NOT EXISTS `admin_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '登录用户名',
  `password` VARCHAR(128) NOT NULL COMMENT 'BCrypt密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '显示名称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `email` VARCHAR(100) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色: SUPER_ADMIN, ADMIN, VIEWER',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
  `security_code` VARCHAR(64) DEFAULT NULL COMMENT '安全码(用于忘记密码重置)',
  `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户';

-- 默认管理员: admin / admin123 / 安全码: EPa9xK2mR7wQ
INSERT INTO `admin_user` (`username`, `password`, `nickname`, `role`, `status`, `security_code`)
VALUES ('admin', '$2a$10$vr6QT6LtcsiIw7cxWH2qUu4wUdr9MHptIrgfqm2yh0f.FUpPnauEa', '超级管理员', 'SUPER_ADMIN', 1, 'EPa9xK2mR7wQ');

-- 支付通道配置表
CREATE TABLE IF NOT EXISTS `payment_channel_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `channel_code` VARCHAR(20) NOT NULL COMMENT '通道编码: ALIPAY, WECHAT, PAYPAL',
  `channel_name` VARCHAR(50) NOT NULL COMMENT '通道名称',
  `config_data` TEXT NOT NULL COMMENT 'JSON配置',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '1=默认通道',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_channel_code` (`channel_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付通道配置';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `config_group` VARCHAR(50) NOT NULL DEFAULT 'general' COMMENT '分组: general, payment',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

-- 系统配置种子数据
INSERT INTO `system_config` (`config_key`, `config_value`, `config_group`, `description`) VALUES
('site_name', 'EPay Payment Gateway', 'general', '站点名称'),
('site_url', 'http://yzf.gysy.ltd', 'general', '站点地址'),
('site_logo', '', 'general', 'Logo URL'),
('order_timeout_minutes', '30', 'payment', '订单超时时间(分钟)'),
('merchant_key_length', '32', 'payment', '商户密钥长度');
