-- 先删最下游的表（无其他表依赖它）
DROP TABLE IF EXISTS appointment_audit;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS refunds;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS waitlist;
DROP TABLE IF EXISTS schedule_exceptions;
DROP TABLE IF EXISTS doctor_leaves;
DROP TABLE IF EXISTS appointment_rules;
DROP TABLE IF EXISTS user_verifications;
DROP TABLE IF EXISTS logs;
DROP TABLE IF EXISTS appointment_histories;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS schedules;
DROP TABLE IF EXISTS appointment_types;
DROP TABLE IF EXISTS consultation_rooms;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS users;

-- 用户信息表
CREATE TABLE users (
                       user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
                       username VARCHAR(50) UNIQUE COMMENT '用户真实姓名',
                       password VARCHAR(100) NOT NULL COMMENT '加密后的密码',
                       gender CHAR(1) DEFAULT NULL COMMENT '性别：M=男，F=女',
                       phone CHAR(11) NOT NULL UNIQUE COMMENT '手机号',
                       email VARCHAR(100) UNIQUE COMMENT '电子邮箱',
                       role_type ENUM('admin','doctor','patient') NOT NULL,
                       status ENUM('unverified', 'pending', 'verified', 'rejected') DEFAULT 'unverified' COMMENT '用户当前身份认证状态',
                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 医院科室表
CREATE TABLE departments (
                             dept_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '科室ID，主键',
                             parent_dept_id INT DEFAULT NULL COMMENT '用于表示科室层级，比如 内科 -> 消化内科',
                             dept_name VARCHAR(50) NOT NULL UNIQUE COMMENT '科室名称，如"消化内科"',
                             building VARCHAR(50) NOT NULL COMMENT '所在楼宇名称，如"东楼"',
                             floor TINYINT NOT NULL COMMENT '所在楼层，如2',
                             room VARCHAR(20) NOT NULL COMMENT '房间号/诊室号，如201',
                             description VARCHAR(255) COMMENT '科室简介或说明',
                             FOREIGN KEY (parent_dept_id) REFERENCES departments(dept_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院科室表';

-- 医生信息表
CREATE TABLE doctors (
                         doctor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL UNIQUE ,
                         dept_id INT NOT NULL,
                         title ENUM('住院医师','主治医师','副主任医师','主任医师') NOT NULL COMMENT '职称',
                         bio TEXT,
                         status ENUM('active','on-leave','retired') DEFAULT 'active' COMMENT '医生状态：在职，暂时离岗（请假、进修、休假），离职/退休',
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                         FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生信息表';

-- 患者信息表
CREATE TABLE patients (
                          patient_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '患者ID，主键',
                          user_id BIGINT NOT NULL UNIQUE COMMENT '关联用户ID，外键关联users(user_id)',
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
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                          UNIQUE(user_id, patient_account) COMMENT '同一个用户对应的患者账号不能重复'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者信息表';

-- 诊室表
CREATE TABLE consultation_rooms (
                                    room_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '诊室ID，主键',
                                    dept_id INT NOT NULL COMMENT '所属科室ID，外键关联departments(dept_id)',
                                    room_name VARCHAR(20) NOT NULL COMMENT '诊室名称/编号，如"201"',
                                    building VARCHAR(50) NOT NULL COMMENT '所在楼宇名称，可冗余科室的楼宇信息',
                                    floor TINYINT NOT NULL COMMENT '所在楼层，可冗余科室的楼层信息',
                                    description VARCHAR(255) COMMENT '诊室备注或说明',
                                    CONSTRAINT fk_room_dept FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
                                        ON UPDATE CASCADE
                                        ON DELETE RESTRICT,
                                    UNIQUE (dept_id, room_name) COMMENT '同一科室的诊室唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诊室表';

-- 挂号类型表
CREATE TABLE appointment_types (
                                   appointment_type_id INT AUTO_INCREMENT PRIMARY KEY,
                                   type_key VARCHAR(50) NOT NULL UNIQUE COMMENT '系统内部标识（普通、专家、特需）',
                                   type_name VARCHAR(50) NOT NULL COMMENT '用户界面显示号别',
                                   fee_amount DECIMAL(8,2) NOT NULL COMMENT '挂号费',
                                   description VARCHAR(255),
                                   created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号类型表（支持管理端修改）';

-- 医生排班表
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
                           status ENUM ('open','full','cancelled') DEFAULT 'open' COMMENT '排班状态:开放,已满,停诊',
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
                           FOREIGN KEY (dept_id) REFERENCES departments(dept_id),
                           FOREIGN KEY (room_id) REFERENCES consultation_rooms(room_id),
                           FOREIGN KEY (appointment_type_id) REFERENCES appointment_types(appointment_type_id),
                           UNIQUE (doctor_id, work_date, time_slot, room_id) COMMENT '同一医生同一天同时间段同诊室唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生排班表（支持管理端修改）';

-- 排班例外（请假/停诊/临时调整）
CREATE TABLE schedule_exceptions (
                                     exception_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '例外记录ID，主键',
                                     schedule_id INT NULL COMMENT '关联原排班ID，部分调整或加号时使用',
                                     doctor_id BIGINT NULL COMMENT '关联医生ID，可单独指定医生',
                                     start_date DATE NOT NULL COMMENT '例外开始日期',
                                     end_date DATE NOT NULL COMMENT '例外结束日期',
                                     exception_type ENUM('leave','cancel_all','partial_adjust','special_add') NOT NULL COMMENT '例外类型：leave(医生请假，需审核）,cancel_all(停诊，管理员操作),partial_adjust(管理员调整部分排班),special_add(临时加号)',
                                     extra_slots INT DEFAULT 0 COMMENT '临时加号数量，仅 exception_type=special_add 时有效',
                                     adjusted_room_id INT NULL COMMENT '调整后的诊室ID，仅 exception_type=partial_adjust 时可用',
                                     adjusted_time_slot TINYINT NULL COMMENT '调整后的时间段，仅 exception_type=partial_adjust 时可用',
                                     adjusted_date DATE NULL COMMENT '调整后的日期，仅 exception_type=partial_adjust 时可用',
                                     reason VARCHAR(255) COMMENT '例外原因说明，如请假、开会、加号说明等',
                                     status ENUM('pending','approved','rejected') DEFAULT NULL COMMENT '申请状态（仅用于医生申请的调班）',
                                     created_by BIGINT NULL COMMENT '操作人ID（医生或管理员），允许为空',
                                     created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id) ON DELETE SET NULL,
                                     FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
                                     FOREIGN KEY (adjusted_room_id) REFERENCES consultation_rooms(room_id) ON DELETE SET NULL,
                                     FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班例外表（请假/停诊/部分调整/临时加号）';

-- 医生请假记录
CREATE TABLE doctor_leaves (
                               leave_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '请假记录ID，主键',
                               doctor_id BIGINT NOT NULL COMMENT '请假的医生ID，关联 doctors 表',
                               from_date DATE NOT NULL COMMENT '请假开始日期',
                               to_date DATE NOT NULL COMMENT '请假结束日期',
                               reason VARCHAR(255) COMMENT '请假原因说明',
                               status ENUM('pending','approved','rejected') DEFAULT 'pending' COMMENT '请假状态：pending=待审核, approved=批准, rejected=拒绝',
                               applied_by BIGINT COMMENT '提交请假申请的用户ID（通常是医生本人）',
                               applied_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请提交时间',
                               reviewed_by BIGINT COMMENT '审批人ID（管理员）',
                               reviewed_at DATETIME COMMENT '审批时间',
                               FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
                               FOREIGN KEY (applied_by) REFERENCES users(user_id),
                               FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生请假记录表';

-- 挂号记录表
CREATE TABLE appointments (
                              appointment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '挂号ID，主键',
                              patient_id BIGINT NOT NULL COMMENT '患者ID，外键关联patients表',
                              schedule_id INT NOT NULL COMMENT '关联排班ID，外键关联schedules表',
                              dept_id INT NOT NULL COMMENT '挂号科室ID，外键关联departments(dept_id)',
                              room_id INT NOT NULL COMMENT '挂号诊室ID，外键关联consultation_rooms(room_id)',
                              appointment_type_id INT NOT NULL COMMENT '号别ID，外键关联appointment_types表',
                              queue_number INT NOT NULL COMMENT '在该时段的排序（pre-assigned）',
                              payment_status ENUM('unpaid','paid','expired','refunded') DEFAULT 'unpaid' COMMENT '支付状态',
                              appointment_status ENUM('pending','booked','cancelled','completed','no_show') DEFAULT 'pending' COMMENT '挂号状态:已下单未支付，挂号成功，患者或管理员取消，就诊完完成，未到诊',
                              fee_original DECIMAL(8,2) NOT NULL COMMENT '原始挂号费',
                              fee_final DECIMAL(8,2) NOT NULL COMMENT '实际支付挂号费',
                              booking_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '挂号时间',
                              expire_time DATETIME NULL COMMENT '挂号支付超时时间（默认下单后20分钟）',
                              visit_time DATETIME COMMENT '实际就诊时间',
                              notes VARCHAR(255) COMMENT '备注信息',
                              created_by BIGINT NULL,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
                              FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id),
                              FOREIGN KEY (dept_id) REFERENCES departments(dept_id),
                              FOREIGN KEY (room_id) REFERENCES consultation_rooms(room_id),
                              FOREIGN KEY (appointment_type_id) REFERENCES appointment_types(appointment_type_id),
                              INDEX idx_patient(patient_id),
                              INDEX idx_schedule(schedule_id),
                              INDEX idx_booking_time(booking_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号记录表';

-- 候补队列（waitlist）
CREATE TABLE waitlist (
                          wait_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          schedule_id INT NOT NULL,
                          patient_id BIGINT NOT NULL,
                          requested_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          priority INT DEFAULT 0 COMMENT '优先级：可基于报销级别/紧急程度/先到先得',
                          status ENUM('waiting','notified','converted','cancelled') DEFAULT 'waiting' COMMENT '等待中,已通知可挂号,已转正式挂号,取消候补',
                          notified_at DATETIME,
                          converted_appointment_id BIGINT COMMENT '对应正式挂号id',
                          FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id) ON DELETE CASCADE,
                          FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
                          FOREIGN KEY (converted_appointment_id) REFERENCES appointments(appointment_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候补表';

-- 支付与退款
CREATE TABLE payments (
                          payment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支付记录ID',
                          appointment_id BIGINT NOT NULL COMMENT '关联的预约ID',
                          amount DECIMAL(10,2) NOT NULL COMMENT '本次实际支付金额，与appointments.fee_final对应但不做外键约束',
                          method ENUM('alipay','wechat','card','cash') NOT NULL COMMENT '支付方式',
                          trade_no VARCHAR(255) COMMENT '第三方交易号或订单号',
                          status ENUM('initiated','success','failed') DEFAULT 'initiated' COMMENT '支付状态：发起/成功/失败',
                          paid_at DATETIME COMMENT '支付完成时间',
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                          FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
                              ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

CREATE TABLE refunds (
                         refund_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         payment_id BIGINT,
                         appointment_id BIGINT,
                         amount DECIMAL(10,2) NOT NULL,
                         reason VARCHAR(255),
                         status ENUM('initiated','success','failed') DEFAULT 'initiated',
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         processed_at DATETIME COMMENT '实际退款完成时间',
                         FOREIGN KEY (payment_id) REFERENCES payments(payment_id),
                         FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '退款记录表';

CREATE TABLE notifications (
                               notification_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
                               user_id BIGINT NOT NULL COMMENT '接收通知的用户ID',
                               email VARCHAR(255) NOT NULL COMMENT '收件人邮箱地址（冗余存储，避免频繁JOIN users，保留历史记录）',
                               subject VARCHAR(255) NOT NULL COMMENT '邮件主题',
                               content TEXT NOT NULL COMMENT '邮件内容',
                               status ENUM('pending','sent','failed') DEFAULT 'pending' COMMENT '发送状态：待发送/已发送/发送失败',
                               sent_at DATETIME COMMENT '发送时间',
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件通知记录表';

CREATE TABLE appointment_histories (
                                       hist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       appointment_id BIGINT,
                                       action VARCHAR(50) NOT NULL COMMENT 'create/cancel/modify/payment/refund/convert_from_waitlist',
                                       actor_user_id BIGINT COMMENT '执行操作的用户ID（患者或管理员）',
                                       payload JSON COMMENT '记录操作时的关键数据快照（如旧状态/新状态/金额等）',
                                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                       FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id),
                                       FOREIGN KEY (actor_user_id) REFERENCES users(user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号历史表';

CREATE TABLE appointment_audit (
                                   audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   appointment_id BIGINT,
                                   old_data JSON,
                                   new_data JSON,
                                   changed_by BIGINT,
                                   changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id),
                                   FOREIGN KEY (changed_by) REFERENCES users(user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号审计快照（数据溯源）';

CREATE TABLE logs (
                      log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
                      user_id BIGINT COMMENT '执行操作的用户ID',
                      action ENUM('create','update','delete','login','logout','approve','reject') NOT NULL COMMENT '操作类型',
                      resource_type ENUM('user','doctor','appointment','schedule','payment','refund','rule','system') NOT NULL COMMENT '资源类型',
                      resource_id BIGINT COMMENT '被操作资源ID',
                      message TEXT COMMENT '详细说明（可选）',
                      ip VARCHAR(50) COMMENT '操作IP',
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                      FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

CREATE TABLE appointment_rules (
                                   rule_id INT AUTO_INCREMENT PRIMARY KEY,
                                   key_name VARCHAR(100) UNIQUE COMMENT '规则标识（系统识别用）',
                                   description VARCHAR(255) COMMENT '规则描述',
                                   config JSON COMMENT '规则配置内容（JSON结构）',
                                   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约规则表';

CREATE TABLE user_verifications (
                                    verification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    user_id BIGINT NOT NULL,
                                    identity_type ENUM('student','teacher','staff') NOT NULL COMMENT '身份类型',
                                    id_number VARCHAR(100),
                                    doc_url VARCHAR(500) COMMENT '上传的凭证（校园卡/证明）',
                                    status ENUM('pending','approved','rejected') DEFAULT 'pending',
                                    reviewed_by BIGINT NULL COMMENT '审核人',
                                    reviewed_at DATETIME NULL COMMENT '审核时间',
                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    rejection_reason VARCHAR(255) NULL COMMENT '拒绝理由',
                                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                    FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者身份认证（学生、教工）';