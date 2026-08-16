package com.mavis.model.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mavis.common.exception.BusinessException;
import com.mavis.entity.Merchant;
import com.mavis.entity.MerchantAccount;
import com.mavis.entity.MerchantWithdraw;
import com.mavis.mapper.MerchantAccountMapper;
import com.mavis.mapper.MerchantMapper;
import com.mavis.mapper.MerchantWithdrawMapper;
import com.mavis.model.admin.dto.AlipayTransferResult;
import com.mavis.model.admin.dto.WithdrawDetailVO;
import com.mavis.model.admin.dto.WithdrawListVO;
import com.mavis.service.AlipayService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminWithdrawService {

    @Autowired
    private MerchantWithdrawMapper merchantWithdrawMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private MerchantAccountMapper merchantAccountMapper;

    @Autowired
    private AlipayService alipayService;

    public Page<WithdrawListVO> getWithdrawPage(int page, int size, Long merchantId, String merchantName, Integer status) {
        Page<MerchantWithdraw> p = new Page<>(page, size);
        LambdaQueryWrapper<MerchantWithdraw> wrapper = new LambdaQueryWrapper<>();

        if (merchantId != null) {
            wrapper.eq(MerchantWithdraw::getMerchantId, merchantId);
        }

        if (merchantName != null && StringUtils.isNotBlank(merchantName)) {
            List<Merchant> merchants = merchantMapper.selectList(
                    new LambdaQueryWrapper<Merchant>().like(Merchant::getName, merchantName)
            );
            if (!merchants.isEmpty()) {
                List<Long> merchantIds = new ArrayList<>();
                for (Merchant m : merchants) {
                    merchantIds.add(m.getId());
                }
                wrapper.in(MerchantWithdraw::getMerchantId, merchantIds);
            } else {
                wrapper.eq(MerchantWithdraw::getMerchantId, -1L);
            }
        }

        if (status != null) {
            wrapper.eq(MerchantWithdraw::getStatus, status);
        }

        wrapper.orderByDesc(MerchantWithdraw::getCreateTime);
        Page<MerchantWithdraw> result = merchantWithdrawMapper.selectPage(p, wrapper);

        // 批量查询商户名
        List<Long> mIds = result.getRecords().stream()
                .map(MerchantWithdraw::getMerchantId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> merchantNameMap = new HashMap<>();
        if (!mIds.isEmpty()) {
            List<Merchant> merchantList = merchantMapper.selectBatchIds(mIds);
            for (Merchant m : merchantList) {
                merchantNameMap.put(m.getId(), m.getName());
            }
        }

        // 转换为 VO
        Page<WithdrawListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<WithdrawListVO> voList = result.getRecords().stream().map(withdraw -> {
            WithdrawListVO vo = new WithdrawListVO();
            BeanUtils.copyProperties(withdraw, vo);
            vo.setMerchantName(merchantNameMap.getOrDefault(withdraw.getMerchantId(), "-"));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    public MerchantWithdraw getWithdrawDetail(Long id) {
        MerchantWithdraw withdraw = merchantWithdrawMapper.selectById(id);
        if (withdraw == null) {
            throw new BusinessException("提现记录不存在");
        }
        return withdraw;
    }

    public WithdrawDetailVO getWithdrawDetailVO(Long id) {
        MerchantWithdraw withdraw = merchantWithdrawMapper.selectById(id);
        if (withdraw == null) {
            throw new BusinessException("提现记录不存在");
        }

        Merchant merchant = merchantMapper.selectById(withdraw.getMerchantId());

        WithdrawDetailVO vo = new WithdrawDetailVO();
        BeanUtils.copyProperties(withdraw, vo);
        if (merchant != null) {
            vo.setMerchantName(merchant.getName());
            vo.setAlipayAccount(merchant.getAlipayAccount());
            vo.setNickName(merchant.getNickName());
            vo.setPhone(merchant.getPhone());
        }

        return vo;
    }

    public void approve(Long id) {
        MerchantWithdraw withdraw = merchantWithdrawMapper.selectById(id);
        if (withdraw == null) {
            throw new BusinessException("提现记录不存在");
        }
        if (withdraw.getStatus() != 0) {
            throw new BusinessException("只能处理待处理的提现任务");
        }

        Merchant merchant = merchantMapper.selectById(withdraw.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        if (StringUtils.isBlank(merchant.getAlipayAccount())) {
            throw new BusinessException("商户未维护支付宝账号");
        }
        if (StringUtils.isBlank(merchant.getNickName())) {
            throw new BusinessException("商户未维护姓名");
        }
        if (withdraw.getAmountCredited() == null || withdraw.getAmountCredited().compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal serviceFee = withdraw.getServiceFee() != null ? withdraw.getServiceFee() : BigDecimal.ZERO;
            withdraw.setAmountCredited(withdraw.getAmount().subtract(serviceFee));
        }
        if (withdraw.getAmountCredited().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("实际到账金额无效");
        }
        if (!alipayService.isCertificateMode()) {
            throw new BusinessException("当前支付宝通道为普通公钥模式，提现转账接口需要公钥证书模式。请前往支付宝开放平台 → 控制台 → 我的应用 → 开发设置 → 接口加签方式，切换/配置为公钥证书模式，下载应用公钥证书、支付宝公钥证书、支付宝根证书后，在后台支付通道中选择“公钥证书”并填写证书路径。");
        }

        String transferNo = withdraw.getTransferNo();
        if (StringUtils.isBlank(transferNo)) {
            transferNo = buildTransferNo(withdraw.getId());
        }

        AlipayTransferResult transferResult;
        try {
            transferResult = alipayService.transferToAlipayAccount(
                    transferNo,
                    withdraw.getAmountCredited(),
                    merchant.getAlipayAccount(),
                    merchant.getNickName(),
                    "商户提现"
            );
        } catch (Exception e) {
            withdraw.setTransferNo(transferNo);
            withdraw.setTransferStatus("EXCEPTION");
            withdraw.setTransferMsg(limitMessage(e.getMessage()));
            withdraw.setUpdateTime(LocalDateTime.now());
            merchantWithdrawMapper.updateById(withdraw);
            throw new BusinessException("支付宝转账异常：" + e.getMessage());
        }

        withdraw.setTransferNo(transferNo);
        withdraw.setAlipayOrderId(transferResult.getOrderId());
        withdraw.setPayFundOrderId(transferResult.getPayFundOrderId());
        withdraw.setTransferStatus(transferResult.getStatus());
        withdraw.setTransferMsg(limitMessage(transferResult.getMessage()));
        withdraw.setUpdateTime(LocalDateTime.now());

        if (!transferResult.isSuccess()) {
            merchantWithdrawMapper.updateById(withdraw);
            throw new BusinessException("支付宝转账失败：" + transferResult.getMessage());
        }

        // 支付宝转账成功后，状态改为已提现，并扣减冻结金额
        withdraw.setStatus(1);
        withdraw.setTransferTime(LocalDateTime.now());
        merchantWithdrawMapper.updateById(withdraw);

        MerchantAccount account = merchantAccountMapper.selectOne(
                new LambdaQueryWrapper<MerchantAccount>()
                        .eq(MerchantAccount::getMerchantId, withdraw.getMerchantId())
        );
        if (account != null) {
            account.setFrozenBalance(account.getFrozenBalance().subtract(withdraw.getAmount()));
            merchantAccountMapper.updateById(account);
        }
    }

    /**
     * 手动打款：管理员线下转账完成后标记提现已打款
     */
    public void approveManual(Long id) {
        MerchantWithdraw withdraw = merchantWithdrawMapper.selectById(id);
        if (withdraw == null) {
            throw new BusinessException("提现记录不存在");
        }
        if (withdraw.getStatus() != 0) {
            throw new BusinessException("只能处理待处理的提现任务");
        }

        if (withdraw.getAmountCredited() == null || withdraw.getAmountCredited().compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal serviceFee = withdraw.getServiceFee() != null ? withdraw.getServiceFee() : BigDecimal.ZERO;
            withdraw.setAmountCredited(withdraw.getAmount().subtract(serviceFee));
        }

        withdraw.setStatus(1);
        withdraw.setTransferStatus("MANUAL");
        withdraw.setTransferMsg("管理员手动转账");
        withdraw.setTransferTime(LocalDateTime.now());
        withdraw.setUpdateTime(LocalDateTime.now());
        merchantWithdrawMapper.updateById(withdraw);

        MerchantAccount account = merchantAccountMapper.selectOne(
                new LambdaQueryWrapper<MerchantAccount>()
                        .eq(MerchantAccount::getMerchantId, withdraw.getMerchantId())
        );
        if (account != null) {
            account.setFrozenBalance(account.getFrozenBalance().subtract(withdraw.getAmount()));
            merchantAccountMapper.updateById(account);
        }
    }

    private String buildTransferNo(Long withdrawId) {
        return "WD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + withdrawId;
    }

    private String limitMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 255 ? message.substring(0, 255) : message;
    }

    public void reject(Long id, String reason) {
        if (reason == null || StringUtils.isBlank(reason)) {
            throw new IllegalArgumentException("请填写拒绝理由");
        }

        MerchantWithdraw withdraw = merchantWithdrawMapper.selectById(id);
        if (withdraw == null) {
            throw new BusinessException("提现记录不存在");
        }
        if (withdraw.getStatus() != 0) {
            throw new BusinessException("只能处理待处理的提现任务");
        }

        // 更新状态为已拒绝
        withdraw.setStatus(2);
        withdraw.setRemark(reason);
        withdraw.setUpdateTime(LocalDateTime.now());
        merchantWithdrawMapper.updateById(withdraw);

        // 冻结金额释放回可用余额
        MerchantAccount account = merchantAccountMapper.selectOne(
                new LambdaQueryWrapper<MerchantAccount>()
                        .eq(MerchantAccount::getMerchantId, withdraw.getMerchantId())
        );
        if (account != null) {
            account.setAvailableBalance(account.getAvailableBalance().add(withdraw.getAmount()));
            account.setFrozenBalance(account.getFrozenBalance().subtract(withdraw.getAmount()));
            merchantAccountMapper.updateById(account);
        }
    }
}
