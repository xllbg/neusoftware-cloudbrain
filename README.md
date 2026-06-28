# 智慧云脑诊疗平台

基于 Spring Boot 3 + Vue 3 前后端分离架构，融合 AI 大模型、知识图谱与大数据分析技术的全流程智能诊疗综合实训项目。

## 项目简介

本平台围绕 "AI+诊疗" 展开，采用前后端分离架构。后端基于 Spring Boot 3 框架，提供 RESTful API，整合 AI 模型接口、知识图谱、数据持久化与业务逻辑。前端基于 Vue 3 配合 TypeScript 与 Element Plus 组件库，实现患者端与医生端交互界面，通过 HTTP 客户端调用后端 API 完成数据展示与操作。

## 功能模块

| 模块 | 描述 |
|------|------|
| 患者管理 | 患者注册、登录、信息管理 |
| 医生管理 | 医生信息、科室管理 |
| 在线挂号 | 挂号创建、列表、取消 |
| AI 智能分诊 | 输入主诉，智能推荐科室与医生 |
| AI 处方审核 | 处方开具 + AI 审核 + 风险提示 |
| AI 病历生成 | 医患对话 → 结构化病历 |

## 技术栈

### 后端

- Spring Boot 3.2.0
- Spring Data JPA
- MySQL 8.0+
- Knife4j (Swagger3)
- JWT
- Lombok

### 前端

- Vue 3 + TypeScript
- Element Plus
- Axios
- Pinia
- Vue Router
- Vite

## 开发成员及分工

| 成员 | 角色 | 主要职责 |
|------|------|---------|
| wyq | 后端基础设施担当 | Spring Boot 项目搭建、数据库设计、CORS/JWT/Knife4j 等基础设施 |
| zyx | 后端业务担当 | 患者/医生/挂号业务接口、AI 服务封装 |
| dyr | 前端基础设施担当 | Vue 3 项目搭建、Axios 拦截器、Pinia、路由配置 |
| lyy | 患者端前端担当 | 注册/登录/个人中心、智能分诊页、挂号页 |
| jzy | 医生端前端担当 | 挂号患者列表、问诊页、处方开具 + AI 审核、病历录入 + AI 生成 |

## 快速开始

### 后端

```bash
cd backend
mvn spring-boot:run
```

访问 API 文档：http://localhost:8080/doc.html

### 前端

```bash
cd frontend
npm install
npm run dev
```

访问前端：http://localhost:5173

## 数据库

数据库建表脚本位于 [backend/src/main/resources/schema.sql](backend/src/main/resources/schema.sql)。

## 项目规范

### 代码提交规范

```
feat:     新增功能
fix:      修复 bug
docs:     文档变更
style:    代码格式
refactor: 重构代码
chore:    构建/工具
```

### 分支策略

- `main`：稳定主线
- `develop`：开发分支（从 develop 切出 feature 分支，PR 合并回 develop）
- `feature/*`：各成员的功能分支


