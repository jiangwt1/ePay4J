package com.mavis.model.admin.controller;

import com.mavis.model.admin.dto.MerchantAccountVO;
import com.mavis.model.admin.dto.Result;
import com.mavis.model.admin.service.AdminMerchantAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/merchant-accounts")
public class AdminMerchantAccountController {

    @Autowired
    private AdminMerchantAccountService adminMerchantAccountService;

    @GetMapping("/{merchantId}")
    public Result<MerchantAccountVO> getByMerchantId(@PathVariable Long merchantId) {
        return Result.ok(adminMerchantAccountService.getByMerchantId(merchantId));
    }
}
