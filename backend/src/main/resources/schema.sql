CREATE DATABASE IF NOT EXISTS cloudbrain DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cloudbrain;

DROP TABLE IF EXISTS prescription_check;
DROP TABLE IF EXISTS prescription;
DROP TABLE IF EXISTS medical_record;
DROP TABLE IF EXISTS triage;
DROP TABLE IF EXISTS registration;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS patient;

CREATE TABLE patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '患者ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    phone VARCHAR(20) COMMENT '手机号',
    id_card VARCHAR(18) COMMENT '身份证号',
    address VARCHAR(255) COMMENT '地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者表';

CREATE TABLE doctor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '医生ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    department VARCHAR(50) NOT NULL COMMENT '科室',
    title VARCHAR(50) COMMENT '职称',
    hospital VARCHAR(100) COMMENT '医院',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    introduction TEXT COMMENT '简介',
    status VARCHAR(20) DEFAULT 'APPROVED' COMMENT '审核状态: PENDING/APPROVED/REJECTED',
    reject_reason VARCHAR(255) COMMENT '拒绝原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

CREATE TABLE registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '挂号ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    department VARCHAR(50) NOT NULL COMMENT '科室',
    registration_date DATE NOT NULL COMMENT '挂号日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '时间段',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待就诊，completed-已完成，cancelled-已取消',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号记录表';

CREATE TABLE triage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分诊ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    chief_complaint TEXT NOT NULL COMMENT '主诉',
    recommended_department VARCHAR(50) COMMENT '推荐科室',
    recommended_doctor_ids TEXT COMMENT '推荐医生ID列表（逗号分隔）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分诊记录表';

CREATE TABLE medical_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '病历ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    registration_id BIGINT COMMENT '挂号ID',
    chief_complaint TEXT COMMENT '主诉',
    present_illness TEXT COMMENT '现病史',
    past_history TEXT COMMENT '既往史',
    physical_examination TEXT COMMENT '体格检查',
    diagnosis TEXT COMMENT '初步诊断',
    treatment_plan TEXT COMMENT '治疗意见',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    FOREIGN KEY (registration_id) REFERENCES registration(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历表';

CREATE TABLE prescription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '处方ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    registration_id BIGINT COMMENT '挂号ID',
    medicine_list TEXT NOT NULL COMMENT '药品列表（JSON格式）',
    dosage TEXT COMMENT '剂量说明',
    `usage` TEXT COMMENT '用法说明',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft-草稿，submitted-已提交，checked-已审核',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    FOREIGN KEY (registration_id) REFERENCES registration(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方表';

CREATE TABLE prescription_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审核ID',
    prescription_id BIGINT NOT NULL COMMENT '处方ID',
    check_result TEXT COMMENT '审核结果',
    medication_suggestions TEXT COMMENT '用药建议',
    interaction_detection TEXT COMMENT '药物相互作用检测',
    risk_level VARCHAR(20) DEFAULT 'low' COMMENT '风险等级：low-低，medium-中，high-高',
    risk_hints TEXT COMMENT '风险提示',
    checked_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    FOREIGN KEY (prescription_id) REFERENCES prescription(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方审核表';

INSERT INTO doctor (username, password, name, gender, age, department, title, hospital, phone, status) VALUES
('doctor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王明', '男', 45, '心内科', '主任医师', '东软医院', '13800138001', 'APPROVED'),
('doctor2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李芳', '女', 38, '呼吸内科', '副主任医师', '东软医院', '13800138002', 'APPROVED'),
('doctor3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张勇', '男', 50, '骨科', '主任医师', '东软医院', '13800138003', 'APPROVED'),
('doctor4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈静', '女', 35, '儿科', '主治医师', '东软医院', '13800138004', 'APPROVED'),
('doctor5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘伟', '男', 42, '神经内科', '副主任医师', '东软医院', '13800138005', 'APPROVED');
