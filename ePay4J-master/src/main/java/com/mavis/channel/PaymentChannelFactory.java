package com.mavis.channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentChannelFactory {

    private final Map<String, PaymentChannelStrategy> strategyMap = new HashMap<>();

    @Autowired
    public PaymentChannelFactory(List<PaymentChannelStrategy> strategies) {
        for (PaymentChannelStrategy strategy : strategies) {
            strategyMap.put(strategy.getChannelCode(), strategy);
        }
    }

    public PaymentChannelStrategy getStrategy(String channelCode) {
        return strategyMap.get(channelCode);
    }

    public Map<String, PaymentChannelStrategy> getAllStrategies() {
        return strategyMap;
    }
}
