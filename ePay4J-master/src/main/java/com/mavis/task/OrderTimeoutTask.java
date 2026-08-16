package com.mavis.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mavis.entity.PayOrder;
import com.mavis.entity.SystemConfig;
import com.mavis.mapper.PayOrderMapper;
import com.mavis.mapper.SystemConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTimeoutTask {

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Scheduled(fixedRate = 60000)
    public void closeExpiredOrders() {
        int timeoutMinutes = getTimeoutMinutes();
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<PayOrder> expired = payOrderMapper.selectList(
                new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getStatus, 0)
                        .le(PayOrder::getCreateTime, deadline)
        );
        if (!expired.isEmpty()) {
            for (PayOrder order : expired) {
                order.setStatus(2);
                payOrderMapper.updateById(order);
            }
            log.info("自动关闭 {} 笔过期订单 (超时{}分钟)", expired.size(), timeoutMinutes);
        }
    }

    private int getTimeoutMinutes() {
        try {
            SystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<SystemConfig>()
                            .eq(SystemConfig::getConfigKey, "order_timeout_minutes")
            );
            if (config != null && config.getConfigValue() != null) {
                return Math.max(Integer.parseInt(config.getConfigValue()), 1);
            }
        } catch (Exception ignored) {}
        return 30;
    }
}
