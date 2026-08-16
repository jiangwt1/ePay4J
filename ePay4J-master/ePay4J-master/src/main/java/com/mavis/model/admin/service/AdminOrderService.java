package com.mavis.model.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mavis.model.admin.dto.OrderVO;
import com.mavis.model.admin.dto.PageResult;
import com.mavis.entity.Merchant;
import com.mavis.entity.PayOrder;
import com.mavis.mapper.MerchantMapper;
import com.mavis.mapper.PayOrderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AdminOrderService {

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private com.mavis.service.AlipayService alipayService;

    public PageResult<OrderVO> getOrderPage(int page, int size, String outTradeNo,
                                               Integer status, String startDate, String endDate, String payType,
                                               Long merchantId, String merchantName) {
        Page<PayOrder> p = new Page<>(page, size);
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();

        // 商户ID筛选
        if (merchantId != null) {
            wrapper.eq(PayOrder::getPid, merchantId);
        }

        // 商户名筛选（通过商户名查找对应商户ID）
        if (StringUtils.hasText(merchantName)) {
            List<Merchant> merchants = merchantMapper.selectList(
                    new LambdaQueryWrapper<Merchant>().like(Merchant::getName, merchantName)
            );
            if (!merchants.isEmpty()) {
                List<Long> merchantIds = new ArrayList<>();
                for (Merchant m : merchants) {
                    merchantIds.add(m.getId());
                }
                wrapper.in(PayOrder::getPid, merchantIds);
            } else {
                // 没有匹配商户时，结果为空
                wrapper.eq(PayOrder::getPid, -1L);
            }
        }

        if (StringUtils.hasText(outTradeNo)) {
            wrapper.eq(PayOrder::getOutTradeNo, outTradeNo);
        }
        if (status != null) {
            wrapper.eq(PayOrder::getStatus, status);
        }
        if (StringUtils.hasText(payType)) {
            wrapper.eq(PayOrder::getPayType, payType);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(PayOrder::getCreateTime, LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(PayOrder::getCreateTime, LocalDate.parse(endDate).atTime(LocalTime.MAX));
        }
        wrapper.orderByDesc(PayOrder::getCreateTime);

        Page<PayOrder> result = payOrderMapper.selectPage(p, wrapper);

        // 转换为VO并填充商户名
        List<OrderVO> voList = new ArrayList<>();
        Map<Long, String> merchantNameMap = new HashMap<>();

        for (PayOrder order : result.getRecords()) {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);

            // 获取商户名
            if (order.getPid() != null) {
                String mName = merchantNameMap.get(order.getPid());
                if (mName == null) {
                    Merchant merchant = merchantMapper.selectById(order.getPid());
                    mName = merchant != null ? merchant.getName() : "";
                    merchantNameMap.put(order.getPid(), mName);
                }
                vo.setMerchantName(mName + "(" + order.getPid() + ")");
            }

            voList.add(vo);
        }

        PageResult<OrderVO> pr = new PageResult<>();
        pr.setRecords(voList);
        pr.setTotal(result.getTotal());
        pr.setPage(result.getCurrent());
        pr.setSize(result.getSize());
        return pr;
    }

    public PayOrder getOrderDetail(Long id) {
        return payOrderMapper.selectById(id);
    }

    public void refundOrder(Long id, BigDecimal refundAmount) throws Exception {
        PayOrder order = payOrderMapper.selectById(id);
        if (order == null) throw new com.mavis.common.exception.BusinessException("订单不存在");
        if (order.getStatus() != 1) throw new com.mavis.common.exception.BusinessException("只能退款已支付的订单");
        boolean success = alipayService.refund(order.getOutTradeNo(), refundAmount);
        if (!success) throw new com.mavis.common.exception.BusinessException("退款失败");
    }

    public void closeOrder(Long id) throws Exception {
        PayOrder order = payOrderMapper.selectById(id);
        if (order == null) throw new com.mavis.common.exception.BusinessException("订单不存在");
        if (order.getStatus() != 0) throw new com.mavis.common.exception.BusinessException("只能关闭待支付的订单");
        boolean success = alipayService.close(order.getOutTradeNo());
        if (!success) throw new com.mavis.common.exception.BusinessException("关闭失败");
    }

    public void deleteOrder(Long id) {
        PayOrder order = payOrderMapper.selectById(id);
        if (order == null) throw new com.mavis.common.exception.BusinessException("订单不存在");
        if (order.getStatus() == 0) throw new com.mavis.common.exception.BusinessException("请先关闭待支付订单");
        payOrderMapper.deleteById(id);
    }

    public void batchDeleteOrders(List<Long> ids) {
        payOrderMapper.deleteBatchIds(ids);
    }
}
