# 智慧云脑诊疗平台 - 后端

基于 Spring Boot 3.2.0 + JPA + MySQL 的后端服务。

## 目录结构

```
backend/
├── src/main/java/com/neusoft/cloudbrain/
│   ├── CloudBrainApplication.java   # 启动类
│   ├── config/                      # 配置类
│   │   ├── CorsConfig.java          # 跨域配置
│   │   ├── JpaConfig.java           # JPA 审计配置
│   │   ├── JwtAuthFilter.java       # JWT 认证过滤器
│   │   ├── SwaggerConfig.java       # Knife4j / Swagger 配置
│   │   └── WebConfig.java           # Web 过滤器注册
│   ├── controller/                  # 控制器层（业务接口）
│   ├── service/                     # 业务服务层
│   ├── repository/                  # 数据访问层
│   ├── entity/                      # 数据库实体
│   ├── dto/                         # 数据传输对象
│   │   └── Result.java              # 统一响应结构
│   ├── util/                        # 工具类
│   │   └── JwtUtils.java            # JWT 生成与解析
│   └── exception/                   # 异常处理
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java
└── src/main/resources/
    ├── application.yml              # 主配置（公共配置）
    ├── application-dev.yml          # 开发环境配置
    └── schema.sql                   # 数据库初始化脚本
```

## 启动方式

```bash
mvn spring-boot:run
```

启动后访问：
- API 文档：http://localhost:8080/doc.html
- 健康检查：http://localhost:8080/

## API 接口规范

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

在 Controller 中使用：

```java
return Result.success(data);
return Result.fail("错误信息");
```

### 需要登录的接口

在请求头中携带：

```
Authorization: Bearer <token>
```

JWT 过滤器会自动解析，并把 `userId` / `username` / `role` 注入 request。

## 白名单接口（无需登录）

- `POST /api/patient/register` - 患者注册
- `POST /api/patient/login` - 患者登录
- `GET /api/doctor/list` - 医生列表
- `GET /api/doctor/detail` - 医生详情
- `POST /api/triage/consult` - AI 分诊咨询

其他接口需携带 JWT Token。
