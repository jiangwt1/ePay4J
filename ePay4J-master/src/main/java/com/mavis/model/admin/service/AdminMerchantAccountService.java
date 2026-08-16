package com.mavis.model.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mavis.model.admin.dto.MerchantAccountVO;
import com.mavis.entity.Merchant;
import com.mavis.entity.MerchantAccount;
import com.mavis.mapper.MerchantAccountMapper;
import com.mavis.mapper.MerchantMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminMerchantAccountService {

    @Autowired
    private MerchantAccountMapper merchantAccountMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    public MerchantAccountVO getByMerchantId(Long merchantId) {
        MerchantAccount account = merchantAccountMapper.selectOne(
                new LambdaQueryWrapper<MerchantAccount>().eq(MerchantAccount::getMerchantId, merchantId)
        );
        if (account == null) {
            return null;
        }

        Merchant merchant = merchantMapper.selectById(merchantId);
        return buildVO(account, merchant);
    }

    public List<MerchantAccountVO> listByMerchantIds(List<Long> merchantIds) {
        List<MerchantAccount> accounts = merchantAccountMapper.selectList(
                new LambdaQueryWrapper<MerchantAccount>().in(MerchantAccount::getMerchantId, merchantIds)
        );

        List<Merchant> merchants = merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>().in(Merchant::getId, merchantIds)
        );
        Map<Long, Merchant> merchantMap = new HashMap<>();
        for (Merchant m : merchants) {
            merchantMap.put(m.getId(), m);
        }

        List<MerchantAccountVO> voList = new ArrayList<>();
        for (MerchantAccount account : accounts) {
            Merchant merchant = merchantMap.get(account.getMerchantId());
            voList.add(buildVO(account, merchant));
        }
        return voList;
    }

    private MerchantAccountVO buildVO(MerchantAccount account, Merchant merchant) {
        MerchantAccountVO vo = new MerchantAccountVO();
        BeanUtils.copyProperties(account, vo);
        if (merchant != null) {
            vo.setMerchantName(merchant.getName());
            vo.setAlipayAccount(merchant.getAlipayAccount());
            vo.setNickName(merchant.getNickName());
            vo.setPhone(merchant.getPhone());
        }
        return vo;
    }
}
