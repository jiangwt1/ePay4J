package com.mavis.model.admin.controller;

import com.mavis.model.admin.dto.OrderVO;
import com.mavis.model.admin.dto.PageResult;
import com.mavis.model.admin.dto.Result;
import com.mavis.model.admin.service.AdminOrderService;
import com.mavis.entity.PayOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    @Autowired
    private com.mavis.mapper.SystemConfigMapper systemConfigMapper;

    @GetMapping("/timeout")
    public Result<Integer> getTimeout() {
        com.mavis.entity.SystemConfig config = systemConfigMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.mavis.entity.SystemConfig>()
                        .eq(com.mavis.entity.SystemConfig::getConfigKey, "order_timeout_minutes")
        );
        int minutes = 30;
        if (config != null && config.getConfigValue() != null) {
            try { minutes = Math.max(Integer.parseInt(config.getConfigValue()), 1); } catch (NumberFormatException ignored) {}
        }
        return Result.ok(minutes);
    }

    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String outTradeNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String payType,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String merchantName) {
        return Result.ok(adminOrderService.getOrderPage(page, size, outTradeNo, status, startDate, endDate, payType, merchantId, merchantName));
    }

    @GetMapping("/{id}")
    public Result<PayOrder> detail(@PathVariable Long id) {
        return Result.ok(adminOrderService.getOrderDetail(id));
    }

    @PostMapping("/{id}/refund")
    public Result<?> refund(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            BigDecimal amount = new BigDecimal(body.get("refundAmount"));
            adminOrderService.refundOrder(id, amount);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/{id}/close")
    public Result<?> close(@PathVariable Long id) {
        try {
            adminOrderService.closeOrder(id);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminOrderService.deleteOrder(id);
        return Result.ok();
    }

    @DeleteMapping("/batch")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        adminOrderService.batchDeleteOrders(ids);
        return Result.ok();
    }
}
