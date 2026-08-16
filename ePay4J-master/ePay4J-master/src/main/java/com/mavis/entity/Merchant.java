package com.mavis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("merchant")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("merchant_key")
    private String merchantKey;

    private String name;

    private Integer status;

    private LocalDateTime createTime;

    private String userId;

    /**
     * 支付宝账号（提现账号）
     */
    private String alipayAccount;

    /**
     * 姓名
     */
    private String nickName;

    /**
     * 手机号
     */
    private String phone;
}
