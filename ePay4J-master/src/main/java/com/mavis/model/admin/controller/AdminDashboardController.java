package com.mavis.model.admin.controller;

import com.mavis.model.admin.dto.DashboardStatsVO;
import com.mavis.model.admin.dto.Result;
import com.mavis.model.admin.service.AdminDashboardService;
import com.mavis.entity.PayOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.ok(dashboardService.getStats());
    }

    @GetMapping("/revenue-chart")
    public Result<List<Map<String, Object>>> revenueChart(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(dashboardService.getRevenueChart(days));
    }

    @GetMapping("/order-status-chart")
    public Result<List<Map<String, Object>>> orderStatusChart() {
        return Result.ok(dashboardService.getOrderStatusChart());
    }

    @GetMapping("/recent-orders")
    public Result<List<PayOrder>> recentOrders(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(dashboardService.getRecentOrders(limit));
    }
}
