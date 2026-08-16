package com.mavis.common.constant;

public class Constants {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final int ORDER_STATUS_PENDING = 0;
    public static final int ORDER_STATUS_PAID = 1;
    public static final int ORDER_STATUS_CLOSED = 2;
    public static final int ORDER_STATUS_REFUNDED = 3;

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_VIEWER = "VIEWER";
    public static final String ROLE_MERCHANT = "MERCHANT";
}
