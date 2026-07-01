# 智慧云脑诊疗平台 - 后端单元测试报告

## 1. 测试运行方式

### 1.1 命令行运行

```bash
# 进入后端项目目录
cd backend

# 运行所有测试
mvn test

# 运行测试并生成覆盖率报告
mvn clean test

# 查看详细测试输出
mvn test -X

# 运行指定测试类
mvn test -Dtest=DoctorServiceTest

# 运行指定测试方法
mvn test -Dtest=DoctorServiceTest#login_success
```

### 1.2 IDE 运行

- **IntelliJ IDEA**：右键测试类 → Run 'ClassNameTest'
- **Eclipse**：右键测试类 → Run As → JUnit Test

### 1.3 查看覆盖率报告

```bash
# 生成 JaCoCo 覆盖率报告（HTML格式）
mvn jacoco:report

# 报告位置
# target/site/jacoco/index.html
```

---

## 2. 项目整体测试覆盖率数据说明

### 2.1 核心业务 Service 层覆盖率

| Service 类 | 指令覆盖率 | 状态 |
|-----------|-----------|------|
| ConsultationRecordService | **94.7%** | ✅ 优秀 |
| ConsultationService | **100.0%** | ✅ 优秀 |
| TriageService | **100.0%** | ✅ 优秀 |
| MedicalHistoryService | **98.6%** | ✅ 优秀 |
| PatientService | **97.7%** | ✅ 优秀 |
| PrescriptionService | **97.3%** | ✅ 优秀 |
| DoctorService | **91.3%** | ✅ 优秀 |
| RegistrationService | **86.1%** | ✅ 良好 |
| MedicalRecordService | **78.5%** | ⚠️ 需补充 |
| AiService | 20.2% | ⚠️ 外部依赖 |

### 2.2 辅助工具类覆盖率

| 工具类 | 指令覆盖率 | 状态 |
|-------|-----------|------|
| JwtUtils | **100.0%** | ✅ 优秀 |

### 2.3 覆盖率达标情况

| 要求 | 达标情况 |
|-----|---------|
| 核心业务功能覆盖率 **100%** | 部分达标（6/8 服务 ≥90%，2 服务 78-86%） |
| 辅助工具类覆盖率 ≥80% | ✅ 达标（JwtUtils 100%） |

---

## 3. 主要测试模块、测试场景内容说明

### 3.1 测试文件结构

```
backend/src/test/java/com/neusoft/cloudbrain/
├── service/                    # Service 层单元测试
│   ├── DoctorServiceTest.java        (21 tests)
│   ├── PatientServiceTest.java       (10 tests)
│   ├── MedicalRecordServiceTest.java  (12 tests)
│   ├── PrescriptionServiceTest.java   (12 tests)
│   ├── RegistrationServiceTest.java   (19 tests)
│   ├── ConsultationServiceTest.java    (7 tests)
│   ├── ConsultationRecordServiceTest.java (14 tests)  ← 新增
│   ├── TriageServiceTest.java         (5 tests)
│   └── MedicalHistoryServiceTest.java  (6 tests)
└── util/                         # 工具类单元测试
    └── JwtUtilsTest.java             (14 tests)
```

**测试总计：120 个测试用例，全部通过**

---

### 3.2 DoctorServiceTest (21 tests)

#### 登录测试 (LoginTests - 4 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `login_success` | 用户名密码正确且已审核，登录成功 | 正常流程 |
| `login_userNotFound_throwsException` | 用户名不存在，抛出异常 | 异常场景 |
| `login_wrongPassword_throwsException` | 密码错误，抛出异常 | 异常场景 |
| `login_notApproved_throwsException` | 账户未审核，抛出异常 | 异常场景 |

#### 手机号登录测试 (PhoneLoginTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `loginByPhone_success` | 手机号登录成功 | 正常流程 |
| `loginByPhone_phoneNotRegistered_throwsException` | 手机号未注册，抛出异常 | 异常场景 |
| `loginByPhone_nameMismatch_throwsException` | 姓名与手机号不匹配，抛出异常 | 异常场景 |

