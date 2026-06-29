-- 添加测试患者2： 李四 (patient002/123456)
-- 注意：密码123456经过BCrypt加密后的值

INSERT INTO patient (username, password, name, gender, age, phone, id_card, address, created_at, updated_at)
VALUES (
    'patient002',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',  -- 123456
    '李四',
    '女',
    28,
    '13800000003',
    '210000199801010032',
    '北京市朝阳区某某街道',
    NOW(),
    NOW()
);
