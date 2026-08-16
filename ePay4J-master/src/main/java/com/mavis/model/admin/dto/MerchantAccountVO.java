package com.mavis.model.admin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantAccountVO {
    private Long id;
    private Long merchantId;
    private String merchantName;
    private BigDecimal totalIncome;
    private BigDecimal availableBalance;
    private BigDecimal frozenBalance;
    private String alipayAccount;
    private String nickName;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
