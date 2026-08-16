package com.mavis.common.constant;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public enum PaymentChannel {
    ALIPAY("alipay", "支付宝", "#1677FF"),
    WECHAT("wxpay", "微信支付", "#07C160"),
    PAYPAL("paypal", "PayPal", "#003087");

    private final String epayType;
    private final String displayName;
    private final String color;

    PaymentChannel(String epayType, String displayName, String color) {
        this.epayType = epayType;
        this.displayName = displayName;
        this.color = color;
    }

    public String getEpayType() { return epayType; }
    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }

    public static PaymentChannel fromCode(String channelCode) {
        return Arrays.stream(values())
                .filter(c -> c.name().equals(channelCode))
                .findFirst().orElse(null);
    }

    public static PaymentChannel fromEpayType(String epayType) {
        return Arrays.stream(values())
                .filter(c -> c.epayType.equals(epayType))
                .findFirst().orElse(null);
    }

    public static String channelCodeToEpayType(String channelCode) {
        PaymentChannel ch = fromCode(channelCode);
        return ch != null ? ch.epayType : channelCode.toLowerCase();
    }

    public static String epayTypeToChannelCode(String epayType) {
        PaymentChannel ch = fromEpayType(epayType);
        return ch != null ? ch.name() : epayType.toUpperCase();
    }

    public static String getDisplayNameByCode(String channelCode) {
        PaymentChannel ch = fromCode(channelCode);
        return ch != null ? ch.displayName : channelCode;
    }

    public static Map<String, String> allTypes() {
        Map<String, String> map = new LinkedHashMap<>();
        for (PaymentChannel ch : values()) {
            map.put(ch.epayType, ch.displayName);
        }
        return map;
    }
}
