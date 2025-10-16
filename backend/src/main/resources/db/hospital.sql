-- 用户信息表
DROP TABLE IF EXISTS users;
CREATE TABLE users (
                       user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
                       username VARCHAR(50)  UNIQUE COMMENT '用户真实姓名',
                       password VARCHAR(100) NOT NULL COMMENT '加密后的密码',
                       gender CHAR(1) DEFAULT NULL COMMENT '性别：M=男，F=女',
                       phone CHAR(11) NOT NULL UNIQUE COMMENT '手机号',
                       id_card CHAR(18) NOT NULL UNIQUE COMMENT '身份证号',
                       email VARCHAR(100) UNIQUE COMMENT '电子邮箱',
                       role_type ENUM('admin','doctor','patient') NOT NULL,
                       status TINYINT NOT NULL DEFAULT 1 COMMENT '0=active,1=pending,2=disabled',
                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 医院科室表
DROP TABLE IF EXISTS departments;
CREATE TABLE departments (
                             dept_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '科室ID，主键',
                             dept_name VARCHAR(50) NOT NULL UNIQUE COMMENT '科室名称，如"消化内科"',
                             building VARCHAR(50) NOT NULL COMMENT '所在楼宇名称，如"东楼"',
                             floor TINYINT NOT NULL COMMENT '所在楼层，如2',
                             room VARCHAR(20) NOT NULL COMMENT '房间号/诊室号，如201',
                             description VARCHAR(255) COMMENT '科室简介或说明'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院科室表';

-- 诊室表
DROP TABLE IF EXISTS consultation_rooms;
CREATE TABLE consultation_rooms (
                                    room_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '诊室ID，主键',
                                    dept_id INT NOT NULL COMMENT '所属科室ID，外键关联departments(dept_id)',
                                    room_name VARCHAR(20) NOT NULL COMMENT '诊室名称/编号，如"201室"',
                                    building VARCHAR(50) NOT NULL COMMENT '所在楼宇名称，可冗余科室的楼宇信息',
                                    floor TINYINT NOT NULL COMMENT '所在楼层，可冗余科室的楼层信息',
                                    description VARCHAR(255) COMMENT '诊室备注或说明',
                                    CONSTRAINT fk_room_dept FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
                                        ON UPDATE CASCADE
                                        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诊室表';

-- 医生信息表
DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (
                         doctor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         dept_id INT NOT NULL,
                         title ENUM('住院医师','主治医师','副主任医师','主任医师') NOT NULL COMMENT '职称',
                         bio TEXT,
                         status TINYINT NOT NULL DEFAULT 0 COMMENT '在职状态：0=active,1=on_leave,2=retired',
                         FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                         FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生信息表';

-- 患者信息表
DROP TABLE IF EXISTS patients;
CREATE TABLE patients (
                          patient_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '患者ID，主键',
                          user_id BIGINT NOT NULL COMMENT '关联用户ID，外键关联users(user_id)',
    patient_account varchar(20) not null comment '患者的学号/教工号',
                          identity_type ENUM('student','teacher','staff') NOT NULL COMMENT '身份类型',
                          birth_date DATE COMMENT '出生日期',
                          address VARCHAR(255) COMMENT '家庭地址',
                          height DECIMAL(5,2) COMMENT '身高(cm)',
                          weight DECIMAL(5,2) COMMENT '体重(kg)',
                          blood_type ENUM('A','B','AB','O','Unknown') NOT NULL DEFAULT 'Unknown' COMMENT '血型',
                          medical_history TEXT COMMENT '既往病史',
                          emergency_contact VARCHAR(50) COMMENT '紧急联系人姓名',
                          emergency_phone VARCHAR(20) COMMENT '紧急联系人电话',
                          FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者信息表';

-- 挂号类型表
DROP TABLE IF EXISTS appointment_types;
CREATE TABLE appointment_types (
                                   appointment_type_id INT AUTO_INCREMENT PRIMARY KEY,
                                   type_name VARCHAR(30) NOT NULL UNIQUE,
                                   fee_amount DECIMAL(8,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号类型表';

-- 医生排班表
DROP TABLE IF EXISTS schedules;
CREATE TABLE schedules (
                           schedule_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '排班ID，主键',
                           doctor_id BIGINT NOT NULL COMMENT '医生ID，外键关联doctors表',
                           dept_id INT NOT NULL COMMENT '所属科室ID，外键关联departments(dept_id)',
                           room_id INT NOT NULL COMMENT '诊室ID，外键关联consultation_rooms(room_id)',
                           work_date DATE NOT NULL COMMENT '排班日期',
                           time_slot TINYINT NOT NULL COMMENT '时间段，0=上午,1=下午,2=晚上',
                           appointment_type_id INT NOT NULL COMMENT '号别ID，外键关联appointment_types表',
                           max_slots INT NOT NULL DEFAULT 10 COMMENT '该时间段最大号源数',
                           available_slots INT NOT NULL DEFAULT 10 COMMENT '当前剩余号源数',
                           status TINYINT NOT NULL DEFAULT 0 COMMENT '排班状态:0=开放,1=已满,2=停诊',
                           FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
                           FOREIGN KEY (dept_id) REFERENCES departments(dept_id),
                           FOREIGN KEY (room_id) REFERENCES consultation_rooms(room_id),
                           FOREIGN KEY (appointment_type_id) REFERENCES appointment_types(appointment_type_id),
                           UNIQUE (doctor_id, work_date, time_slot, room_id) COMMENT '同一医生同一天同时间段同诊室唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生排班表';

-- 挂号记录表
DROP TABLE IF EXISTS appointments;
CREATE TABLE appointments (
                              appointment_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '挂号ID，主键',
                              patient_id BIGINT NOT NULL COMMENT '患者ID，外键关联patients表',
                              schedule_id INT NOT NULL COMMENT '关联排班ID，外键关联schedules表',
                              dept_id INT NOT NULL COMMENT '挂号科室ID，外键关联departments(dept_id)',
                              room_id INT NOT NULL COMMENT '挂号诊室ID，外键关联consultation_rooms(room_id)',
                              appointment_type_id INT NOT NULL COMMENT '号别ID，外键关联appointment_types表',
                              payment_status ENUM('unpaid','paid','expired','refunded') DEFAULT 'unpaid' COMMENT '支付状态',
                              appointment_status ENUM('pending','booked','cancelled','completed','no_show') DEFAULT 'pending' COMMENT '挂号状态',
                              fee_original DECIMAL(8,2) NOT NULL COMMENT '原始挂号费',
                              fee_final DECIMAL(8,2) NOT NULL COMMENT '实际支付挂号费',
                              booking_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '挂号时间',
                              expire_time DATETIME NULL COMMENT '挂号支付超时时间（默认下单后20分钟）',
                              visit_time DATETIME COMMENT '实际就诊时间',
                              notes VARCHAR(255) COMMENT '备注信息',
                              FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
                              FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id),
                              FOREIGN KEY (dept_id) REFERENCES departments(dept_id),
                              FOREIGN KEY (room_id) REFERENCES consultation_rooms(room_id),
                              FOREIGN KEY (appointment_type_id) REFERENCES appointment_types(appointment_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号记录表';