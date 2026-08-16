package com.mavis.channel.impl;

import com.mavis.channel.PaymentChannelStrategy;
import com.mavis.entity.PayOrder;
import com.mavis.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AlipayChannelStrategy implements PaymentChannelStrategy {

    @Autowired
    private AlipayService alipayService;

    @Override
    public String getChannelCode() {
        return "ALIPAY";
    }

    @Override
    public String pagePay(PayOrder order) throws Exception {
        return alipayService.pagePayForOrder(order);
    }

    @Override
    public String wapPay(PayOrder order) throws Exception {
        return alipayService.wapPay(order.getSubject(), order.getTotalAmount());
    }

    @Override
    public Object query(String outTradeNo) throws Exception {
        return alipayService.query(outTradeNo);
    }

    @Override
    public boolean refund(String outTradeNo, BigDecimal amount) throws Exception {
        return alipayService.refund(outTradeNo, amount);
    }

    @Override
    public boolean close(String outTradeNo) throws Exception {
        return alipayService.close(outTradeNo);
    }

    @Override
    public void handleNotify(Map<String, String> params) {
        alipayService.handleNotify(
                params.get("out_trade_no"),
                params.get("trade_no"),
                params.get("trade_status"),
                params.get("buyer_id")
        );
    }
}
