package com.mavis.model.admin.dto;

import lombok.Data;

@Data
public class AlipayTransferResult {
    private boolean success;
    private String outBizNo;
    private String orderId;
    private String payFundOrderId;
    private String status;
    private String message;

    public static AlipayTransferResult success(String outBizNo, String orderId, String payFundOrderId, String status, String message) {
        AlipayTransferResult result = new AlipayTransferResult();
        result.setSuccess(true);
        result.setOutBizNo(outBizNo);
        result.setOrderId(orderId);
        result.setPayFundOrderId(payFundOrderId);
        result.setStatus(status);
        result.setMessage(message);
        return result;
    }

    public static AlipayTransferResult failure(String outBizNo, String status, String message) {
        AlipayTransferResult result = new AlipayTransferResult();
        result.setSuccess(false);
        result.setOutBizNo(outBizNo);
        result.setStatus(status);
        result.setMessage(message);
        return result;
    }
}
