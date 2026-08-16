package com.mavis.model.admin.service;

import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mavis.model.admin.dto.LoginRequest;
import com.mavis.model.admin.dto.LoginResponse;
import com.mavis.common.exception.BusinessException;
import com.mavis.entity.AdminUser;
import com.mavis.mapper.AdminUserMapper;
import com.mavis.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AdminAuthService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        AdminUser user = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, request.getUsername())
        );
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!"SUPER_ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "仅限超级管理员登录");
        }

        // 更新登录信息
        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = httpRequest.getRemoteAddr();
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
        resp.setLoginType("ADMIN");
        return resp;
    }

    public AdminUser getUserInfo(Long userId) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        user.setPassword(null);
        user.setSecurityCode(null);
        return user;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        adminUserMapper.updateById(user);
    }

    public void resetPassword(String username, String securityCode, String newPassword) {
        AdminUser user = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username)
        );
        if (user == null) throw new BusinessException("用户不存在");
        if (user.getSecurityCode() == null || !user.getSecurityCode().equals(securityCode)) {
            throw new BusinessException("安全码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        adminUserMapper.updateById(user);
    }
}
