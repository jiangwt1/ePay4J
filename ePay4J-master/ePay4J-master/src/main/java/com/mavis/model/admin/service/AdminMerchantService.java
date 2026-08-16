package com.mavis.model.admin.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mavis.model.admin.dto.PageResult;
import com.mavis.common.constant.Constants;
import com.mavis.common.exception.BusinessException;
import com.mavis.entity.AdminUser;
import com.mavis.entity.Merchant;
import com.mavis.entity.MerchantAccount;
import com.mavis.mapper.AdminUserMapper;
import com.mavis.mapper.MerchantMapper;
import com.mavis.mapper.MerchantAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminMerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private MerchantAccountMapper merchantAccountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public PageResult<Merchant> getMerchantPage(int page, int size, String name, Integer status) {
        Page<Merchant> p = new Page<>(page, size);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Merchant::getName, name);
        }
        if (status != null) {
            wrapper.eq(Merchant::getStatus, status);
        }
        wrapper.orderByDesc(Merchant::getCreateTime);

        Page<Merchant> result = merchantMapper.selectPage(p, wrapper);

        PageResult<Merchant> pr = new PageResult<>();
        pr.setRecords(result.getRecords());
        pr.setTotal(result.getTotal());
        pr.setPage(result.getCurrent());
        pr.setSize(result.getSize());
        return pr;
    }

    public Merchant getMerchantDetail(Long id) {
        return merchantMapper.selectById(id);
    }

    public Merchant createMerchant(String name, String username, String password) {
        // 校验商户名
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("商户名不能为空");
        }
        // 校验用户名是否已存在
        AdminUser existUser = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username)
        );
        if (existUser != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        // 校验密码长度
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码不能少于6位");
        }

        // 创建商户用户
        AdminUser user = new AdminUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(name);
        user.setRole(Constants.ROLE_MERCHANT);
        user.setStatus(1);
        adminUserMapper.insert(user);

        // 创建商户并绑定用户ID
        Merchant m = new Merchant();
        m.setName(name);
        m.setMerchantKey(IdUtil.fastSimpleUUID());
        m.setStatus(1);
        m.setUserId(String.valueOf(user.getId()));
        merchantMapper.insert(m);

        // 创建商户账户记录，初始化余额为0
        MerchantAccount account = new MerchantAccount();
        account.setMerchantId(m.getId());
        account.setTotalIncome(BigDecimal.ZERO);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setFrozenBalance(BigDecimal.ZERO);
        merchantAccountMapper.insert(account);

        return m;
    }

    public Merchant updateMerchant(Long id, String name) {
        Merchant m = merchantMapper.selectById(id);
        if (m == null) throw new BusinessException("商户不存在");
        m.setName(name);
        merchantMapper.updateById(m);
        return m;
    }

    public void updateStatus(Long id, int status) {
        Merchant m = merchantMapper.selectById(id);
        if (m == null) throw new BusinessException("商户不存在");
        m.setStatus(status);
        merchantMapper.updateById(m);
    }

    public String resetKey(Long id) {
        Merchant m = merchantMapper.selectById(id);
        if (m == null) throw new BusinessException("商户不存在");
        m.setMerchantKey(IdUtil.fastSimpleUUID());
        merchantMapper.updateById(m);
        return m.getMerchantKey();
    }

    public Merchant bindCurrentAdmin(Long merchantId, Long adminUserId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        AdminUser admin = adminUserMapper.selectById(adminUserId);
        if (admin == null || !Constants.ROLE_SUPER_ADMIN.equals(admin.getRole())) {
            throw new BusinessException("仅超级管理员可绑定自用商户");
        }
        Merchant existed = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, String.valueOf(adminUserId))
        );
        if (existed != null && !existed.getId().equals(merchantId)) {
            throw new BusinessException("当前超级管理员已绑定自用商户：" + existed.getName());
        }
        merchant.setUserId(String.valueOf(adminUserId));
        merchantMapper.updateById(merchant);
        return merchant;
    }

    public void deleteMerchant(Long id) {
        Merchant m = merchantMapper.selectById(id);
        if (m == null) throw new BusinessException("商户不存在");
        // 删除普通商户绑定用户；如果绑定的是超级管理员自用商户，不能误删超级管理员账号
        if (m.getUserId() != null && !m.getUserId().isEmpty()) {
            Long userId = Long.parseLong(m.getUserId());
            AdminUser user = adminUserMapper.selectById(userId);
            if (user != null && Constants.ROLE_MERCHANT.equals(user.getRole())) {
                adminUserMapper.deleteById(userId);
            }
        }
        merchantMapper.deleteById(id);
    }
}
