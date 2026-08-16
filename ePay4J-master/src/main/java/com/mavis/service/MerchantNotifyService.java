package com.mavis.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import com.mavis.common.constant.PaymentChannel;
import com.mavis.entity.Merchant;
import com.mavis.entity.PayOrder;
import com.mavis.mapper.MerchantMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MerchantNotifyService {

    @Autowired
    private MerchantMapper merchantMapper;

    public void notifyMerchant(PayOrder order) {
        Merchant merchant = merchantMapper.selectById(order.getPid());
        if (merchant == null) {
            log.warn("商户不存在: pid={}", order.getPid());
            return;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("pid", String.valueOf(merchant.getId()));
        params.put("trade_no", order.getOutTradeNo());
        params.put("out_trade_no", order.getOutTradeNo());
        params.put("type", PaymentChannel.channelCodeToEpayType(order.getPayType()));
        params.put("name", order.getSubject());
        params.put("money", order.getTotalAmount().toPlainString());
        params.put("trade_status", "TRADE_SUCCESS");

        String sign = buildSign(params, merchant.getMerchantKey());
        params.put("sign", sign);
        params.put("sign_type", "MD5");

        try {
            String result = HttpUtil.post(order.getNotifyUrl(), params, 5000);
            log.info("商户通知完成: outTradeNo={}, notifyUrl={}, result={}",
                    order.getOutTradeNo(), order.getNotifyUrl(), result);
        } catch (Exception e) {
            log.error("商户通知失败: outTradeNo={}, notifyUrl={}",
                    order.getOutTradeNo(), order.getNotifyUrl(), e);
        }
    }

    private String buildSign(Map<String, Object> params, String merchantKey) {
        String signedString = params.entrySet().stream()
                .filter(e -> e.getValue() != null && StrUtil.isNotBlank(e.getValue().toString()))
                .filter(e -> !"sign".equals(e.getKey()) && !"sign_type".equals(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())                .collect(Collectors.joining("&"));
        return DigestUtil.md5Hex(signedString + merchantKey);
    }
}
