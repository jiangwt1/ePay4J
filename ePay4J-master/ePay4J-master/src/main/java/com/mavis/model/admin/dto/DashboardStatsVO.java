package com.mavis.model.admin.dto;

import lombok.Data;

@Data
public class DashboardStatsVO {
    private String todayRevenue;
    private int todayOrders;
    private String successRate;
    private int totalMerchants;
}
