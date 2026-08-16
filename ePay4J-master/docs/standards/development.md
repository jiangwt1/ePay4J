# 开发规范

## 构建命令
```bash
mvn clean package -DskipTests  # 构建项目
mvn spring-boot:run           # 运行项目
mvn test                       # 运行测试
```

## 数据库初始化
```bash
mysql -u root -p < src/main/resources/sql/init.sql
mysql -u root -p < src/main/resources/sql/init_admin.sql
```

## 质量标准
- 新增代码建议包含单元测试
- 禁止在 Controller 层使用 `@Transactional`

## 代码风格
- 使用 Lombok 减少样板代码
- MyBatis-Plus 查询使用 LambdaQueryWrapper
- 统一使用 `Result` 包装响应

## 安全要求
- 禁止在代码中硬编码密钥
- 支付宝私钥等敏感配置通过环境变量或配置中心管理
- 商户密钥定期轮换
