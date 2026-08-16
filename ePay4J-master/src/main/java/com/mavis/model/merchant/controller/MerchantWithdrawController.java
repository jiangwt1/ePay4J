package com.mavis.model.merchant.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mavis.entity.Merchant;
import com.mavis.entity.MerchantWithdraw;
import com.mavis.model.admin.dto.Result;
import com.mavis.model.merchant.service.MerchantService;
import com.mavis.model.merchant.support.MerchantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/withdraw")
public class MerchantWithdrawController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MerchantContext merchantContext;

    /**
     * 提交提现任务
     */
    @PostMapping
    public Result<MerchantWithdraw> withdraw(@RequestBody Map<String, String> body, HttpServletRequest request) {
        if (merchantContext.isActingAsAdmin()) {
            throw new IllegalArgumentException("管理员代入商户视角不能代商户发起提现");
        }
        Merchant merchant = merchantContext.getCurrentMerchant(request);

        String amountStr = body.get("amount");
        if (amountStr == null || StringUtils.isBlank(amountStr)) {
            throw new IllegalArgumentException("提现金额不能为空");
        }
        BigDecimal amount = new BigDecimal(amountStr);

        MerchantWithdraw withdraw = merchantService.withdraw(merchant.getId(), amount);
        return Result.ok(withdraw);
    }

    /**
     * 查询提现记录
     */
    @GetMapping("/records")
    public Result<Map<String, Object>> getWithdrawRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Merchant merchant = merchantContext.getCurrentMerchant(request);

        List<MerchantWithdraw> records = merchantService.getWithdrawRecords(merchant.getId(), page, size);

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("page", page);
        data.put("size", size);

        return Result.ok(data);
    }
}
