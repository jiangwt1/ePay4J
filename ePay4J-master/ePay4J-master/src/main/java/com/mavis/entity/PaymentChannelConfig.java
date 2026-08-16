package com.mavis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_channel_config")
public class PaymentChannelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String channelCode;
    private String channelName;
    private String configData;
    private Integer status;
    private Integer isDefault;
    private Integer sortOrder;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
