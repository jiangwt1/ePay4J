package com.mavis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant_withdraw")
public class MerchantWithdraw {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 提现金额
     */
    private BigDecimal amount;

    /**
     * 手续费
     */
    private BigDecimal serviceFee;

    /**
     * 实际到账金额
     */
    private BigDecimal amountCredited;

    /**
     * 任务状态：0-待处理(处理中) 1-已提现(已完成) 2-已拒绝
     */
    private Integer status;

    /**
     * 备注/拒绝原因
     */
    private String remark;

    /**
     * 平台转账单号
     */
    private String transferNo;

    /**
     * 支付宝转账订单号
     */
    private String alipayOrderId;

    /**
     * 支付宝资金流水号
     */
    private String payFundOrderId;

    /**
     * 支付宝转账状态
     */
    private String transferStatus;

    /**
     * 支付宝转账返回信息
     */
    private String transferMsg;

    /**
     * 实际打款时间
     */
    private LocalDateTime transferTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
