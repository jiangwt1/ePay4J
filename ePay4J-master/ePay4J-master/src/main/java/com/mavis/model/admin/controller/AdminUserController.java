package com.mavis.model.admin.controller;

import com.mavis.model.admin.dto.PageResult;
import com.mavis.model.admin.dto.Result;
import com.mavis.model.admin.service.AdminUserService;
import com.mavis.entity.AdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/page")
    public Result<PageResult<AdminUser>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(adminUserService.getUserPage(page, size));
    }

    @PostMapping
    public Result<AdminUser> create(@RequestBody Map<String, String> body) {
        return Result.ok(adminUserService.createUser(
                body.get("username"), body.get("password"),
                body.get("nickname"), body.getOrDefault("role", "ADMIN")
        ));
    }

    @PutMapping("/{id}")
    public Result<AdminUser> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return Result.ok(adminUserService.updateUser(id, body.get("nickname"), body.get("role")));
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminUserService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminUserService.resetPassword(id, body.get("password"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return Result.ok();
    }
}
