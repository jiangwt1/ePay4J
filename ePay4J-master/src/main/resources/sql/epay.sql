/*
 Navicat Premium Dump SQL

 Source Server         : pay.b509.cn
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : pay.b509.cn:3306
 Source Schema         : epay

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 15/05/2026 09:40:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'BCrypt密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ADMIN' COMMENT '角色: SUPER_ADMIN, ADMIN, VIEWER',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
  `security_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '安全码(用于忘记密码重置)',
  `last_login_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin_user
-- ----------------------------
INSERT INTO `admin_user` VALUES (1, 'admin', '$2a$10$p5flqTRMoeiu4LUk6O.eGOQKRJyl6s.mOiWQmcKJgKWKzyYwFSMrO', '超级管理员', '', NULL, NULL, 'SUPER_ADMIN', 1, 'EPa9xK2mR7wQ', '0:0:0:0:0:0:0:1', '2026-05-15 03:09:56', '2026-05-14 23:48:06', '2026-05-15 00:26:45');
INSERT INTO `admin_user` VALUES (7, 'mavis', '$2a$10$X74LDZ/u5vCNFbLmKtbo2OhAHgH/oBErYD5iyuhEjzfpt3FhQvMsy', 'mavis', NULL, NULL, NULL, 'MERCHANT', 1, NULL, '0:0:0:0:0:0:0:1', '2026-05-15 03:10:09', '2026-05-15 00:55:37', '2026-05-15 01:49:13');

-- ----------------------------
-- Table structure for merchant
-- ----------------------------
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商户ID（即pid）',
  `merchant_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'API密钥',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户名称',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1-启用 0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '对应用户id',
  `alipay_account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝账号',
  `nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓名',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of merchant
-- ----------------------------
INSERT INTO `merchant` VALUES (6, '00c9d936adc5411fa5076b0efdec3039', '小轩同学', 1, '2026-05-15 00:55:37', '7', '13797383596', '郭逸轩', '13797383596');

-- ----------------------------
-- Table structure for merchant_account
-- ----------------------------
DROP TABLE IF EXISTS `merchant_account`;
CREATE TABLE `merchant_account`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(20) NOT NULL COMMENT '商户ID',
  `total_income` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '累计收入',
  `available_balance` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '可用余额',
  `frozen_balance` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结余额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_id`(`merchant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户账户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of merchant_account
-- ----------------------------
INSERT INTO `merchant_account` VALUES (1, 6, 2.00, 1.00, 0.00, '2026-05-15 01:20:14', '2026-05-15 01:20:43');

-- ----------------------------
-- Table structure for merchant_withdraw
-- ----------------------------
DROP TABLE IF EXISTS `merchant_withdraw`;
CREATE TABLE `merchant_withdraw`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint(20) NOT NULL,
  `amount` decimal(12, 2) NOT NULL,
  `service_fee` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '手续费',
  `amount_credited` decimal(12, 2) NULL DEFAULT NULL COMMENT '实际到账金额',
  `status` tinyint(4) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `transfer_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '平台转账单号',
  `alipay_order_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝转账订单号',
  `pay_fund_order_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝资金流水号',
  `transfer_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝转账状态',
  `transfer_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝转账返回信息',
  `transfer_time` datetime NULL DEFAULT NULL COMMENT '实际打款时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  UNIQUE INDEX `uk_transfer_no`(`transfer_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户提现记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of merchant_withdraw
-- ----------------------------
INSERT INTO `merchant_withdraw` (`id`, `merchant_id`, `amount`, `status`, `create_time`, `update_time`, `remark`) VALUES (1, 6, 1.00, 2, '2026-05-15 02:57:13', '2026-05-15 03:09:26', '测试');
INSERT INTO `merchant_withdraw` (`id`, `merchant_id`, `amount`, `status`, `create_time`, `update_time`, `remark`) VALUES (2, 6, 1.00, 1, '2026-05-15 03:09:48', '2026-05-15 03:10:01', NULL);

-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `out_trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户订单号',
  `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝交易号',
  `subject` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单标题',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单金额（元）',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付 1-已支付 2-已关闭 3-已退款',
  `pay_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式：PAGE-电脑网站 WAP-手机网站',
  `buyer_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '买家支付宝用户ID',
  `pid` bigint(20) NULL DEFAULT NULL COMMENT '商户ID（易支付订单时有值）',
  `notify_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户异步通知地址',
  `return_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户同步跳转地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `notify_time` datetime NULL DEFAULT NULL COMMENT '通知时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_out_trade_no`(`out_trade_no`) USING BTREE,
  INDEX `idx_trade_no`(`trade_no`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_pid`(`pid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_order
-- ----------------------------
INSERT INTO `pay_order` VALUES (2, 'USR1NOxcoZWA1778777858', NULL, 'TUC500', 500.00, 2, 'ALIPAY', NULL, 6, 'https://ai.lehe.com/api/user/epay/notify', 'https://ai.lehe.com/console/log', '2026-05-15 00:57:39', NULL, NULL);
INSERT INTO `pay_order` VALUES (3, 'USR1NOB5hepp1778777963', NULL, 'TUC500', 500.00, 2, 'ALIPAY', NULL, 6, 'https://ai.lehe.com/api/user/epay/notify', 'https://ai.lehe.com/console/log', '2026-05-15 00:59:24', NULL, NULL);
INSERT INTO `pay_order` VALUES (4, 'USR1NOLFKuqK1778778044', '2026051522001411461414549724', 'TUC1', 1.00, 1, 'ALIPAY', NULL, 6, 'https://ai.lehe.com/api/user/epay/notify', 'https://ai.lehe.com/console/log', '2026-05-15 01:00:45', '2026-05-15 01:01:22', '2026-05-15 01:01:22');
INSERT INTO `pay_order` VALUES (5, 'USR1NOtDXdmH1778779451', '2026051522001411461412521929', 'TUC1', 1.00, 1, 'ALIPAY', NULL, 6, 'https://ai.lehe.com/api/user/epay/notify', 'https://ai.lehe.com/console/log', '2026-05-15 01:24:14', '2026-05-15 01:24:26', '2026-05-15 01:24:26');
INSERT INTO `pay_order` VALUES (6, 'USR1NOs4KjGe1778780110', NULL, 'TUC1', 1.00, 2, 'ALIPAY', NULL, 6, 'https://ai.lehe.com/api/user/epay/notify', 'https://ai.lehe.com/console/log', '2026-05-15 01:35:11', NULL, NULL);

-- ----------------------------
-- Table structure for payment_channel_config
-- ----------------------------
DROP TABLE IF EXISTS `payment_channel_config`;
CREATE TABLE `payment_channel_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channel_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通道编码: ALIPAY, WECHAT, PAYPAL',
  `channel_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通道名称',
  `config_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'JSON配置',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
  `is_default` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1=默认通道',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_channel_code`(`channel_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付通道配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payment_channel_config
-- ----------------------------
INSERT INTO `payment_channel_config` VALUES (4, 'ALIPAY', '支付宝-沙箱', '{\"appId\":\"9021000163631639\",\"privateKey\":\"MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCBKz2LY91ZnLO9dEaXPUUwgSK2LLrvTbuCm6Pg4FLX776mgY9aJh4daoJyXXHj55K4n2drgu0avMl4Zkx9+RCbu9BwYcyK1U4ejKyvIHYDRnVk1H6vAS8NeL6ALD9jNW5KI8L3wv4SgYLFo5NAkwmXkdvD18b96Tv0MSoF9CiYY/kH9TG6A8SvJQYuMbmP6NF7Fw+LfN4/7gd3pbUw5cxxfcGsjopWmOdz1GwwGAc5sMVAa1J6r5uA3eIbYBUNIjAEQnyOEjpCvmwhCaiPSJDYEBgQ/aRglZrjdtv0nMRUUdAQB0PBTYFXwTx4HwjhSmTVu49A7sM0NF64XhVKLfdXAgMBAAECggEAJQcjPmkjVAQC1oWVktdilTP/ol+SOvV6kkdRpC5KMVKtwYeUc5BHw8D7ohM9icqhmcFAYWaH18lUq+oPdXFHnI0rhHvUqdvZ6BIFkrJoginZj00G/94EAgWh/pYwmZ0Af8peoq3ILb/ooAVzOeissEGE0VBy3+jTYf4SAODsHjrqLoEIPHQdVQ27pBC8szZ5TLe56ewNpYxxM/hPxPltPODgjW8gjbh1Sozk6ov0ZQ1yzaKyVOOh+Ot9ylGAEnSE54FDn1GMAk1Sh1lFFmiUAfMZEhOEk6Z295oky1CmR7nbOLQ8kFu0ZREG5Wy9crXXnlVHTVmS6agC2MUZiHonKQKBgQC3o15s/Lclj5hjvGbq8v+OzFitTzOhf4rVBBIt10NysegmMgrWkDMMP0E/oQUQpeyv95wMTIbQ+CwZN71N1lYp3p9SB1kAFw2ew7LbNeKn6K1xu69ZnpVkYHXb6KEthqcoylsODKZvWPfQ0iPk2W2pRhagJ1w5wmZ1A6ih/GC8wwKBgQC0ET8nbd2xDagtgU6902oh5Q7vRiNxhL5LEZ9VL/K+Y54zIvuIilUU+zt3yNMfxRUKMPvcPYfL7hGjF4/w7NoRZvJVgn8+A/ASdIU4s+VWt1aeCEf5XWHybuBKk8Ok+ELSFCzad/FyYNLEzM7GV8AH6F4RXKE8erCYd9Ac/HTB3QKBgQCAzU56pRqUds5NDiUloADNmNiN3qJ5IOswk6Voj7bYKJ6Kw8jES+nyKh/K0FasIaUMm2DRY4+f3KGZonXdtXtD7MjUkSDEpmbAN8UoafgrC1E0F49bDZb9C4c/EkCSnQJ74srQ5OCL8twGlI3mgvaepvFRranLPWX9pu5Nh6YWXwKBgDznxHu6royPeP4DSbXtR221g2qtR5nC83aCvGjc8fcxzLWxNHEHkSWqoH+wnrCVQbuTu+zPpxn0KWUHuDkec9gFARypKg642D/1x4XZW04MbzC+zlFrI5ETraur9TXcfwV9q3otra4pT8EfISV3Us2Z7A9lTGgfe5HmnK7hOUDFAoGAeaCChu4Gi7icFO/uOk9PV3wQYwkyk2RzhXbzn1dK/I6LQ8E9XIwE+6z5tttm9rmAqByBMn0s8WpQuajSZPYav0fL8qYTKCa1ntGbq+lErck/X+VlJWq3G0+aKxCRK+7ve5G6AgwofC7c9WFECi0tyP2i6lb9P2ukioGpBbVTsjA=\",\"publicKey\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq9tXUr0BL0uNFUARqErXirAktxZSI9FIhCjmh+/VLLlAsE/gTdp9vhiL+ocICrMho0A0udNxJ14ShtS7HwxbzmzDOrZkfBfao6FUPqFRmCecHGWDtvsb/HDMO1Ybq3qqNpmGHTMkYigLnPMCCiesDGCHwVUDWinRDiXtZaykMGlDlxXisfSo74zdLSY+XpiIwWoHrkCwUzAKAMbNf81Dy0XNWqjLfC3CpNO2k0bs6W+YfK3g6tbCiG5Oog3cCNMIfbDgY2L2eNELQdwbMVlDvPBrHq83bQGcVbE3yfPwReaDT8Mlu6NOdNScs/IJjeKLIb1tvMoORAqerUxTolyo3QIDAQAB\",\"gatewayUrl\":\"https://openapi-sandbox.dl.alipaydev.com/gateway.do\",\"notifyUrl\":\"http://c95a665a.natappfree.cc/api/alipay/notify\",\"returnUrl\":\"http://c95a665a.natappfree.cc/api/alipay/return\",\"signType\":\"RSA2\",\"charset\":\"UTF-8\",\"format\":\"json\"}', 0, 0, 0, '支付宝-沙箱', '2026-05-15 00:53:53', '2026-05-15 00:53:53');
INSERT INTO `payment_channel_config` VALUES (5, 'ALIPAY', '支付宝-Mavis', '{\"appId\":\"2021006152627419\",\"privateKey\":\"MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCHDHupF+FU1ZapBwJklFlFzx9aDWPbNDEhoUZBPDiw1I0clkExK+C4KAP0W32cXUkCuiSl1pfUraiXjSumlffCd+eCYPkwFtHoHHoGBpjDFbV2k8ibvBQkZtz2Sjq7AfNVR0fK9gXgTmd2RJimAxvUrEM3C20E8d+1/q+Y68iJ5TnhmGLlSF1HITxtJGbo4a2Lgss9sBrhvR9cZvEnjtgLxLVXo+9XN1GnktWUOS2vNxBzb4dzor81XJfNaZNHSqX1P4wqZm6kEjjn/M5SHq+MRc3vtuHNEsSN6u1GoTju9pXKy1YZZP4SUXn/xIDpv9N0kXpFVDYeSPe2Lr72NZw7AgMBAAECggEADFMiUftA8DoR5tkh6cX0I7/UT6abL10jAruyKq/xSIoNWseHqZIkjwnv2nQjEYsd56bPQ2dDMgCrOzGCA1D6bH/+29I3y6C5hWJb7ILbnCo2Bl1UWIogmHXewodhtsFl0KmRrr8nKCJfpNZ7y+LKvmz+MV7Ukzfzv6s/oUgbZCIqTlk5PgGNh/pLzpaL4Jzdw/52XzpJS2M+IpOva6crgxOrYbjK1vf+2z6TlJ41hBKE700w35ZYxHae96EwL0g7VatI3JXR0KR5J+WkFqDbBy7Y/EehM6Kmrpo4aCZ11Bc3olt7szurJdRTj3yfZp80hW/kyAlwhyJxTdmfg+qUwQKBgQDIdsDPcKQe6Ffjo9t8Y5qdFnGsAMRI90dQ8HFOqFeseS0loUgzzZqeBEpH/G7qTRO2ErtK2PCYNntt2YbocbdDcAy+XTpcOVEwbWuZUr7flR8rVsuspJB4v/lS8FOGh9hN+m/bB/ycCT+Pyth0k71k7Dg23C3qNNk4n1W8I5s9YQKBgQCsdmBCznMKeKV835zJ2xButrc4wugd3GXngvO1cZ83HmBTr9hORhPI8Wv1QGWhJqQ67QEaHX1yl6qzdycznCj+tbWrm6nkrzmoiw4E1WNAUc+yV9VFthrfVTPyJsGFRAS9SAb8mYDDj3LYXb9eE+WeqFjTcXGar6egmf8syd4DGwKBgGpMjnFxiZXaHsDXYsvTBWnHPP+4aF1Iwshot6y+F7+dghMyMtdFb3fbTohdk3Mq7AjgZfCKmQod2dWmQ6N+qW9ZO2i2hvvgaO0FGMl010I4EQ8qN1aU7QcaxVr1lVUBM/IlmqrK0WjsfogBSEaJESUzHZv5pV9MJuOfmhbpyqgBAoGBAJdTc1mLTvQ0MnkkuK2B86RB3PkIRKXW+u+uPuvJm/vAQqOL20+rO7cI50KWU9aYUmiGB36Du8xt+lFoztQBRRNiG9tNRkCr4/rYsN9a4bDRWF5TpYZ6qI+ocmEa0pl9um7foYnJo2t1Fj912vHQoZ2ad/5oP1eMo5hPJ9GD+7NbAoGASBKHulZkzo66L9wEbWxHVw/bXnHkIVyhdDZiBQ4sf8DFQdSOrMDqby20N5BTb1Iutn7ApZR6N3PY39XngQc0ha5WtQaRZmd8HD5BSgqLuRAfKqgdOD2MNmVtTwk5XWNPHz5iD6vowfA76nJ3WV/HbKdHELuehJ6X1V8NL4+LajI=\",\"publicKey\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlUUvA3K8RAddwu5vFeBOOtb1kluvokL6OSRXUeF+VePA1WGUyyHLbQSw5FarHL+xmCJ2yTdlyJ8rFKoADQQfjGv3noydlp36m3p8Qqq6YSyY6Jw47jlJU8qRydmUOtG0kw5P0aNohe4cbpJWhQQ43k1UNYJRVmprVjqx4E/fWczcjRIOTU5OOEYnSWVmMr0Nuwh3mNgg+tq56aPCGcyKCe9kJCOWFGBGv4+TOxxdfeeQa+PMJrhog2An19AsPCEwKvzcs1nn6Qq7xqJbVIbpKRA3Lq6W7Bjg/gBDOZvJnBtJGB21mdnOBsV1yQ54UhhkhuqH3zrqUOwITG7kKGy1gQIDAQAB\",\"gatewayUrl\":\"https://openapi.alipay.com/gateway.do\",\"notifyUrl\":\"http://c95a665a.natappfree.cc/api/alipay/notify\",\"returnUrl\":\"http://c95a665a.natappfree.cc/api/alipay/return\",\"signType\":\"RSA2\",\"charset\":\"UTF-8\",\"format\":\"json\"}', 1, 1, 0, '支付宝-Mavis', '2026-05-15 01:00:36', '2026-05-15 01:00:36');

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置值',
  `config_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'general' COMMENT '分组: general, payment',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'site_name', 'MavisPay', 'general', '站点名称', '2026-05-14 23:48:06', '2026-05-14 23:48:06');
INSERT INTO `system_config` VALUES (2, 'site_url', 'http://pay.b509.cn', 'general', '站点地址', '2026-05-14 23:48:06', '2026-05-14 23:48:06');
INSERT INTO `system_config` VALUES (3, 'site_logo', '', 'general', 'Logo URL', '2026-05-14 23:48:06', '2026-05-14 23:48:06');
INSERT INTO `system_config` VALUES (4, 'order_timeout_minutes', '30', 'payment', '订单超时时间(分钟)', '2026-05-14 23:48:06', '2026-05-14 23:48:06');
INSERT INTO `system_config` VALUES (5, 'merchant_key_length', '32', 'payment', '商户密钥长度', '2026-05-14 23:48:06', '2026-05-14 23:48:06');
INSERT INTO `system_config` VALUES (6, 'rate', '0.3', 'payment', '提现费率', '2026-05-15 00:46:58', '2026-05-15 01:08:04');
INSERT INTO `system_config` VALUES (7, 'pay_api_url', 'http://pay.b509.cn', 'general', '支付接口', '2026-05-15 02:25:48', '2026-05-15 02:25:57');

SET FOREIGN_KEY_CHECKS = 1;
