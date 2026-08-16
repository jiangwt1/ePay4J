package com.mavis.model.admin.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String nickname;
    private String role;
    private String loginType;
    private Long merchantId;
    private String merchantName;
}
