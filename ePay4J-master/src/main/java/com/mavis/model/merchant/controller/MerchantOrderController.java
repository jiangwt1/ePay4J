package com.mavis.model.merchant.controller;

import com.mavis.entity.PayOrder;
import com.mavis.model.admin.dto.Result;
import com.mavis.model.merchant.service.MerchantService;
import com.mavis.model.merchant.support.MerchantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/orders")
public class MerchantOrderController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MerchantContext merchantContext;

    /**
     * 获取当前商户的所有订单
     */
    @GetMapping
    public Result<Map<String, Object>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        com.mavis.entity.Merchant merchant = merchantContext.getCurrentMerchant(request);

        List<PayOrder> orders = merchantService.getOrders(merchant.getId(), page, size);
        long total = merchantService.getOrderCount(merchant.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("records", orders);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);

        return Result.ok(data);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<PayOrder> getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        com.mavis.entity.Merchant merchant = merchantContext.getCurrentMerchant(request);

        PayOrder order = merchantService.getOrderDetail(id, merchant.getId());
        return Result.ok(order);
    }
}
