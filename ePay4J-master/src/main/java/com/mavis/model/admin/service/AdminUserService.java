package com.mavis.model.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mavis.model.admin.dto.PageResult;
import com.mavis.common.exception.BusinessException;
import com.mavis.entity.AdminUser;
import com.mavis.mapper.AdminUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public PageResult<AdminUser> getUserPage(int page, int size) {
        Page<AdminUser> p = new Page<>(page, size);
        Page<AdminUser> result = adminUserMapper.selectPage(p,
                new LambdaQueryWrapper<AdminUser>().orderByDesc(AdminUser::getCreateTime));

        PageResult<AdminUser> pr = new PageResult<>();
        pr.setRecords(result.getRecords());
        pr.setTotal(result.getTotal());
        pr.setPage(result.getCurrent());
        pr.setSize(result.getSize());
        return pr;
    }

    public AdminUser createUser(String username, String password, String nickname, String role) {
        long count = adminUserMapper.selectCount(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username)
        );
        if (count > 0) throw new BusinessException("用户名已存在");

        AdminUser user = new AdminUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(1);
        adminUserMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    public AdminUser updateUser(Long id, String nickname, String role) {
        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if (nickname != null) user.setNickname(nickname);
        if (role != null) user.setRole(role);
        adminUserMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    public void updateStatus(Long id, int status) {
        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(status);
        adminUserMapper.updateById(user);
    }

    public void resetPassword(Long id, String newPassword) {
        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(passwordEncoder.encode(newPassword));
        adminUserMapper.updateById(user);
    }

    public void deleteUser(Long id) {
        adminUserMapper.deleteById(id);
    }
}
