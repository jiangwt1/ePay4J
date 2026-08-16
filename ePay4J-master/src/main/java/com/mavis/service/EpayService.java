package com.mavis.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mavis.model.admin.service.AdminChannelService;
import com.mavis.common.constant.PaymentChannel;
import com.mavis.entity.Merchant;
import com.mavis.entity.PayOrder;
import com.mavis.entity.PaymentChannelConfig;
import com.mavis.mapper.MerchantMapper;
import com.mavis.mapper.PayOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EpayService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private WechatPayService wechatPayService;

    @Autowired
    private AdminChannelService adminChannelService;

    // ========== 页面跳转支付 ==========

    public String submitPay(Map<String, String> params) {
        try {
            Merchant merchant = validateAndVerify(params);
            String outTradeNo = params.get("out_trade_no");
            String name = params.get("name");
            BigDecimal money = new BigDecimal(params.get("money"));
            String type = params.get("type");

            if (StrUtil.isNotBlank(type)) {
                return routePayment(merchant, outTradeNo, name, money, type, params);
            }

            return buildSelectionPage(merchant, outTradeNo, name, money, params);
        } catch (Exception e) {
            log.error("易支付下单异常", e);
            return buildErrorHtml(e.getMessage());
        }
    }

    private String routePayment(Merchant merchant, String outTradeNo, String name,
                                BigDecimal money, String type, Map<String, String> params) throws Exception {
        String channelCode = PaymentChannel.epayTypeToChannelCode(type);

        PaymentChannelConfig channel = findEnabledDefaultChannel(channelCode);
        if (channel == null) {
            return buildErrorHtml("支付通道 [" + type + "] 未启用或未配置");
        }

        PayOrder order = new PayOrder();
        order.setOutTradeNo(outTradeNo);
        order.setSubject(name);
        order.setTotalAmount(money);
        order.setStatus(0);
        order.setPayType(type.toUpperCase());
        order.setPid(merchant.getId());
        order.setNotifyUrl(params.get("notify_url"));
        order.setReturnUrl(params.get("return_url"));
        payOrderMapper.insert(order);

        switch (channelCode) {
            case "ALIPAY":
                return alipayService.pagePayForOrder(order);
            case "WECHAT":
                return wechatPayService.nativePayForOrder(order);
            default:
                return buildErrorHtml("支付通道 [" + type + "] 暂未实现");
        }
    }

    private String buildSelectionPage(Merchant merchant, String outTradeNo, String name,
                                      BigDecimal money, Map<String, String> params) {
        List<PaymentChannelConfig> channels = adminChannelService.getEnabledDefaultChannels();

        if (channels.isEmpty()) {
            return buildErrorHtml("暂无可用支付通道");
        }

        if (channels.size() == 1) {
            try {
                PaymentChannelConfig ch = channels.get(0);
                String epayType = PaymentChannel.channelCodeToEpayType(ch.getChannelCode());
                params.put("type", epayType);
                return routePayment(merchant, outTradeNo, name, money, epayType, params);
            } catch (Exception e) {
                return buildErrorHtml(e.getMessage());
            }
        }

        StringBuilder optionsHtml = new StringBuilder();
        for (PaymentChannelConfig ch : channels) {
            PaymentChannel pc = PaymentChannel.fromCode(ch.getChannelCode());
            String epayType = PaymentChannel.channelCodeToEpayType(ch.getChannelCode());
            String displayName = ch.getChannelName();
            String color = pc != null ? pc.getColor() : "#409EFF";

            optionsHtml.append("<a href=\"?pid=").append(params.get("pid"))
                    .append("&type=").append(epayType)
                    .append("&out_trade_no=").append(outTradeNo)
                    .append("&notify_url=").append(params.getOrDefault("notify_url", ""))
                    .append("&return_url=").append(params.getOrDefault("return_url", ""))
                    .append("&name=").append(name)
                    .append("&money=").append(money.toPlainString())
                    .append("&sign=").append(params.getOrDefault("sign", ""))
                    .append("&sign_type=").append(params.getOrDefault("sign_type", ""))
                    .append("\" class=\"pay-option\" style=\"border-color:").append(color).append("\">")
                    .append("<span class=\"pay-dot\" style=\"background:").append(color).append("\"></span>")
                    .append("<span>").append(displayName).append("</span>")
                    .append("</a>");
        }

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>选择支付方式</title>"
                + "<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;"
                + "background:#f5f7fa;display:flex;justify-content:center;align-items:center;min-height:100vh;padding:20px}"
                + ".card{background:#fff;border-radius:12px;padding:32px;max-width:400px;width:100%;box-shadow:0 2px 12px rgba(0,0,0,0.08)}"
                + ".title{text-align:center;font-size:18px;font-weight:600;color:#303133;margin-bottom:8px}"
                + ".amount{text-align:center;font-size:28px;font-weight:700;color:#409EFF;margin-bottom:24px}"
                + ".pay-option{display:flex;align-items:center;gap:12px;padding:16px;border:2px solid #e4e7ed;border-radius:8px;"
                + "margin-bottom:12px;text-decoration:none;color:#303133;font-size:16px;font-weight:500;transition:all .2s}"
                + ".pay-option:hover{transform:translateY(-1px);box-shadow:0 4px 12px rgba(0,0,0,0.1)}"
                + ".pay-dot{width:10px;height:10px;border-radius:50%;flex-shrink:0}"
                + ".order-info{text-align:center;font-size:13px;color:#909399;margin-bottom:20px}"
                + "</style></head><body>"
                + "<div class=\"card\">"
                + "<div class=\"title\">" + escapeHtml(name) + "</div>"
                + "<div class=\"amount\">¥" + money.toPlainString() + "</div>"
                + "<div class=\"order-info\">订单号: " + escapeHtml(outTradeNo) + "</div>"
                + optionsHtml.toString()
                + "</div></body></html>";
    }

    private PaymentChannelConfig findEnabledDefaultChannel(String channelCode) {
        List<PaymentChannelConfig> channels = adminChannelService.getEnabledDefaultChannels();
        return channels.stream()
                .filter(c -> channelCode.equals(c.getChannelCode()))
                .findFirst().orElse(null);
    }

    // ========== API接口支付 ==========

    public JSONObject apiPay(Map<String, String> params) {
        JSONObject result = new JSONObject();
        try {
            Merchant merchant = validateAndVerify(params);
            String outTradeNo = params.get("out_trade_no");
            String name = params.get("name");
            BigDecimal money = new BigDecimal(params.get("money"));
            String type = params.get("type");

            if (StrUtil.isBlank(type)) {
                List<PaymentChannelConfig> channels = adminChannelService.getEnabledDefaultChannels();
                if (channels.isEmpty()) {
                    result.put("code", -1);
                    result.put("msg", "暂无可用支付通道");
                    return result;
                }
                type = PaymentChannel.channelCodeToEpayType(channels.get(0).getChannelCode());
            }

            PayOrder order = new PayOrder();
            order.setOutTradeNo(outTradeNo);
            order.setSubject(name);
            order.setTotalAmount(money);
            order.setStatus(0);
            order.setPayType(type.toUpperCase());
            order.setPid(merchant.getId());
            order.setNotifyUrl(params.get("notify_url"));
            order.setReturnUrl(params.get("return_url"));
            payOrderMapper.insert(order);

            String payUrl = "/submit.php?type=" + type + "&" + params.entrySet().stream()
                    .filter(e -> StrUtil.isNotBlank(e.getValue()))
                    .filter(e -> !"type".equals(e.getKey()))
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));

            result.put("code", 1);
            result.put("trade_no", outTradeNo);
            result.put("type", type);
            result.put("payurl", payUrl);
            return result;
        } catch (Exception e) {
            log.error("易支付API下单异常", e);
            result.put("code", -1);
            result.put("msg", e.getMessage());
            return result;
        }
    }

    // ========== API接口 ==========

    public JSONObject handleApiRequest(String act, Map<String, String> params) {
        switch (act) {
            case "query":
                return queryMerchant(params);
            case "order":
                return queryOrder(params);
            case "orders":
                return queryOrders(params);
            case "refund":
                return refundOrder(params);
            case "types":
                return listPayTypes(params);
            default:
                JSONObject result = new JSONObject();
                result.put("code", -1);
                result.put("msg", "未知操作类型");
                return result;
        }
    }

    private JSONObject listPayTypes(Map<String, String> params) {
        JSONObject result = new JSONObject();
        try {
            validateByKey(params);
            List<PaymentChannelConfig> channels = adminChannelService.getEnabledDefaultChannels();
            List<JSONObject> types = channels.stream().map(ch -> {
                JSONObject item = new JSONObject();
                String epayType = PaymentChannel.channelCodeToEpayType(ch.getChannelCode());
                item.put("type", epayType);
                item.put("name", ch.getChannelName());
                item.put("code", ch.getChannelCode());
                return item;
            }).collect(Collectors.toList());
            result.put("code", 1);
            result.put("data", types);
        } catch (Exception e) {
            result.put("code", -1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    private JSONObject queryMerchant(Map<String, String> params) {
        JSONObject result = new JSONObject();
        Merchant merchant = validateByKey(params);
        if (merchant == null) {
            result.put("code", -1);
            result.put("msg", "商户ID或密钥错误");
            return result;
        }
        result.put("code", 1);
        result.put("pid", merchant.getId());
        result.put("key", merchant.getMerchantKey());
        result.put("active", merchant.getStatus());
        result.put("money", "0.00");
        return result;
    }

    private JSONObject queryOrder(Map<String, String> params) {
        JSONObject result = new JSONObject();
        Merchant merchant = validateByKey(params);
        if (merchant == null) {
            result.put("code", -1);
            result.put("msg", "商户ID或密钥错误");
            return result;
        }

        String tradeNo = params.get("trade_no");
        String outTradeNo = params.get("out_trade_no");

        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getPid, merchant.getId());
        if (StrUtil.isNotBlank(tradeNo)) {
            wrapper.eq(PayOrder::getOutTradeNo, tradeNo);
        } else if (StrUtil.isNotBlank(outTradeNo)) {
            wrapper.eq(PayOrder::getOutTradeNo, outTradeNo);
        } else {
            result.put("code", -1);
            result.put("msg", "请传入订单号");
            return result;
        }
        wrapper.last("LIMIT 1");

        PayOrder order = payOrderMapper.selectList(wrapper).stream().findFirst().orElse(null);
        if (order == null) {
            result.put("code", -1);
            result.put("msg", "订单不存在");
            return result;
        }

        result.put("code", 1);
        result.put("msg", "查询订单号成功！");
        result.put("trade_no", order.getOutTradeNo());
        result.put("out_trade_no", order.getOutTradeNo());
        result.put("type", PaymentChannel.channelCodeToEpayType(order.getPayType()));
        result.put("pid", order.getPid());
        result.put("name", order.getSubject());
        result.put("money", order.getTotalAmount().toPlainString());
        result.put("status", order.getStatus() == 1 ? 1 : 0);
        result.put("addtime", order.getCreateTime() != null ? order.getCreateTime().toString() : "");
        result.put("endtime", order.getPayTime() != null ? order.getPayTime().toString() : "");
        return result;
    }

    private JSONObject queryOrders(Map<String, String> params) {
        JSONObject result = new JSONObject();
        Merchant merchant = validateByKey(params);
        if (merchant == null) {
            result.put("code", -1);
            result.put("msg", "商户ID或密钥错误");
            return result;
        }

        int limit = 20;
        int page = 1;
        try { limit = Math.min(Integer.parseInt(params.getOrDefault("limit", "20")), 50); } catch (NumberFormatException ignored) {}
        try { page = Integer.parseInt(params.getOrDefault("page", "1")); } catch (NumberFormatException ignored) {}

        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getPid, merchant.getId())
                .orderByDesc(PayOrder::getId)
                .last("LIMIT " + limit + " OFFSET " + (page - 1) * limit);

        List<PayOrder> orders = payOrderMapper.selectList(wrapper);

        result.put("code", 1);
        result.put("msg", "查询成功");
        result.put("data", orders.stream().map(o -> {
            JSONObject item = new JSONObject();
            item.put("trade_no", o.getOutTradeNo());
            item.put("out_trade_no", o.getOutTradeNo());
            item.put("type", PaymentChannel.channelCodeToEpayType(o.getPayType()));
            item.put("name", o.getSubject());
            item.put("money", o.getTotalAmount().toPlainString());
            item.put("status", o.getStatus() == 1 ? 1 : 0);
            item.put("addtime", o.getCreateTime() != null ? o.getCreateTime().toString() : "");
            return item;
        }).collect(Collectors.toList()));
        return result;
    }

    private JSONObject refundOrder(Map<String, String> params) {
        JSONObject result = new JSONObject();
        Merchant merchant = validateByKey(params);
        if (merchant == null) {
            result.put("code", -1);
            result.put("msg", "商户ID或密钥错误");
            return result;
        }

        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String money = params.get("money");
        if (StrUtil.isBlank(money)) {
            result.put("code", -1);
            result.put("msg", "缺少退款金额");
            return result;
        }

        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getPid, merchant.getId());
        if (StrUtil.isNotBlank(tradeNo)) {
            wrapper.eq(PayOrder::getOutTradeNo, tradeNo);
        } else if (StrUtil.isNotBlank(outTradeNo)) {
            wrapper.eq(PayOrder::getOutTradeNo, outTradeNo);
        } else {
            result.put("code", -1);
            result.put("msg", "请传入订单号");
            return result;
        }
        wrapper.last("LIMIT 1");

        PayOrder order = payOrderMapper.selectList(wrapper).stream().findFirst().orElse(null);
        if (order == null) {
            result.put("code", -1);
            result.put("msg", "订单不存在");
            return result;
        }

        try {
            boolean success = alipayService.refund(order.getOutTradeNo(), new BigDecimal(money));
            result.put("code", success ? 1 : -1);
            result.put("msg", success ? "退款成功" : "退款失败");
        } catch (AlipayApiException e) {
            result.put("code", -1);
            result.put("msg", "退款异常: " + e.getMessage());
        }
        return result;
    }

    // ========== 签名相关 ==========

    private Merchant validateAndVerify(Map<String, String> params) {
        String pidStr = params.get("pid");
        if (StrUtil.isBlank(pidStr)) {
            throw new RuntimeException("缺少商户ID");
        }

        Merchant merchant = merchantMapper.selectById(Long.parseLong(pidStr));
        if (merchant == null || merchant.getStatus() != 1) {
            throw new RuntimeException("商户不存在或已禁用");
        }

        String sign = params.get("sign");
        if (StrUtil.isBlank(sign)) {
            throw new RuntimeException("缺少签名");
        }

        String expectedSign = buildSign(params, merchant.getMerchantKey());
        if (!sign.equalsIgnoreCase(expectedSign)) {
            throw new RuntimeException("签名验证失败");
        }

        return merchant;
    }

    private Merchant validateByKey(Map<String, String> params) {
        String pidStr = params.get("pid");
        String key = params.get("key");
        if (StrUtil.isBlank(pidStr) || StrUtil.isBlank(key)) {
            return null;
        }
        Merchant merchant = merchantMapper.selectById(Long.parseLong(pidStr));
        if (merchant == null || merchant.getStatus() != 1) {
            return null;
        }
        if (!key.equals(merchant.getMerchantKey())) {
            return null;
        }
        return merchant;
    }

    private String buildSign(Map<String, String> params, String merchantKey) {
        String signedString = params.entrySet().stream()
                .filter(e -> StrUtil.isNotBlank(e.getValue()))
                .filter(e -> !"sign".equals(e.getKey()) && !"sign_type".equals(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        return DigestUtil.md5Hex(signedString + merchantKey);
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String buildErrorHtml(String msg) {
        return "<html><body><h3>" + escapeHtml(msg) + "</h3></body></html>";
    }
}