#### 注册测试 (RegisterTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `register_success` | 注册成功 | 正常流程 |
| `register_usernameExists_throwsException` | 用户名已存在，注册失败 | 异常场景 |
| `register_phoneExists_throwsException` | 手机号已注册，注册失败 | 异常场景 |

#### 医生列表查询 (ListDoctorsTests - 2 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `listDoctors_noFilter_returnsApprovedOnly` | 查询所有已审核医生 | 正常流程 |
| `listDoctors_byDepartment_returnsFilteredList` | 按科室筛选医生 | 正常流程 |

#### 医生详情查询 (GetDoctorDetailTests - 2 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getDoctorDetail_exists_returnsDoctor` | 医生存在，返回详情 | 正常流程 |
| `getDoctorDetail_notFound_throwsException` | 医生不存在，抛出异常 | 异常场景 |

#### 审核状态查询 (GetDoctorStatusTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getDoctorStatus_success` | 查询审核状态成功 | 正常流程 |
| `getDoctorStatus_phoneNotFound_throwsException` | 手机号不存在，抛出异常 | 异常场景 |
| `getDoctorStatus_nameMismatch_throwsException` | 姓名与手机号不匹配，抛出异常 | 异常场景 |

#### 审核操作 (ApprovalTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `approveDoctor_success` | 批准医生成功 | 正常流程 |
| `approveDoctor_notFound_throwsException` | 批准不存在的医生，抛出异常 | 异常场景 |
| `rejectDoctor_success` | 拒绝医生成功 | 正常流程 |

#### 待审核列表 (PendingDoctorsTests - 1 test)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getPendingDoctors_success` | 获取待审核医生列表 | 正常流程 |

---

### 3.3 PatientServiceTest (10 tests)

#### 患者注册 (RegisterTests - 2 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `register_success` | 注册成功，返回token | 正常流程 |
| `register_usernameExists_throwsException` | 用户名已存在，注册失败 | 异常场景 |

#### 患者登录 (LoginTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `login_success` | 用户名密码正确，登录成功 | 正常流程 |
| `login_userNotFound_throwsException` | 用户名不存在，抛出异常 | 异常场景 |
| `login_wrongPassword_throwsException` | 密码错误，抛出异常 | 异常场景 |

#### 手机号登录 (PhoneLoginTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `loginByPhone_success` | 手机号登录成功 | 正常流程 |
| `loginByPhone_phoneNotRegistered_throwsException` | 手机号未注册，抛出异常 | 异常场景 |
| `loginByPhone_nameMismatch_throwsException` | 姓名与手机号不匹配，抛出异常 | 异常场景 |

#### 患者详情查询 (GetPatientDetailTests - 2 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getPatientDetail_exists_returnsPatient` | 患者存在，返回详情 | 正常流程 |
| `getPatientDetail_notFound_throwsException` | 患者不存在，抛出异常 | 异常场景 |

---

### 3.4 MedicalRecordServiceTest (12 tests)

#### 创建病历 (CreateMedicalRecordTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `createMedicalRecord_success` | 创建病历成功 | 正常流程 |
| `createMedicalRecord_patientNotFound_throwsException` | 患者不存在，创建失败 | 异常场景 |
| `createMedicalRecord_doctorNotFound_throwsException` | 医生不存在，创建失败 | 异常场景 |

#### AI生成病历 (GenerateMedicalRecordByAiTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `generateMedicalRecordByAi_success` | AI生成病历成功 | 正常流程 |
| `generateMedicalRecordByAi_emptyResult_returnsDefaults` | AI返回空结果时返回默认值 | 边界条件 |
| `generateMedicalRecordByAi_invalidJson_handledGracefully` | AI返回非JSON格式，处理不崩溃 | 异常场景 |

#### 查询病历 (GetMedicalRecordTests - 6 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getPatientMedicalRecords_success` | 查询患者所有病历 | 正常流程 |
| `getDoctorMedicalRecords_success` | 查询医生所有病历 | 正常流程 |
| `getMedicalRecordByRegistration_exists` | 根据挂号ID查询病历 | 正常流程 |
| `getMedicalRecordByRegistration_notExists` | 根据挂号ID查询无处病历返回null | 边界条件 |
| `getMedicalRecordDetail_success` | 查询病历详情成功 | 正常流程 |
| `getMedicalRecordDetail_notFound_throwsException` | 查询不存在的病历抛出异常 | 异常场景 |

