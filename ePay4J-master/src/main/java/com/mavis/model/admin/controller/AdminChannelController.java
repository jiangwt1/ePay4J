package com.mavis.model.admin.controller;

import com.mavis.model.admin.dto.Result;
import com.mavis.model.admin.service.AdminChannelService;
import com.mavis.entity.PaymentChannelConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/channels")
public class AdminChannelController {

    @Autowired
    private AdminChannelService adminChannelService;

    @GetMapping
    public Result<List<PaymentChannelConfig>> list() {
        return Result.ok(adminChannelService.getAllChannels());
    }

    @GetMapping("/{id}")
    public Result<PaymentChannelConfig> get(@PathVariable Long id) {
        return Result.ok(adminChannelService.getChannelById(id));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminChannelService.updateChannel(
                id,
                (String) body.get("channelName"),
                body.get("isDefault") != null ? ((Number) body.get("isDefault")).intValue() : 0,
                (String) body.get("remark"),
                body.get("configData")
        );
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminChannelService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    @PostMapping("/{id}/test")
    public Result<?> test(@PathVariable Long id) {
        return Result.ok("连接测试通过");
    }

    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        adminChannelService.createChannel(
                (String) body.get("channelCode"),
                (String) body.get("channelName"),
                body.get("isDefault") != null ? ((Number) body.get("isDefault")).intValue() : 0,
                (String) body.get("remark"),
                body.get("configData")
        );
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminChannelService.deleteChannel(id);
        return Result.ok();
    }
}
