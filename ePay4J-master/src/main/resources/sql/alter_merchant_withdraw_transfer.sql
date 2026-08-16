-- 为商户提现记录增加支付宝自动转账字段
ALTER TABLE `merchant_withdraw`
    ADD COLUMN `service_fee` DECIMAL(12,2) DEFAULT 0.00 COMMENT '手续费' AFTER `amount`,
    ADD COLUMN `amount_credited` DECIMAL(12,2) DEFAULT NULL COMMENT '实际到账金额' AFTER `service_fee`,
    ADD COLUMN `transfer_no` VARCHAR(64) DEFAULT NULL COMMENT '平台转账单号' AFTER `remark`,
    ADD COLUMN `alipay_order_id` VARCHAR(128) DEFAULT NULL COMMENT '支付宝转账订单号' AFTER `transfer_no`,
    ADD COLUMN `pay_fund_order_id` VARCHAR(128) DEFAULT NULL COMMENT '支付宝资金流水号' AFTER `alipay_order_id`,
    ADD COLUMN `transfer_status` VARCHAR(32) DEFAULT NULL COMMENT '支付宝转账状态' AFTER `pay_fund_order_id`,
    ADD COLUMN `transfer_msg` VARCHAR(255) DEFAULT NULL COMMENT '支付宝转账返回信息' AFTER `transfer_status`,
    ADD COLUMN `transfer_time` DATETIME DEFAULT NULL COMMENT '实际打款时间' AFTER `transfer_msg`,
    ADD UNIQUE KEY `uk_transfer_no` (`transfer_no`);
