package com.mavis.model.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mavis.entity.SystemConfig;
import com.mavis.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminSystemService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    public List<SystemConfig> getAllConfigs() {
        return systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>().orderByAsc(SystemConfig::getConfigGroup).orderByAsc(SystemConfig::getId)
        );
    }

    public void batchUpdate(List<SystemConfig> configs) {
        for (SystemConfig config : configs) {
            if (config.getConfigKey() != null) {
                SystemConfig existing = systemConfigMapper.selectOne(
                        new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, config.getConfigKey())
                );
                if (existing != null) {
                    existing.setConfigValue(config.getConfigValue());
                    systemConfigMapper.updateById(existing);
                }
            }
        }
    }
}
