package com.mavis.model.merchant.controller;

import com.mavis.model.admin.dto.Result;
import com.mavis.entity.Merchant;
import com.mavis.entity.MerchantAccount;
import com.mavis.model.merchant.service.MerchantService;
import com.mavis.model.merchant.support.MerchantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MerchantContext merchantContext;

    /**
     * 获取当前商户用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo(HttpServletRequest request) {
        Merchant merchant = merchantContext.getCurrentMerchant(request);

        Map<String, Object> data = new HashMap<>();
        data.put("id", merchant.getId());
        data.put("name", merchant.getName());
        data.put("alipayAccount", merchant.getAlipayAccount());
        data.put("nickName", merchant.getNickName());
        data.put("phone", merchant.getPhone());

        return Result.ok(data);
    }

    /**
     * 获取当前商户账户信息（余额）
     */
    @GetMapping("/account")
    public Result<MerchantAccount> getAccount(HttpServletRequest request) {
        Merchant merchant = merchantContext.getCurrentMerchant(request);
        MerchantAccount account = merchantService.getMerchantAccount(merchant.getId());
        return Result.ok(account);
    }

    /**
     * 修改商户信息
     */
    @PutMapping("/info")
    public Result<Merchant> updateInfo(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Merchant merchant = merchantContext.getCurrentMerchant(request);

        String alipayAccount = body.get("alipayAccount");
        String nickName = body.get("nickName");
        String phone = body.get("phone");

        Merchant updated = merchantService.updateMerchant(merchant.getId(), alipayAccount, nickName, phone);
        return Result.ok(updated);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> body) {
        if (merchantContext.isActingAsAdmin()) {
            throw new IllegalArgumentException("管理员代入商户视角不能修改商户登录密码");
        }
        Long userId = getCurrentUserId();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        merchantService.changePassword(userId, oldPassword, newPassword);
        return Result.ok();
    }

    /**
     * 获取商户密钥信息
     */
    @GetMapping("/credentials")
    public Result<Map<String, Object>> getCredentials(HttpServletRequest request) {
        Merchant merchant = merchantContext.getCurrentMerchant(request);

        Map<String, Object> data = new HashMap<>();
        data.put("pid", merchant.getId());
        data.put("key", merchant.getMerchantKey());
        data.put("name", merchant.getName());

        return Result.ok(data);
    }

    /**
     * 重置商户密钥
     */
    @PostMapping("/reset-key")
    public Result<Map<String, Object>> resetKey(HttpServletRequest request) {
        if (merchantContext.isActingAsAdmin()) {
            throw new IllegalArgumentException("管理员代入商户视角不能重置商户密钥，请在后台商户管理中操作");
        }
        Merchant merchant = merchantContext.getCurrentMerchant(request);

        String newKey = merchantService.resetKey(merchant.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("pid", merchant.getId());
        data.put("key", newKey);

        return Result.ok(data);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
