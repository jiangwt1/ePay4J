package com.mavis.model.admin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private String outTradeNo;
    private String tradeNo;
    private String subject;
    private BigDecimal totalAmount;
    private Integer status;
    private String payType;
    private String buyerId;
    private Long pid;
    private String merchantName;
    private String notifyUrl;
    private String returnUrl;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime notifyTime;
}
