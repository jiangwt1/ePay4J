package com.mavis.model.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mavis.model.admin.dto.PageResult;
import com.mavis.model.admin.dto.Result;
import com.mavis.model.admin.service.AdminMerchantService;
import com.mavis.entity.Merchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/merchants")
public class AdminMerchantController {

    @Autowired
    private AdminMerchantService adminMerchantService;

    @GetMapping("/page")
    public Result<PageResult<Merchant>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        return Result.ok(adminMerchantService.getMerchantPage(page, size, name, status));
    }

    @GetMapping("/{id}")
    public Result<Merchant> detail(@PathVariable Long id) {
        return Result.ok(adminMerchantService.getMerchantDetail(id));
    }

    @PostMapping
    public Result<Merchant> create(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return Result.ok(adminMerchantService.createMerchant(body.get("name"), username, password));
    }

    @PutMapping("/{id}")
    public Result<Merchant> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return Result.ok(adminMerchantService.updateMerchant(id, body.get("name")));
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminMerchantService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    @PostMapping("/{id}/reset-key")
    public Result<?> resetKey(@PathVariable Long id) {
        return Result.ok(adminMerchantService.resetKey(id));
    }

    @PostMapping("/{id}/bind-current-admin")
    public Result<Merchant> bindCurrentAdmin(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(adminMerchantService.bindCurrentAdmin(id, userId));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminMerchantService.deleteMerchant(id);
        return Result.ok();
    }
}
