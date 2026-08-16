package com.mavis.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mavis.entity.SystemConfig;
import com.mavis.mapper.SystemConfigMapper;
import com.mavis.model.admin.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @GetMapping
    public Result<Map<String, String>> getSettings() {
        List<SystemConfig> configs = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>()
                        .in(SystemConfig::getConfigKey, "site_name", "pay_api_url")
        );
        Map<String, String> map = new HashMap<>();
        for (SystemConfig config : configs) {
            map.put(config.getConfigKey(), config.getConfigValue());
        }
        return Result.ok(map);
    }
}
