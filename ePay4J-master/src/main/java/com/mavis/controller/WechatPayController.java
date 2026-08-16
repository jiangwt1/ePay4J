package com.mavis.controller;

import com.alibaba.fastjson.JSONObject;
import com.mavis.config.WechatPayClientHolder;
import com.mavis.entity.PayOrder;
import com.mavis.mapper.PayOrderMapper;
import com.mavis.service.WechatPayService;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

@Slf4j
@RestController
@RequestMapping("/api/wechat")
public class WechatPayController {

    @Autowired
    private WechatPayService wechatPayService;

    @Autowired
    private WechatPayClientHolder wechatPayClientHolder;

    @Autowired
    private PayOrderMapper payOrderMapper;

    /**
     * 收银页轮询：订单是否已支付
     */
    @GetMapping("/query")
    public JSONObject query(@RequestParam String outTradeNo) {
        JSONObject result = new JSONObject();
        PayOrder order = payOrderMapper.selectList(
                new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getOutTradeNo, outTradeNo)
                        .last("LIMIT 1")
        ).stream().findFirst().orElse(null);
        result.put("paid", order != null && order.getStatus() == 1);
        return result;
    }

    /**
     * 支付结果回调（APIv3：平台证书验签 + AES-256-GCM 解密由 SDK 完成）
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            com.wechat.pay.java.core.notification.RequestParam requestParam =
                    new com.wechat.pay.java.core.notification.RequestParam.Builder()
                    .serialNumber(request.getHeader("Wechatpay-Serial"))
                    .nonce(request.getHeader("Wechatpay-Nonce"))
                    .timestamp(request.getHeader("Wechatpay-Timestamp"))
                    .signature(request.getHeader("Wechatpay-Signature"))
                    .signType(request.getHeader("Wechatpay-Signature-Type"))
                    .body(body.toString())
                    .build();

            Transaction transaction = wechatPayClientHolder.getNotificationParser()
                    .parse(requestParam, Transaction.class);
            log.info("微信异步通知验签成功: outTradeNo={}, tradeState={}",
                    transaction.getOutTradeNo(), transaction.getTradeState());

            wechatPayService.handleNotify(transaction);
            // 微信要求返回 200 + {"code":"SUCCESS"}
            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            log.error("微信异步通知处理异常", e);
            return "{\"code\":\"FAIL\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }
}
