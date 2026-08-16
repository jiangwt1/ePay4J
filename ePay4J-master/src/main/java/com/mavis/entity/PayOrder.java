package com.mavis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pay_order")
public class PayOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商户订单号 */
    private String outTradeNo;

    /** 支付宝交易号 */
    private String tradeNo;

    /** 订单标题 */
    private String subject;

    /** 订单金额（元） */
    private BigDecimal totalAmount;

    /** 订单状态：0-待支付 1-已支付 2-已关闭 3-已退款 */
    private Integer status;

    /** 支付方式：PAGE-电脑网站 WAP-手机网站 */
    private String payType;

    /** 买家支付宝用户ID */
    private String buyerId;

    /** 商户ID（易支付订单时有值） */
    private Long pid;

    /** 商户异步通知地址 */
    private String notifyUrl;

    /** 商户同步跳转地址 */
    private String returnUrl;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 通知时间 */
    private LocalDateTime notifyTime;
}
