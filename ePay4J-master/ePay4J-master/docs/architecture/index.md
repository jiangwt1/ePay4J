# 分层架构规则

```
Controller → Service → Mapper → Database
                ↓
           Utils
```

- **Controller** (`controller/`): REST 入口，处理 HTTP 请求，返回统一响应 (Result)
- **Service** (`service/`): 业务逻辑层，包含核心业务逻辑
- **Mapper** (`mapper/`): MyBatis Plus 数据库映射接口
- **Utils** (`utils/`): 通用工具类

## 模块划分

```
com.mavis
├── controller/          # 对外 API (AlipayController, EpayController)
├── admin/
│   ├── controller/  # 管理后台 API
│   ├── service/     # 管理后台业务逻辑
│   └── dto/         # 数据传输对象
├── service/         # 核心业务服务
├── channel/         # 支付通道策略模式
├── entity/          # MyBatis Plus 实体类
├── mapper/          # MyBatis Mapper 接口
├── security/        # Spring Security + JWT
├── config/          # Spring 配置类
├── common/          # 公共组件
└── task/            # 定时任务
```

## 认证机制

- 商户认证：签名验证 (MD5)
- 管理员认证：JWT Token
