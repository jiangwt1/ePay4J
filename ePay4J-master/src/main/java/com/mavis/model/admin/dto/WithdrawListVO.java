package com.mavis.model.admin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawListVO {
    private Long id;
    private Long merchantId;
    private String merchantName;
    private BigDecimal amount;
    private BigDecimal serviceFee;
    private BigDecimal amountCredited;
    private String alipayAccount;
    private Integer status;
    private String remark;
    private String transferNo;
    private String alipayOrderId;
    private String payFundOrderId;
    private String transferStatus;
    private String transferMsg;
    private LocalDateTime transferTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
