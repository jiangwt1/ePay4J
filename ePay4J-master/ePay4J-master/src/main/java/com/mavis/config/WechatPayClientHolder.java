package com.mavis.config;

import com.alibaba.fastjson.JSONObject;
import com.mavis.entity.PaymentChannelConfig;
import com.mavis.mapper.PaymentChannelConfigMapper;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WechatPayClientHolder {

    @Autowired
    private PaymentChannelConfigMapper channelConfigMapper;

    private volatile NativePayService nativePayService;
    private volatile RefundService refundService;
    private volatile NotificationParser notificationParser;
    private volatile JSONObject configJson;

    public NativePayService getNativePayService() {
        if (nativePayService == null) {
            refresh();
        }
        return nativePayService;
    }

    public RefundService getRefundService() {
        if (refundService == null) {
            refresh();
        }
        return refundService;
    }

    public NotificationParser getNotificationParser() {
        if (notificationParser == null) {
            refresh();
        }
        return notificationParser;
    }

    public JSONObject getConfig() {
        if (configJson == null) {
            refresh();
        }
        return configJson;
    }

    public synchronized void refresh() {
        PaymentChannelConfig config = channelConfigMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentChannelConfig>()
                        .eq(PaymentChannelConfig::getChannelCode, "WECHAT")
                        .eq(PaymentChannelConfig::getStatus, 1)
                        .eq(PaymentChannelConfig::getIsDefault, 1)
                        .last("LIMIT 1")
        );
        if (config == null || config.getStatus() != 1) {
            log.warn("微信支付通道未配置或已禁用");
            return;
        }

        JSONObject json = JSONObject.parseObject(config.getConfigData());
        this.configJson = json;
        buildServices(json);
        log.info("微信支付客户端已刷新: appId={}, mchId={}", json.getString("appId"), json.getString("mchId"));
    }

    private void buildServices(JSONObject json) {
        RSAAutoCertificateConfig config = new RSAAutoCertificateConfig.Builder()
                .merchantId(json.getString("mchId"))
                .privateKeyFromPath(json.getString("privateKeyPath"))
                .merchantSerialNumber(json.getString("merchantSerialNumber"))
                .apiV3Key(json.getString("apiV3Key"))
                .build();

        this.nativePayService = new NativePayService.Builder().config(config).build();
        this.refundService = new RefundService.Builder().config(config).build();
        this.notificationParser = new NotificationParser((NotificationConfig) config);
    }
}
