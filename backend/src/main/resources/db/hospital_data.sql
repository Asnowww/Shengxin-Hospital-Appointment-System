-- 修改departments.room为可空
ALTER TABLE departments MODIFY COLUMN room VARCHAR(20) NULL COMMENT '房间号/诊室号，如201';

ALTER DATABASE hospital CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE departments CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE doctors CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE departments MODIFY building VARCHAR(50) COMMENT '楼宇名称';
ALTER TABLE doctors MODIFY title VARCHAR(50) COMMENT '职称';


-- ==================== 科室和诊室数据 ====================

-- 一级科室：内科（1楼）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (NULL, '内科', '圣心楼', 1, NULL, '内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
SET @parent_内科 = LAST_INSERT_ID();
-- 二级科室：老年病科（1楼，诊室号：101，所属：内科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_内科, '老年病科', '圣心楼', 1, '101', '老年病科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '101', '圣心楼', 1, '老年病科诊室');

-- 二级科室：风湿免疫科（1楼，诊室号：112，所属：内科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_内科, '风湿免疫科', '圣心楼', 1, '112', '风湿免疫科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '112', '圣心楼', 1, '风湿免疫科诊室');

-- 二级科室：高血压科（1楼，诊室号：105，所属：内科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_内科, '高血压科', '圣心楼', 1, '105', '高血压科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '105', '圣心楼', 1, '高血压科诊室');

-- 二级科室：呼吸内科（1楼，诊室号：104，所属：内科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_内科, '呼吸内科', '圣心楼', 1, '104', '呼吸内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '104', '圣心楼', 1, '呼吸内科诊室');

-- 二级科室：内分泌科（1楼，诊室号：103，所属：内科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_内科, '内分泌科', '圣心楼', 1, '103', '内分泌科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '103', '圣心楼', 1, '内分泌科诊室');

-- 二级科室：神经内科（1楼，诊室号：102，所属：内科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_内科, '神经内科', '圣心楼', 1, '102', '神经内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '102', '圣心楼', 1, '神经内科诊室');

-- 一级科室：外科（5楼）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (NULL, '外科', '圣心楼', 5, NULL, '外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
SET @parent_外科 = LAST_INSERT_ID();
-- 二级科室：骨科（5楼，诊室号：502，所属：外科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_外科, '骨科', '圣心楼', 5, '502', '骨科专业诊治各类骨科疾病，包括骨折、关节疾病、脊柱疾病等，拥有先进的骨科治疗技术。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '502', '圣心楼', 5, '骨科诊室');

-- 二级科室：功能神经外科（5楼，诊室号：510，所属：外科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_外科, '功能神经外科', '圣心楼', 5, '510', '功能神经外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '510', '圣心楼', 5, '功能神经外科诊室');

-- 二级科室：心脏外科（5楼，诊室号：507，所属：外科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_外科, '心脏外科', '圣心楼', 5, '507', '心脏外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '507', '圣心楼', 5, '心脏外科诊室');

-- 二级科室：神经外科（5楼，诊室号：501，所属：外科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_外科, '神经外科', '圣心楼', 5, '501', '神经外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '501', '圣心楼', 5, '神经外科诊室');

-- 二级科室：烧伤整形科（5楼，诊室号：504，所属：外科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_外科, '烧伤整形科', '圣心楼', 5, '504', '烧伤整形科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '504', '圣心楼', 5, '烧伤整形科诊室');

-- 二级科室：普胸外科（5楼，诊室号：509，所属：外科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_外科, '普胸外科', '圣心楼', 5, '509', '普胸外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '509', '圣心楼', 5, '普胸外科诊室');

-- 一级科室：儿科（5楼）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (NULL, '儿科', '圣心楼', 5, NULL, '儿科专注于儿童健康成长，提供儿童常见病、多发病的诊疗服务，医护人员经验丰富，深受患儿家长信赖。');
SET @parent_儿科 = LAST_INSERT_ID();
-- 二级科室：小儿内科（5楼，诊室号：512，所属：儿科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_儿科, '小儿内科', '圣心楼', 5, '512', '小儿内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '512', '圣心楼', 5, '小儿内科诊室');

-- 二级科室：小儿外科（5楼，诊室号：511，所属：儿科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_儿科, '小儿外科', '圣心楼', 5, '511', '小儿外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '511', '圣心楼', 5, '小儿外科诊室');

-- 一级科室：妇产科（5楼）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (NULL, '妇产科', '圣心楼', 5, NULL, '妇产科为广大女性提供全方位的医疗保健服务，包括孕期检查、分娩、妇科疾病诊疗等。');
SET @parent_妇产科 = LAST_INSERT_ID();
-- 二级科室：妇科（5楼，诊室号：508，所属：妇产科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_妇产科, '妇科', '圣心楼', 5, '508', '妇科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '508', '圣心楼', 5, '妇科诊室');

-- 一级科室：医学检验科（5楼）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (NULL, '医学检验科', '圣心楼', 5, NULL, '医学检验科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
SET @parent_医学检验科 = LAST_INSERT_ID();
-- 二级科室：核医学科（5楼，诊室号：505，所属：医学检验科）
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, room, description) VALUES (@parent_医学检验科, '核医学科', '圣心楼', 5, '505', '核医学科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT IGNORE INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES ((SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '505', '圣心楼', 5, '核医学科诊室');


-- ==================== 医生数据 ====================

-- 医生：徐志红 (老年病科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐志红', '$2b$12$CC/P7CynA7gnNmqsexeBBOdj3PhcIrtUFaBgVqUXKh/gZAwPaHRd.', 'M', '19965241839', '19965241839@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年呼吸系统及心血管常见病，包括肺部感染，慢阻肺，慢支，肺结节，睡眠呼吸暂停，高血压，冠心病及体检报告的分析解读等。', 'active');

-- 医生：杜萱 (老年病科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杜萱', '$2b$12$zPYm5w3IBLollzmOBQCbz.Whf7dGkjCENeF.bggBtmf7f0wadTaGi', 'M', '18553778756', '18553778756@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '老年高血压，冠心病，心律失常，心力衰竭等心血管疾病的诊治。', 'active');

-- 医生：梁伟 (老年病科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('梁伟', '$2b$12$C71FQ0ciCWeu5oeNAoXNgeCKqNVC7VBS6prRVMvo3mjr.AZxE4Dti', 'F', '15266944844', '15266944844@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '血脂紊乱，高血压病，冠状动脉粥样硬化性心脏病。

', 'active');

-- 医生：庞小芬 (老年病科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('庞小芬', '$2b$12$dSEyCVU2MaGlukiNlncu.Ojm5NaKMQO77a/uZJo06vKGqRY/l4Fqe', 'M', '19461415646', '19461415646@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '老年代谢性疾病：原发及继发性骨质疏松症的诊断、治疗，老年人的骨关节炎，老年高血压、高脂血症，老年糖尿病等老年性疾病。', 'active');

-- 医生：曹久妹 (老年病科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曹久妹', '$2b$12$xrGKP4PiEofbM.bpnn5uHetHDYQ/xX4vkncK1VY8zJIy6HjrU9SNu', 'M', '13507943839', '13507943839@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '高血压病、冠心病、血脂异常、心律失常等心血管疾病的诊断与治疗，慢性病的健康管理。', 'active');

-- 医生：缪婕 (老年病科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('缪婕', '$2b$12$hYUyh2lIrguBhuH20tJ41.N5OeBUAc2Fal.pNoVBqedOW0EmiU.Je', 'M', '15469319644', '15469319644@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年内分泌疾病（甲状腺相关疾病、糖尿病等）', 'active');

-- 医生：赵雅洁 (老年病科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵雅洁', '$2b$12$QkPxxzx0bnd38X75hN7VWOeGn5gN/wPlumwr6TcmCCnTf/v2a60oi', 'F', '19146654552', '19146654552@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年肌少症，老年糖尿病，老年骨质疏松，老年甲状腺疾病与老年相关性疾病的诊治。', 'active');

-- 医生：苏征佳 (老年病科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('苏征佳', '$2b$12$K5DoWEvMW6BN76XLvGvsL.o2g9aFhcNB4O6n5yiRei5R9OVCOTt/S', 'F', '17234031070', '17234031070@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年血脂异常、冠心病、高血压的个体化治疗。', 'active');

-- 医生：杨程德 (风湿免疫科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨程德', '$2b$12$dIkPvxoZTqzulic.a0obyuRU5ywpXLZ9XNmuzQ.NHvxFTOdctNJLu', 'F', '13692749116', '13692749116@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '主任医师', '系统性红斑狼疮、抗磷脂综合征、强直性脊柱炎、类风湿关节炎', 'active');

-- 医生：滕佳临 (风湿免疫科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('滕佳临', '$2b$12$BP37jILYk5y8qr6rU6GtpufCJ965Ln/r16iy2XwH0OQcToOLwgnaC', 'F', '19774996843', '19774996843@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '主任医师', '系统性红斑狼疮等多种风湿免疫性疾病的诊治', 'active');

-- 医生：刘宏磊 (风湿免疫科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘宏磊', '$2b$12$Z5dbxJBTU9BnPaSIDkdxleHDFQj6aBm9KFgeWIxoVAvRRLDs8CNSy', 'F', '17306468299', '17306468299@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '系统性红斑狼疮、皮肌炎、类风湿性关节炎、强直性脊柱炎等', 'active');

-- 医生：程笑冰 (风湿免疫科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('程笑冰', '$2b$12$25pROKvuoFQlpR0AQIEDheaDVHCU5.SKo.t/6qiJqzWHOyx8PfuMi', 'M', '13810026086', '13810026086@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '关节痛及系统性红斑狼疮、皮肌炎等常见风湿病。', 'active');

-- 医生：叶俊娜 (风湿免疫科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶俊娜', '$2b$12$PR58bxafdBatRGijTXnVR.tB.40LwyiHhWxsv/lxlauNylfv/6RVu', 'M', '19410727955', '19410727955@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '系统性红斑狼疮，干燥综合症，类风湿关节炎，强直性脊柱炎，皮肌炎，硬皮病等', 'active');

-- 医生：苏禹同 (风湿免疫科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('苏禹同', '$2b$12$i0dkZ8/m0HxdLZRQQL1ftemeT6s/WFPt1sDwcW12aIXaOaPEZgN4S', 'M', '19349957310', '19349957310@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '皮肌炎、系统性红斑狼疮、类风湿性关节炎、强直性脊柱炎、成人斯蒂尔病等多种风湿性疾病的诊治', 'active');

-- 医生：石慧 (风湿免疫科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('石慧', '$2b$12$xK4/EN3fnnQjnvViDgJ26eIcGF.kgFekgkONG5QP7ugwKoSFKRJBS', 'M', '16398471886', '16398471886@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '系统性红斑狼疮、抗磷脂综合征、类风湿关节炎', 'active');

-- 医生：高平进 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('高平进', '$2b$12$NVYP0P0gwnN.shnseKIYfewqOAz0s/.T.lqmgSStwDWmoin379Lpq', 'F', '18995619255', '18995619255@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '并发症的诊断与治疗，单基因遗传性高血压（二级教授）', 'active');

-- 医生：陈绍行 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈绍行', '$2b$12$Q0yE39R3qUqrHBRS.5mfLevDwlxkE8nGvAp39POyVA1vetNVBGWam', 'F', '14497478786', '14497478786@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '降压药物合理应用、高血压及其相关疾病鉴别诊断和治疗。', 'active');

-- 医生：孔燕 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孔燕', '$2b$12$0B5l.0RldKgILtZxy08qZuMEOkDuML.Zso23kDo4V6lDjjFgChC5m', 'F', '14819595113', '14819595113@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '老年性及顽固性高血压、高血压合并糖尿病', 'active');

-- 医生：龚艳春 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('龚艳春', '$2b$12$ROr.7/fwcsefWGzHCDHyzeZdO5uZAvZwLyQTYUtpE1.uArha0pC6m', 'F', '18833953718', '18833953718@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '难治性高血压及内分泌性高血压的诊断和治疗', 'active');

-- 医生：胡亚蓉 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡亚蓉', '$2b$12$dtYmRmb0vu3bltXFANVzO.QyLRFhERh9ztmnjSbXCqoW/37/Dex/6', 'M', '17781802744', '17781802744@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压合并肾脏损害的诊断和治疗', 'active');

-- 医生：朱理敏 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱理敏', '$2b$12$oaaT4FxWcWLXu4ZASxfEEuIFPcigpilGjSV3JLPSXJ.iZCV7GxUvO', 'M', '17882893941', '17882893941@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '继发性高血压和难治性高血压的诊断与治疗', 'active');

-- 医生：蔡凯愉 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蔡凯愉', '$2b$12$aBPl0Fm1WdZ/6pDguJqkKO2R00Nfe0OkuLtlDVLCCD1Hs3brjZAJC', 'M', '14596348124', '14596348124@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压合并心脏病及糖尿病的诊治', 'active');

-- 医生：唐晓峰 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('唐晓峰', '$2b$12$Nr.R4H7Hrc00UfGKZVykcOWbmYI6QlmHSaxlp.2uq4MKsQSjmJdMy', 'F', '15787194506', '15787194506@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '难治性高血压的诊断筛查和治疗', 'active');

-- 医生：陶波 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陶波', '$2b$12$stb1sPHEe32XmA9PIN3KwOSTDS88tjLaCF1LIfhvzhwOnzawSE0iC', 'M', '18448195935', '18448195935@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '肾动脉狭窄等继发性高血压的筛查、难治性高血压的治疗、血脂调控。', 'active');

-- 医生：李燕 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李燕', '$2b$12$GKO5TlrDn0eM41YCuAKvmO2t3qVx7SBBY1Yh/8EkDrt.qs6zFnZfq', 'M', '14982403818', '14982403818@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主治医师', '高血压诊断和优化治疗', 'active');

-- 医生：张瑾 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张瑾', '$2b$12$S2YfQ8ZAFZLeMsjk2nRfzuvJsnA5QgrLLhG2pnOHCPE6ZxWpZ4WeS', 'M', '19438715135', '19438715135@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '继发性及原发性高血压的诊治及合理用药。', 'active');

-- 医生：王继光 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王继光', '$2b$12$.WHloWNIpPOPIwstySJIYe5IiFskr0DuD6ZniYOv6muhthUwn0MC.', 'F', '15171069472', '15171069472@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主治医师', '难治性高血压、合并靶器官损害高血压', 'active');

-- 医生：杨龑 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨龑', '$2b$12$86cYwbwuLATRemY4rvWAAuquRFvKpvXvFJ4lvDsMaNIesaf8.AvKS', 'M', '17870854579', '17870854579@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '特长：高血压心脏病的诊治、优化降压治疗', 'active');

-- 医生：盛长生 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('盛长生', '$2b$12$EGWf5jsgFq7kNEFJo6He2eC4ww6A6T6DXMu7pxvtB0PnCxvc5xqpG', 'F', '14803771909', '14803771909@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压及相关疾病的综合评估及病因诊治', 'active');

-- 医生：黄绮芳 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄绮芳', '$2b$12$tCDXfSmzGw4Ilaw4BpIrGeNDm2AxuteKOTaT2t2azCM7Em0ly/ufm', 'F', '16790256940', '16790256940@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压及合并靶器官损害的诊治、降压药物合理使用', 'active');

-- 医生：葛茜 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('葛茜', '$2b$12$CFl29X1Ps1x8uFIOKpz7hOn5PppOPj/QV.Zc1oZRQjaLK9Zf/skPy', 'F', '14384412919', '14384412919@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '难治性高血压及继发性高血压诊治，睡眠呼吸暂停相关高血压诊治。', 'active');

-- 医生：王彦 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王彦', '$2b$12$Tun.Vmgddr7cJiV2/c3bleVZEIp8gSdQy4OsFDtDU2D1D3JL42Xxi', 'M', '14899925830', '14899925830@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主治医师', '高血压病的诊断和优化治疗，遗传性高血压诊治', 'active');

-- 医生：许建忠 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('许建忠', '$2b$12$CWfd5E3zS0DO.DRtIFgLoOigy1QHR7CNlOqepjvQzAtxGILw9j4mS', 'F', '18727694430', '18727694430@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', ' 难治性高血压肾神经消融治疗，肾血管性高血压介入治疗', 'active');

-- 医生：徐婷嬿 (高血压科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐婷嬿', '$2b$12$a1O9RgNdNMvSGqrPvepyXO5MLuIX7OSnHMQiVaVXzz6giL/d1pTPe', 'F', '17528853029', '17528853029@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '高血压靶器官损害诊治及降压药物合理应用', 'active');

-- 医生：李敏 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李敏', '$2b$12$Z055joqR5Hq8Hp2V/G5ksuFLGJlJ2QsLNsfMOaOTcEQjnRK3i9/e6', 'F', '14248532577', '14248532577@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '哮喘,慢性阻塞性肺病,睡眠呼吸障碍,肺部感染', 'active');

-- 医生：高蓓莉   (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('高蓓莉  ', '$2b$12$dfsUIh.f5gQpsY4kcYKzM.gESanaacsM4oH03Rb1Auf8diZaOvvRe', 'F', '13911514914', '13911514914@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺癌、肺部感染和慢性阻塞性肺病等', 'active');

-- 医生：时国朝   (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('时国朝  ', '$2b$12$y0.YIYoR9a7UaymTmqIHUeXhObxR95nHnFIUGwi9PF4BETttz8iOq', 'M', '19217734861', '19217734861@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '呼吸系统疾病的诊治', 'active');

-- 医生：程齐俭 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('程齐俭', '$2b$12$waVD5u6STDg82pgDRa2cmujkO4tFtWahtUyTdbB7P6aXbk8ukLoX2', 'M', '18271779360', '18271779360@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部影像学诊断，急慢性下呼吸道阻塞性疾病、肺部感染性疾病、肺癌的诊断治疗。', 'active');

-- 医生：朱雪梅 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱雪梅', '$2b$12$.QjuytkdUPvidPNxudhwXO.DhzmPzIt42iqt4/cVmnxwvhkV/TloW', 'F', '17168212356', '17168212356@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '肺弥漫性间质疾病、肺癌、肺内节结及呼吸系统其他疾病的诊断、鉴别及治疗。', 'active');

-- 医生：汤葳 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汤葳', '$2b$12$G0uk8teJsErlyhRPQAjAludlTl3M0gemz5KRPUxS9sB4LB6bIFgMe', 'F', '16739830322', '16739830322@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '哮喘，慢阻肺，呼吸道过敏，肺部肿瘤等相关呼吸系统疾病的诊治', 'active');

-- 医生：项轶 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('项轶', '$2b$12$zLTrQofn2mwokW5tdYncpesOSFJ6dCmob5CPO7TfdCG.jxYpKCeza', 'F', '17369953851', '17369953851@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '各种肺部恶性肿瘤的诊治（化疗、靶向治疗）、戒烟', 'active');

-- 医生：李庆云 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李庆云', '$2b$12$bY5RMC/LVCvYr43fIkSl0.2JDSQenpIC3365XuvIkdR7uP4xxcMp6', 'M', '18873869166', '18873869166@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '睡眠呼吸疾病，慢性咳嗽，哮喘，慢性阻塞性肺疾病，肺癌，无创通气，肺部感染', 'active');

-- 医生：戴然然 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('戴然然', '$2b$12$LIdHul8KfSqqEK8w/TYu0u9OeOeBETyz65sGPT2okKpXp/wiHXqhu', 'M', '18676567501', '18676567501@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '慢性气道疾病及肺癌的诊治，戒烟', 'active');

-- 医生：倪磊 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('倪磊', '$2b$12$xuXT1i7iHjCFy7JtyA7X9Ox/CAOC/DBfoPnrGLNfu2aqxRKcDUC2O', 'F', '19788227492', '19788227492@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部结节、肺部恶性肿瘤、间质性肺病、慢性气道疾病等呼吸系统疾病的诊治。', 'active');

-- 医生：陈巍 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈巍', '$2b$12$RMkTHq/b.6uEf7Ej9pV.7OOacPXl6g6MRuoYMA2BOgifwSQYe.EZi', 'F', '13415143362', '13415143362@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺小结节、肺癌、哮喘、气管镜介入治疗', 'active');

-- 医生：过依 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('过依', '$2b$12$aR7CAlbMpoLhFvJoaeQK1epyZBHk9IivtEmxXgwWWTylV1gs6oGcu', 'F', '14587182120', '14587182120@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部感染，慢阻肺，哮喘，肺癌等呼吸系统疾病的诊治', 'active');

-- 医生：周敏 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周敏', '$2b$12$nRqrkeVp7TV2rPEncCATseFeoRupfGQ9eiMvpwBj0io7dEFhO0dEa', 'M', '18872751234', '18872751234@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '主任医师，正教授，博士研究生导师，法国居里研究所博士后；瑞金医院呼吸与危重症医学科副主任，上海市科委优秀学科带头人，中国医师协会呼吸病分会优秀中青年医师，上海市卫生系统三八红旗手和巾帼建功标兵；任中华医学会呼吸病分会呼吸治疗组副组长、中国医师协会呼吸分会慢阻肺委员会委员、上海市女医师协会肺癌专委会副主任委员等。主持科技部国家重点研发计划慢病专项和国家自然基金等多项科研项目，发表论文100余篇，其中SCI论文近50篇，参编或副主编专著多部，擅长重症哮喘、慢阻肺、肺部感染及肺癌等呼吸疾病的诊治。



专家门诊时间：周二下午，周四上午

特需专家门诊：周五上午

瑞金医院舟山分院：名医工作室（每月一次）', 'active');

-- 医生：丁永杰 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('丁永杰', '$2b$12$Yu4VqNmbgp7mho0IhmmcmOyjiaGDF7C3nY.H.XrxxxuEk7jUmFT4u', 'F', '17918150683', '17918150683@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '肺栓塞、肺动脉高压、肺源性心脏病、肺部感染。', 'active');

-- 医生：李宁 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李宁', '$2b$12$BdTZVTkvXjCV/qZan5MGme9fmwInzFmY8AvRN7rooKGkfE0wVye0K', 'M', '17214257751', '17214257751@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '无创通气治疗、睡眠呼吸障碍、肺部结节、咳嗽、肺部感染', 'active');

-- 医生：包志瑶 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('包志瑶', '$2b$12$NLDCtvxdngA9oAr.7MS.0.vnLRw6gAo9cIm6cCdX0dC6P8JAG.r2C', 'F', '19786066793', '19786066793@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '肺癌全程管理，肺部感染性疾病，慢性气道疾病规范化管理。', 'active');

-- 医生：王晓斐 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王晓斐', '$2b$12$E6TXZFhsZ4BeGWom4fUWOuQHI.VxD0aWFUkbHiIgjb9j7Db.yibXy', 'M', '14501486939', '14501486939@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部恶性肿瘤的个体化诊疗', 'active');

-- 医生：周剑平 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周剑平', '$2b$12$6Qc.ITmVMrPESDv9PJx9ge8Rxzwjh39LKZ.hyjiJ6XebEUzVgGopK', 'M', '17936043811', '17936043811@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '哮喘、肺炎、咳嗽、肺结节读片、气管镜诊治、戒烟干预。', 'active');

-- 医生：冯耘 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯耘', '$2b$12$EIrDYgwyHb7dmtEAE24TgOXwS2YaoRUOdnu9GVR/Pwji8x6vLUmXe', 'M', '17448059918', '17448059918@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部结节、咳嗽、支气管扩张、肺部感染、肺癌', 'active');

-- 医生：孙娴雯 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙娴雯', '$2b$12$pEF7UthnC6T4Wa2lr3zZf.nGeOhbI66LD7qvcebcoayIsFbNPeLk2', 'F', '13220117054', '13220117054@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '慢性气道疾病及肺血管疾病的规范化诊治。', 'active');

-- 医生：瞿介明 (呼吸内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('瞿介明', '$2b$12$jZb7lA2GxEBzKHc2GpS8D.VYCNVEinSWYB8.8zQ4p1miE0jKLr5Qu', 'F', '19966267851', '19966267851@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '慢性咳嗽、哮喘、COPD和肺癌等呼吸科常见病及呼吸疑难疾病的诊治。', 'active');

-- 医生：陈瑛 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈瑛', '$2b$12$j01GSOlqEyqKqT7vnS26TuLXCInvri9oPnwSauNsT2/6vXq.4GnPi', 'F', '14162196678', '14162196678@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '尿崩症、水、电解质紊乱、内分泌与代谢疾病', 'active');

-- 医生：汤正义 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汤正义', '$2b$12$ss0p6tp485snkiniGNaB2.fLuxT5vw0cs34b3zOZqrwegng/Am3lO', 'M', '17184564737', '17184564737@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病血糖控制与疑难并发症的诊治，甲状腺、肾上腺与垂体等内分泌病的疑难杂症', 'active');

-- 医生：刘建民 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘建民', '$2b$12$GTmUoZGULdW0CeA24n2TEef6x97wV6kiBEeD286X/WB6PBzeYNRjy', 'M', '18621828280', '18621828280@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲亢、肾上腺疾病、甲状旁腺疾病、骨质疏松', 'active');

-- 医生：赵红燕 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵红燕', '$2b$12$cErcLs6VQJyVyxmUf/fiLeV.1PdPFlqSoVxH2QV6MqLr8Db7GTece', 'M', '19671988889', '19671988889@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲状腺和甲状旁腺疾病以及骨质疏松症的诊治', 'active');

-- 医生：陈宇红 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈宇红', '$2b$12$XiZNCGoui3pyvPov3Cr/Xu4PETYiVKmrkLN4T4yvCNRvFB58Yg.bC', 'M', '14808402051', '14808402051@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病，甲状腺疾病的诊治', 'active');

-- 医生：王曙 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王曙', '$2b$12$xmLu3DiXOJrELLL7Or.JnuReAvrOyLreMf61.9vAynoCDXCVDh4kO', 'F', '17277303722', '17277303722@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '内分泌疾病、骨质疏松、糖尿病、甲状腺等', 'active');

-- 医生：毕宇芳 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('毕宇芳', '$2b$12$aj2igbNhhNrb79u3BLvah.euvjp0GSDHlD4GIahXM/jDeewgJ3.Ym', 'F', '17751325257', '17751325257@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病和甲状腺疾病的临床诊治（二级教授）', 'active');

-- 医生：洪洁 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('洪洁', '$2b$12$H0Gc2t4RoqF86kN1CiikzOz5ZNWQcwE5iOttGIAzsYvDVHtUoSdW6', 'F', '14679064766', '14679064766@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '肥胖和糖尿病的病因诊断和治疗。', 'active');

-- 医生：孙首悦 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙首悦', '$2b$12$pMnsYdfXC2Q8f2RMXmpF6usgEyh8nmpztmjJ1wg4/FnJCEdDL.UQO', 'M', '18434702822', '18434702822@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '内分泌科性腺发育异常', 'active');

-- 医生：孙立昊 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙立昊', '$2b$12$mdPXN8BeLyfCckrhSUn2q.bB87ydBnAVGPrMeqAPYsDq6yXHe0jge', 'F', '18797801251', '18797801251@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '骨质疏松、甲状旁腺疾病等代谢性骨病', 'active');

-- 医生：顾卫琼 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('顾卫琼', '$2b$12$ylJCfKj9JKMYismKHqJvwe5WRDUzDTtjyMFB33QrcbkRt5CtqWYvG', 'F', '16655742829', '16655742829@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '青少年糖尿病分型诊断、临床诊治与应用基础研究，以及干细胞新兴领域的探索和临床应用', 'active');

-- 医生：汪启迪 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汪启迪', '$2b$12$1sGaLHSghAhwn5/aVPStG.f7yZw9N6cJzJVWBtj6D.d/meJkIkawy', 'F', '13366186631', '13366186631@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病，甲状腺疾病，及其他内分泌代谢疾病的临床诊疗', 'active');

-- 医生：周瑜琳 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周瑜琳', '$2b$12$682LKNccdGstYjIdc3ObkeCjXHhzB4r2vtSPYWQSXRXXAhdRsx7Va', 'M', '13463016614', '13463016614@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '各种类型的甲状腺疾病及糖尿病诊治', 'active');

-- 医生：苏颋为 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('苏颋为', '$2b$12$HOlLGalZ1.kOJLkjZ61/XehGZioMaIWoRTF5h5jhovXpLjXMvCmt.', 'M', '17694768704', '17694768704@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病，甲状腺，内分泌相关肿瘤，电解质紊乱，垂体和肾上腺疾病。', 'active');

-- 医生：周丽斌 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周丽斌', '$2b$12$HoQ2h0pJ5f5tgSv1SJhIheqSi/D1pTUPlG9YPNgj3wy3H4HOsVa42', 'M', '17336456621', '17336456621@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲状腺等内分泌代谢性疾病的诊疗', 'active');

-- 医生：张翼飞 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张翼飞', '$2b$12$5E08NIEi0e0xRE8JprbIw.WBarkf9YfrRWdPFBAkOwW7Qtcaf80aa', 'M', '13860038427', '13860038427@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '擅长肥胖、代谢综合症、糖尿病及相关疾病的临床诊治', 'active');

-- 医生：顾燕云 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('顾燕云', '$2b$12$7IREEh389pmaM9QuuuX3WutyHKX5R47KKn5mLbJcvpsW3Qlz5uGE2', 'M', '14172370545', '14172370545@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '各种内分泌代谢病相关常见疾病，包括2型糖尿病、胰岛素抵抗、高尿酸血症及常见甲状腺疾患等', 'active');

-- 医生：陆洁莉 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陆洁莉', '$2b$12$SFkBPuWD3qUSZWxw.NiMDu63Ko23mcVy4dbR.6MP0QNvK1sjL53Gy', 'M', '19454794895', '19454794895@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲状腺疾病及内分泌代谢病', 'active');

-- 医生：姜蕾 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('姜蕾', '$2b$12$b7tenJqzFrNl7wMqVf8nt.2OluoSU5OmsXZ08lRdfgwKUVOUzyCGO', 'M', '17355555531', '17355555531@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '尿崩症等垂体—肾上腺疾病，糖尿病，甲状腺', 'active');

-- 医生：叶蕾 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶蕾', '$2b$12$Z1SShL8wHjZcXtw4GlvNpOmHW2JyoeUvlcmYpAmh87DJvycbokkBa', 'F', '18621209849', '18621209849@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '甲状腺癌、多发内分泌肿瘤、疑难甲状腺疾病', 'active');

-- 医生：陶蓓 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陶蓓', '$2b$12$w5LR2Zb1Rm42qMlN97ra5uvqcmSMC7V8FTuFO/e7u9aNMK/8b.bvm', 'M', '17242068763', '17242068763@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '钙磷代谢、骨质疏松、代谢性骨病的诊治', 'active');

-- 医生：周薇薇 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周薇薇', '$2b$12$z1mF1bGrDh6pEgazvJQ4j.yz3h493OLnQ/9dEQGoJh3q1A2lyIlOG', 'F', '14942478695', '14942478695@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '垂体肾上腺疾病', 'active');

-- 医生：田景琰 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('田景琰', '$2b$12$R/cTVBDylO408iPgM/bjA.Vwex.50TYbA2hPy6RpqtQm4Pc0xWWIa', 'F', '19537077308', '19537077308@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '擅长糖尿病初发病的诊治和糖尿病的强化治疗', 'active');

-- 医生：蒋怡然 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蒋怡然', '$2b$12$gqnvi5P66aZ3MJ6Dtow.BO2Z0vR2oBGnkBNqeZNGU2kR3t557X5gK', 'M', '13204078666', '13204078666@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '垂体肾上腺疾病', 'active');

-- 医生：张翠 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张翠', '$2b$12$fPA0Bjv7cOKlKhUtRIEHqefHWRU01tOqXZ5jAU2.5aPFq5ehODTTW', 'F', '15554814084', '15554814084@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '肾上腺疾病', 'active');

-- 医生：沈力韵 (内分泌科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈力韵', '$2b$12$6ZwxzfNAXXM.9QAG1aY4WOXr9v517RTwwt/aJZzEpZQ2/C2/W6yve', 'F', '16882839238', '16882839238@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '甲状腺眼病', 'active');

-- 医生：陈生弟 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈生弟', '$2b$12$yEbWHubhwGeT5adFA6FivOUlB68x.dImBhNHvYhitcHkdzzP34U.2', 'M', '18801642483', '18801642483@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病、运动障碍病、老年性痴呆、认知障碍、神经系统疑难杂症等各种神经系统疾病(二级教授)', 'active');

-- 医生：刘建荣 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘建荣', '$2b$12$wtPmZoHCSHQ5CqvK7BkaKOcykb0OTl0r2J9xCdMB5kTHnGquc4Byu', 'M', '13532311310', '13532311310@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '各种神经系统常见病、疑难和危重病人的诊断和治疗，以及脑血管病的诊断、治疗和预防', 'active');

-- 医生：王瑛 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王瑛', '$2b$12$phzyoqlg9i4jWKYt2Znfae.hnCWgTaq7ers9yFmI43A3aGR8/zWJe', 'F', '19217326729', '19217326729@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病、记忆障碍、睡眠障碍、神经系统疾病伴抑郁焦虑', 'active');

-- 医生：傅毅 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('傅毅', '$2b$12$Hapvw5YvzZEfTrNQ9IDOsO.RiYriVFDNyqoSsP2No5RwS3Rs3yU.2', 'M', '14304235259', '14304235259@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '脑血管病、偏头痛及头晕的诊断和治疗', 'active');

-- 医生：汤荟冬 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汤荟冬', '$2b$12$2VAV/3L49eTXDSMZlQ7Suu8WsBUmZQNS7/s2PbfZ.NuM.hIvFgKfa', 'F', '14552991960', '14552991960@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '记忆力减退、癫痫', 'active');

-- 医生：肖勤 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('肖勤', '$2b$12$15OMsiBFnsQmnIROhyiifuw5OSmFaY/ajtUR8v.VM.58oNcUPsyAi', 'M', '15596743109', '15596743109@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森氏病、肌萎缩、神经内科常见疾病', 'active');

-- 医生：邓钰蕾 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('邓钰蕾', '$2b$12$UN.j0sCYyDKlJ4sIbdWROu6.Y4vj3vrFzYqgt581ImThPY8YrvZ5K', 'M', '19180943908', '19180943908@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '癫痫等发作性疾病、老年性痴呆、头痛、睡眠障碍、脑卒中等神经系统疾病。', 'active');

-- 医生：马建芳 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('马建芳', '$2b$12$Xyqb6nZ6pmo1vp.41u//gectENB9u1JfaM82vC/NPqfpW7JiJjClm', 'F', '19690907304', '19690907304@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森、失眠、不宁腿，打鼾等睡眠障碍，姿势障碍，抽动症和斜颈，偏头痛等。', 'active');

-- 医生：任汝静 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('任汝静', '$2b$12$9fIYzKRYKJQVcN6ROPrOJuIq7mahL/pUwD0pee.HxcwZ/hk6WmCuu', 'M', '13800235246', '13800235246@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '痴呆、帕金森病、脑血管病、头痛头晕、癫痫、睡眠障碍等神经疾病。', 'active');

-- 医生：刘军 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘军', '$2b$12$YGtxx.qYFl6camM1/JHgiesMf4QC.I8rzsf6vByZMCpCFp68ULjni', 'M', '13909134796', '13909134796@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病、痴呆、肌张力障碍、肌萎缩、睡眠障碍、脑血管病等神经内科常见病和多发病的诊治。', 'active');

-- 医生：曾丽莉 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曾丽莉', '$2b$12$YQlAoYRtWiLJ3PMZw00ZCeucnJC.0v.Yv9Z7Pi30Ab1GY41LVQAkS', 'M', '14536383774', '14536383774@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病及相关情感认知障碍的诊断及治疗', 'active');

-- 医生：徐玮 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐玮', '$2b$12$LxVvEPwt0YquljMOLKKpJeP903zasDZ/fq/TBsh05Jc5LHc2uOy.e', 'F', '16329509408', '16329509408@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '记忆力障碍，睡眠障碍，头晕头痛及其他神经系统疾病的诊断和治疗。', 'active');

-- 医生：辛晓瑜 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('辛晓瑜', '$2b$12$fnwcjCXrXbeuK8VmqR9fWex1BCtwJObiPhucLTsCGZ9.V7dAj1M9a', 'F', '13276777762', '13276777762@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病及卒中后管理，血管性认知障碍，头晕', 'active');

-- 医生：吴逸雯 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴逸雯', '$2b$12$1./cMxqw9VOlkPqLz1BBdOxqcnB5oiEZ3gdiZswFssN7e4QLQM8lG', 'F', '13519212169', '13519212169@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '肌张力障碍、震颤、帕金森病、舞蹈、共济失调；肉毒毒素治疗；神经调控治疗', 'active');

-- 医生：周海燕 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周海燕', '$2b$12$tOV4VPPdw6a7exxHDXE.vetoNbczqFI8zwbJwP2GJVJrRmF2MkHKO', 'F', '19943026227', '19943026227@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森病，震颤，肌无力，肌萎缩，DBS程控，神经科常见病。', 'active');

-- 医生：陈晟 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈晟', '$2b$12$2S4tNb9kS8AW2IiHjnBofeMmb9VbjTLOr/73hEs8lOAZGJZof5aEq', 'F', '15554200826', '15554200826@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '擅长神经科常见疾病，头痛，头晕，脑血管意外，肌无力，肌萎缩等诊治，尤其擅长神经免疫性疾病（自身免疫性脑炎、多发性硬化、视神经脊髓炎、重症肌无力）、疑难疾病诊治', 'active');

-- 医生：潘静 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('潘静', '$2b$12$VTX3vzIRaXElfoUT9xA/IePOvmRFF4RTdjas91Z/iDdzVJyDD8WCi', 'F', '14303901283', '14303901283@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森病、特发性震颤、睡眠障碍、脑血管病', 'active');

-- 医生：谭玉燕 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('谭玉燕', '$2b$12$o36XPRUhH8dFn99pAu5/t.033LeUWOYhz08cflMGkURWQBUST15Hm', 'F', '14162795957', '14162795957@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病，多系统萎缩及其他运动障碍疾病。', 'active');

-- 医生：李彬寅 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李彬寅', '$2b$12$Um9ZRIXcomLuv2tOB7Hy1eiNGclaUSb9zXDTDf12priqM8AekygIC', 'M', '18436730606', '18436730606@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '阿尔兹海默病、血管性痴呆等认知障碍类疾病。', 'active');

-- 医生：尹豆 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('尹豆', '$2b$12$W96Nkb5mpSEemvHGmgKFRuK1pFnqzOqETHE91lyH6n3gbXFhW.Xk.', 'M', '13727255918', '13727255918@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病，脑动脉狭窄，神经血管介入治疗。', 'active');

-- 医生：李媛媛 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李媛媛', '$2b$12$W2YI/TStT1E5oQeJAPnvMOgQewFWshH4FDvvPetDYVBMDtF6aIXy6', 'F', '17670292350', '17670292350@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森病，震颤及睡眠行为障碍。', 'active');

-- 医生：杨晓东 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨晓东', '$2b$12$DgCKORPSq36LH24ECaym9OVMgyhdiv3dzcNoFA8pjv7QhkFAhMM5C', 'M', '13645276696', '13645276696@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森，脑血管病，头晕，失眠等神经内科常见病。', 'active');

-- 医生：沈帆霞 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈帆霞', '$2b$12$glI7N/Jp/NfpLBZRFo/Qcu9YorHsP4V.cgTT5uGjiJAOp5prN46ZG', 'M', '19299528037', '19299528037@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '各种脑血管病（如脑小血管病、血管炎、脑白质病）、血管性痴呆、头痛及其它神经系统常见与疑难病诊治。', 'active');

-- 医生：康文岩 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('康文岩', '$2b$12$hAAd92aYjMuW9hQuT8N2t.vZUsACd5MbkP6WFvj23.nQB4jMfnrXe', 'M', '17172972420', '17172972420@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '诊治帕金森、智能减退、脑血管病、头痛、头晕、睡眠障碍、肢体麻木、面肌抽搐', 'active');

-- 医生：江静雯 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('江静雯', '$2b$12$lpKQ7Jm4tWTl6XKiRdBYfeCpmtCTRhIJkYPMZAKcpfr55u.Yg4a2y', 'M', '16228727266', '16228727266@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '头晕、腔梗、脑白质病、记忆减退及神经科常见病。', 'active');

-- 医生：胡震 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡震', '$2b$12$/m7f3F6oEOgzkOwA44RRBOUmfbTHqrhITiFOBAHL152ws2/VeqBwi', 'M', '17738360085', '17738360085@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑梗塞，脑血管病的微创介入治疗，脑动脉支架植入术', 'active');

-- 医生：刘斌 (神经内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘斌', '$2b$12$Lp.Q8wkQe3VztpYl9slfR.jk2p682gb72FfN57X2lOnWJbI1QpgTu', 'M', '17188029013', '17188029013@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病的诊治，脑血管介入治疗。', 'active');

-- 医生：王亚梓 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王亚梓', '$2b$12$Q.4GFG76i87CoaL0ZXno.O4PX7TyOhKhYQe75hvf9K3p7C4II5vrG', 'F', '18726713347', '18726713347@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '四肢关节周围骨折、骨折愈合障碍、膝关节和肘关节屈伸功能障碍的治疗', 'active');

-- 医生：梁裕 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('梁裕', '$2b$12$zenjoIrUZVJtB7R.SoHd9uwfgTXDgtPtMSqaa/hnDNhUIv29ic.AW', 'F', '15319321646', '15319321646@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '脊柱骨折脱位伴神经损伤；脊柱畸形，包括青少年特发性脊柱侧弯、先天性脊柱侧弯、成人脊柱侧弯以及创伤后脊柱畸形等；脊柱退行性疾病，包括椎间盘突出，椎管狭窄，腰椎滑脱等', 'active');

-- 医生：张伟滨 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张伟滨', '$2b$12$gCOEWRDz1fhs.Oo4ypUad.sXgrKMYZGv25sbNkWyTjPMediwn6O0S', 'F', '14385201412', '14385201412@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '各类骨肿瘤保肢治疗，转移性骨肿瘤的综合治疗，骨质疏松诊治、各类复杂骨折', 'active');

-- 医生：冯建民 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯建民', '$2b$12$hNWZWnDv6HRlVpLFRgQxT.9JUKpJ3ujitVz6mC7iUSi7QLiSUcU1e', 'F', '14821221887', '14821221887@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '关节外科，骨关节病，人工关节', 'active');

-- 医生：王蕾 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王蕾', '$2b$12$JkglSrJPqMcW1H8LadXB3.Zt.HtmcsZ.TEBD10JJzpnzxxACckOOG', 'F', '16439492674', '16439492674@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '肩关节外科、肩关节外伤、肩关节疼痛等疑难病例，四肢关节周围创伤、骨质疏松骨折、骨折后并发症', 'active');

-- 医生：曹鹏 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曹鹏', '$2b$12$hV.hhYkAE4qK7gLXeFX.WOOI/LSkN1RQ.AUXNLPUtMwEoIqWSl8vS', 'M', '13592080329', '13592080329@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '临床常见颈肩痛、腰腿痛等脊柱退行性疾病、脊柱损伤的微创手术治疗；复杂脊柱疾病（如：脊柱畸形、脊柱肿瘤、脊柱炎症）的综合诊断和治疗。', 'active');

-- 医生：刘志宏 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘志宏', '$2b$12$q15J0Fb4ZWjNrwpG5JgFZeBwcnGLrcWI71mCrpz.KPw3hzo1xA9jm', 'M', '13677280546', '13677280546@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '髋膝关节外科，包括髋、膝关节骨关节炎，股骨头坏死，髋关节发育不良的人工关节置换，关节周围截骨手术以及髋膝关节置换术后的翻修手术治疗。', 'active');

-- 医生：刘津浩 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘津浩', '$2b$12$OMS3IK5Fkuz/Gv8XUHz6COxMUtVGQPCzl/zIPRr8bpUcNytmveMPW', 'M', '17384756779', '17384756779@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝部畸形（包括先天性、后天性、创伤性）矫正、运动损伤、四肢骨折及其并发症的治疗', 'active');

-- 医生：徐向阳 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐向阳', '$2b$12$OOUuzzdtDBAvGB/fv.tnc.Oc0czOPNKJnsmfkcb63Bgw.QOztrtie', 'M', '15173864104', '15173864104@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '足踝外科各类疾病', 'active');

-- 医生：张兴凯 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张兴凯', '$2b$12$YcU8Bl5s9ETWeNE2ja8LOueZowQeIJ0u9A1df8P/6/18FFKA7y7/m', 'M', '15406002884', '15406002884@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '脊柱疾病， 颈腰椎间盘突出，椎管狭窄，脊柱骨折，脊髓损伤，脊柱畸形，骨质疏松症', 'active');

-- 医生：万荣 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('万荣', '$2b$12$dfaBI7RadiD.kkBBW6zCuOvHoioOMkcU6Y.EijQnKqhzf.Tg6k21y', 'M', '16995226828', '16995226828@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '骨与软组织肿瘤诊治，骨质疏松症及各类关节内骨折。', 'active');

-- 医生：吴文坚 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴文坚', '$2b$12$Lgo5YdT84KXmD7DxymKIMe1vsyStpFhaQJDKNQ80tl.pBGUDDiUO.', 'F', '17966616964', '17966616964@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '脊柱微创手术，颈椎病，腰腿痛，脊柱损伤、畸形', 'active');

-- 医生：张炅 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张炅', '$2b$12$cAUspr6Ep5SIvsHujD0PMeC9kv45lLwsH/XA0bF31EFzRgkaYWzsC', 'M', '18977358886', '18977358886@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '髋关节膝关节骨关节炎诊治；先天性髋臼发育不良诊治；股骨头无菌性坏死诊治；髋膝关节人工关节置换手术', 'active');

-- 医生：庄澄宇 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('庄澄宇', '$2b$12$uvqhfK./UjpX3ezI7Ld1nOtNx5.C1kK/uzQ1gZbWOCsLs5WkDwU/S', 'F', '18211225158', '18211225158@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '肩关节外科及运动伤病的治疗', 'active');

-- 医生：沈宇辉 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈宇辉', '$2b$12$wHb4B2Qoa/K2EDYoZQOICeXL7aGWloQGr893Xrm5iUuKZP7d8y0iK', 'M', '15223940587', '15223940587@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '良恶性骨与软组织肿瘤、转移性骨肿瘤、髋膝肩关节疾病、各种代谢性骨病、骨质疏松。', 'active');

-- 医生：朱渊 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱渊', '$2b$12$y3CJFckvPht6zmjw9Pjv3.fcyI2xQKnHgHWnrgr6l/DpyTIwCruBy', 'M', '18694019365', '18694019365@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝外科各种畸形矫形、足踝运动医学、创伤', 'active');

-- 医生：何川 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('何川', '$2b$12$2wVNM8buwUCCny/FAo7KduAmaRaQtwJdTaMm2wkcSfba5b0kdHQuG', 'M', '15402533494', '15402533494@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '髋、膝关节疾患的诊断和治疗，关节置换和关节镜手术', 'active');

-- 医生：王碧菠 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王碧菠', '$2b$12$HzI9hFtmMC4ciL/DLaKJVuVaFEKZwWu6WQgQO6SgWf2vDf4.hBeSe', 'M', '18468164906', '18468164906@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '四肢骨折及运动损伤，手足外科各类疾病。', 'active');

-- 医生：叶庭均 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶庭均', '$2b$12$cinAI.ElvB0q3Ym2kpY51.ksTgTeCv6e2CKjt.g4ilhz.QFQGEv62', 'M', '18781007818', '18781007818@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '运动损伤，膝关节半月板，韧带损伤，骨膝关节疾病。', 'active');

-- 医生：王弘毅 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王弘毅', '$2b$12$uupCBBnzySX2Xm1kRxUAn.Q3Ul/Ojdn59a4kiKb/N4Khf8maebaku', 'F', '17624557080', '17624557080@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '髋、膝关节疾病的诊治，包括骨关节炎，创伤性关节炎，股骨头坏死，髋关节发育不良等微创和人工关节置换手术', 'active');

-- 医生：虞佩 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('虞佩', '$2b$12$njixpgZRgmTa9//jrNu6g.xY.T7q4rHccGnRmhLOHjQBRKR.m11FW', 'F', '19154544899', '19154544899@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '复杂骨折微创治疗，运动损伤、慢性损伤', 'active');

-- 医生：温竣翔 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('温竣翔', '$2b$12$NftfPNKqXaJmDR0673kWw.NrFSnc6WIc6UWS9BRvMxS4D6idpiyBi', 'M', '18554811020', '18554811020@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '骨与软组织肿瘤的综合治疗，转移性骨肿瘤的综合治疗，代谢性骨病，骨质疏松的综合治疗。', 'active');

-- 医生：于涛 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('于涛', '$2b$12$Xfblbcf5lk0/64ytqwF3.er6nkTC85PiDN2y95sOLov0u7tNbRbl6', 'F', '13103807155', '13103807155@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝疾病、畸形、关节退变及骨折的诊治。', 'active');

-- 医生：杨云峰 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨云峰', '$2b$12$u5rXp44v04KOFZV76vsHuerq57Zj4EJSwZttrOZ1VeIZ1Oh0IjMya', 'F', '19240466536', '19240466536@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '骨科、足踝外科、创伤骨科', 'active');

-- 医生：李兵 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李兵', '$2b$12$dvD.NwVKWlmaB4JTuuf3Z.ZC3mD.ys8NPLqp2zBaQL8NSTvfBa82K', 'F', '14895890631', '14895890631@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝外科相关疾病（骨折，外翻，手足高强运动损伤，退变等）', 'active');

-- 医生：杨军 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨军', '$2b$12$ZNiNe4QP6b5jc4yFsy.1Z./O6VhtVsjC1F1GGpIA/mlCTaWl4c4wS', 'F', '17857704655', '17857704655@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '脊柱肿瘤，颈、腰椎病，上颈椎脱位，脊柱侧弯畸形，脊柱结核感染。', 'active');

-- 医生：倪明 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('倪明', '$2b$12$Vn8o6PmDFNp1oXu0CmjW0.4EBXHqnED.D5kekKGzMf3cl4qwczV.C', 'F', '17110382761', '17110382761@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '肢体骨折、足踝畸形矫正、运动损伤与康复。', 'active');

-- 医生：徐建强 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐建强', '$2b$12$ckuiNFXzDvzJglAF/X5q9uxyPjsVKQ5VI8MYT697M1yxd1l.lSKmO', 'M', '13841976666', '13841976666@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '骨肿瘤、脊柱肿瘤，各种骨与关节损伤，骨与关节炎症及风湿性关节病，骨质疏松等疑难杂症', 'active');

-- 医生：郭常军 (骨科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郭常军', '$2b$12$VcSaCKi6zgK/Jlcu5HtvIeRFIoGk4Gvmi1lDVvRPDSRpyCTQxxYUS', 'M', '17138684919', '17138684919@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝外科疾病（踇外翻，扁平足，高弓足等），足踝运动损伤和四肢创伤。
', 'active');

-- 医生：占世坤 (功能神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('占世坤', '$2b$12$SsWfVYy9k2i/pCe2kDlTVOkuaLh/m4OhDOYuuYV4VDdk785wWuyCS', 'F', '17693269304', '17693269304@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '主任医师', '帕金森、肌张力障碍、疼痛、癫痫、精神疾病、脑肿瘤、脑瘫的外科手术及伽马刀治疗.', 'active');

-- 医生：孙伯民 (功能神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙伯民', '$2b$12$B0VEByMQrMks3khuq2lG8.9N7shexBx0S/KO1jQMFkU4sRzEo9T8u', 'M', '16236843584', '16236843584@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '主任医师', '癫痫、帕金森、肌张力障碍、疼痛、神经性厌食症、精神分裂症、强迫症、焦虑症、面肌痉挛、三叉神经痛', 'active');

-- 医生：李殿友 (功能神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李殿友', '$2b$12$iCtIaKCxVDuzaslICXVTNehQygMh7Sl4/6Hog118Wt7KECXuhEa9m', 'M', '15491541578', '15491541578@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '主任医师', '帕金森、震颤、肌张力障碍、疼痛、抑郁症的外科治疗', 'active');

-- 医生：潘宜新 (功能神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('潘宜新', '$2b$12$TWV9jOfLFj5i/2XSG/2JC.H9gfAmDI6HvTHA45b3AYtyyBp.EkpKu', 'M', '15325567963', '15325567963@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '副主任医师', '肌张力障碍、帕金森、糖足、疼痛、震颤、截瘫、抑郁症及精神疾病的外科治疗', 'active');

-- 医生：刘伟 (功能神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘伟', '$2b$12$E1bDLlMjRflCB41uCX0i4OzXEero0Xbap1GhVW9Iv.LZOjvop59x2', 'M', '18210373808', '18210373808@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '副主任医师', '疼痛、震颤、糖足、肌张力障碍、帕金森、厌食症、精神疾病的微创治疗', 'active');

-- 医生：胡柯嘉 (功能神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡柯嘉', '$2b$12$T1Zdtxj5pn11u6JLg0SG6uERXF35FaN.FzeAxM11i3DUQ5q/t8Ua.', 'F', '19701170349', '19701170349@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '副主任医师', '疼痛、肌张力障碍、糖足、帕金森、脊髓、周围神经病变。', 'active');

-- 医生：陈海涛 (心脏外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈海涛', '$2b$12$SV7YGj9KQfddELfMNGQXX.64MitHufTWEMSUfNxFaTz42d9/BX8iW', 'F', '17904745462', '17904745462@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '胸腔镜手术，风湿性心脏病，冠心病', 'active');

-- 医生：裘佳培 (心脏外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('裘佳培', '$2b$12$YryEyvcosIiszIXCClLogeADNqP4p69SOXL8/ZztiTtuCVKxgYhFa', 'M', '14274484941', '14274484941@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '特长：微创冠脉搭桥术及各类冠心病、瓣膜病、大血管疾病的外科治疗', 'active');

-- 医生：赵强 (心脏外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵强', '$2b$12$omqrS4hbzDxKNtVP3z.sbusBwdsFQugmTQxXhgJNc8oDqjjU1cwYm', 'M', '16126614158', '16126614158@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '冠状动脉搭桥术，微创冠状动脉搭桥术、微创心脏手术，机器人辅助心脏手术、心脏瓣膜成形术、
主动脉瘤的外科治疗、
心衰的外科治疗，心脏移植，辅助循环
', 'active');

-- 医生：王哲 (心脏外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王哲', '$2b$12$aJwF.v5eKz/HggInj.E./edKqJxeCEJRnNIOJm4x5cPr8k2Zl7lvK', 'M', '18456681425', '18456681425@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '瓣膜病、冠心病、先心病、胸部动脉瘤微创手术', 'active');

-- 医生：叶晓峰 (心脏外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶晓峰', '$2b$12$CF/m025ok1Tkw1NFHowjI.Ezw3G8Vt5T3gxs1CicvFWpJSoUxxBDS', 'F', '19819307302', '19819307302@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '对心脏病的外科诊断和手术治疗有着丰富经验，擅长微创介入瓣膜修补、微创冠脉搭桥和大血管手术。', 'active');

-- 医生：周密 (心脏外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周密', '$2b$12$aIxrtrmEwRIwyucvS4ON/ObDCBMDzNO6lsJvwDkpQrTShGNmsxQbW', 'M', '15270937380', '15270937380@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '心脏移植、人工心脏、冠脉搭桥及瓣膜手术、微创手术、肥厚型心肌病手术', 'active');

-- 医生：李海清 (心脏外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李海清', '$2b$12$Cn4JKwJ6gynEZqxInMdqPuQ2kTiu0ZQzAjO20vYGBXqMW0z0llwvS', 'M', '16141580226', '16141580226@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '冠心病、心脏瓣膜病、先心病、心力衰竭等外科治疗', 'active');

-- 医生：徐洪 (心脏外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐洪', '$2b$12$YnzQ0SHOKHlRF1JnVXlQBed16uEXGXcAXHXvqgGdMt.IH05IxhdWi', 'F', '14314289692', '14314289692@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '各类成人心脏病如复杂冠心病、瓣膜病、大血管疾病、以及微创介入治疗、终末期心衰综合治疗。', 'active');

-- 医生：朱云鹏 (心脏外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱云鹏', '$2b$12$pCt4RTGqpHyGWnVXs1Bsm.Kz1bR/snLNnUtgv7kf/6vd5gbZnArvW', 'F', '15427696198', '15427696198@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '冠心病心脏搭桥术及心脏外科其他常见病诊疗', 'active');

-- 医生：赵卫国 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵卫国', '$2b$12$lufNs2dYac91h5KNVOqJxeMNP3ggyyD410zUvYnWfCGGuyOqFKeke', 'M', '14125409494', '14125409494@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '神经外科疑难疾病的诊断和微创手术治疗。特别是垂体瘤和颅内各类肿瘤；面肌痉挛，三叉神经痛，舌咽神经痛；颈动脉狭窄和脑血管病的微创外科治疗。', 'active');

-- 医生：王健 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王健', '$2b$12$wyXkpv28uUdQ7aQForEYj.RutCjXG7dtUeflCdT/andvjgxXRgQAO', 'M', '16452468587', '16452468587@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '颅脑外伤的诊治，预后康复，各类颅内肿瘤的手术和放化疗方案。脊髓椎管内肿瘤的诊治', 'active');

-- 医生：濮春华 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('濮春华', '$2b$12$HuCraDITaoeauqj4gFOjV.OODU215ie1NFpyAX7PjCLz6mXo/ybnC', 'F', '19174539964', '19174539964@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '颅神经疾病治疗（面肌痉挛、三叉神经痛、舌咽神经痛的微创微血管减压术）、颅内及椎管内肿瘤手术', 'active');

-- 医生：李云峰 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李云峰', '$2b$12$JhGcXsCx0DMUX288Xa6QBewZtEmlW5DvOoQHa2RXeP8FcnRMYzZxG', 'F', '15788785773', '15788785773@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '微创手术、内窥镜、颅脑肿瘤综合治疗', 'active');

-- 医生：林东 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('林东', '$2b$12$ZitfSQMvxemjOvVfNdSngeYYtYJoRRuIg6Y6AD2aNWhZ9SpAkrlyW', 'F', '18675757257', '18675757257@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑、脊髓血管病的血管内治疗、颅内肿瘤', 'active');

-- 医生：胡锦清 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡锦清', '$2b$12$RW3uIOWtVd8NExy6qwrfAuc/Vz5tbK7raICWgjGBqU3T.PUEsN6.O', 'F', '13223847257', '13223847257@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑血管病（颅内外动脉狭窄、脑动脉瘤、脑血管畸形等）介入和外科治疗，脑外伤、脑肿瘤等。', 'active');

-- 医生：蔡瑜 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蔡瑜', '$2b$12$sIYvX2TzIU7Pyf7IY.OpFuLxIGFom1rmXNUHNFPhZryRG4xpjSq5G', 'F', '14723403479', '14723403479@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '脑血管病（颈动脉狭窄的手术治疗）、颅神经疾病（面肌痉挛，三叉神经痛）', 'active');

-- 医生：孙青芳 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙青芳', '$2b$12$ZsDdAP88nlII3fzJPL6nP.dLW8bzamqdZHwTSwGR3NrTFHjcZPfJe', 'F', '13216396351', '13216396351@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅内肿瘤、垂体瘤、脑积水、神经脊柱脊髓外科、脑血管病', 'active');

-- 医生：卞留贯 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('卞留贯', '$2b$12$9rSr8nLf7NatDM/7vGbC9uGfcyKD86r8AT.XSoIsqTlln8dwNX2Bm', 'F', '15882269302', '15882269302@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅内肿瘤及脑血管病', 'active');

-- 医生：江泓 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('江泓', '$2b$12$S.izXI72NBv6OLxzcNry3ObF/19GPs2CJD2nkIvipIXpvJnHKJiaC', 'F', '16750911797', '16750911797@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅内动脉瘤，脑动静脉畸形，颅内动脉狭窄，颈动脉狭窄，椎动脉狭窄，脑梗，脑出血，蛛网膜下腔出血等颅内出血性和缺血性疾病。搏动性耳鸣介入治疗。', 'active');

-- 医生：朱军 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱军', '$2b$12$brKwxai/Y2PTVoBxf.NO7.UoRzOnBNatgPMaMtSNIg4THUoOQuOM2', 'M', '16719106699', '16719106699@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑血管病微创治疗及脑出血、脑梗死、颅脑肿瘤诊治', 'active');

-- 医生：尚寒冰 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('尚寒冰', '$2b$12$sQIVXvDA1wWXCab/Cf5wi.Unmfysav1a9Skejs8A5v.Y08x24CbxC', 'M', '15147659674', '15147659674@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅神经疾病（三叉神经痛、面肌痉挛）微创手术、脑血管病、脑肿瘤外科治疗、神经系统疑难罕见疾病新技术治疗咨询。', 'active');

-- 医生：潘斯俭 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('潘斯俭', '$2b$12$fOstN04ERn4t0qE8TxeC.ejDfcGHpVfTWytEZkwD5Xqaxte8TkBOK', 'F', '13658260221', '13658260221@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '三叉神经痛的MVD，PBC及伽玛刀；脑肿瘤，脑血管畸形及癫痫的伽玛刀', 'active');

-- 医生：吴哲褒 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴哲褒', '$2b$12$3/ms74pzF1GFJq6QAvhCrO5UGu8P9r/ebg3RAHLpiN4X32VOef6Uu', 'M', '15563102056', '15563102056@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '垂体腺瘤的内镜经蝶微创手术；颅脑肿瘤（脑膜瘤、胶质瘤、听神经瘤、颅咽管瘤等）显微外科微创手术治疗；垂体腺瘤的药物和个体化治疗。', 'active');

-- 医生：孙昱皓 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙昱皓', '$2b$12$.XbTwgDUAZsObHv5tRiyqe79O5TUbhzUQh1zUim610h3DAfDzWL2C', 'M', '18454549587', '18454549587@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑肿瘤内镜微创手术（垂体瘤、颅咽管瘤等）；颈腰椎微创手术（神经鞘瘤、椎管狭窄等）；其他各类神经疾病的内镜微创治疗（脑积水、腕管综合征等）', 'active');

-- 医生：顾威庭 (神经外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('顾威庭', '$2b$12$HGbHBHZiNFZ2bI.Ya.pMS.qjc4C5ulDscYerRyoNY2bNV8erhtbNS', 'F', '18233815413', '18233815413@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '擅长脑血管疾病、颅内肿瘤和脑积水等疾病的外科治疗', 'active');

-- 医生：袁克俭 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('袁克俭', '$2b$12$vVglxyPhywe7R5I9RIPPWeOQcJykAIDdx4UuYDNXj5SI6Z8OuCb92', 'F', '17432091877', '17432091877@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '大面积烧伤,难治性创面处理及疤痕早期防治', 'active');

-- 医生：郑捷新 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郑捷新', '$2b$12$.fu6ffXa2F8USCh97C7ppOk4epTCHU2HbUagqmi0YjaO.gdjW/VEC', 'F', '15532074125', '15532074125@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤后期疤痕预防和治疗，难愈性创面的治疗', 'active');

-- 医生：张勤 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张勤', '$2b$12$M20AXdAMeE2KSdeTcJaQrOHsmTOsaT6/GmNIAj9DuMX9WRyZZhVH.', 'F', '17236674237', '17236674237@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤疤痕防治，难治烧伤创面的处理和治疗', 'active');

-- 医生：黄伯高 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄伯高', '$2b$12$rLClt5Y6J3mEHdM3wfBEM.w.JEdPwxQaPXITKDvK3l7lSdeGS/9d6', 'M', '16813962536', '16813962536@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤创面修复、抗感染、疤痕防治', 'active');

-- 医生：王文奎 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王文奎', '$2b$12$4.tEPmiuUNzbGbf1b.oOaeIXK2RdzlrqTinF0yoWV0qxhg6hb6dcu', 'F', '18903303363', '18903303363@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤创面、疤痕预治及后期康复指导', 'active');

-- 医生：张剑 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张剑', '$2b$12$4tUbcgvE1.DlelOATWYJL.iBcNvK66XA3fGiuB/PIvjrre.6IaLvm', 'M', '17711072118', '17711072118@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各种灼伤及创伤后疤痕增生的整形康复，以及各种难治性创面的诊治', 'active');

-- 医生：刘琰 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘琰', '$2b$12$F5xM/S64UT47FvwhW1piw.m2K7u09S.Pj9nT4/ue9NqQksv9BCZl.', 'F', '16688343096', '16688343096@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '创面处理、慢性难愈创面（糖尿病足）治疗', 'active');

-- 医生：杨惠忠 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨惠忠', '$2b$12$mUMAMR4SB4z2PUTTRN5DieAPNCkPOQqqqGombbo8I17/aKIba6cRC', 'M', '15408071259', '15408071259@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤创面修复,疤痕修复', 'active');

-- 医生：向军 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('向军', '$2b$12$eJThWR2FT.0K1Af9xuety.DONFzRh4Mjp4m2g/k5/x2qEwyRkv4eu', 'M', '16943702118', '16943702118@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各类创伤创面的修复、疤痕防治', 'active');

-- 医生：牛轶雯 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('牛轶雯', '$2b$12$Nc7f4ciWWi.AzdQdq6iVyeNNVEJYzHGsoEjW69CmdW5/0/k/9DaBu', 'F', '16574364121', '16574364121@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各种类型创面修复及预防、瘢痕防治', 'active');

-- 医生：乔亮 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('乔亮', '$2b$12$51Q1ffKKUSRq/isuqaPj2up2X8.YJgNsK6I9.LIfhq2qumaVTZ7vS', 'F', '18329460134', '18329460134@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '大面积烧伤、儿童烧伤、疤痕治疗、慢性创面。', 'active');

-- 医生：王志勇 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王志勇', '$2b$12$4QTc6.zI.YV9R9glovoVU.8n.GKUfRxcmzFmRFwdZPCZVe/jVkoJ2', 'F', '19954725078', '19954725078@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤创面修复、慢性及难愈创面处理', 'active');

-- 医生：窦懿 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('窦懿', '$2b$12$QdtoRbRtk8sTWD126Ya.X.P8a1YHloFTqkElQy3zdHoIsY7f7egge', 'M', '18191048514', '18191048514@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '疑难复杂伤口修复、疤痕治疗、皮肤肿物治疗', 'active');

-- 医生：黄晓琴 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄晓琴', '$2b$12$SUI4.D07cIqwGA5efq6EzeVEf00PmT4f0kEpJInSm7xqKpB7.idcm', 'F', '17812807629', '17812807629@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤创面修复、烧伤瘢痕整形、瘢痕激光治疗', 'active');

-- 医生：王西樵 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王西樵', '$2b$12$LUththTZ5FFeZLHjtvea2uGhIkJIRW4N5xDdmjJ0uK9k5GrjEDzaO', 'F', '13978775497', '13978775497@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '擅长深度烧伤、创伤等难愈创面的非手术愈合，疤痕的修复治疗和激光治疗', 'active');

-- 医生：施燕 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('施燕', '$2b$12$IyQT.HlgCRVyhU.EfiDRN.rfVh8GxdvrYrEsGH9PwRYVlJj94ZsAa', 'M', '18433310361', '18433310361@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '瘢痕光电及综合治疗、小儿烧伤、创面修复。', 'active');

-- 医生：唐佳俊 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('唐佳俊', '$2b$12$ZgOQmzH/c3rFrKmsM4Cuu.DA13AMYnVlUJKOR/KQRfhoeGQwEdXJS', 'M', '19313814138', '19313814138@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各类急慢性创面修复、增生性疤痕治疗', 'active');

-- 医生：郇京宁 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郇京宁', '$2b$12$7RNBmrt5d5vbLnfyWMXnaOB7tma9g9.xsSqgtyov6YkX5dpQJcYWq', 'M', '13149621604', '13149621604@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤创面、慢性、感染创面治疗，疤痕防治', 'active');

-- 医生：李学川 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李学川', '$2b$12$SkLrCmvieVaJKw2dijI4V.bIe2yKTcmPDn9/XUk4WWhI4lO9RbKru', 'M', '16756350429', '16756350429@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '瘢痕综合治疗；淋巴水肿手术治疗；精细缝合及复杂创面修复。', 'active');

-- 医生：原博 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('原博', '$2b$12$.Dfse.d5aufCmNaO0emOAuMNevnSKJiPYG0EsqKYF6RrYiBONpzGm', 'M', '16545002643', '16545002643@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '小儿烧伤、瘢痕防治、创面的细胞治疗与再生。', 'active');

-- 医生：周增丁 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周增丁', '$2b$12$8wPImkZ/ZGY7QKCAoA0vdeV1fHGyb7OK2im56Hz915VnSSoyARY1.', 'M', '18847704681', '18847704681@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '艾灸、暖宝宝等低温烫伤治疗；腋臭祛除、腹壁整形；手部指蹼瘢痕治疗；疤、痣、纹身及皮脂腺切除精细缝合。', 'active');

-- 医生：易磊 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('易磊', '$2b$12$uazPvpCoQ.ZlSCsNJNncDe4rNQ/OLv.D7qEdylJm.UCTsc/MbLbNy', 'F', '16529123992', '16529123992@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '瘢痕治疗，伤口修复，皮肤肿瘤与黑痣，整形，灼伤与外伤治疗。', 'active');

-- 医生：冯少清 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯少清', '$2b$12$B2SFSf/T1qcs1mRSNUPt3.H0DJmiUxc52V7CjewVFfQiBmvo9Mfqu', 'M', '14804436922', '14804436922@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '特长：烧创伤瘢痕综合治疗、乳房腹壁整形、眼整形', 'active');

-- 医生：穆雄铮 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('穆雄铮', '$2b$12$n60KBjDUpZVpvsrrnGivS.5lxBYvzvxOoElE4OwcemYlZFXcP/n5.', 'M', '19926830795', '19926830795@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '各类面部轮廓整形及颅颌面畸形疾病。', 'active');

-- 医生：林炜栋 (烧伤整形科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('林炜栋', '$2b$12$yR7/xolxUyy6sSWexnZLJum1exUuh3FFbyIkdFrsuj4vl3g6pLn5O', 'M', '19556497252', '19556497252@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '各类创面修复、疤痕防治及慢性难愈创面诊治', 'active');

-- 医生：陈中元 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈中元', '$2b$12$DULW2PJH0SiNU/xGEUuDT.9V95yzGqbwmRV4oHpyhIVKw1a1RReim', 'M', '14963406382', '14963406382@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '主任医师', '心脏，食管、贲门肿瘤、肺部，纵隔肿瘤', 'active');

-- 医生：任健 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('任健', '$2b$12$04HeRLTAxToUxIqS.regRe.OpZEzK/QLfWxjUmZhFCKZnZqCx729m', 'F', '13698509918', '13698509918@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '胸腔镜微创手术，食管肿瘤、肺肿瘤 、纵膈肿瘤、手汗症', 'active');

-- 医生：杭钧彪 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杭钧彪', '$2b$12$1FI3pXkhHSOIrc2jQH/hQO6XOf.dGIyO3iHIi49PFpt9b5vuiwFGy', 'M', '19230307805', '19230307805@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '主任医师', '胸外科微创手术、食管贲门肿瘤手术、肺癌综合治疗、纵膈肿瘤及胸部疑难疾病诊治', 'active');

-- 医生：车嘉铭   (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('车嘉铭  ', '$2b$12$4SrmkSMAocN6X0S8b1sYMefuv7cuT2aPlfi7zgddzkcktD89uMsIa', 'F', '14960514523', '14960514523@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺肿瘤、肺小结节、食管癌、贲门癌、纵膈肿瘤等的外科微创治疗', 'active');

-- 医生：朱良纲 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱良纲', '$2b$12$rJY2obmYxtiUcfxwW.IJY.y2/C4uKuQRKj4XFDgrr9HN7fd7ok2wq', 'F', '18670298515', '18670298515@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '食管、肺、纵隔疾病、冠心病、先心、瓣膜病', 'active');

-- 医生：周翔 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周翔', '$2b$12$JDzYKk4C4ESS4o1XTZiSl.Wz08aS5Sw8hVaOAahip7AoFL616.QdO', 'F', '19575209597', '19575209597@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '食管、肺部、纵膈肿瘤的手术及胸腔镜诊疗', 'active');

-- 医生：杨孝清   (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨孝清  ', '$2b$12$wHl7NfmJgCx9AN2n7UmaL.Swko9QgrLqJFopzzbpOf.Ze2iPAz.RC', 'F', '19688273030', '19688273030@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '食道肿瘤、肺肿瘤、纵膈肿瘤的外科手术及综合治疗。普胸外科疑难疾病诊疗', 'active');

-- 医生：项捷 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('项捷', '$2b$12$OPYUZkGEwSQmnjCeSQnBtewq1Q6Q0MRG6NOYVlnJ.cl5Hhk1t.7l.', 'F', '14898493949', '14898493949@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺、食管、纵隔肿瘤等常见胸部疾病以手术为主的综合治疗，熟练开展包括胸腔镜食管癌根治术，单孔胸腔镜肺癌根治术剑突下纵隔肿瘤切除术。巨大纵隔肿瘤切除术及达芬奇机器人在内的各类胸外科手术。', 'active');

-- 医生：陈凯 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈凯', '$2b$12$d5j672f275jBfHkC0..C3u4SAIFMo3vOoHSlcvPLxs8Q8AfGz/B6i', 'F', '16378304807', '16378304807@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '微创治疗纵隔、肺、食管等胸部良恶性肿瘤。', 'active');

-- 医生：杜海磊 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杜海磊', '$2b$12$/yiOgYt7KgsiH0uhaDZNc.WTgzkPn/C.Cl9kTsz7.FRPDPpDmSPrO', 'M', '19784683755', '19784683755@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺结节、肺癌、食管癌、纵隔肿瘤、手汗症、气胸等微创治疗。', 'active');

-- 医生：韩丁培 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('韩丁培', '$2b$12$Hn4wDzPvzMX0T.B0VTOCruO7P739.SNZe.C3ndmBXH6ebZiEh/4Gq', 'F', '19935021998', '19935021998@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺结节、磁导航定位、肺癌、食管贲门癌、胸腺、纵膈肿瘤微创外科治疗。', 'active');

-- 医生：李鹤成 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李鹤成', '$2b$12$EoI/MPxYODHhelx.AKVAmujOCVViQdyYDg4KwlJuFwpLXSzW2OHh2', 'F', '18356888294', '18356888294@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '主任医师', '肺癌、食管癌、纵隔病变、胸膜间皮瘤等以微创手术为主的综合治疗；肺移植外科治疗。', 'active');

-- 医生：张亚杰 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张亚杰', '$2b$12$1v2bRwI.AZMEPOvVs.eiT.ZAAbIiDTVZAC4wPmbJB9nISaRdHFvTu', 'F', '16183197117', '16183197117@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '胸部肿瘤机器人、胸腔镜等微创手术及综合治疗。', 'active');

-- 医生：金润森 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('金润森', '$2b$12$uoYENbCKKk.UkS4dpma6YuA0d/9NDInTlHwFXHFUubdXVKCEoNjQ6', 'F', '14391751878', '14391751878@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺小结节的鉴别诊断，肺癌、食管癌、纵隔疾病微创外科治疗。', 'active');

-- 医生：陈学瑜 (普胸外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈学瑜', '$2b$12$NRXOxu5QE5ZPri1Reqm5VepNda4Ne5jzZdesqinK5NiqJfb/1cv2m', 'F', '15679984033', '15679984033@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺部良恶性病变、磨玻璃结节的微创手术治疗。', 'active');

-- 医生：李卫 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李卫', '$2b$12$yhJ8.WjM3PbG3KAC95Ztt.5OTJm3R4VGd9g/pX4FLzhPSszBiGVDW', 'M', '14261953189', '14261953189@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童血液及恶性肿瘤疾病的临床诊治、肝脾淋巴结肿大、儿童感染性疾病诊治', 'active');

-- 医生：许春娣 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('许春娣', '$2b$12$pgT.VKy2Ntxl9Chah5GaW.Z9aNcdpu0XKgk2JkzmOWGmc3/owQXBO', 'M', '16845122940', '16845122940@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童消化道疾病的诊治（便血、腹泻、腹痛、呕吐等疑难杂症）', 'active');

-- 医生：董治亚 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('董治亚', '$2b$12$T5o0UuSVTPcfpb2sVBVmSu.fW0x6.3uSCvMA1N0GIIoIW729rhYRG', 'M', '18329730155', '18329730155@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童生长发育相关疾病诊疗，包括矮小症、早发育、性发育异常、肥胖症；儿童糖尿病、甲状腺疾病及遗传代谢病。', 'active');

-- 医生：陆文丽 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陆文丽', '$2b$12$86c.lqNBxdLwAbWN3Fw9Ee5OzGaG.RByNTR3jVWQPk7MXxS1iSoLO', 'M', '16537656494', '16537656494@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童生长发育评估（乳房早发育、性早熟、矮小、肥胖等）及甲状腺疾病、先天性肾上腺皮质增生症、McCune-Albrght综合征等。', 'active');

-- 医生：肖园 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('肖园', '$2b$12$Nqy.QwQsJGpuNQzJb1VBkuY0pc.tm/sQyhXa66MDP2s4N11qnUcHq', 'F', '17600282414', '17600282414@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童内分泌疾病（矮小、性发育、糖尿病、甲状腺疾病等）、儿童胃肠病（慢性腹泻、炎症性肠病）和消化内镜诊治。', 'active');

-- 医生：吴群 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴群', '$2b$12$k4Zy8eE2gWnxQVd0vPLpW.y0wCR9YjU4Oh3CjAJggEWBeiNBVB7UO', 'F', '13322086735', '13322086735@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童呼吸系统疾病，儿童哮喘，儿童过敏性鼻炎，婴儿湿疹，食物过敏。', 'active');

-- 医生：余熠 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('余熠', '$2b$12$tmvO/wEG0dn2SyA9bpOh1OfFwR/pQel0Nk7K2qQUZaxLz2tpZQjSa', 'F', '16926561919', '16926561919@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿内科常见疾病诊治，擅长消化系统疾病。', 'active');

-- 医生：王歆琼 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王歆琼', '$2b$12$SXN9TAhR8tLm/sE8jy.rN.T9zMLeMIKJVSSZ7v5P6HRf/CNL2TsOa', 'M', '19922050912', '19922050912@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童胃肠疾病（炎症性肠病、急慢性腹泻、慢性胃炎、过敏性肠炎等）及其它儿内科常见病的诊治', 'active');

-- 医生：马晓宇 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('马晓宇', '$2b$12$4655Mrf9NfJPq7sD2/CtJeCK1c2z7Vp1mK.B4ZFsVfjR3qG7nS/l6', 'F', '16106330865', '16106330865@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童内分泌遗传代谢，擅长矮小症、性早熟、糖尿病、肥胖、甲状腺疾病等儿童内分泌疾病的诊治。', 'active');

-- 医生：曹玮 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曹玮', '$2b$12$lW/HMOKyzkL9pVje/OqE/.RZaTb4l.3X/QOM.LA8cF49y1VdaIgO2', 'F', '15909037759', '15909037759@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童生长发育评估（矮小、性早熟、肥胖）、甲状腺疾病及常见疾病。', 'active');

-- 医生：倪继红 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('倪继红', '$2b$12$v237b9oJ6wUz.EjJTAdOVembriUCJ0bRCJNTyl8Yn2tFJltKYBilG', 'F', '19997356828', '19997356828@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童矮小症、性早熟和性腺发育低下、甲状腺疾病、先天性肾上皮质增生', 'active');

-- 医生：赵建琴 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵建琴', '$2b$12$l7hCXAYWcX/cFgJGNKHUHOdQycmPZZ1PU/lorJJx4LiL.LJepH3P.', 'F', '17902638839', '17902638839@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '小儿常见多发病，擅长小儿呼吸道疾病、哮喘等疑难杂症', 'active');

-- 医生：邱定众 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('邱定众', '$2b$12$HrFldgG5zGzRhKol7ru.DepUAV3FjJkluIy.F/204wIeR911bc1JW', 'M', '16335602191', '16335602191@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '小儿感染性疾病、心血管与呼吸道疾病、自身免疫性疾病及疑难杂症', 'active');

-- 医生：于意 (小儿内科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('于意', '$2b$12$QNAUoIMbxOtOVILvlDuTCO1asKVMBDmDJGWnLvy4nR9Y0dhW1weRK', 'F', '16621454127', '16621454127@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童内分泌疾病(矮小、肥胖、性早熟等)及儿内科常见病', 'active');

-- 医生：沈丽萍 (小儿外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈丽萍', '$2b$12$y7PiAw7Jti2.YIapy2i9pekRSGUIrnfH4uA6fHMjS161M9U7izaYe', 'M', '16460916347', '16460916347@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '消化、新生儿、腹腔镜', 'active');

-- 医生：陈建雯 (小儿外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈建雯', '$2b$12$AxSzrU1Kktu3fYm00hclFenQ1Vr1A.lFbD3zBwKWq1X4d1AWQO8Ei', 'F', '18277215428', '18277215428@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '小儿的脐茸、包茎、各种肿瘤、儿童骨折及先天性骨性病变', 'active');

-- 医生：周曙光 (小儿外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周曙光', '$2b$12$LmUwgSah2wpImVX1PQsqLOkAjSNrWV5oW.sAPucQTrHFKLD6zvMUy', 'F', '14768143326', '14768143326@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '小儿骨科、小儿泌尿外科、小儿脐茸诊治', 'active');

-- 医生：刘德鸿 (小儿外科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘德鸿', '$2b$12$rFuYhrFT3VGezPc/30iLwO2yzzBCMvG/1R3mRl/CRRVrCBfrkKei6', 'M', '16735540030', '16735540030@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '小儿泌尿外科各类疾病的诊治', 'active');

-- 医生：刘延 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘延', '$2b$12$4oSIteEJ7TBe.15rs5U8QOr79tkh0jv1oqK/txYlKyVREi8kMU/2S', 'M', '13790147183', '13790147183@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '高危产科、月经异常等妇科疾病、围绝经期综合症、不孕症', 'active');

-- 医生：黄健 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄健', '$2b$12$2xlEen4qFujr.qWixgrnfuu.oVlLd8KRPvyenDNdi/riUhmldgUx6', 'F', '14595744935', '14595744935@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '慢性宫颈炎症、子宫肌瘤、月经失调、不孕症', 'active');

-- 医生：滕宗荣 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('滕宗荣', '$2b$12$GaNz18A5uwvuq8NKQxz5Z.w/qPdUCWgiCniroRXGrwUG097yHapri', 'M', '13379340713', '13379340713@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科内分泌疾病、妇科炎症', 'active');

-- 医生：刘淳 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘淳', '$2b$12$qljHhtTNCd5txtTyEWdf3u2jvBnRbIDed5luEbMNy2X.2XsIkaKBe', 'F', '15327268503', '15327268503@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科炎症、妇科内分泌疾病 ', 'active');

-- 医生：钟慧萍 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('钟慧萍', '$2b$12$pvP1iuVFwDuk/xuKbMA78.0ULhWiFsB7Tn6YQ.UGYIdAhr4akxhpO', 'F', '15462388455', '15462388455@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '妇科宫腔镜、腹腔镜手术，包括宫颈疾病、子宫肌瘤，卵巢囊肿的治疗。妊娠合并甲状腺疾病（甲亢、甲减、桥本甲状腺炎等），妊娠期糖尿病，妊娠合并甲旁亢等内分泌疾病的诊治，及产科危重疑难杂症的诊治。', 'active');

-- 医生：薛梅 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('薛梅', '$2b$12$0woZ8ukcfQJWCxzASS1vNOlqRgvxUk89RqxxHLuAfw2KN9ocUIw22', 'F', '15907464398', '15907464398@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科肿瘤的综合治疗\月经失调、更年期综合征', 'active');

-- 医生：沈立翡 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈立翡', '$2b$12$5KSlpm1SS61TrpcT2vWuvu/epksZBY4vQl0gX.9qBtqYrdvda9u4i', 'F', '15996478772', '15996478772@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '子宫肌瘤、子宫内膜异位症、卵巢良恶性肿瘤、子宫颈癌及子宫内膜癌的腹腔镜手术', 'active');

-- 医生：龙雯晴 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('龙雯晴', '$2b$12$7q0MuHotPpHUC2YfkAIxwufJ./qjyqeriq83l//tSjYKNRAKgxL8K', 'M', '16120812641', '16120812641@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '女性不孕症、子宫内膜异位症、子宫肌瘤、卵巢囊肿等妇科良恶性肿瘤的宫腹腔镜治疗。', 'active');

-- 医生：王敏敏 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王敏敏', '$2b$12$kojXaE5fqhJGECNfdgmuxup6E.tbMSrNv43Va0ArEoCkgRy/JN0AS', 'M', '15340756713', '15340756713@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '绝经综合症、功能失调性子宫出血、妇科炎症', 'active');

-- 医生：沈育红 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈育红', '$2b$12$XvKlCh5DjlIw.1mgGdWSjuGfwkL8qokNeF1YHoKZxsaWyzIJy.t52', 'M', '19799816232', '19799816232@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科常见病如子宫内膜异位症、子宫肌瘤、卵巢肿瘤等宫腹腔镜手术治疗以及妇科恶性肿瘤的诊治。', 'active');

-- 医生：朱岚 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱岚', '$2b$12$PRxX0evzhv3UkOtoOmAAaOAZi8A0zSXJZrH9Kr.S4lN9/39Bbd70u', 'M', '19133318206', '19133318206@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科疾病、妇科肿瘤、宫颈疾病的诊治，内窥镜治疗', 'active');

-- 医生：陈晨 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈晨', '$2b$12$CLPi.PXekQM/dXTvnH.O9OJSJul8ej8aaPCTqZDYh4zgh/aRMsXfu', 'M', '14121882877', '14121882877@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '月经异常包括多囊卵巢综合征、不孕症、性腺及生长发育异常，卵巢早衰，围绝经期综合征。', 'active');

-- 医生：蔡蕾 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蔡蕾', '$2b$12$8VWnGMKjVyUIDfyccQP1h.9tMSyARZXoQetxp372zzxsbSasfGbJ2', 'M', '14235528741', '14235528741@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '宫颈疾病诊治，妇科肿瘤宫腹腔镜手术', 'active');

-- 医生：沈健 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈健', '$2b$12$.s/GSYCmKcFgJcfdQQoAbe7uJWUr723gHK2tKUVYhJXKUUhwQYvR2', 'F', '18222824732', '18222824732@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科内分泌、更年期综合症、不孕不育、生殖道感染', 'active');

-- 医生：杨辰敏 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨辰敏', '$2b$12$.jNCVXLI8FH5DOs8Skkjm.Un3JHYk03LcI86vKStMna25etoZ6j4u', 'M', '16851044796', '16851044796@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '阴道镜下宫颈疾病的诊治，宫腔镜下治疗内膜息肉、粘膜下肌瘤、子宫纵隔，腹腔镜下治疗不孕症、子宫肌瘤、内异症、卵巢肿瘤及子宫切除术等。', 'active');

-- 医生：刘华 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘华', '$2b$12$gcrD7UrOKwfG68hpd78Zm.clqhbg/BrJfQw9/439fxyxxfdVKJUie', 'F', '19496098177', '19496098177@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '内膜异位症，妇科良恶性肿瘤，腹腔镜手术，宫腔镜手术', 'active');

-- 医生：焦海宁 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('焦海宁', '$2b$12$dM590tq/Mw4OQ9l.QvHIjuV39NagCJoRPzFGwtLAnsUGxnwKD3VCe', 'M', '17752027025', '17752027025@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '宫腔占位，子宫肌瘤，卵巢囊肿，子宫内膜异位症等良性肿瘤的诊治；产科危重疾病的综合治疗及妊娠合并糖尿病、高血压、甲状腺疾病和自身免疫性疾病的诊治。', 'active');

-- 医生：陈慧 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈慧', '$2b$12$JdDMbABR5DidPV1BjSnMF.B/IIacnmvuIicBYHio7EJcTYh5S3Acy', 'M', '19979717936', '19979717936@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '各类妇产科疑难超声诊断：如妇科肿瘤（尤其是卵巢肿瘤）、子宫内膜异位症、女性生殖系统发育异常及高危妊娠产前超声诊断等。', 'active');

-- 医生：许啸声 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('许啸声', '$2b$12$l0h5UHMoEdb1TdJu7cYD.uxAsw7uDwv0f5b8VJBqSbh6eZ4X969Cu', 'M', '15216070149', '15216070149@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '子宫肌瘤、卵巢囊肿等妇科常见良性肿瘤的微创手术治疗，妇科恶性肿瘤的综合治疗，HPV感染防治及宫颈癌前病变的物理治疗。', 'active');

-- 医生：冯炜炜 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯炜炜', '$2b$12$22IR2y2XUpNtHJ0fH73d/OnwkjKiUPIFnt.8A3MW3FQks4H2nkq5m', 'M', '15718257024', '15718257024@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '精通妇科良恶性肿瘤，子宫内膜异位症的腹腔镜微创手术，机器人手术，开腹肿瘤减灭手术等；以及恶性肿瘤的综合治疗。', 'active');

-- 医生：程忠平 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('程忠平', '$2b$12$5c8Mj95PGp7sIX9qi9rVX.SXa0oku5tyRS/BAlDz.3aizqt7hLVfS', 'F', '16867741192', '16867741192@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '腺肌症/肌瘤(微创保宫)、妇科肿瘤微创与免疫治疗。', 'active');

-- 医生：李菁 (妇科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李菁', '$2b$12$ZsThL9rECyarRfKob/HCvuL1TiP5xlVEDyfDlq.3oJtwY.zdEe.K6', 'M', '13735760237', '13735760237@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科宫腹腔镜手术，包括内膜和宫颈疾病的诊治。', 'active');

-- 医生：李彪 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李彪', '$2b$12$T17n9zFT2Zsg7HWzGJIxVOY/G.0AQMC.5RkQYkyKo5thLfnPI6rrS', 'M', '13848605284', '13848605284@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', 'PET/CT肿瘤临床诊断，甲亢、甲癌核素治疗。', 'active');

-- 医生：李培勇 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李培勇', '$2b$12$3p/PFfi447Wu6Qm3NUeui.i9veMC6d9wgfwoLWZZdvo7YPInLMWke', 'F', '19834717549', '19834717549@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '甲亢和甲癌的碘-131治疗，桥本甲状腺炎的内科治疗', 'active');

-- 医生：管樑 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('管樑', '$2b$12$WHwNE.L0p3TDrJyvfZTWjOEKjjfU04AX2W/RIloDzuSYs/eJkuoPS', 'M', '19707665448', '19707665448@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '碘131治疗甲亢，诊断和治疗甲减（妊娠期）、桥本、甲状腺结节等甲状腺良性疾病。', 'active');

-- 医生：陈刚 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈刚', '$2b$12$/b23UrFMR.qiCjfSmWCJIOUNx5d7e2v2RCu1u0Mvw4AGm3NFEfbgW', 'M', '15672030854', '15672030854@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '骨转移骨痛，甲亢、甲状腺癌131I治疗，甲减，甲状腺炎等甲状腺相关疾病。肿瘤心理支持治疗。', 'active');

-- 医生：江旭峰 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('江旭峰', '$2b$12$cs5K8GW5uJ2jKpIZSTbatOKp./m2G16IfNWD.YKQuY3Z/eVlms5Ra', 'F', '18497897455', '18497897455@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '擅长甲状腺疾病（甲亢、甲状腺癌等）、肿瘤骨转移核素治疗； 骨质疏松诊治', 'active');

-- 医生：张一帆 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张一帆', '$2b$12$DHeVr1q9e4D5g6GlwaAuGOECtTrQC18t7F.7JPf17GB3IAHvWlw7m', 'M', '17795294275', '17795294275@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '甲状腺癌碘-131治疗、甲亢碘-131治疗等', 'active');

-- 医生：张敏 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张敏', '$2b$12$vzNg3se3AD1p/qOdt2bD1.HZrA6CBTGFn.F.A1kWeFF9tjwQ7URJS', 'F', '13551034639', '13551034639@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '甲状腺功能亢进和甲状腺癌碘-131治疗，以及PET/MR，PET/CT，SPECT/CT多模态影像评估肿瘤、神经及心血管疾病。', 'active');

-- 医生：胡佳佳 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡佳佳', '$2b$12$Q5mgDTlXJg0D9R9a8ywdg.Hu/NKnUvuIfPyk8u7ORT0FuINta2.Ui', 'F', '13565492204', '13565492204@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '肿瘤分子影像诊断及甲亢、甲癌、骨痛核素治疗。', 'active');

-- 医生：张淼 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张淼', '$2b$12$4TUmJNdG8ca7cAt7eU4NYuJLKZmnLOAt36HvKyKaqbJzUjuIyllcG', 'F', '18989817696', '18989817696@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '甲亢、甲状腺癌碘131治疗，肿瘤及记忆力减退PET/CT、PET/MR多模态影像评估', 'active');

-- 医生：郭睿 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郭睿', '$2b$12$aWoYp.SUbCuP9YPi.7z7xubyF30bivZvKrP54rlPPuZzw4KKa8ruG', 'F', '18264272995', '18264272995@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '甲状腺疾病(甲亢、甲癌等）核素治疗，淋巴瘤PET评估。', 'active');

-- 医生：席云 (核医学科)
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('席云', '$2b$12$wdjHf1WtoFp2o4.pPHz6me/YrdrDPAb6pLQQza0eXUfXMEL503VTy', 'F', '14887906704', '14887906704@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '甲亢碘-131治疗、甲癌碘-131治疗、肿瘤多模态分子影像评估。', 'active');

INSERT INTO appointment_types (type_key, type_name, fee_amount, description)
VALUES
    ('normal', '普通号', 10.00, '普通门诊挂号'),
    ('expert', '专家号', 30.00, '专家门诊挂号'),
    ('special', '特需号', 100.00, '特需门诊挂号');