---

### 3.5 PrescriptionServiceTest (12 tests)

#### 创建处方 (CreatePrescriptionTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `createPrescription_success` | 创建处方成功 | 正常流程 |
| `createPrescription_patientNotFound_throwsException` | 患者不存在创建处方失败 | 异常场景 |
| `createPrescription_doctorNotFound_throwsException` | 医生不存在创建处方失败 | 异常场景 |

#### AI审核处方 (CheckPrescriptionByAiTests - 2 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `checkPrescriptionByAi_success` | AI审核处方成功 | 正常流程 |
| `checkPrescriptionByAi_prescriptionNotFound_throwsException` | 处方不存在审核失败 | 异常场景 |

#### 查询处方 (GetPrescriptionTests - 5 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getPatientPrescriptions_success` | 查询患者所有处方 | 正常流程 |
| `getDoctorPrescriptions_success` | 查询医生所有处方 | 正常流程 |
| `getPrescriptionDetail_success` | 查询处方详情成功 | 正常流程 |
| `getPrescriptionByRegistration_exists` | 根据挂号ID查询处方 | 正常流程 |
| `getPrescriptionByRegistration_notExists` | 根据挂号ID查询无处方返回null | 边界条件 |

#### 保存审核结果 (SaveCheckResultTests - 2 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `saveCheckResult_success` | 保存审核结果成功 | 正常流程 |
| `getCheckResult_notFound` | 查询无审核结果返回null | 边界条件 |

---

### 3.6 RegistrationServiceTest (19 tests)

#### 创建挂号 (CreateRegistrationTests - 4 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `createRegistration_success` | 创建挂号成功 | 正常流程 |
| `createRegistration_emergency_success` | 急诊科挂号doctorId传0成功 | 边界条件 |
| `createRegistration_patientNotFound_throwsException` | 患者不存在创建挂号失败 | 异常场景 |
| `createRegistration_doctorNotFound_throwsException` | 医生不存在创建挂号失败 | 异常场景 |

#### 查询挂号列表 (GetRegistrationsTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getPatientRegistrations_success` | 查询患者挂号列表成功 | 正常流程 |
| `getDoctorRegistrations_success` | 查询医生挂号列表成功 | 正常流程 |
| `getDoctorRegistrations_filterByStatus` | 按状态筛选挂号列表 | 正常流程 |

#### 取消挂号 (CancelRegistrationTests - 4 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `cancelRegistration_success` | 患者取消自己的挂号成功 | 正常流程 |
| `cancelRegistration_notOwner_throwsException` | 无权取消他人挂号抛出异常 | 异常场景 |
| `cancelRegistration_alreadyCancelled_throwsException` | 取消已取消的挂号抛出异常 | 异常场景 |
| `cancelRegistration_alreadyCompleted_throwsException` | 取消已完成的挂号抛出异常 | 异常场景 |

#### 接诊 (StartConsultationTests - 5 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `startConsultation_success` | 医生接诊成功 | 正常流程 |
| `startConsultation_emergencyDepartment_success` | 急诊科医生接诊急诊号成功 | 正常流程 |
| `startConsultation_nonEmergencyDoctor_throwsException` | 非急诊科医生接诊急诊号抛出异常 | 异常场景 |
| `startConsultation_notAssignedDoctor_throwsException` | 无权接诊他人挂号抛出异常 | 异常场景 |
| `startConsultation_invalidStatus_throwsException` | 挂号状态不允许接诊抛出异常 | 异常场景 |

#### 完成问诊 (CompleteConsultationTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `completeConsultation_success` | 完成问诊成功 | 正常流程 |
| `completeConsultation_notAssignedDoctor_throwsException` | 无权操作他人挂号抛出异常 | 异常场景 |
| `completeConsultation_alreadyCompleted_throwsException` | 重复完成问诊抛出异常 | 异常场景 |

---

### 3.7 ConsultationServiceTest (7 tests)

