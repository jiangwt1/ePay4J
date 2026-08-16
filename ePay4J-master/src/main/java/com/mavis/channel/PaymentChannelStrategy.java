package com.mavis.channel;

import com.mavis.entity.PayOrder;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentChannelStrategy {
    String getChannelCode();
    String pagePay(PayOrder order) throws Exception;
    String wapPay(PayOrder order) throws Exception;
    Object query(String outTradeNo) throws Exception;
    boolean refund(String outTradeNo, BigDecimal amount) throws Exception;
    boolean close(String outTradeNo) throws Exception;
    void handleNotify(Map<String, String> params);
}
