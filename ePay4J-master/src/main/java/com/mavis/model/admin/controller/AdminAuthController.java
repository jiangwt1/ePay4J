package com.mavis.model.admin.controller;

import com.mavis.model.admin.dto.LoginRequest;
import com.mavis.model.admin.dto.Result;
import com.mavis.model.admin.service.AdminAuthService;
import com.mavis.entity.AdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.ok(adminAuthService.login(request, httpRequest));
    }

    @GetMapping("/info")
    public Result<AdminUser> info() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(adminAuthService.getUserInfo(userId));
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.ok();
    }

    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        adminAuthService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }

    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestBody Map<String, String> body) {
        adminAuthService.resetPassword(body.get("username"), body.get("securityCode"), body.get("newPassword"));
        return Result.ok();
    }
}
