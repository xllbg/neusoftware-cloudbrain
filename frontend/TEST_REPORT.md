# 智慧云脑诊疗平台 - 前端单元测试报告

## 1. 测试运行方式

### 1.1 环境要求

- Node.js >= 18
- npm >= 9

### 1.2 命令行运行

```bash
# 进入前端项目目录
cd frontend

# 运行所有测试
npm run test:run

# 运行测试并生成覆盖率报告
npm run test:coverage

# 交互式运行（监听模式）
npm run test
```

### 1.3 IDE 运行

- **VS Code**：安装 Vitest 插件后，可直接点击测试文件中的 `▶` 按钮运行
- **WebStorm**：右键测试文件 → Run

### 1.4 查看覆盖率报告

```bash
# 生成覆盖率报告（HTML格式）
npm run test:coverage

# 报告位置
# coverage/index.html
```

---

## 2. 测试结果概览

| 测试文件 | 测试数 | 状态 |
|---------|--------|------|
| [format.test.ts](src/utils/format.test.ts) | 24 | ✅ 通过 |
| [auth.test.ts](src/utils/auth.test.ts) | 30 | ✅ 通过 |
| [crypto.test.ts](src/utils/crypto.test.ts) | 4 | ✅ 通过 |
| [user.test.ts](src/stores/user.test.ts) | 21 | ✅ 通过 |
| [registration.test.ts](src/stores/registration.test.ts) | 15 | ✅ 通过 |
| [consultation.test.ts](src/stores/consultation.test.ts) | 7 | ✅ 通过 |
| [medicalRecord.test.ts](src/stores/medicalRecord.test.ts) | 10 | ✅ 通过 |
| [prescription.test.ts](src/stores/prescription.test.ts) | 11 | ✅ 通过 |
| **合计** | **122** | **全部通过 ✅** |

> **测试运行截图：**


<!-- TODO: 请在此处插入终端运行 npm run test:run 的截图，显示 8 passed / 122 passed -->


---

## 3. 测试覆盖率报告

| 模块 | 文件 | 语句覆盖 | 分支覆盖 | 函数覆盖 | 行覆盖 |
|------|------|---------|---------|---------|-------|
| 工具函数 | [format.ts](src/utils/format.ts) | 100% | 100% | 100% | 100% |
| 认证工具 | [auth.ts](src/utils/auth.ts) | 100% | 100% | 100% | 100% |
| RSA加密 | [crypto.ts](src/utils/crypto.ts) | 100% | 100% | 100% | 100% |
| 用户状态 | [stores/user.ts](src/stores/user.ts) | 96% | 96% | 100% | 96% |
| 挂号管理 | [stores/registration.ts](src/stores/registration.ts) | 100% | 100% | 100% | 100% |
| 问诊对话 | [stores/consultation.ts](src/stores/consultation.ts) | 100% | 100% | 100% | 100% |
| 病历管理 | [stores/medicalRecord.ts](src/stores/medicalRecord.ts) | 100% | 90% | 100% | 100% |
| 处方管理 | [stores/prescription.ts](src/stores/prescription.ts) | 91% | 91% | 88% | 91% |

> **覆盖率报告截图：**


<!-- TODO: 请在此处插入终端运行 npm run test:coverage 的覆盖率表格截图 -->


---

## 4. 测试用例详情

### 4.1 工具函数测试 — format.test.ts（24个测试）

| 测试分组 | 测试内容 | 数量 |
|---------|---------|------|
| formatDate | 日期格式化、空值处理、前导零补全 | 4 |
| formatDateTime | 日期时间格式化、空值处理、前导零补全 | 3 |
| getStatusTag | 各状态标签类型映射（warning/primary/success/info/danger）、大写兼容、未知状态 | 8 |
| getStatusLabel | 各状态中文标签映射、大写兼容、未知状态 | 9 |

### 4.2 认证工具测试 — auth.test.ts（30个测试）

| 测试分组 | 测试内容 | 数量 |
|---------|---------|------|
| doctor token | 存取、覆盖、空值 | 3 |
| doctor user | 存取、空值、无效JSON | 3 |
| removeDoctorToken | 清理token和user、空安全 | 2 |
| isDoctor | 正确角色、错误角色、空 | 3 |
| isPatient | 正确角色、错误角色、空 | 3 |
| isLoggedIn | 医生token、患者token、无token、双token | 4 |
| general token | 通用token存取、通用user存取、无效JSON、通用removeToken | 7 |
| patient token | 患者token/user存取、空值、无效JSON、移除 | 5 |

### 4.3 RSA加密测试 — crypto.test.ts（4个测试）

| 测试内容 | 说明 |
|---------|------|
| 获取公钥并加密 | 验证RSA加密流程正确 |
| 加密失败处理 | 加密返回空时抛出错误 |
| 公钥缓存 | 第一次获取后缓存，第二次不再请求 |
| 清除缓存 | clearPublicKey 正常执行 |

### 4.4 用户Store测试 — user.test.ts（21个测试）

**医生端（11个）：**

