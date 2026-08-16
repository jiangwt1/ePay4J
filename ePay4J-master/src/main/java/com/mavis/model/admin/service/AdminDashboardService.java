package com.mavis.model.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mavis.model.admin.dto.DashboardStatsVO;
import com.mavis.entity.PayOrder;
import com.mavis.mapper.MerchantMapper;
import com.mavis.mapper.PayOrderMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AdminDashboardService {

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    public DashboardStatsVO getStats() {
        DashboardStatsVO vo = new DashboardStatsVO();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 今日收入
        LambdaQueryWrapper<PayOrder> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.eq(PayOrder::getStatus, 1).ge(PayOrder::getPayTime, todayStart);
        List<PayOrder> paidOrders = payOrderMapper.selectList(paidWrapper);
        BigDecimal todayRevenue = paidOrders.stream()
                .map(PayOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodayRevenue(todayRevenue.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());

        // 今日订单数
        LambdaQueryWrapper<PayOrder> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(PayOrder::getCreateTime, todayStart);
        vo.setTodayOrders(Math.toIntExact(payOrderMapper.selectCount(todayWrapper)));

        // 成功率
        long totalOrders = payOrderMapper.selectCount(null);
        LambdaQueryWrapper<PayOrder> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.eq(PayOrder::getStatus, 1);
        long successOrders = payOrderMapper.selectCount(successWrapper);
        vo.setSuccessRate(totalOrders > 0 ? String.format("%.1f%%", successOrders * 100.0 / totalOrders) : "0%");

        // 商户总数
        vo.setTotalMerchants(Math.toIntExact(merchantMapper.selectCount(null)));

        return vo;
    }

    public List<Map<String, Object>> getRevenueChart(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PayOrder::getStatus, 1)
                    .ge(PayOrder::getPayTime, dayStart)
                    .le(PayOrder::getPayTime, dayEnd);
            List<PayOrder> orders = payOrderMapper.selectList(wrapper);

            BigDecimal revenue = orders.stream()
                    .map(PayOrder::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("revenue", revenue.doubleValue());
            item.put("count", orders.size());
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getOrderStatusChart() {
        List<Map<String, Object>> result = new ArrayList<>();
        int[] statuses = {0, 1, 2, 3};
        for (int status : statuses) {
            LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PayOrder::getStatus, status);
            long count = payOrderMapper.selectCount(wrapper);
            Map<String, Object> item = new HashMap<>();
            item.put("status", String.valueOf(status));
            item.put("count", count);
            result.add(item);
        }
        return result;
    }

    public List<PayOrder> getRecentOrders(int limit) {
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PayOrder::getCreateTime).last("LIMIT " + limit);
        return payOrderMapper.selectList(wrapper);
    }
}
