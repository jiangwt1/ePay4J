package com.mavis.model.merchant.support;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mavis.common.exception.BusinessException;
import com.mavis.entity.Merchant;
import com.mavis.model.merchant.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class MerchantContext {

    public static final String ACTING_MERCHANT_HEADER = "X-Acting-Merchant-Id";

    @Autowired
    private MerchantService merchantService;

    public Merchant getCurrentMerchant(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        Long userId = (Long) auth.getPrincipal();
        if (isSuperAdmin(auth)) {
            String merchantIdValue = request.getHeader(ACTING_MERCHANT_HEADER);
            if (StringUtils.isNotBlank(merchantIdValue)) {
                try {
                    return merchantService.getMerchantInfo(Long.parseLong(merchantIdValue));
                } catch (NumberFormatException e) {
                    throw new BusinessException("商户视角参数无效");
                }
            }
            try {
                return merchantService.getMerchantInfoByUserId(userId);
            } catch (BusinessException e) {
                throw new BusinessException("请选择要查看的商户或先绑定自用商户");
            }
        }

        return merchantService.getMerchantInfoByUserId(userId);
    }

    public boolean isActingAsAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && isSuperAdmin(auth) && StringUtils.isNotBlank(getCurrentRequestActingMerchantId());
    }

    private String getCurrentRequestActingMerchantId() {
        org.springframework.web.context.request.RequestAttributes attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof org.springframework.web.context.request.ServletRequestAttributes)) {
            return null;
        }
        return ((org.springframework.web.context.request.ServletRequestAttributes) attributes).getRequest().getHeader(ACTING_MERCHANT_HEADER);
    }

    private boolean isSuperAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }
}
