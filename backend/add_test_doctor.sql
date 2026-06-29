-- 添加测试医生2：王医生 (doctor002/123456)
-- 状态设为 APPROVED 可以直接登录

INSERT INTO doctor (username, password, name, gender, age, department, title, hospital, phone, status, created_at, updated_at)
VALUES (
    'doctor002',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',  -- 123456
    '王医生',
    '男',
    38,
    '心内科',
    '副主任医师',
    '智慧云脑医院',
    '13900000005',
    'APPROVED',
    NOW(),
    NOW()
);
