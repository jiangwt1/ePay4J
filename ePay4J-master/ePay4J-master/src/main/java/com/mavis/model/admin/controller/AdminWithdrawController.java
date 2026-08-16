package com.mavis.model.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mavis.entity.MerchantWithdraw;
import com.mavis.mapper.MerchantWithdrawMapper;
import com.mavis.model.admin.dto.PageResult;
import com.mavis.model.admin.dto.Result;
import com.mavis.model.admin.dto.WithdrawDetailVO;
import com.mavis.model.admin.dto.WithdrawListVO;
import com.mavis.model.admin.service.AdminWithdrawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/withdraw")
public class AdminWithdrawController {

    @Autowired
    private AdminWithdrawService adminWithdrawService;

    @Autowired
    private MerchantWithdrawMapper merchantWithdrawMapper;

    /**
     * 查询所有提现任务
     */
    @GetMapping("/page")
    public Result<PageResult<WithdrawListVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String merchantName,
            @RequestParam(required = false) Integer status) {
        Page<WithdrawListVO> result = adminWithdrawService.getWithdrawPage(page, size, merchantId, merchantName, status);

        PageResult<WithdrawListVO> pageResult = new PageResult<>();
        pageResult.setRecords(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setPage(result.getCurrent());
        pageResult.setSize(result.getSize());

        return Result.ok(pageResult);
    }

    /**
     * 获取提现任务详情
     */
    @GetMapping("/{id}")
    public Result<WithdrawDetailVO> detail(@PathVariable Long id) {
        return Result.ok(adminWithdrawService.getWithdrawDetailVO(id));
    }

    /**
     * 完成提现任务（自动转账）
     */
    @PostMapping("/{id}/approve")
    public Result<?> approve(@PathVariable Long id) {
        adminWithdrawService.approve(id);
        return Result.ok();
    }

    /**
     * 完成提现任务（管理员手动转账后标记）
     */
    @PostMapping("/{id}/approve-manual")
    public Result<?> approveManual(@PathVariable Long id) {
        adminWithdrawService.approveManual(id);
        return Result.ok();
    }

    /**
     * 拒绝提现任务
     */
    @PostMapping("/{id}/reject")
    public Result<?> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        adminWithdrawService.reject(id, reason);
        return Result.ok();
    }
}
