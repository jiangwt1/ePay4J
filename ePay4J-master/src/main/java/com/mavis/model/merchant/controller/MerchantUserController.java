package com.mavis.model.merchant.controller;

import com.mavis.model.admin.dto.LoginRequest;
import com.mavis.model.admin.dto.LoginResponse;
import com.mavis.model.admin.dto.Result;
import com.mavis.model.merchant.service.MerchantUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/merchant/auth")
public class MerchantUserController {

    @Autowired
    private MerchantUserService merchantUserService;

    /**
     * 商户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse resp = merchantUserService.login(request.getUsername(), request.getPassword(), httpRequest);
        return Result.ok(resp);
    }
}
