package com.mavis.model.merchant.service;

import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mavis.model.admin.dto.LoginResponse;
import com.mavis.common.constant.Constants;
import com.mavis.common.exception.BusinessException;
import com.mavis.entity.AdminUser;
import com.mavis.entity.Merchant;
import com.mavis.mapper.AdminUserMapper;
import com.mavis.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class MerchantUserService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MerchantService merchantService;

    public LoginResponse login(String username, String password, HttpServletRequest request) {
        AdminUser user = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username)
        );
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        Merchant merchant = null;
        if (Constants.ROLE_MERCHANT.equals(user.getRole())) {
            merchant = merchantService.getMerchantInfoByUserId(user.getId());
        } else if (Constants.ROLE_SUPER_ADMIN.equals(user.getRole())) {
            try {
                merchant = merchantService.getMerchantInfoByUserId(user.getId());
            } catch (BusinessException e) {
                throw new BusinessException(403, "超级管理员未绑定自用商户，请先在后台绑定");
            }
        } else {
            throw new BusinessException(403, "非商户账号禁止登录");
        }

        // 更新登录信息
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        adminUserMapper.updateById(user);

        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setRole(user.getRole());
        resp.setLoginType("MERCHANT");
        if (merchant != null) {
            resp.setMerchantId(merchant.getId());
            resp.setMerchantName(merchant.getName());
        }
        return resp;
    }
}
