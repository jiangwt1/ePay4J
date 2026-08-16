package com.mavis.model.admin.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mavis.common.exception.BusinessException;
import com.mavis.config.AlipayClientHolder;
import com.mavis.config.WechatPayClientHolder;
import com.mavis.entity.PaymentChannelConfig;
import com.mavis.mapper.PaymentChannelConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminChannelService {

    @Autowired
    private PaymentChannelConfigMapper channelConfigMapper;

    @Autowired
    private AlipayClientHolder alipayClientHolder;

    @Autowired
    private WechatPayClientHolder wechatPayClientHolder;

    public List<PaymentChannelConfig> getEnabledDefaultChannels() {
        return channelConfigMapper.selectList(
                new LambdaQueryWrapper<PaymentChannelConfig>()
                        .eq(PaymentChannelConfig::getStatus, 1)
                        .eq(PaymentChannelConfig::getIsDefault, 1)
                        .orderByAsc(PaymentChannelConfig::getSortOrder)
        );
    }

    public List<PaymentChannelConfig> getAllChannels() {
        return channelConfigMapper.selectList(
                new LambdaQueryWrapper<PaymentChannelConfig>().orderByAsc(PaymentChannelConfig::getSortOrder)
        );
    }

    public PaymentChannelConfig getChannelById(Long id) {
        return channelConfigMapper.selectById(id);
    }

    public void updateChannel(Long id, String channelName, Integer isDefault, String remark, Object configData) {
        PaymentChannelConfig config = channelConfigMapper.selectById(id);
        if (config == null) throw new BusinessException("通道不存在");

        if (isDefault != null && isDefault == 1) {
            unsetDefaultByCode(config.getChannelCode());
        }

        config.setChannelName(channelName);
        config.setIsDefault(isDefault != null ? isDefault : config.getIsDefault());
        config.setRemark(remark);
        if (configData != null) {
            config.setConfigData(configData instanceof String ? (String) configData : JSONObject.toJSONString(configData));
        }
        channelConfigMapper.updateById(config);
        refreshIfAlipay(config.getChannelCode());
    }

    public void updateStatus(Long id, int status) {
        PaymentChannelConfig config = channelConfigMapper.selectById(id);
        if (config == null) throw new BusinessException("通道不存在");
        config.setStatus(status);
        channelConfigMapper.updateById(config);
        refreshIfAlipay(config.getChannelCode());
    }

    public void createChannel(String channelCode, String channelName, Integer isDefault, String remark, Object configData) {
        if (isDefault != null && isDefault == 1) {
            unsetDefaultByCode(channelCode);
        }

        PaymentChannelConfig config = new PaymentChannelConfig();
        config.setChannelCode(channelCode);
        config.setChannelName(channelName);
        config.setIsDefault(isDefault != null ? isDefault : 0);
        config.setRemark(remark);
        config.setStatus(1);
        config.setSortOrder(0);
        config.setConfigData(configData != null
                ? (configData instanceof String ? (String) configData : JSONObject.toJSONString(configData))
                : "{}");
        channelConfigMapper.insert(config);
        refreshIfAlipay(channelCode);
    }

    public void deleteChannel(Long id) {
        PaymentChannelConfig config = channelConfigMapper.selectById(id);
        if (config == null) throw new BusinessException("通道不存在");
        channelConfigMapper.deleteById(id);
        refreshIfAlipay(config.getChannelCode());
    }

    private void unsetDefaultByCode(String channelCode) {
        channelConfigMapper.selectList(
                new LambdaQueryWrapper<PaymentChannelConfig>()
                        .eq(PaymentChannelConfig::getChannelCode, channelCode)
                        .eq(PaymentChannelConfig::getIsDefault, 1)
        ).forEach(c -> {
            c.setIsDefault(0);
            channelConfigMapper.updateById(c);
        });
    }

    private void refreshIfAlipay(String channelCode) {
        if ("ALIPAY".equals(channelCode)) {
            alipayClientHolder.refresh();
        } else if ("WECHAT".equals(channelCode)) {
            wechatPayClientHolder.refresh();
        }
    }
}
