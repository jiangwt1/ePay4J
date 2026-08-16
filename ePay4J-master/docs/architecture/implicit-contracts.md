# 隐性业务约定

## API 端点

### 支付宝直连接口 (`/api/alipay/**`)
```
GET  /api/alipay/pagePay?subject=...&totalAmount=...  # 电脑网站支付
GET  /api/alipay/wapPay?subject=...&totalAmount=...   # 手机网站支付
GET  /api/alipay/query?outTradeNo=...                  # 查询订单
POST /api/alipay/refund  Body: { outTradeNo, refundAmount }  # 退款
POST /api/alipay/notify  # 异步通知回调
GET  /api/alipay/return  # 同步跳转回调
```

### 易支付商户接口
```
GET/POST /submit.php   # 页面跳转支付
POST     /mapi.php     # API接口支付
GET/POST /api.php?act=query|order|orders|refund|types  # 商户管理接口
```

### 管理后台接口 (`/api/admin/**`)
```
POST /api/admin/auth/login          # 管理员登录
POST /api/admin/auth/reset-password # 重置密码
GET  /api/admin/dashboard/stats    # 仪表盘统计
GET  /api/admin/merchant/...       # 商户管理
GET  /api/admin/order/...         # 订单管理
GET  /api/admin/channel/...       # 通道管理
GET  /api/admin/system/...         # 系统配置
```

## 关键业务流程

**支付流程**: 商户发起支付 → 签名验证 → 创建订单 → 路由到支付通道 → 支付宝支付 → 异步通知 → 更新订单状态

**商户签名**: 将参数按 key 字母排序后拼接，用 MD5 加密，格式: `key1=value1&key2=value2&...&merchantKey`

## 数据库

- 数据库名: `epay`
- 表: `pay_order`, `merchant`, `admin_user`, `payment_channel_config`, `system_config`

## 代码约定

- 依赖注入使用 `@Autowired` 或 `@Resource`
- 实体类使用 Lombok `@Data` 注解
- MyBatis-Plus 使用 LambdaQueryWrapper 进行类型安全查询
- Controller 中不使用 `@Transactional`
- 支付通道使用策略模式，实现 `PaymentChannelStrategy` 接口

## 注意事项

- 支付宝配置通过 `AlipayClientHolder` 获取 (从数据库或配置中心)
- 订单超时通过 `@Scheduled` 定时任务处理
- JWT Secret 在 `application.yml` 中配置
