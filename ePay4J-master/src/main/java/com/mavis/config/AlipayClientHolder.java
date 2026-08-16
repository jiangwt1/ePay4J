package com.mavis.config;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.mavis.entity.PaymentChannelConfig;
import com.mavis.mapper.PaymentChannelConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlipayClientHolder {

    @Autowired
    private PaymentChannelConfigMapper channelConfigMapper;

    private volatile AlipayClient client;
    private volatile JSONObject configJson;

    public AlipayClient getClient() {
        if (client == null) {
            refresh();
        }
        return client;
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
                        .eq(PaymentChannelConfig::getChannelCode, "ALIPAY")
                        .eq(PaymentChannelConfig::getStatus, 1)
                        .eq(PaymentChannelConfig::getIsDefault, 1)
                        .last("LIMIT 1")
        );
        if (config == null || config.getStatus() != 1) {
            log.warn("支付宝通道未配置或已禁用");
            return;
        }

        JSONObject json = JSONObject.parseObject(config.getConfigData());
        this.configJson = json;
        this.client = buildClient(json);
        log.info("支付宝客户端已刷新: appId={}, mode={}", json.getString("appId"), isCertMode(json) ? "CERT" : "PUBLIC_KEY");
    }

    private AlipayClient buildClient(JSONObject json) {
        if (isCertMode(json)) {
            CertAlipayRequest certRequest = new CertAlipayRequest();
            certRequest.setServerUrl(json.getString("gatewayUrl"));
            certRequest.setAppId(json.getString("appId"));
            certRequest.setPrivateKey(json.getString("privateKey"));
            certRequest.setFormat(json.getString("format"));
            certRequest.setCharset(json.getString("charset"));
            certRequest.setSignType(json.getString("signType"));
            certRequest.setCertPath(json.getString("appCertPath"));
            certRequest.setAlipayPublicCertPath(json.getString("alipayPublicCertPath"));
            certRequest.setRootCertPath(json.getString("alipayRootCertPath"));
            try {
                return new DefaultAlipayClient(certRequest);
            } catch (AlipayApiException e) {
                throw new IllegalStateException("支付宝证书模式客户端初始化失败，请检查证书路径和证书内容", e);
            }
        }

        return new DefaultAlipayClient(
                json.getString("gatewayUrl"),
                json.getString("appId"),
                json.getString("privateKey"),
                json.getString("format"),
                json.getString("charset"),
                json.getString("publicKey"),
                json.getString("signType")
        );
    }

    private boolean isCertMode(JSONObject json) {
        return hasText(json.getString("appCertPath"))
                && hasText(json.getString("alipayPublicCertPath"))
                && hasText(json.getString("alipayRootCertPath"));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