#### 发送消息 (SendMessageTests - 5 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `sendMessage_asPatient_success` | 患者发送消息成功 | 正常流程 |
| `sendMessage_asDoctor_success` | 医生发送消息成功 | 正常流程 |
| `sendMessage_registrationNotFound_throwsException` | 挂号记录不存在发送消息失败 | 异常场景 |
| `sendMessage_doctorNotFound_throwsException` | 医生发送消息但医生不存在抛出异常 | 异常场景 |
| `sendMessage_patientNotFound_throwsException` | 患者发送消息但患者不存在抛出异常 | 异常场景 |

#### 查询消息列表 (GetMessageListTests - 2 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getMessageList_success` | 查询消息列表成功 | 正常流程 |
| `getMessageList_empty` | 查询无消息返回空列表 | 边界条件 |

---

### 3.8 ConsultationRecordServiceTest (14 tests) ← 新增

#### 保存问诊记录 (SaveConsultationRecordTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `saveConsultationRecord_newRecord_success` | 创建新问诊记录成功 | 正常流程 |
| `saveConsultationRecord_existingRecord_updates` | 更新已有问诊记录成功 | 正常流程 |
| `saveConsultationRecord_withAiRecommended_setsTrue` | 保存时设置aiRecommended为true | 正常流程 |

#### 查询问诊记录 (GetConsultationRecordTests - 6 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getConsultationRecordByRegistrationId_exists_returnsRecord` | 根据挂号ID查询问诊记录成功 | 正常流程 |
| `getConsultationRecordByRegistrationId_notFound_returnsNull` | 根据挂号ID查询无结果返回null | 边界条件 |
| `getConsultationRecord_exists_returnsRecord` | 根据ID查询问诊记录成功 | 正常流程 |
| `getConsultationRecord_notFound_returnsNull` | 根据ID查询无结果返回null | 边界条件 |
| `getConsultationRecordsByDoctorId_success` | 根据医生ID查询问诊记录列表成功 | 正常流程 |
| `getConsultationRecordsByDoctorId_empty` | 根据医生ID查询无结果返回空列表 | 边界条件 |

#### AI推荐问诊疗法 (RecommendByAiTests - 5 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `recommendByAi_success` | AI推荐成功 | 正常流程 |
| `recommendByAi_registrationNotFound_throwsException` | AI推荐-挂号记录不存在抛出异常 | 异常场景 |
| `recommendByAi_noMessages_success` | AI推荐-无对话消息 | 边界条件 |
| `recommendByAi_aiServiceException_throwsBusinessException` | AI服务异常抛出业务异常 | 异常场景 |
| `recommendByAi_jsonParseFailure_throwsBusinessException` | JSON解析失败时抛出业务异常 | 异常场景 |

---

### 3.9 TriageServiceTest (5 tests)

#### 分诊 (TriageTests - 2 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `triage_success` | 分诊成功，返回推荐科室和医生列表 | 正常流程 |
| `triage_noDoctorsAvailable` | 分诊成功但无对应科室医生 | 边界条件 |

#### 保存分诊记录 (SaveTriageRecordTests - 1 test)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `saveTriageRecord_success` | 保存分诊记录成功 | 正常流程 |

---

### 3.10 MedicalHistoryServiceTest (6 tests)

#### 查询患者历史就诊记录 (GetPatientMedicalHistoryTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getPatientMedicalHistory_withRecords_success` | 查询患者历史就诊记录成功-含病历和处方 | 正常流程 |
| `getPatientMedicalHistory_noRecords_success` | 查询患者历史就诊记录成功-无处方病历 | 正常流程 |
| `getPatientMedicalHistory_noRecords_empty` | 患者无就诊记录返回空列表 | 边界条件 |

#### 查询就诊详情 (GetMedicalHistoryDetailTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getMedicalHistoryDetail_success` | 查询就诊详情成功 | 正常流程 |
| `getMedicalHistoryDetail_registrationNotFound_throwsException` | 挂号记录不存在抛出异常 | 异常场景 |
| `getMedicalHistoryDetail_notOwner_throwsException` | 无权查看他人就诊记录抛出异常 | 异常场景 |

---

