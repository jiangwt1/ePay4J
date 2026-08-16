package com.mavis.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.mavis.config.AlipayClientHolder;
import com.mavis.entity.PayOrder;
import com.mavis.mapper.PayOrderMapper;
import com.mavis.service.AlipayService;
import com.mavis.util.PayUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private AlipayClientHolder alipayClientHolder;

    @Autowired
    private PayOrderMapper payOrderMapper;

    @GetMapping("/pagePay")
    public String pagePay(@RequestParam String subject,
                          @RequestParam String totalAmount) {
        try {
            return alipayService.pagePay(subject, new java.math.BigDecimal(totalAmount));
        } catch (AlipayApiException e) {
            log.error("电脑网站支付异常", e);
            return "支付发起失败: " + e.getMessage();
        }
    }

    @GetMapping("/wapPay")
    public String wapPay(@RequestParam String subject,
                         @RequestParam String totalAmount) {
        try {
            return alipayService.wapPay(subject, new java.math.BigDecimal(totalAmount));
        } catch (AlipayApiException e) {
            log.error("手机网站支付异常", e);
            return "支付发起失败: " + e.getMessage();
        }
    }

    @GetMapping("/query")
    public JSONObject query(@RequestParam String outTradeNo) {
        try {
            com.alipay.api.response.AlipayTradeQueryResponse response = alipayService.query(outTradeNo);
            JSONObject result = new JSONObject();
            result.put("success", response.isSuccess());
            result.put("tradeStatus", response.getTradeStatus());
            result.put("tradeNo", response.getTradeNo());
            result.put("outTradeNo", response.getOutTradeNo());
            result.put("totalAmount", response.getTotalAmount());
            result.put("buyerId", response.getBuyerUserId());
            return result;
        } catch (AlipayApiException e) {
            log.error("查询订单异常", e);
            JSONObject result = new JSONObject();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    @PostMapping("/refund")
    public JSONObject refund(@RequestBody JSONObject params) {
        try {
            String outTradeNo = params.getString("outTradeNo");
            String refundAmount = params.getString("refundAmount");
            boolean success = alipayService.refund(outTradeNo, new java.math.BigDecimal(refundAmount));
            JSONObject result = new JSONObject();
            result.put("success", success);
            return result;
        } catch (AlipayApiException e) {
            log.error("退款异常", e);
            JSONObject result = new JSONObject();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = PayUtils.extractParams(request);
        log.info("收到支付宝异步通知: {}", params);

        try {
            JSONObject config = alipayClientHolder.getConfig();
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    config.getString("publicKey"),
                    config.getString("charset"),
                    config.getString("signType")
            );

            if (!signVerified) {
                log.warn("异步通知验签失败");
                return "failure";
            }

            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");
            String buyerId = params.get("buyer_id");

            log.info("异步通知验签成功: outTradeNo={}, tradeNo={}, status={}", outTradeNo, tradeNo, tradeStatus);

            alipayService.handleNotify(outTradeNo, tradeNo, tradeStatus, buyerId);

            return "success";
        } catch (AlipayApiException e) {
            log.error("异步通知处理异常", e);
            return "failure";
        }
    }

    @GetMapping("/return")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = PayUtils.extractParams(request);

        try {
            JSONObject config = alipayClientHolder.getConfig();
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    config.getString("publicKey"),
                    config.getString("charset"),
                    config.getString("signType")
            );

            if (signVerified) {
                String outTradeNo = params.get("out_trade_no");
                log.info("同步跳转验签成功: outTradeNo={}", outTradeNo);

                PayOrder order = payOrderMapper.selectList(
                        new LambdaQueryWrapper<PayOrder>()
                                .eq(PayOrder::getOutTradeNo, outTradeNo)
                                .last("LIMIT 1")
                ).stream().findFirst().orElse(null);

                if (order != null && order.getReturnUrl() != null) {
                    String redirectUrl = order.getReturnUrl();
                    if (!redirectUrl.contains("?")) {
                        redirectUrl += "?";
                    } else {
                        redirectUrl += "&";
                    }
                    redirectUrl += "out_trade_no=" + outTradeNo
                            + "&trade_no=" + params.getOrDefault("trade_no", "")
                            + "&total_amount=" + params.getOrDefault("total_amount", "");
                    response.sendRedirect(redirectUrl);
                    return;
                }

                response.setContentType("application/json;charset=UTF-8");
                JSONObject result = new JSONObject();
                result.put("success", true);
                result.put("outTradeNo", outTradeNo);
                result.put("tradeNo", params.get("trade_no"));
                result.put("totalAmount", params.get("total_amount"));
                response.getWriter().write(result.toJSONString());
            } else {
                response.setContentType("application/json;charset=UTF-8");
                JSONObject result = new JSONObject();
                result.put("success", false);
                result.put("message", "验签失败");
                response.getWriter().write(result.toJSONString());
            }
        } catch (AlipayApiException e) {
            log.error("同步跳转验签异常", e);
            response.setContentType("application/json;charset=UTF-8");
            JSONObject result = new JSONObject();
            result.put("success", false);
            result.put("message", e.getMessage());
            response.getWriter().write(result.toJSONString());
        }
    }
}
