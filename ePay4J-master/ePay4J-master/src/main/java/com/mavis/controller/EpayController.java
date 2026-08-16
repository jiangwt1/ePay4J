package com.mavis.controller;

import com.alibaba.fastjson.JSONObject;
import com.mavis.service.EpayService;
import com.mavis.util.PayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
public class EpayController {

    @Autowired
    private EpayService epayService;

    /**
     * 页面跳转支付
     * GET/POST /submit.php
     */
    @RequestMapping(value = "/submit.php", method = {RequestMethod.GET, RequestMethod.POST})
    public String submit(HttpServletRequest request) {
        Map<String, String> params = PayUtils.extractParams(request);
        log.info("易支付页面支付请求: {}", params);
        return epayService.submitPay(params);
    }

    /**
     * API接口支付
     * POST /mapi.php
     */
    @PostMapping("/mapi.php")
    public JSONObject mapi(HttpServletRequest request) {
        Map<String, String> params = PayUtils.extractParams(request);
        log.info("易支付API支付请求: {}", params);
        return epayService.apiPay(params);
    }

    /**
     * API管理接口
     * GET/POST /api.php?act=xxx
     */
    @RequestMapping(value = "/api.php", method = {RequestMethod.GET, RequestMethod.POST})
    public JSONObject api(@RequestParam String act, HttpServletRequest request) {
        Map<String, String> params = PayUtils.extractParams(request);
        log.info("易支付API请求: act={}, params={}", act, params);
        return epayService.handleApiRequest(act, params);
    }
}