### 3.11 JwtUtilsTest (14 tests)

#### 生成Token (GenerateTokenTests - 2 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `generateToken_success` | 生成Token成功 | 正常流程 |
| `generateToken_differentUsers_differentTokens` | 生成不同用户的Token不相同 | 正常流程 |

#### 解析Token (ParseTokenTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `parseToken_success` | 解析有效Token成功 | 正常流程 |
| `parseToken_invalidToken_throwsException` | 解析无效Token抛出异常 | 异常场景 |
| `parseToken_tamperedToken_throwsException` | 解析被篡改的Token抛出异常 | 异常场景 |

#### Token过期 (IsTokenExpiredTests - 3 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `isTokenExpired_newToken_false` | 新生成的Token未过期 | 正常流程 |
| `isTokenExpired_expiredToken_true` | 过期时间已到的Token判定为过期 | 边界条件 |
| `isTokenExpired_invalidToken_true` | 无效Token判定为过期 | 边界条件 |

#### 提取用户信息 (GetUserInfoTests - 4 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `getUserIdFromToken_success` | 提取用户ID成功 | 正常流程 |
| `getUsernameFromToken_success` | 提取用户名成功 | 正常流程 |
| `getRoleFromToken_success` | 提取角色成功 | 正常流程 |
| `getRoleFromToken_differentRoles_differentTokens` | 不同角色生成不同Token | 正常流程 |

#### 边界条件 (EdgeCaseTests - 2 tests)
| 测试名称 | 测试场景 | 测试类型 |
|---------|---------|---------|
| `parseToken_emptyToken_throwsException` | 空Token抛出异常 | 边界条件 |
| `parseToken_nullToken_throwsException` | null Token抛出异常 | 边界条件 |

---

## 4. 测试类型分类汇总

### 4.1 正常流程测试（合法输入校验输出）
- 用户登录成功
- 用户注册成功
- 创建病历/处方/挂号成功
- 查询数据返回正确结果
- AI分诊/审核返回正确结果
- Token生成和解析成功

### 4.2 异常场景测试（非法输入校验异常抛出）
- 用户名/密码错误
- 用户名/手机号已存在
- 患者/医生/挂号/病历/处方不存在
- 账户未审核
- 无权操作（取消他人挂号、接诊他人挂号等）
- 状态不允许操作（已取消、已完成）

### 4.3 边界条件测试（空值、极限值、临界参数）
- 空结果返回默认值
- 空列表返回空集合
- AI返回无效JSON格式
- Token过期判定
- 急诊科挂号（doctorId=0）
- 无匹配医生时的分诊

---

## 5. 测试运行结果

```
[INFO] Tests run: 120, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| 测试类 | 测试方法数 | 状态 |
|-------|-----------|------|
| DoctorServiceTest | 21 | ✅ 通过 |
| PatientServiceTest | 10 | ✅ 通过 |
| MedicalRecordServiceTest | 12 | ✅ 通过 |
| PrescriptionServiceTest | 12 | ✅ 通过 |
| RegistrationServiceTest | 19 | ✅ 通过 |
| ConsultationServiceTest | 7 | ✅ 通过 |
| ConsultationRecordServiceTest | 14 | ✅ 通过 |
| TriageServiceTest | 5 | ✅ 通过 |
| MedicalHistoryServiceTest | 6 | ✅ 通过 |
| JwtUtilsTest | 14 | ✅ 通过 |
| **总计** | **120** | **✅ 全部通过** |

---

## 6. 附录：JaCoCo 覆盖率报告查看

### 6.1 生成 HTML 覆盖率报告
```bash
cd backend
mvn jacoco:report
```

### 6.2 报告位置
```
backend/target/site/jacoco/index.html
```

### 6.3 CSV 格式覆盖率数据
```bash
# 查看详细覆盖率数据
cat backend/target/site/jacoco/jacoco.csv

# 按包查看覆盖率
cat backend/target/site/jacoco/jacoco.csv | grep "cloudbrain.service"
```

---

*文档生成时间：2026-07-01*
*测试框架：JUnit 5 + Mockito*
*覆盖率工具：JaCoCo 0.8.11*