| 测试内容 | 说明 |
|---------|------|
| 初始化状态 | token/userInfo/login/doctor/patient 默认值 |
| 医生登录态读取 | 从storage读取后状态正确 |
| 用户名显示 | userName计算属性正确 |
| 空用户时用户名 | 无userInfo时userName为空 |
| isLoggedIn判断 | token存在时为true，null时为false |
| isDoctor判断 | DOCTOR角色为true，PATIENT为false |
| isPatient判断 | 医生路径下isPatient为false |
| 医生登出 | 清理token和user，不再登录 |
| 无用户登出 | 不会抛异常 |
| 医生账号登录API | 调用API成功后更新store |
| 医生手机号登录API | 调用API成功后更新store |

**患者端（10个）：**

| 测试内容 | 说明 |
|---------|------|
| 患者登录态读取 | 从storage读取后状态正确 |
| isPatient判断 | PATIENT角色为true，DOCTOR为false |
| 患者登出 | 清理患者token和user |
| 登出不影响医生 | 患者登出后医生token仍在 |
| 患者账号登录API | patientLogin调用成功 |
| 患者手机号登录API | patientLoginByPhone调用成功 |
| 患者注册API | patientRegister调用成功 |
| 登录API错误 | 登录失败时store不更新 |

### 4.5 挂号Store测试 — registration.test.ts（15个测试）

| 测试内容 | 说明 |
|---------|------|
| 初始化状态 | records为空，loading为false |
| 获取列表 | 查询医生端挂号列表 |
| 空列表处理 | 返回空数组 |
| 请求错误处理 | 网络错误时loading重置 |
| 创建挂号 | 新建挂号添加到列表 |
| 取消挂号 | 更新列表中的状态为cancelled |
| 开始接诊 | 更新状态为in_progress |
| 完成接诊 | 更新状态为completed |
| 找不到ID | 取消不存在的id不影响原记录 |
| 带症状创建 | 患者填写症状信息（"头痛三天"） |
| 按患者ID查询 | 患者查看自己的挂号 |
| 按状态筛选 | 筛选待接诊记录 |
| 急诊科挂号 | doctorId为0的急诊场景 |
| 多条记录 | 列表中有多条挂号记录 |
| 创建后保留旧记录 | unshift后列表长度正确 |

### 4.6 问诊Store测试 — consultation.test.ts（7个测试）

| 测试内容 | 说明 |
|---------|------|
| 初始化状态 | messages为空，loading/sending为false |
| 获取消息 | 按registrationId获取问诊记录 |
| 空消息 | 无消息时返回空数组 |
| 请求错误 | 网络错误时loading重置 |
| 发送消息 | 消息成功推送并追加到列表 |
| 添加消息 | 不调用API直接添加消息 |
| 清空消息 | clearMessages后列表为空 |

### 4.7 病历Store测试 — medicalRecord.test.ts（10个测试）

| 测试内容 | 说明 |
|---------|------|
| 初始化状态 | records为空，currentRecord为null |
| 获取列表 | 按条件查询病历列表 |
| createTime回退 | createTime不存在时使用createdAt |
| 空列表 | 无记录时返回空数组 |
| 请求错误 | 网络错误时loading重置 |
| 获取详情 | 设置currentRecord |
| 保存病历 | 调用save接口返回数据 |
| 按挂号ID查询 | 查询指定挂号的病历 |
| 查询错误 | 找不到时loading重置 |
| AI生成病历 | 调用AI接口生成病历结果 |

### 4.8 处方Store测试 — prescription.test.ts（11个测试）

| 测试内容 | 说明 |
|---------|------|
| 初始化状态 | records为空，currentRecord为null |
| 获取列表 | 按条件查询处方列表 |
| 空列表 | 无记录时返回空数组 |
| 请求错误 | 网络错误时loading重置 |
| 获取详情 | 设置currentRecord |
| 按挂号ID查询 | 查询指定挂号的处方 |
| 创建处方 | 新处方添加到列表头部 |
| 处方审核 | 调用check接口返回审核结果 |
| AI审核 | 调用aiCheck接口 |
| 推荐药品 | 按症状/科室推荐药品 |
| 保存审核结果 | 调用save-check接口保存 |

---

## 5. 测试环境

| 项目 | 版本 |
|------|------|
| Node.js | >= 18 |
| npm | >= 9 |
| Vitest | ^2.1.0 |
| jsdom | ^25.0.0 |
| @vue/test-utils | ^2.4.11 |
| 测试框架 | Vitest |
| 测试环境 | jsdom（浏览器环境模拟） |

---

## 6. 测试覆盖范围说明

- **红色/未覆盖部分**：覆盖率报告中 `.vue` 页面组件（App.vue、Login.vue、Consultation.vue 等）显示为红色，这是基于推荐的最佳实践，单元测试主要覆盖**工具函数层**和**状态管理层（Store）**，UI 组件层的测试建议使用 E2E 测试（如 Playwright/Cypress）进行。
- **已覆盖的核心逻辑**：认证授权、RSA加密、日期格式化、状态映射、登录/注册、挂号、问诊、病历、处方等核心业务逻辑。

---

## 7. 测试结果截图

<!-- TODO：请在下方插入两张截图 -->

### 7.1 测试运行结果

<!-- 插入 npm run test:run 的终端截图 -->


### 7.2 覆盖率报告

<!-- 插入 npm run test:coverage 的终端截图 -->
