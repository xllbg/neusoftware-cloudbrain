-- V2: 为 doctor 表添加审核状态字段
-- Hibernate ddl-auto=update 会自动执行，此脚本仅供手动迁移参考

ALTER TABLE doctor
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'APPROVED'
    COMMENT '审核状态: PENDING/APPROVED/REJECTED';

ALTER TABLE doctor
    ADD COLUMN IF NOT EXISTS reject_reason VARCHAR(255)
    COMMENT '拒绝原因';

-- 现有医生默认设为已批准
UPDATE doctor SET status = 'APPROVED' WHERE status IS NULL;
