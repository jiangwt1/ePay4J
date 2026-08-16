package com.mavis.model.admin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawDetailVO {
    private Long id;
    private Long merchantId;
    private BigDecimal amount;
    private BigDecimal serviceFee;
    private BigDecimal amountCredited;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String merchantName;
    private String alipayAccount;
    private String nickName;
    private String phone;
    private String transferNo;
    private String alipayOrderId;
    private String payFundOrderId;
    private String transferStatus;
    private String transferMsg;
    private LocalDateTime transferTime;
}
