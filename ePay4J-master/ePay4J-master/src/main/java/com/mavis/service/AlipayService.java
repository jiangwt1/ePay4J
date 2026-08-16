package com.mavis.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.mavis.config.AlipayClientHolder;
import com.mavis.entity.MerchantAccount;
import com.mavis.entity.PayOrder;
import com.mavis.mapper.MerchantAccountMapper;
import com.mavis.mapper.PayOrderMapper;
import com.mavis.model.admin.dto.AlipayTransferResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AlipayService {

    @Autowired
    private AlipayClientHolder alipayClientHolder;

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    private MerchantAccountMapper merchantAccountMapper;

    @Autowired
    private MerchantNotifyService merchantNotifyService;

    private AlipayClient alipayClient() {
        return alipayClientHolder.getClient();
    }

    private String notifyUrl() {
        return alipayClientHolder.getConfig().getString("notifyUrl");
    }

    private String returnUrl() {
        return alipayClientHolder.getConfig().getString("returnUrl");
    }

    public String pagePay(String subject, BigDecimal totalAmount) throws AlipayApiException {
        String outTradeNo = generateOrderNo();

        PayOrder order = new PayOrder();
        order.setOutTradeNo(outTradeNo);
        order.setSubject(subject);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setPayType("PAGE");
        payOrderMapper.insert(order);

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl());
        request.setReturnUrl(returnUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", totalAmount.toPlainString());
        bizContent.put("subject", subject);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toJSONString());

        AlipayTradePagePayResponse response = alipayClient().pageExecute(request);
        if (response.isSuccess()) {
            log.info("电脑网站支付下单成功: outTradeNo={}", outTradeNo);
            return response.getBody();
        } else {
            log.error("电脑网站支付下单失败: code={}, msg={}, subMsg={}",
                    response.getCode(), response.getMsg(), response.getSubMsg());
            throw new RuntimeException("下单失败: " + response.getSubMsg());
        }
    }

    public String wapPay(String subject, BigDecimal totalAmount) throws AlipayApiException {
        String outTradeNo = generateOrderNo();

        PayOrder order = new PayOrder();
        order.setOutTradeNo(outTradeNo);
        order.setSubject(subject);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setPayType("WAP");
        payOrderMapper.insert(order);

        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(notifyUrl());
        request.setReturnUrl(returnUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", totalAmount.toPlainString());
        bizContent.put("subject", subject);
        bizContent.put("product_code", "QUICK_WAP_WAY");
        request.setBizContent(bizContent.toJSONString());

        AlipayTradeWapPayResponse response = alipayClient().pageExecute(request);
        if (response.isSuccess()) {
            log.info("手机网站支付下单成功: outTradeNo={}", outTradeNo);
            return response.getBody();
        } else {
            log.error("手机网站支付下单失败: {}", response.getSubMsg());
            throw new RuntimeException("下单失败: " + response.getSubMsg());
        }
    }

    public String pagePayForOrder(PayOrder order) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl());
        request.setReturnUrl(returnUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", order.getOutTradeNo());
        bizContent.put("total_amount", order.getTotalAmount().toPlainString());
        bizContent.put("subject", order.getSubject());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toJSONString());

        AlipayTradePagePayResponse response = alipayClient().pageExecute(request);
        if (response.isSuccess()) {
            log.info("易支付代下单成功: outTradeNo={}", order.getOutTradeNo());
            return response.getBody();
        } else {
            log.error("易支付代下单失败: code={}, msg={}, subMsg={}",
                    response.getCode(), response.getMsg(), response.getSubMsg());
            throw new RuntimeException("下单失败: " + response.getSubMsg());
        }
    }

    public AlipayTradeQueryResponse query(String outTradeNo) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        request.setBizContent(bizContent.toJSONString());
        return alipayClient().execute(request);
    }

    public boolean refund(String outTradeNo, BigDecimal refundAmount) throws AlipayApiException {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("refund_amount", refundAmount.toPlainString());
        request.setBizContent(bizContent.toJSONString());

        AlipayTradeRefundResponse response = alipayClient().execute(request);
        if (response.isSuccess()) {
            PayOrder order = getOrderByOutTradeNo(outTradeNo);
            if (order != null) {
                order.setStatus(3);
                payOrderMapper.updateById(order);
            }
            log.info("退款成功: outTradeNo={}, refundAmount={}", outTradeNo, refundAmount);
            return true;
        }
        log.error("退款失败: {}", response.getSubMsg());
        return false;
    }

    public boolean close(String outTradeNo) throws AlipayApiException {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        request.setBizContent(bizContent.toJSONString());

        AlipayTradeCloseResponse response = alipayClient().execute(request);
        if (response.isSuccess() || "ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
            PayOrder order = getOrderByOutTradeNo(outTradeNo);
            if (order != null) {
                order.setStatus(2);
                payOrderMapper.updateById(order);
            }
            return true;
        }
        log.error("关闭订单失败: outTradeNo={}, subCode={}, subMsg={}", outTradeNo, response.getSubCode(), response.getSubMsg());
        return false;
    }

    public boolean isCertificateMode() {
        JSONObject config = alipayClientHolder.getConfig();
        return hasText(config.getString("appCertPath"))
                && hasText(config.getString("alipayPublicCertPath"))
                && hasText(config.getString("alipayRootCertPath"));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public AlipayTransferResult transferToAlipayAccount(String outBizNo, BigDecimal amount, String alipayAccount, String realName, String title) throws AlipayApiException {
        AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();

        JSONObject payeeInfo = new JSONObject();
        payeeInfo.put("identity", alipayAccount);
        payeeInfo.put("identity_type", "ALIPAY_LOGON_ID");
        payeeInfo.put("name", realName);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_biz_no", outBizNo);
        bizContent.put("trans_amount", amount.toPlainString());
        bizContent.put("product_code", "TRANS_ACCOUNT_NO_PWD");
        bizContent.put("biz_scene", "DIRECT_TRANSFER");
        bizContent.put("order_title", title);
        bizContent.put("payee_info", payeeInfo);
        request.setBizContent(bizContent.toJSONString());

        AlipayFundTransUniTransferResponse response = alipayClient().execute(request);
        if (response.isSuccess()) {
            log.info("支付宝转账成功: outBizNo={}, orderId={}, payFundOrderId={}, status={}",
                    outBizNo, response.getOrderId(), response.getPayFundOrderId(), response.getStatus());
            return AlipayTransferResult.success(
                    outBizNo,
                    response.getOrderId(),
                    response.getPayFundOrderId(),
                    response.getStatus(),
                    response.getMsg()
            );
        }

        String message = response.getSubMsg() != null ? response.getSubMsg() : response.getMsg();
        log.error("支付宝转账失败: outBizNo={}, code={}, subCode={}, message={}",
                outBizNo, response.getCode(), response.getSubCode(), message);
        return AlipayTransferResult.failure(outBizNo, response.getSubCode(), message);
    }

    public void handleNotify(String outTradeNo, String tradeNo, String tradeStatus, String buyerId) {
        PayOrder order = getOrderByOutTradeNo(outTradeNo);
        if (order == null) {
            log.warn("异步通知：订单不存在 outTradeNo={}", outTradeNo);
            return;
        }
        if (order.getStatus() == 1) {
            return;
        }

        order.setTradeNo(tradeNo);
        order.setBuyerId(buyerId);
        order.setNotifyTime(LocalDateTime.now());

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            order.setStatus(1);
            order.setPayTime(LocalDateTime.now());
            log.info("订单支付成功: outTradeNo={}, tradeNo={}", outTradeNo, tradeNo);

            // 更新商户余额
            if (order.getPid() != null) {
                updateMerchantAccount(order.getPid(), order.getTotalAmount());
            }
        } else {
            order.setStatus(2);
        }
        payOrderMapper.updateById(order);

        if (order.getPid() != null && order.getNotifyUrl() != null) {
            merchantNotifyService.notifyMerchant(order);
        }
    }

    private PayOrder getOrderByOutTradeNo(String outTradeNo) {
        return payOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getOutTradeNo, outTradeNo)
                        .last("LIMIT 1")
        ).stream().findFirst().orElse(null);
    }

    private String generateOrderNo() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    private void updateMerchantAccount(Long merchantId, BigDecimal amount) {
        MerchantAccount account = merchantAccountMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MerchantAccount>()
                        .eq(MerchantAccount::getMerchantId, merchantId)
        );
        if (account != null) {
            account.setTotalIncome(account.getTotalIncome().add(amount));
            account.setAvailableBalance(account.getAvailableBalance().add(amount));
            merchantAccountMapper.updateById(account);
            log.info("更新商户余额: merchantId={}, amount={}, newAvailable={}", merchantId, amount, account.getAvailableBalance());
        }
    }
}
