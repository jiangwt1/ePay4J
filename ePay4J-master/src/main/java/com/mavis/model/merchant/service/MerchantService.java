package com.mavis.model.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mavis.common.exception.BusinessException;
import com.mavis.entity.AdminUser;
import com.mavis.entity.Merchant;
import com.mavis.entity.MerchantAccount;
import com.mavis.entity.MerchantWithdraw;
import com.mavis.entity.PayOrder;
import com.mavis.entity.SystemConfig;
import com.mavis.mapper.AdminUserMapper;
import com.mavis.mapper.MerchantAccountMapper;
import com.mavis.mapper.MerchantMapper;
import com.mavis.mapper.MerchantWithdrawMapper;
import com.mavis.mapper.PayOrderMapper;
import com.mavis.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import cn.hutool.core.util.IdUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private MerchantAccountMapper merchantAccountMapper;

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private MerchantWithdrawMapper merchantWithdrawMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Merchant getMerchantInfo(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        return merchant;
    }

    public Merchant getMerchantInfoByUserId(Long userId) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, String.valueOf(userId))
        );
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        return merchant;
    }

    public Merchant updateMerchant(Long merchantId, String alipayAccount, String nickName, String phone) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }

        if (alipayAccount != null) {
            merchant.setAlipayAccount(alipayAccount);
        }
        if (nickName != null) {
            merchant.setNickName(nickName);
        }
        if (phone != null) {
            merchant.setPhone(phone);
        }

        merchantMapper.updateById(merchant);
        return merchant;
    }

    public MerchantAccount getMerchantAccount(Long merchantId) {
        MerchantAccount account = merchantAccountMapper.selectOne(
                new LambdaQueryWrapper<MerchantAccount>().eq(MerchantAccount::getMerchantId, merchantId)
        );
        if (account == null) {
            throw new BusinessException("商户账户不存在");
        }
        return account;
    }

    public List<PayOrder> getOrders(Long merchantId, int page, int size) {
        Page<PayOrder> p = new Page<>(page, size);
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getPid, merchantId);
        wrapper.orderByDesc(PayOrder::getCreateTime);
        Page<PayOrder> result = payOrderMapper.selectPage(p, wrapper);
        return result.getRecords();
    }

    public long getOrderCount(Long merchantId) {
        return payOrderMapper.selectCount(
                new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getPid, merchantId)
        );
    }

    public PayOrder getOrderDetail(Long orderId, Long merchantId) {
        PayOrder order = payOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!merchantId.equals(order.getPid())) {
            throw new BusinessException("无权查看此订单");
        }
        return order;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码不能少于6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        adminUserMapper.updateById(user);
    }

    public String resetKey(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        String newKey = IdUtil.fastSimpleUUID();
        merchant.setMerchantKey(newKey);
        merchantMapper.updateById(merchant);
        return newKey;
    }

    public MerchantWithdraw withdraw(Long merchantId, BigDecimal amount) {
        // 获取商户信息
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }

        // 检查是否维护了姓名、手机号、支付宝账号
        if (merchant.getNickName() == null || StringUtils.isBlank(merchant.getNickName())) {
            throw new IllegalArgumentException("请先维护姓名");
        }
        if (merchant.getPhone() == null || StringUtils.isBlank(merchant.getPhone())) {
            throw new IllegalArgumentException("请先维护手机号");
        }
        if (merchant.getAlipayAccount() == null || StringUtils.isBlank(merchant.getAlipayAccount())) {
            throw new IllegalArgumentException("请先维护支付宝账号");
        }

        // 检查余额
        MerchantAccount account = getMerchantAccount(merchantId);
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("可用余额不足");
        }

        // 读取提现费率
        SystemConfig rateConfig = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "rate")
        );
        BigDecimal rate = BigDecimal.ZERO;
        if (rateConfig != null && rateConfig.getConfigValue() != null) {
            rate = new BigDecimal(rateConfig.getConfigValue());
        }

        // 计算手续费和实际到账金额
        BigDecimal serviceFee = amount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal amountCredited = amount.subtract(serviceFee);

        // 冻结金额：可用余额减少，冻结余额增加
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        account.setFrozenBalance(account.getFrozenBalance().add(amount));
        merchantAccountMapper.updateById(account);

        // 创建提现记录
        MerchantWithdraw withdraw = new MerchantWithdraw();
        withdraw.setMerchantId(merchantId);
        withdraw.setAmount(amount);
        withdraw.setServiceFee(serviceFee);
        withdraw.setAmountCredited(amountCredited);
        withdraw.setStatus(0); // 待处理(处理中)
        withdraw.setCreateTime(LocalDateTime.now());
        withdraw.setUpdateTime(LocalDateTime.now());
        merchantWithdrawMapper.insert(withdraw);

        return withdraw;
    }

    public List<MerchantWithdraw> getWithdrawRecords(Long merchantId, int page, int size) {
        Page<MerchantWithdraw> p = new Page<>(page, size);
        LambdaQueryWrapper<MerchantWithdraw> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantWithdraw::getMerchantId, merchantId);
        wrapper.orderByDesc(MerchantWithdraw::getCreateTime);
        Page<MerchantWithdraw> result = merchantWithdrawMapper.selectPage(p, wrapper);
        return result.getRecords();
    }
}
