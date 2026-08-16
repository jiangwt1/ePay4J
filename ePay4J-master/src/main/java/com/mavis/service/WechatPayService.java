package com.mavis.service;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.fastjson.JSONObject;
import com.mavis.config.WechatPayClientHolder;
import com.mavis.entity.MerchantAccount;
import com.mavis.entity.PayOrder;
import com.mavis.mapper.MerchantAccountMapper;
import com.mavis.mapper.PayOrderMapper;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
public class WechatPayService {

    @Autowired
    private WechatPayClientHolder wechatPayClientHolder;

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    private MerchantAccountMapper merchantAccountMapper;

    @Autowired
    private MerchantNotifyService merchantNotifyService;

    /**
     * Native 扫码支付：预下单并返回二维码收银页 HTML（页面轮询 /api/wechat/query 等待支付结果）
     */
    public String nativePayForOrder(PayOrder order) {
        PrepayRequest request = new PrepayRequest();
        request.setDescription(order.getSubject());
        request.setOutTradeNo(order.getOutTradeNo());
        request.setNotifyUrl(wechatPayClientHolder.getConfig().getString("notifyUrl"));
        Amount amount = new Amount();
        amount.setTotal((int) toFen(order.getTotalAmount()));
        amount.setCurrency("CNY");
        request.setAmount(amount);

        PrepayResponse response = wechatPayClientHolder.getNativePayService().prepay(request);
        log.info("微信Native下单成功: outTradeNo={}, codeUrl={}", order.getOutTradeNo(), response.getCodeUrl());
        return buildCashierHtml(order, response.getCodeUrl());
    }

    public Transaction query(String outTradeNo) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setOutTradeNo(outTradeNo);
        request.setMchid(wechatPayClientHolder.getConfig().getString("mchId"));
        return wechatPayClientHolder.getNativePayService().queryOrderByOutTradeNo(request);
    }

    public boolean refund(String outTradeNo, BigDecimal refundAmount) {
        PayOrder order = getOrderByOutTradeNo(outTradeNo);
        if (order == null) {
            log.error("退款失败：订单不存在 outTradeNo={}", outTradeNo);
            return false;
        }

        CreateRequest request = new CreateRequest();
        request.setOutTradeNo(outTradeNo);
        request.setOutRefundNo(outTradeNo + "R");
        AmountReq amount = new AmountReq();
        amount.setRefund(toFen(refundAmount));
        amount.setTotal(toFen(order.getTotalAmount()));
        amount.setCurrency("CNY");
        request.setAmount(amount);
        request.setNotifyUrl(wechatPayClientHolder.getConfig().getString("notifyUrl"));

        wechatPayClientHolder.getRefundService().create(request);
        order.setStatus(3);
        payOrderMapper.updateById(order);
        log.info("退款成功: outTradeNo={}, refundAmount={}", outTradeNo, refundAmount);
        return true;
    }

    public void close(String outTradeNo) {
        CloseOrderRequest request = new CloseOrderRequest();
        request.setOutTradeNo(outTradeNo);
        request.setMchid(wechatPayClientHolder.getConfig().getString("mchId"));
        wechatPayClientHolder.getNativePayService().closeOrder(request);
        PayOrder order = getOrderByOutTradeNo(outTradeNo);
        if (order != null && order.getStatus() == 0) {
            order.setStatus(2);
            payOrderMapper.updateById(order);
        }
    }

    /**
     * 支付结果回调处理（APIv3 通知已在 Controller 完成验签解密）
     */
    public void handleNotify(Transaction transaction) {
        String outTradeNo = transaction.getOutTradeNo();
        PayOrder order = getOrderByOutTradeNo(outTradeNo);
        if (order == null) {
            log.warn("微信异步通知：订单不存在 outTradeNo={}", outTradeNo);
            return;
        }
        if (order.getStatus() == 1) {
            return;
        }

        order.setTradeNo(transaction.getTransactionId());
        if (transaction.getPayer() != null) {
            order.setBuyerId(transaction.getPayer().getOpenid());
        }
        order.setNotifyTime(LocalDateTime.now());

        if (Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
            order.setStatus(1);
            order.setPayTime(LocalDateTime.now());
            log.info("订单支付成功: outTradeNo={}, transactionId={}", outTradeNo, transaction.getTransactionId());

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

    private String buildCashierHtml(PayOrder order, String codeUrl) {
        String qrBase64 = QrCodeUtil.generateAsBase64(codeUrl, new QrConfig(220, 220), ImgUtil.IMAGE_TYPE_PNG);
        String returnUrl = order.getReturnUrl();
        String redirectJs;
        if (returnUrl != null && !returnUrl.trim().isEmpty()) {
            String sep = returnUrl.contains("?") ? "&" : "?";
            String target = returnUrl + sep + "out_trade_no=" + order.getOutTradeNo()
                    + "&trade_no=&total_amount=" + order.getTotalAmount().toPlainString();
            redirectJs = "window.location.href='" + target + "'";
        } else {
            redirectJs = "document.getElementById('tip').textContent='支付成功，可关闭页面'";
        }

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>微信扫码支付</title>"
                + "<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;"
                + "background:#f5f7fa;display:flex;justify-content:center;align-items:center;min-height:100vh;padding:20px}"
                + ".card{background:#fff;border-radius:12px;padding:32px;max-width:360px;width:100%;box-shadow:0 2px 12px rgba(0,0,0,0.08);text-align:center}"
                + ".title{font-size:18px;font-weight:600;color:#303133;margin-bottom:8px}"
                + ".amount{font-size:28px;font-weight:700;color:#07C160;margin-bottom:20px}"
                + ".qr{display:flex;justify-content:center;margin-bottom:16px}"
                + ".order-info{font-size:13px;color:#909399;margin-bottom:20px;word-break:break-all}"
                + ".tip{font-size:14px;color:#67C23A;height:20px}"
                + "</style></head><body>"
                + "<div class=\"card\">"
                + "<div class=\"title\">" + escapeHtml(order.getSubject()) + "</div>"
                + "<div class=\"amount\">¥" + order.getTotalAmount().toPlainString() + "</div>"
                + "<div class=\"qr\"><img src=\"" + qrBase64 + "\" alt=\"微信支付二维码\" width=\"220\" height=\"220\"/></div>"
                + "<div class=\"order-info\">订单号: " + escapeHtml(order.getOutTradeNo()) + "</div>"
                + "<div class=\"tip\" id=\"tip\">请使用微信扫一扫完成支付</div>"
                + "</div>"
                + "<script>"
                + "var t=setInterval(function(){"
                + "fetch('/api/wechat/query?outTradeNo=" + order.getOutTradeNo() + "')"
                + ".then(function(r){return r.json()})"
                + ".then(function(d){if(d.paid){clearInterval(t);" + redirectJs + "}})"
                + ".catch(function(){});"
                + "},2000);"
                + "</script></body></html>";
    }

    private PayOrder getOrderByOutTradeNo(String outTradeNo) {
        return payOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getOutTradeNo, outTradeNo)
                        .last("LIMIT 1")
        ).stream().findFirst().orElse(null);
    }

    private long toFen(BigDecimal amount) {
        return amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
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
