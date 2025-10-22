-- 添加appointment_quota字段（如果不存在）
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'doctors'
      AND COLUMN_NAME = 'appointment_quota'
);

SET @sql := IF(@col_exists = 0,
    'ALTER TABLE doctors ADD COLUMN appointment_quota INT DEFAULT 30 COMMENT ''预约数量配额'' AFTER bio;',
    'SELECT ''Column already exists'';');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- 修改departments.room为可空
ALTER TABLE departments MODIFY COLUMN room VARCHAR(20) NULL COMMENT '房间号/诊室号，如201';

ALTER DATABASE hospital CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE departments CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE doctors CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE departments MODIFY building VARCHAR(50) COMMENT '楼宇名称';
ALTER TABLE doctors MODIFY title VARCHAR(50) COMMENT '职称';

-- 一级科室：内科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '内科', '圣心楼', 1, '内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
-- 二级科室：老年病科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '老年病科', '圣心楼', 1, '老年病科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
-- 医生：徐志红
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐志红', '$2b$12$46HByvuePKft2p6WF50WTOOPeu8AGQ32cRjaMJpmC5E/WZSrdYVKW', 'F', '14339670711', '14339670711@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年呼吸系统及心血管常见病，包括肺部感染，慢阻肺，慢支，肺结节，睡眠呼吸暂停，高血压，冠心病及体检报告的分析解读等。', 20, 'active');

-- 医生：杜萱
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杜萱', '$2b$12$V4s2NuNZd9L0Hljf.6xLAu7hIAUkY3O3mzcjPGikVIG7hodmImNxy', 'M', '18210053353', '18210053353@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '老年高血压，冠心病，心律失常，心力衰竭等心血管疾病的诊治。', 10, 'active');

-- 医生：梁伟
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('梁伟', '$2b$12$GZIpE9/fj92lh/mR11f33ee4/Qnptr5m0wgLLDpgsZdqorRBm5ACO', 'M', '17553035110', '17553035110@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '血脂紊乱，高血压病，冠状动脉粥样硬化性心脏病。

', 10, 'active');

-- 医生：庞小芬
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('庞小芬', '$2b$12$Su2T1CbdSPo8UiXHrOPtmetXL18PTebxkFVU47tMZ.372PSnFwXSa', 'M', '13200604502', '13200604502@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '老年代谢性疾病：原发及继发性骨质疏松症的诊断、治疗，老年人的骨关节炎，老年高血压、高脂血症，老年糖尿病等老年性疾病。', 10, 'active');

-- 医生：曹久妹
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曹久妹', '$2b$12$fZthvtvlB0QreQg1bKbp2euqyZQzpN9a7gj9B0AMgksTrmWfD6LYS', 'M', '14642621108', '14642621108@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '高血压病、冠心病、血脂异常、心律失常等心血管疾病的诊断与治疗，慢性病的健康管理。', 10, 'active');

-- 医生：缪婕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('缪婕', '$2b$12$XHRlIz8EbOP1k3gbmfEwcu6wi0f1S1cdMI/5oE/3qJcsqsfaAA8Nq', 'M', '17313500298', '17313500298@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年内分泌疾病（甲状腺相关疾病、糖尿病等）', 20, 'active');

-- 医生：赵雅洁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵雅洁', '$2b$12$0i/sOBmcLeTF66Ytf9N/x.0n/70hGD.TGXVk3Cm8Q6gWOh41GLWZq', 'F', '14582334538', '14582334538@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年肌少症，老年糖尿病，老年骨质疏松，老年甲状腺疾病与老年相关性疾病的诊治。', 20, 'active');

-- 医生：苏征佳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('苏征佳', '$2b$12$S1/t5pUgt0SouEbMRmfvvuzq4DNtMnuxrKqpEVSoaPpiz8.r/D1CG', 'F', '19106977991', '19106977991@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年血脂异常、冠心病、高血压的个体化治疗。', 20, 'active');

-- 二级科室：风湿免疫科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '风湿免疫科', '圣心楼', 2, '风湿免疫科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
-- 医生：杨程德
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨程德', '$2b$12$bS2zEelGMCJ7F0XU6rgJXu0QlazhB6ipN5Zrr73F33Fh8/5dpPUhy', 'F', '15398362082', '15398362082@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '主任医师', '系统性红斑狼疮、抗磷脂综合征、强直性脊柱炎、类风湿关节炎', 10, 'active');

-- 医生：滕佳临
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('滕佳临', '$2b$12$bWltEHHRWgTGng3nDjuN9ePxLtsF.rqUiEAuWJXPHHzrQb46ZhGOa', 'M', '14919795579', '14919795579@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '主任医师', '系统性红斑狼疮等多种风湿免疫性疾病的诊治', 10, 'active');

-- 医生：刘宏磊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘宏磊', '$2b$12$4kItivs.feOJJ8AtIGXEX.OUcIPYQuH4CA60sW1HvYxP92/5i6qNm', 'F', '13199585092', '13199585092@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '系统性红斑狼疮、皮肌炎、类风湿性关节炎、强直性脊柱炎等', 20, 'active');

-- 医生：程笑冰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('程笑冰', '$2b$12$uisR6O8coIIBOXinAjBR0eoAj0qK6nO40VQOf3TfUoNS42tfZ2Ufq', 'F', '13485451171', '13485451171@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '关节痛及系统性红斑狼疮、皮肌炎等常见风湿病。', 20, 'active');

-- 医生：叶俊娜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶俊娜', '$2b$12$6trX7T/1xzh6hl5Qx5E29ews64VynH3015oUVLaIjOjHy4q5qkq0C', 'F', '17384027113', '17384027113@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '系统性红斑狼疮，干燥综合症，类风湿关节炎，强直性脊柱炎，皮肌炎，硬皮病等', 20, 'active');

-- 医生：苏禹同
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('苏禹同', '$2b$12$qDBt7byN2wxBeG0FDBS3gear8cfFmEfT8D2nR3bU5BQf/o5hagB7y', 'M', '18593303705', '18593303705@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '皮肌炎、系统性红斑狼疮、类风湿性关节炎、强直性脊柱炎、成人斯蒂尔病等多种风湿性疾病的诊治', 20, 'active');

-- 医生：石慧
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('石慧', '$2b$12$Qw7uHXdcd1CG7.am2Yge7.OquvvF6QXTFgSZ2jYXEwG5iYgMbI4Su', 'M', '16184611066', '16184611066@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '系统性红斑狼疮、抗磷脂综合征、类风湿关节炎', 20, 'active');

-- 二级科室：高血压科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '高血压科', '圣心楼', 5, '高血压科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
-- 医生：高平进
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('高平进', '$2b$12$MlkdOXmCZoES6o1foXjMsuSGi7VOQ/f2BmR8uuSYwvW91SlgPQOAm', 'F', '19774996843', '19774996843@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '并发症的诊断与治疗，单基因遗传性高血压（二级教授）', 10, 'active');

-- 医生：陈绍行
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈绍行', '$2b$12$UXjeZpKZoYMV3cwMtYtwRukCXplmg18n6bevY1Sahpy1qMumxusz6', 'F', '17306468299', '17306468299@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '降压药物合理应用、高血压及其相关疾病鉴别诊断和治疗。', 10, 'active');

-- 医生：孔燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孔燕', '$2b$12$f7e9Jia26AbHlLMjPlL2S.thlDIORDBmo4vNt3LliIisT7ppbB4qu', 'M', '13810026086', '13810026086@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '老年性及顽固性高血压、高血压合并糖尿病', 20, 'active');

-- 医生：龚艳春
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('龚艳春', '$2b$12$f4EU.bGakkwGtmmb85rjf.ibR.EpWB6vt/D2aqF0iQ7/ka/.ewY5C', 'M', '19410727955', '19410727955@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '难治性高血压及内分泌性高血压的诊断和治疗', 10, 'active');

-- 医生：胡亚蓉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡亚蓉', '$2b$12$3yaPWKXdbKEwrmWmXAP8yuGTLQ.EbeqTCVl5QnWAJYbMOf.6/ByEq', 'M', '19349957310', '19349957310@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压合并肾脏损害的诊断和治疗', 20, 'active');

-- 医生：朱理敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱理敏', '$2b$12$fqEsYUIsBSpRQXWXK83TR.p1jWfXI6uFCwdUadDjgHZLqbx.Jy3/q', 'M', '16398471886', '16398471886@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '继发性高血压和难治性高血压的诊断与治疗', 10, 'active');

-- 医生：蔡凯愉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蔡凯愉', '$2b$12$Cc7pbaSkHq.YnpllphhmQuO.gFHR80n5U2TfvjB/oYhrrqJN0AztC', 'F', '18995619255', '18995619255@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压合并心脏病及糖尿病的诊治', 20, 'active');

-- 医生：唐晓峰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('唐晓峰', '$2b$12$K2PtskeR.pKd0zgIFB92rubE8OfvPSby1EayPwVcRukF2lv.Si81e', 'F', '14497478786', '14497478786@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '难治性高血压的诊断筛查和治疗', 20, 'active');

-- 医生：陶波
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陶波', '$2b$12$jRErl.CYed6mhQdAeF0VMOvezCbwGUUcxAUpC7zvSCa9ADnOdE.f2', 'F', '14819595113', '14819595113@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '肾动脉狭窄等继发性高血压的筛查、难治性高血压的治疗、血脂调控。', 20, 'active');

-- 医生：李燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李燕', '$2b$12$36G7Q3LEp9rbDu07B.wnyOA5MTVJlHuTdPKQ5njjDjeq0Dpe0ud3W', 'F', '18833953718', '18833953718@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主治医师', '高血压诊断和优化治疗', 30, 'active');

-- 医生：张瑾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张瑾', '$2b$12$TrM1tApdJd4fe1Mtsd7NAOxPcod9CkimyXajNmRyJ5VDFRxXBi52W', 'M', '17781802744', '17781802744@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '继发性及原发性高血压的诊治及合理用药。', 20, 'active');

-- 医生：王继光
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王继光', '$2b$12$kQ2oUrDEztMZ8FR9IvePQuL/wDuJqBUrcqP.QXwLKGmdDsXZ4jH7i', 'M', '17882893941', '17882893941@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主治医师', '难治性高血压、合并靶器官损害高血压', 30, 'active');

-- 医生：杨龑
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨龑', '$2b$12$065iSMGcntIGvqgVlT5/suNkXafOn3SxX3CQ/kfAR07W8FJ8AXEtu', 'M', '14596348124', '14596348124@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '特长：高血压心脏病的诊治、优化降压治疗', 20, 'active');

-- 医生：盛长生
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('盛长生', '$2b$12$F8ux/YRL9lMNnir/7qcAUeK3tjE2KJ8vsPWoJ0lXa568uomEwTxIO', 'F', '15787194506', '15787194506@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压及相关疾病的综合评估及病因诊治', 20, 'active');

-- 医生：黄绮芳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄绮芳', '$2b$12$mCfdjhtpj1dRpZ5PWySLXuZikcGHNRH2Qa44INWkiXQXbAaMzC1PK', 'M', '18448195935', '18448195935@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压及合并靶器官损害的诊治、降压药物合理使用', 20, 'active');

-- 医生：葛茜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('葛茜', '$2b$12$H1y5ZtZy/LAKhi7sEAgICuk9tmBmIh8MpMoolvtB/XxR4zAPkf7cy', 'M', '14982403818', '14982403818@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '难治性高血压及继发性高血压诊治，睡眠呼吸暂停相关高血压诊治。', 20, 'active');

-- 医生：王彦
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王彦', '$2b$12$HcifZpmuv0zc7p/VL8IQnOYQ0KLj03l8ZDXk1k9szUY61yBmjDz3u', 'M', '19438715135', '19438715135@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主治医师', '高血压病的诊断和优化治疗，遗传性高血压诊治', 30, 'active');

-- 医生：许建忠
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('许建忠', '$2b$12$AWHGikZjSmzyRiDcIKDnWu0gi6zZSsVcnFoBuZMpMaJquNfQllEHe', 'F', '15171069472', '15171069472@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', ' 难治性高血压肾神经消融治疗，肾血管性高血压介入治疗', 10, 'active');

-- 医生：徐婷嬿
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐婷嬿', '$2b$12$1u.AjwlsZ/P89PT.clP7Uet4qcytw.991PLYLru9ZQ4QEHxR1SNte', 'M', '17870854579', '17870854579@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '高血压靶器官损害诊治及降压药物合理应用', 10, 'active');

-- 二级科室：呼吸内科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '呼吸内科', '圣心楼', 3, '呼吸内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
-- 医生：李敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李敏', '$2b$12$OnynpllUeLqLV4x8qkwvbufJ.32fJ/ap4AWNcamPrQz6.N3sDmNZW', 'M', '18636045484', '18636045484@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '哮喘,慢性阻塞性肺病,睡眠呼吸障碍,肺部感染', 10, 'active');

-- 医生：高蓓莉  
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('高蓓莉  ', '$2b$12$U6m0NbWQg7lqcCr2Vs8Bbe6fAbY55i3UdxIwIU4ICAWJWg8VRf5Ny', 'F', '18592688426', '18592688426@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺癌、肺部感染和慢性阻塞性肺病等', 10, 'active');

-- 医生：时国朝  
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('时国朝  ', '$2b$12$sLPK8Y1HqjjKIkauUn10vej9fGd6MButGNoW.9etzNoQ0oB0.CsVy', 'M', '15249926919', '15249926919@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '呼吸系统疾病的诊治', 10, 'active');

-- 医生：程齐俭
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('程齐俭', '$2b$12$KyFZB/oYgmSA0OzsMyuN8uIQzz1nm2X689e7EVS4WU1nzQc7zM8GK', 'M', '18702764446', '18702764446@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部影像学诊断，急慢性下呼吸道阻塞性疾病、肺部感染性疾病、肺癌的诊断治疗。', 10, 'active');

-- 医生：朱雪梅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱雪梅', '$2b$12$kNo1CRpPHNr2h5SMmhUwvOiFlJ5L1/aAVbXDk1WR18q44fFh4q/Ae', 'F', '18727694430', '18727694430@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '肺弥漫性间质疾病、肺癌、肺内节结及呼吸系统其他疾病的诊断、鉴别及治疗。', 20, 'active');

-- 医生：汤葳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汤葳', '$2b$12$AMg5UCPcC5GRgyH.nvTAtumwKwOqaTslrurXP93FuF0lRW/SfVRKu', 'F', '17528853029', '17528853029@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '哮喘，慢阻肺，呼吸道过敏，肺部肿瘤等相关呼吸系统疾病的诊治', 10, 'active');

-- 医生：项轶
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('项轶', '$2b$12$Gem9qZvxfN1yZIUnJbciDu9/1I.A5iVcUZRWiWgVm2k1KuOVpYl7G', 'F', '14248532577', '14248532577@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '各种肺部恶性肿瘤的诊治（化疗、靶向治疗）、戒烟', 10, 'active');

-- 医生：李庆云
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李庆云', '$2b$12$cZx1KA9fczpfxdDakKOXeObGk8irrrd7n8N2Yr9JL8QFC9TW/DB9S', 'F', '13911514914', '13911514914@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '睡眠呼吸疾病，慢性咳嗽，哮喘，慢性阻塞性肺疾病，肺癌，无创通气，肺部感染', 10, 'active');

-- 医生：戴然然
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('戴然然', '$2b$12$606K8T2hmU9jjNP8c1u.f.trf9s5fd.2IkXilL5p6PNGp./z/ytvq', 'M', '19217734861', '19217734861@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '慢性气道疾病及肺癌的诊治，戒烟', 20, 'active');

-- 医生：倪磊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('倪磊', '$2b$12$kYG28X31qH78oa1UPI3Yl.y5DH3F1lcLEhU3q.sKG2rmFq/9gEgFS', 'M', '18271779360', '18271779360@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部结节、肺部恶性肿瘤、间质性肺病、慢性气道疾病等呼吸系统疾病的诊治。', 10, 'active');

-- 医生：陈巍
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈巍', '$2b$12$0BslqhSakDza6flmhnDcL.Jw.o4gELU.xQqyjX4t.UxoVaNZLQVK2', 'F', '17168212356', '17168212356@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺小结节、肺癌、哮喘、气管镜介入治疗', 10, 'active');

-- 医生：过依
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('过依', '$2b$12$1iz5m4c2xrzsT39uT78pr.ZDOK5.MzLEGAqEHvB3hUWhePaZrTVn6', 'F', '16739830322', '16739830322@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部感染，慢阻肺，哮喘，肺癌等呼吸系统疾病的诊治', 10, 'active');

-- 医生：周敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周敏', '$2b$12$nuDzGKA5ct2wR1gCz8pYW.3YmrsuFYP5ERdkQuq6I3C.YKgWqul4u', 'F', '17369953851', '17369953851@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '主任医师，正教授，博士研究生导师，法国居里研究所博士后；瑞金医院呼吸与危重症医学科副主任，上海市科委优秀学科带头人，中国医师协会呼吸病分会优秀中青年医师，上海市卫生系统三八红旗手和巾帼建功标兵；任中华医学会呼吸病分会呼吸治疗组副组长、中国医师协会呼吸分会慢阻肺委员会委员、上海市女医师协会肺癌专委会副主任委员等。主持科技部国家重点研发计划慢病专项和国家自然基金等多项科研项目，发表论文100余篇，其中SCI论文近50篇，参编或副主编专著多部，擅长重症哮喘、慢阻肺、肺部感染及肺癌等呼吸疾病的诊治。', 10, 'active');

-- 医生：丁永杰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('丁永杰', '$2b$12$tXbkZXC/zXJXJhIc0Y8LSe4UHyRC.mdRNpdYlAzEEO5QeGat2LtOi', 'M', '18873869166', '18873869166@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '肺栓塞、肺动脉高压、肺源性心脏病、肺部感染。', 20, 'active');

-- 医生：李宁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李宁', '$2b$12$qQL5YsOIEbqy.qJhnohyc.spN/jLvPpOx8LG3RXlw7XUr5rbquOqy', 'M', '18676567501', '18676567501@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '无创通气治疗、睡眠呼吸障碍、肺部结节、咳嗽、肺部感染', 20, 'active');

-- 医生：包志瑶
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('包志瑶', '$2b$12$XetBHJQsQCsUpCTrxitR5.xsresHSmOUaDlQhwkyaf5xF/MSSrrIu', 'F', '19788227492', '19788227492@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '肺癌全程管理，肺部感染性疾病，慢性气道疾病规范化管理。', 20, 'active');

-- 医生：王晓斐
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王晓斐', '$2b$12$Wt2bW9QkIFZSs3U0o8D5T.JMRTwILZ0vln46GkR/9IXT4ykKjzP.O', 'F', '13415143362', '13415143362@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部恶性肿瘤的个体化诊疗', 10, 'active');

-- 医生：周剑平
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周剑平', '$2b$12$m2fX15T2t4OenHIVSsfiCuV9R9trcnSW7T77TdHoH.pUTDA14YJlq', 'F', '14587182120', '14587182120@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '哮喘、肺炎、咳嗽、肺结节读片、气管镜诊治、戒烟干预。', 20, 'active');

-- 医生：冯耘
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯耘', '$2b$12$LSpO6BuUMmASjr3oUCOkre4xuCmNisLYOEJ/v.5CkfaHsej8SPHiq', 'M', '18872751234', '18872751234@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部结节、咳嗽、支气管扩张、肺部感染、肺癌', 10, 'active');

-- 医生：孙娴雯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙娴雯', '$2b$12$dkJmTTLFeJQFtqwGwkHcvuoDay3sKotrx5NuuPErLHeM2E0M3emxC', 'F', '17918150683', '17918150683@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '慢性气道疾病及肺血管疾病的规范化诊治。', 20, 'active');

-- 医生：瞿介明
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('瞿介明', '$2b$12$yEYC75WY8Bisp266/7hmp.i63Gmtp9wj01AX9GWgSxO98w.Ld0ZdK', 'M', '17214257751', '17214257751@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '慢性咳嗽、哮喘、COPD和肺癌等呼吸科常见病及呼吸疑难疾病的诊治。', 10, 'active');

-- 二级科室：内分泌科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '内分泌科', '圣心楼', 3, '内分泌科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
-- 医生：陈瑛
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈瑛', '$2b$12$eRUPjns38MuBYACSK32C2OCK5Xcp57Yg112ABKqzBgCf0t7jLu4WG', 'M', '14501486939', '14501486939@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '尿崩症、水、电解质紊乱、内分泌与代谢疾病', 20, 'active');

-- 医生：汤正义
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汤正义', '$2b$12$zATOWRq8XK4H315QEbxl.ur4FVwWPdm5r19R8swU2bJwk0QSiVKXi', 'M', '17936043811', '17936043811@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病血糖控制与疑难并发症的诊治，甲状腺、肾上腺与垂体等内分泌病的疑难杂症', 10, 'active');

-- 医生：刘建民
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘建民', '$2b$12$metY/aHq7F3E6HNX0tG.Nuy/HysGtZKKKORMtxInQahq461QLXdvC', 'M', '17448059918', '17448059918@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲亢、肾上腺疾病、甲状旁腺疾病、骨质疏松', 10, 'active');

-- 医生：赵红燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵红燕', '$2b$12$bQuFQqElG3GRjHMq0WaOneb28gFre3BDSklMlWQR9b2.mlLeT8Rru', 'F', '13220117054', '13220117054@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲状腺和甲状旁腺疾病以及骨质疏松症的诊治', 10, 'active');

-- 医生：陈宇红
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈宇红', '$2b$12$d7EcDBz8rJbelFGbEG03h.gAJCJL8i/gbfFes/Fwj5j7w7CXobir6', 'F', '19966267851', '19966267851@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病，甲状腺疾病的诊治', 10, 'active');

-- 医生：王曙
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王曙', '$2b$12$5Yub34Z/0Qvw5j8QNYm8WuDBbbU5D7mpH/lEmc29QMKkCZTpkvYoG', 'F', '14162196678', '14162196678@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '内分泌疾病、骨质疏松、糖尿病、甲状腺等', 10, 'active');

-- 医生：毕宇芳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('毕宇芳', '$2b$12$eZKSO6TR92pzedAyAkSYXeV0kZB9YrDEKJyf4WcKoCZzw9uVVEor.', 'M', '17184564737', '17184564737@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病和甲状腺疾病的临床诊治（二级教授）', 10, 'active');

-- 医生：洪洁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('洪洁', '$2b$12$voTSzrCY5Zqu6fXLV7MGq.V9D4cl11ABBMpi5y8dunDqMIIOflDJO', 'M', '18621828280', '18621828280@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '肥胖和糖尿病的病因诊断和治疗。', 10, 'active');

-- 医生：孙首悦
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙首悦', '$2b$12$DzjSO5uWDaQP.6bHwYU5qeV3S3xJeaeQ1vfOUKCW4rodxFzODl29a', 'M', '19671988889', '19671988889@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '内分泌科性腺发育异常', 10, 'active');

-- 医生：孙立昊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙立昊', '$2b$12$SdrL/DbZ0hBBMl0B.707z.35AbtKAhpaRZQQqeQsAuM6la5L3X9Fe', 'M', '14808402051', '14808402051@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '骨质疏松、甲状旁腺疾病等代谢性骨病', 20, 'active');

-- 医生：顾卫琼
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('顾卫琼', '$2b$12$FNDQ33GUTTwt02qLFFp7Pu0u2KNwGlXL9a8xbXqsNYxWKLGdfgMq.', 'F', '17277303722', '17277303722@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '青少年糖尿病分型诊断、临床诊治与应用基础研究，以及干细胞新兴领域的探索和临床应用', 10, 'active');

-- 医生：汪启迪
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汪启迪', '$2b$12$2/Mcanh8B5/N0PJUIi4acOUjgeN9q2QTBeHwpSBsxlUlSfuKbyIQC', 'F', '17751325257', '17751325257@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病，甲状腺疾病，及其他内分泌代谢疾病的临床诊疗', 10, 'active');

-- 医生：周瑜琳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周瑜琳', '$2b$12$168Oa2bUikn6T1V/iky5RO0aOib80p9Ps3IO3zq/wL68ivjUHQkqi', 'F', '14679064766', '14679064766@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '各种类型的甲状腺疾病及糖尿病诊治', 10, 'active');

-- 医生：苏颋为
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('苏颋为', '$2b$12$rNbOMS7UFYm27vIsDyasTeo6opvtphhk1nSCTUCa9K4PZDbrlzr6.', 'M', '18434702822', '18434702822@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病，甲状腺，内分泌相关肿瘤，电解质紊乱，垂体和肾上腺疾病。', 10, 'active');

-- 医生：周丽斌
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周丽斌', '$2b$12$/It5zVcgkJbbfu5fxZaCtu/LbBEa2e4wDDL.uXs4.x28KZDy8igE2', 'F', '18797801251', '18797801251@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲状腺等内分泌代谢性疾病的诊疗', 10, 'active');

-- 医生：张翼飞
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张翼飞', '$2b$12$Rf/lF9LqrqWnarPyvXwiaOzoeCDeqtwG3ilW4yZW1nU4Kt.ceIBq2', 'F', '16655742829', '16655742829@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '擅长肥胖、代谢综合症、糖尿病及相关疾病的临床诊治', 10, 'active');

-- 医生：顾燕云
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('顾燕云', '$2b$12$.VwKgjIAsSfMNlYkcTu7s.M5Wzd/4y9J2AGCrWARqkXlr8P/fnqL.', 'F', '13366186631', '13366186631@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '各种内分泌代谢病相关常见疾病，包括2型糖尿病、胰岛素抵抗、高尿酸血症及常见甲状腺疾患等', 10, 'active');

-- 医生：陆洁莉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陆洁莉', '$2b$12$jqb004OV93idRTpcO2vB.eO47mSl5VgjLBK2wgLs4AT1V82u6XCrm', 'M', '13463016614', '13463016614@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲状腺疾病及内分泌代谢病', 10, 'active');

-- 医生：姜蕾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('姜蕾', '$2b$12$nLikTdJ96FDn68L/97BIF.mMlg1GCt5WT9ZCG9PNMZTM3UbR1RcPC', 'M', '17694768704', '17694768704@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '尿崩症等垂体—肾上腺疾病，糖尿病，甲状腺', 20, 'active');

-- 医生：叶蕾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶蕾', '$2b$12$7lL3b2e/XgzSuBMfkdHmCerPU0wk9ZmwqA2skuZb.DnQmIHvOCQqW', 'M', '17336456621', '17336456621@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '甲状腺癌、多发内分泌肿瘤、疑难甲状腺疾病', 10, 'active');

-- 医生：陶蓓
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陶蓓', '$2b$12$eP8SC1sxlh5v2FOP/KiTl.luz2a/ag4eenMYVyXGxh.NcbhqXmg0.', 'M', '13860038427', '13860038427@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '钙磷代谢、骨质疏松、代谢性骨病的诊治', 20, 'active');

-- 医生：周薇薇
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周薇薇', '$2b$12$/x0LjmFOZbWtSogl9LIuhedwK4LxCEG1g7PISkr3hsgWVg.hndWJu', 'M', '14172370545', '14172370545@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '垂体肾上腺疾病', 20, 'active');

-- 医生：田景琰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('田景琰', '$2b$12$whby1bxOrlQg/bOyv0ti/ef8np4hGmJmP9lDarkOgvT24EmmRpf0G', 'M', '19454794895', '19454794895@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '擅长糖尿病初发病的诊治和糖尿病的强化治疗', 10, 'active');

-- 医生：蒋怡然
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蒋怡然', '$2b$12$UpnaJuD22bxmN5TcsH3zMu0KHuX/exn2JFwTWSddqtkwm.ziJfWJa', 'M', '17355555531', '17355555531@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '垂体肾上腺疾病', 20, 'active');

-- 医生：张翠
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张翠', '$2b$12$dz.rCuM1eYqJSBgS2hSDYu9djBgcZhKAG0V5Um1BNWNxxxRmqWEQK', 'F', '18621209849', '18621209849@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '肾上腺疾病', 20, 'active');

-- 医生：沈力韵
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈力韵', '$2b$12$GJVnD9bPXYzoDvolaSWJ8.Zq19xGLH22J5W7Il2ltlWRqN08PqPWK', 'M', '17242068763', '17242068763@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '甲状腺眼病', 20, 'active');

-- 二级科室：神经内科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '神经内科', '圣心楼', 5, '神经内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
-- 医生：陈生弟
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈生弟', '$2b$12$1jzBNMKVrutyZatNU8NUv.EADilx.ysQZaVDUAVX8P7jyeBxr//dC', 'F', '14942478695', '14942478695@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病、运动障碍病、老年性痴呆、认知障碍、神经系统疑难杂症等各种神经系统疾病(二级教授)', 10, 'active');

-- 医生：刘建荣
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘建荣', '$2b$12$12mve2bdYgF9htpLPrwg/.zGi57AHXtZbt1sWzjTCgen8Nk.o7siO', 'F', '19537077308', '19537077308@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '各种神经系统常见病、疑难和危重病人的诊断和治疗，以及脑血管病的诊断、治疗和预防', 10, 'active');

-- 医生：王瑛
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王瑛', '$2b$12$Kc.JsxwlrIZFe6GB5NDnp.UUMrdGO/slScOetgnNSh0jHhByqdLPK', 'M', '13204078666', '13204078666@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病、记忆障碍、睡眠障碍、神经系统疾病伴抑郁焦虑', 10, 'active');

-- 医生：傅毅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('傅毅', '$2b$12$9Cr7cj4OErRhp2unjZjw3OgH05YNG8oJWD8VcDeamsGZ3ljS8P9b6', 'F', '15554814084', '15554814084@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '脑血管病、偏头痛及头晕的诊断和治疗', 10, 'active');

-- 医生：汤荟冬
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汤荟冬', '$2b$12$H.7tXt/djgnhZjcGrM1gp.WdmHSKnfc4eRb7HgR8JX1jjD576GmNe', 'F', '16882839238', '16882839238@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '记忆力减退、癫痫', 10, 'active');

-- 医生：肖勤
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('肖勤', '$2b$12$.G/R0LY9Abi0WcUg3buXiOJDOCMko92Ffr.mCT7mioDPTnB3OeAv.', 'M', '18801642483', '18801642483@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森氏病、肌萎缩、神经内科常见疾病', 10, 'active');

-- 医生：邓钰蕾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('邓钰蕾', '$2b$12$UhWd.Osf7MYKOChNJYZA3.DN5fBQHUeOo2GbcVrKhN3Jj/3PCB1ra', 'M', '13532311310', '13532311310@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '癫痫等发作性疾病、老年性痴呆、头痛、睡眠障碍、脑卒中等神经系统疾病。', 10, 'active');

-- 医生：马建芳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('马建芳', '$2b$12$xkjf60V2fCXitETvEkzXTO9XQ.fRQIeoJ.eEJvGbxXxdJGiXjmsey', 'F', '19217326729', '19217326729@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森、失眠、不宁腿，打鼾等睡眠障碍，姿势障碍，抽动症和斜颈，偏头痛等。', 10, 'active');

-- 医生：任汝静
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('任汝静', '$2b$12$/E4dSRu8hod62fdb.LHkYOX3hoLehgN3NlNlKn9s1qFYD6OoxGRD2', 'M', '14304235259', '14304235259@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '痴呆、帕金森病、脑血管病、头痛头晕、癫痫、睡眠障碍等神经疾病。', 10, 'active');

-- 医生：刘军
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘军', '$2b$12$2rMmV012ZP2QTQFRjjMOBupbT84L8HZLo6vqpBzJEOHD/../Hbw7i', 'F', '14552991960', '14552991960@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病、痴呆、肌张力障碍、肌萎缩、睡眠障碍、脑血管病等神经内科常见病和多发病的诊治。', 10, 'active');

-- 医生：曾丽莉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曾丽莉', '$2b$12$8wqAHIwrja1Twp2zSi2RLOT/0QgyEoPsKe/4CiBf2u3T85bmlp7sK', 'M', '15596743109', '15596743109@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病及相关情感认知障碍的诊断及治疗', 20, 'active');

-- 医生：徐玮
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐玮', '$2b$12$hoYDj5PgrxSx1dgj.tk7SOvR1KO3.e9hiJoj8EqMgdCh50D3S4naK', 'M', '19180943908', '19180943908@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '记忆力障碍，睡眠障碍，头晕头痛及其他神经系统疾病的诊断和治疗。', 10, 'active');

-- 医生：辛晓瑜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('辛晓瑜', '$2b$12$PBY7tLRWVAWD6qGNhK3cBOBpp0aSzBP1H/cwa4L6rUqzUt93oyQse', 'F', '19690907304', '19690907304@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病及卒中后管理，血管性认知障碍，头晕', 20, 'active');

-- 医生：吴逸雯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴逸雯', '$2b$12$YqUeIjkqi6hKKgAwOK19r.2.9fouxgpeJxQfpA/P8n5p3S6rqUor6', 'M', '13800235246', '13800235246@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '肌张力障碍、震颤、帕金森病、舞蹈、共济失调；肉毒毒素治疗；神经调控治疗', 10, 'active');

-- 医生：周海燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周海燕', '$2b$12$AVYwWZSIdkkLeRgX4PqY7OGEW4v5ONcaFGv0IzTeqSfp0FBG2o59O', 'M', '13909134796', '13909134796@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森病，震颤，肌无力，肌萎缩，DBS程控，神经科常见病。', 20, 'active');

-- 医生：陈晟
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈晟', '$2b$12$WtMFIf5RWYArL5vbLTS6ouS9yAuJV8TI1U.ICiqeazRPeyCtIrpV2', 'M', '14536383774', '14536383774@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '擅长神经科常见疾病，头痛，头晕，脑血管意外，肌无力，肌萎缩等诊治，尤其擅长神经免疫性疾病（自身免疫性脑炎、多发性硬化、视神经脊髓炎、重症肌无力）、疑难疾病诊治', 10, 'active');

-- 医生：潘静
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('潘静', '$2b$12$tOHCq94LIManr2MXqXNCi.GvfHihYP/l9LFge6rUDcNIrpiWcsVgS', 'F', '16329509408', '16329509408@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森病、特发性震颤、睡眠障碍、脑血管病', 20, 'active');

-- 医生：谭玉燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('谭玉燕', '$2b$12$4z8eosmCvzmYIiYPR7zHReiJyoco7OkPB2IRnUMaugx32NLiNwKPu', 'F', '13276777762', '13276777762@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病，多系统萎缩及其他运动障碍疾病。', 10, 'active');

-- 医生：李彬寅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李彬寅', '$2b$12$F7S7i31ovdEu21V11o3NOutr0nz.i2jgCXMSo3vH1/UgNM/DvYgBu', 'F', '13519212169', '13519212169@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '阿尔兹海默病、血管性痴呆等认知障碍类疾病。', 20, 'active');

-- 医生：尹豆
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('尹豆', '$2b$12$hoVX4yqcpg/b2bjfX/XYluCQqqhg41JDnV/MA/QxjBly77de8NHKK', 'F', '19943026227', '19943026227@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病，脑动脉狭窄，神经血管介入治疗。', 20, 'active');

-- 医生：李媛媛
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李媛媛', '$2b$12$yGEqFjCafFQkJODY2QlfUu5rlpEPBy4RXc0a9FWQbdK5DvJh0n/Q2', 'F', '15554200826', '15554200826@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森病，震颤及睡眠行为障碍。', 20, 'active');

-- 医生：杨晓东
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨晓东', '$2b$12$Tb1dDQSeUnhRW3PXlklJYOlCosAyY0NjkqmDF3sV12WoD5xN2Crqu', 'F', '14303901283', '14303901283@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森，脑血管病，头晕，失眠等神经内科常见病。', 20, 'active');

-- 医生：沈帆霞
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈帆霞', '$2b$12$sYE6eb493KSbnFlPWxxR9eytAdj6/GbtTwZ3xbMhp9N.YwIUyIuxe', 'F', '14162795957', '14162795957@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '各种脑血管病（如脑小血管病、血管炎、脑白质病）、血管性痴呆、头痛及其它神经系统常见与疑难病诊治。', 10, 'active');

-- 医生：康文岩
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('康文岩', '$2b$12$2vlHXPL3dFKkXMi1dv4GV.e/pqOgMLX/Imr4T.fO3vtqRP/JUA2SG', 'M', '18436730606', '18436730606@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '诊治帕金森、智能减退、脑血管病、头痛、头晕、睡眠障碍、肢体麻木、面肌抽搐', 10, 'active');

-- 医生：江静雯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('江静雯', '$2b$12$WSt8op55521DyBWPZ5ahVOgLuYF/afAbjyzFQYsz0YgruldvHpKSe', 'M', '13727255918', '13727255918@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '头晕、腔梗、脑白质病、记忆减退及神经科常见病。', 20, 'active');

-- 医生：胡震
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡震', '$2b$12$FGVhHfeg/VjyAXzrhzFjxe9rn8zEXu1/PCp9e92yYEohlreVPx2j2', 'F', '17670292350', '17670292350@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑梗塞，脑血管病的微创介入治疗，脑动脉支架植入术', 20, 'active');

-- 医生：刘斌
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘斌', '$2b$12$OpYsa6fQRU10waEU8z750.E8iAqWs.YjhRBoIcca6XkLOtybyAPpa', 'M', '13645276696', '13645276696@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病的诊治，脑血管介入治疗。', 20, 'active');

-- 一级科室：外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '外科', '圣心楼', 1, '外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
-- 二级科室：骨科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '骨科', '圣心楼', 2, '骨科专业诊治各类骨科疾病，包括骨折、关节疾病、脊柱疾病等，拥有先进的骨科治疗技术。');
-- 医生：王亚梓
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王亚梓', '$2b$12$riS6XwoWACrBlJstaxQMQefr7B9ONyK90aO2ixmmy/eayzBKrU3rC', 'M', '17172972420', '17172972420@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '四肢关节周围骨折、骨折愈合障碍、膝关节和肘关节屈伸功能障碍的治疗', 20, 'active');

-- 医生：梁裕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('梁裕', '$2b$12$oHRQfWvPaeSPZKiDakzo8u.0.cr8cWt1dXqRUbFRKn.iOondHvfCq', 'M', '16228727266', '16228727266@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '脊柱骨折脱位伴神经损伤；脊柱畸形，包括青少年特发性脊柱侧弯、先天性脊柱侧弯、成人脊柱侧弯以及创伤后脊柱畸形等；脊柱退行性疾病，包括椎间盘突出，椎管狭窄，腰椎滑脱等', 10, 'active');

-- 医生：张伟滨
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张伟滨', '$2b$12$YapFylmf2B9GjPB58nmUqOvEAn7N/kJniwfmKakEZTaxbhbL7r/JS', 'M', '17738360085', '17738360085@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '各类骨肿瘤保肢治疗，转移性骨肿瘤的综合治疗，骨质疏松诊治、各类复杂骨折', 10, 'active');

-- 医生：冯建民
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯建民', '$2b$12$pGhTdIWnefUTrxHzNso9quOCJHNBhjBkPKFvuxVv8hQ2kPZ6jXdUa', 'M', '17188029013', '17188029013@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '关节外科，骨关节病，人工关节', 10, 'active');

-- 医生：王蕾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王蕾', '$2b$12$g8sHzKOWoIs5pKhd4IVAtuvQHpKtVw2V3sFh1vAZkRPoKr8OHAJLG', 'F', '18726713347', '18726713347@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '肩关节外科、肩关节外伤、肩关节疼痛等疑难病例，四肢关节周围创伤、骨质疏松骨折、骨折后并发症', 10, 'active');

-- 医生：曹鹏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曹鹏', '$2b$12$5bHfdxtzd7mT3SBWLnu51Ow0Al8kOALPZPzLJ/IzR.di1Lsmf.Y7O', 'F', '15319321646', '15319321646@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '临床常见颈肩痛、腰腿痛等脊柱退行性疾病、脊柱损伤的微创手术治疗；复杂脊柱疾病（如：脊柱畸形、脊柱肿瘤、脊柱炎症）的综合诊断和治疗。', 10, 'active');

-- 医生：刘志宏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘志宏', '$2b$12$msAkb/2ArdzPNtFbGtmWmO0kbt0eyFiOW9lLkF3XDhrDYVrR3xKHe', 'F', '14385201412', '14385201412@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '髋膝关节外科，包括髋、膝关节骨关节炎，股骨头坏死，髋关节发育不良的人工关节置换，关节周围截骨手术以及髋膝关节置换术后的翻修手术治疗。', 10, 'active');

-- 医生：刘津浩
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘津浩', '$2b$12$NM4.VCj7byL5MeIx0llie.WYCnYc2Ny48r8gF0B6xPTzjRlk5ECry', 'F', '14821221887', '14821221887@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝部畸形（包括先天性、后天性、创伤性）矫正、运动损伤、四肢骨折及其并发症的治疗', 20, 'active');

-- 医生：徐向阳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐向阳', '$2b$12$d/TchNzJhsZ88jpcJTCOu.5Bf0QOWAjDJ9tvwk9xjkXTz0rwFU2i2', 'F', '16439492674', '16439492674@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '足踝外科各类疾病', 10, 'active');

-- 医生：张兴凯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张兴凯', '$2b$12$T.A5WySLDyRhqd8nqyhTIu0.1fkCTEnd343FnZp216aV016qypqkS', 'M', '13592080329', '13592080329@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '脊柱疾病， 颈腰椎间盘突出，椎管狭窄，脊柱骨折，脊髓损伤，脊柱畸形，骨质疏松症', 10, 'active');

-- 医生：万荣
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('万荣', '$2b$12$RWAlD/hqwhMxgsb1bizExeRUb4xjCd.pFOf.F/iQcNwm6nUgXbVo6', 'M', '13677280546', '13677280546@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '骨与软组织肿瘤诊治，骨质疏松症及各类关节内骨折。', 10, 'active');

-- 医生：吴文坚
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴文坚', '$2b$12$fqFfzFdkGuv8BsPq0yeHRujeC.eFNMBgipXBSq5.Im5nXaZ5vPKee', 'M', '17384756779', '17384756779@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '脊柱微创手术，颈椎病，腰腿痛，脊柱损伤、畸形', 20, 'active');

-- 医生：张炅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张炅', '$2b$12$zbllLbJ1hxJvuR4LK0oSiOItJibjUmLsql.P/2xsssbA89G9c5DJK', 'M', '15173864104', '15173864104@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '髋关节膝关节骨关节炎诊治；先天性髋臼发育不良诊治；股骨头无菌性坏死诊治；髋膝关节人工关节置换手术', 20, 'active');

-- 医生：庄澄宇
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('庄澄宇', '$2b$12$jLL4.nfSVhP.defltyJXOuPJ.MzafQlS95yUQIkOmXa28T6FBrsWK', 'M', '15406002884', '15406002884@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '肩关节外科及运动伤病的治疗', 20, 'active');

-- 医生：沈宇辉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈宇辉', '$2b$12$Ovyxt7bOAp2Swh2lySFvxOMa.GlDq8XTV.TFDSIW9A9LHPPSENVsq', 'M', '16995226828', '16995226828@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '良恶性骨与软组织肿瘤、转移性骨肿瘤、髋膝肩关节疾病、各种代谢性骨病、骨质疏松。', 10, 'active');

-- 医生：朱渊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱渊', '$2b$12$9T4vjFl92jMJSMcy/MjjY.SRSE2SaRXjK.ybgOOUU05YdELFALrSC', 'F', '17966616964', '17966616964@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝外科各种畸形矫形、足踝运动医学、创伤', 20, 'active');

-- 医生：何川
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('何川', '$2b$12$Ebou6npNAGlDAHFy7TLs2uzjQxjCATLUPZexTOYXuSbPiq7tUBhKy', 'M', '18977358886', '18977358886@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '髋、膝关节疾患的诊断和治疗，关节置换和关节镜手术', 10, 'active');

-- 医生：王碧菠
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王碧菠', '$2b$12$cJGaEROsc2C53lQZit1IQ.VX5C4xm8ICP4QL.fXnTUonfAun5vwZK', 'F', '18211225158', '18211225158@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '四肢骨折及运动损伤，手足外科各类疾病。', 20, 'active');

-- 医生：叶庭均
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶庭均', '$2b$12$w24IoHNOeR7nJ5IYXl7zyOo6Dnu3RW.yV02ZIoZxyryDQUadYgtmG', 'M', '15223940587', '15223940587@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '运动损伤，膝关节半月板，韧带损伤，骨膝关节疾病。', 20, 'active');

-- 医生：王弘毅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王弘毅', '$2b$12$pr58LZu3DSEBGOimluPoeu1WQYLP/kJ5yUbYwIdI5YL/2NCf2tJ2u', 'M', '18694019365', '18694019365@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '髋、膝关节疾病的诊治，包括骨关节炎，创伤性关节炎，股骨头坏死，髋关节发育不良等微创和人工关节置换手术', 20, 'active');

-- 医生：虞佩
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('虞佩', '$2b$12$9ZWcugbT6BlYxrr3M0937.y1RXSufk6eQgHSvQsIv9ox0hpYDKXa2', 'M', '15402533494', '15402533494@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '复杂骨折微创治疗，运动损伤、慢性损伤', 20, 'active');

-- 医生：温竣翔
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('温竣翔', '$2b$12$xwBKFns0wMHAwh1dZquQwuGgxZqvd.QeonSBJ8oYUJJN/lpqD5ZAG', 'M', '18468164906', '18468164906@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '骨与软组织肿瘤的综合治疗，转移性骨肿瘤的综合治疗，代谢性骨病，骨质疏松的综合治疗。', 20, 'active');

-- 医生：于涛
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('于涛', '$2b$12$EN.EvUG.CPpf6ua0Oa6jE.uBcWZ2vgJBQa.4tio5SBnsVFJCXaB1e', 'M', '18781007818', '18781007818@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝疾病、畸形、关节退变及骨折的诊治。', 20, 'active');

-- 医生：杨云峰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨云峰', '$2b$12$5O2HyP8WvcZh3wK6sibX4eazAtWTw9wketkH9KQSUjgRmz1ImiSiy', 'F', '17624557080', '17624557080@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '骨科、足踝外科、创伤骨科', 10, 'active');

-- 医生：李兵
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李兵', '$2b$12$UvmrPHcGSo.GrzBK.8SGZ.frBgBAzRChvxI/.7h0G4xyi.gQVd8Ru', 'F', '19154544899', '19154544899@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝外科相关疾病（骨折，外翻，手足高强运动损伤，退变等）', 20, 'active');

-- 医生：杨军
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨军', '$2b$12$.tQ7n48qaAez3Z0yRYB1GOmJV1M5Sx10yy9R0PoWhXj6cpr.DxrTK', 'M', '18554811020', '18554811020@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '脊柱肿瘤，颈、腰椎病，上颈椎脱位，脊柱侧弯畸形，脊柱结核感染。', 20, 'active');

-- 医生：倪明
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('倪明', '$2b$12$0XbHl3fDfEH9ibLa.ZM5yuHBEOkQZwnYCfEuDSpCleUzj.JUIFiye', 'F', '13103807155', '13103807155@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '肢体骨折、足踝畸形矫正、运动损伤与康复。', 20, 'active');

-- 医生：徐建强
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐建强', '$2b$12$D7lmi8GEOxx7N3GbxQrC6uqJCjPacpZvKpGuca9Xduwnky0nE/rLO', 'F', '19240466536', '19240466536@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '骨肿瘤、脊柱肿瘤，各种骨与关节损伤，骨与关节炎症及风湿性关节病，骨质疏松等疑难杂症', 20, 'active');

-- 医生：郭常军
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郭常军', '$2b$12$8iP4nMGJ0K.VUwLlVnjTROp9HyJVkoVRs6fdL6rwjw2EZOBNqXYOa', 'F', '14895890631', '14895890631@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝外科疾病（踇外翻，扁平足，高弓足等），足踝运动损伤和四肢创伤。
', 20, 'active');

-- 二级科室：功能神经外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '功能神经外科', '圣心楼', 4, '功能神经外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
-- 医生：占世坤
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('占世坤', '$2b$12$xR0UVgBawKxOXrfQ/a3x/Oh9HwmTmu41.HnkPUjUi0f3g4HKsZDvS', 'F', '17110382761', '17110382761@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '主任医师', '帕金森、肌张力障碍、疼痛、癫痫、精神疾病、脑肿瘤、脑瘫的外科手术及伽马刀治疗.', 10, 'active');

-- 医生：孙伯民
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙伯民', '$2b$12$qikwUNMpGlBvbjLbilPcwuSgvwTdh9RyJZiAdZZjiAojfZYf9Muv.', 'M', '13841976666', '13841976666@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '主任医师', '癫痫、帕金森、肌张力障碍、疼痛、神经性厌食症、精神分裂症、强迫症、焦虑症、面肌痉挛、三叉神经痛', 10, 'active');

-- 医生：李殿友
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李殿友', '$2b$12$4idoDO6LNhv3CNApYy8dA.NWEv9VnUEEn1tlekWYnsZNTBOAutkBi', 'M', '17138684919', '17138684919@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '主任医师', '帕金森、震颤、肌张力障碍、疼痛、抑郁症的外科治疗', 10, 'active');

-- 医生：潘宜新
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('潘宜新', '$2b$12$dXGKrdAzbkU5w18zKgGk2uF.VA8Qof8dgXQ2rSmEqDv9SlCmfquW6', 'F', '17693269304', '17693269304@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '副主任医师', '肌张力障碍、帕金森、糖足、疼痛、震颤、截瘫、抑郁症及精神疾病的外科治疗', 20, 'active');

-- 医生：刘伟
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘伟', '$2b$12$maxoip0hTkwajq0mpNRI1elhFjxUlp.UhNR/4.fOyI4fiJIrIn2.S', 'M', '16236843584', '16236843584@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '副主任医师', '疼痛、震颤、糖足、肌张力障碍、帕金森、厌食症、精神疾病的微创治疗', 20, 'active');

-- 医生：胡柯嘉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡柯嘉', '$2b$12$4dhODom6pPQZx27HH/TC.umvkzjbiJnRGaHPhix4tcQfbcNcFZKjW', 'M', '15491541578', '15491541578@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '副主任医师', '疼痛、肌张力障碍、糖足、帕金森、脊髓、周围神经病变。', 20, 'active');

-- 二级科室：心脏外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '心脏外科', '圣心楼', 1, '心脏外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
-- 医生：陈海涛
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈海涛', '$2b$12$WZZLugXHV2aK6ahwS9yl/ua8qUIg.Jp6iVxK3vlkbSF4JAuZSFQYy', 'F', '14832363533', '14832363533@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '胸腔镜手术，风湿性心脏病，冠心病', 20, 'active');

-- 医生：裘佳培
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('裘佳培', '$2b$12$LjG5tOme2oiJt.Sc1uWuYe79WgJcCkx1YbPVRRFZ78GqGYyIwpssW', 'M', '18210373808', '18210373808@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '特长：微创冠脉搭桥术及各类冠心病、瓣膜病、大血管疾病的外科治疗', 20, 'active');

-- 医生：赵强
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵强', '$2b$12$ar8i/Hbuv4ZN.iPRCLqbf.X1f/EFwe8st1BChU0epK9Op/enGp4oS', 'F', '19701170349', '19701170349@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '冠状动脉搭桥术，微创冠状动脉搭桥术、微创心脏手术，机器人辅助心脏手术、心脏瓣膜成形术、
主动脉瘤的外科治疗、
心衰的外科治疗，心脏移植，辅助循环
', 10, 'active');

-- 医生：王哲
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王哲', '$2b$12$5swAZ35Buda79af6waU/puXBtMKJQqU/2LAwNs0ixE0ZcE5CWAo0y', 'F', '17904745462', '17904745462@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '瓣膜病、冠心病、先心病、胸部动脉瘤微创手术', 10, 'active');

-- 医生：叶晓峰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶晓峰', '$2b$12$hvkcou2Gr8EcKfDM9naSC.EHTuO5BPNqMcwgUnG2H8jyloQVPlTeG', 'M', '14274484941', '14274484941@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '对心脏病的外科诊断和手术治疗有着丰富经验，擅长微创介入瓣膜修补、微创冠脉搭桥和大血管手术。', 10, 'active');

-- 医生：周密
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周密', '$2b$12$xAJ4E8cQ24/qRQgSa/lPmOggw0AzfSrv1IEyuhKv.lz5ddiD5Kt.m', 'M', '16126614158', '16126614158@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '心脏移植、人工心脏、冠脉搭桥及瓣膜手术、微创手术、肥厚型心肌病手术', 10, 'active');

-- 医生：李海清
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李海清', '$2b$12$dS0OsQO.LbHp2Kcgr7Gx6ePBA0OFNGv3vVw4v3eBt3Wst7EfHaUey', 'M', '18456681425', '18456681425@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '冠心病、心脏瓣膜病、先心病、心力衰竭等外科治疗', 20, 'active');

-- 医生：徐洪
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐洪', '$2b$12$MHN1mDAOPNdR2mE5maCyQuPnRZOljg9cZ7WOsnFBPHcH1Cvi4z/dG', 'F', '19819307302', '19819307302@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '各类成人心脏病如复杂冠心病、瓣膜病、大血管疾病、以及微创介入治疗、终末期心衰综合治疗。', 20, 'active');

-- 医生：朱云鹏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱云鹏', '$2b$12$s8QMSRpxMy.wACJmdXxBqO5gGCrUytQGRkMlxOvnO7TtfsmR/OYS.', 'M', '15270937380', '15270937380@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '冠心病心脏搭桥术及心脏外科其他常见病诊疗', 20, 'active');

-- 二级科室：神经外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '神经外科', '圣心楼', 1, '神经外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
-- 医生：赵卫国
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵卫国', '$2b$12$2VSCVxAl81ep1HhxQKf/.OKCkZV3hfFIGbS8f2YV3PVnNWnyY5wd.', 'F', '19141580226', '19141580226@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '神经外科疑难疾病的诊断和微创手术治疗。特别是垂体瘤和颅内各类肿瘤；面肌痉挛，三叉神经痛，舌咽神经痛；颈动脉狭窄和脑血管病的微创外科治疗。', 10, 'active');

-- 医生：王健
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王健', '$2b$12$ywmnq81.hKpXPpNMUF9mbe2gLnWN8j1Xm8v8qAYdRQyU55jcJ6MKm', 'F', '14314289692', '14314289692@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '颅脑外伤的诊治，预后康复，各类颅内肿瘤的手术和放化疗方案。脊髓椎管内肿瘤的诊治', 20, 'active');

-- 医生：濮春华
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('濮春华', '$2b$12$g8gev8xTVX/DkzRUerBXFOWY9QLnRPdyxvYx3ca/ujbll50r5AY06', 'F', '15427696198', '15427696198@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '颅神经疾病治疗（面肌痉挛、三叉神经痛、舌咽神经痛的微创微血管减压术）、颅内及椎管内肿瘤手术', 20, 'active');

-- 医生：李云峰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李云峰', '$2b$12$R4B4Fw3waWxJy0uyjMU28OTMbN6pJayaBYQS/IeUER7kn6aPXc1GK', 'M', '14125409494', '14125409494@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '微创手术、内窥镜、颅脑肿瘤综合治疗', 20, 'active');

-- 医生：林东
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('林东', '$2b$12$jj4s7ZID0oAB4aMhvb/4l.m3XGzGWiAlCCKqZygrEoodKUt6b94JC', 'M', '16452468587', '16452468587@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑、脊髓血管病的血管内治疗、颅内肿瘤', 20, 'active');

-- 医生：胡锦清
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡锦清', '$2b$12$1A9N7tDobpiwa.BmPn4nluYdPJl5BN7PI8iOhYqCm/0Mt.wVm1NSS', 'F', '19174539964', '19174539964@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑血管病（颅内外动脉狭窄、脑动脉瘤、脑血管畸形等）介入和外科治疗，脑外伤、脑肿瘤等。', 20, 'active');

-- 医生：蔡瑜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蔡瑜', '$2b$12$fV3DYDX97q4eVmDFmiPuA.rvLPsNOSQ8VZhWM7kmLz/srktYhUWDy', 'F', '15788785773', '15788785773@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '脑血管病（颈动脉狭窄的手术治疗）、颅神经疾病（面肌痉挛，三叉神经痛）', 10, 'active');

-- 医生：孙青芳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙青芳', '$2b$12$eiAh5kLGBt3GFhdTmLMGmelsOzS2A10WSrmrSWfAeAqL2DJuSdjUW', 'F', '18675757257', '18675757257@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅内肿瘤、垂体瘤、脑积水、神经脊柱脊髓外科、脑血管病', 10, 'active');

-- 医生：卞留贯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('卞留贯', '$2b$12$EFgtKALuUJsvKYkWxlBq/O20Zxsno6TYA9WrdErrSQijmhEOXDiNC', 'F', '13223847257', '13223847257@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅内肿瘤及脑血管病', 10, 'active');

-- 医生：江泓
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('江泓', '$2b$12$66qgi0Maui85Bb4kWIIlnuDrOGsBlripk9DXTimltGtt5OQy.j4E.', 'F', '14723403479', '14723403479@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅内动脉瘤，脑动静脉畸形，颅内动脉狭窄，颈动脉狭窄，椎动脉狭窄，脑梗，脑出血，蛛网膜下腔出血等颅内出血性和缺血性疾病。搏动性耳鸣介入治疗。', 10, 'active');

-- 医生：朱军
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱军', '$2b$12$55Cf23lak51FHAoEPLnQnuZKqC.yBvplyM/1KmBzmvnjWAP0.Ddxe', 'F', '13216396351', '13216396351@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑血管病微创治疗及脑出血、脑梗死、颅脑肿瘤诊治', 20, 'active');

-- 医生：尚寒冰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('尚寒冰', '$2b$12$I1AGJiNP7Fe1Lwn8YCEN1OS/s0vWh.bAKRP7.xiRbPuFHL3zZxyc2', 'F', '15882269302', '15882269302@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅神经疾病（三叉神经痛、面肌痉挛）微创手术、脑血管病、脑肿瘤外科治疗、神经系统疑难罕见疾病新技术治疗咨询。', 10, 'active');

-- 医生：潘斯俭
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('潘斯俭', '$2b$12$t9h44ZrYd1NYJNwXWHecmudpEo95qu3UgbffL5sBFSyLKTjnA96Fq', 'F', '16750911797', '16750911797@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '三叉神经痛的MVD，PBC及伽玛刀；脑肿瘤，脑血管畸形及癫痫的伽玛刀', 10, 'active');

-- 医生：吴哲褒
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴哲褒', '$2b$12$WPfnDdsWreFB7CqUMSLfGOvK20eV63YRR.sO9je28vyyH4tM30HaS', 'M', '16719106699', '16719106699@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '垂体腺瘤的内镜经蝶微创手术；颅脑肿瘤（脑膜瘤、胶质瘤、听神经瘤、颅咽管瘤等）显微外科微创手术治疗；垂体腺瘤的药物和个体化治疗。', 10, 'active');

-- 医生：孙昱皓
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙昱皓', '$2b$12$GFxn2BgtgZULdcnwrNoIVObXXf7gbuc6doukz/xRGEkgOoIbeE/ZO', 'M', '15147659674', '15147659674@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑肿瘤内镜微创手术（垂体瘤、颅咽管瘤等）；颈腰椎微创手术（神经鞘瘤、椎管狭窄等）；其他各类神经疾病的内镜微创治疗（脑积水、腕管综合征等）', 20, 'active');

-- 医生：顾威庭
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('顾威庭', '$2b$12$iNZw1mP2C8EaHYfMfxWWquIqAvFWeYKY7VHwrQcDTQY5XOYbyvfeW', 'F', '13658260221', '13658260221@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '擅长脑血管疾病、颅内肿瘤和脑积水等疾病的外科治疗', 20, 'active');

-- 二级科室：烧伤整形科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '烧伤整形科', '圣心楼', 5, '烧伤整形科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
-- 医生：袁克俭
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('袁克俭', '$2b$12$IEL.muVb8OtyyITz787UweRCoZH9/aGBN9qys2Y33LnmZf1iePw3.', 'M', '15563102056', '15563102056@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '大面积烧伤,难治性创面处理及疤痕早期防治', 10, 'active');

-- 医生：郑捷新
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郑捷新', '$2b$12$i0fOB5KQrG3tK3xu/riCw.xioYn7Qxgya8c937ZjaR3hffUZxBcoO', 'M', '18454549587', '18454549587@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤后期疤痕预防和治疗，难愈性创面的治疗', 20, 'active');

-- 医生：张勤
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张勤', '$2b$12$GYhBxsfzWhaGtY7AuWuu.O4D/N.fmoXDR/MGtP9aGSCkqKmKcO3/y', 'F', '18233815413', '18233815413@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤疤痕防治，难治烧伤创面的处理和治疗', 10, 'active');

-- 医生：黄伯高
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄伯高', '$2b$12$cWei0KcL5MgMfe4/30omE.8yqFkb5SbiM5LtRUwySDaVm1rXB7Dqe', 'F', '17432091877', '17432091877@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤创面修复、抗感染、疤痕防治', 20, 'active');

-- 医生：王文奎
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王文奎', '$2b$12$ocHVF71EUPohPwSwWvW5Q.OaboNRWIZLADC41x04ykfPTyPzbbOCK', 'F', '15532074125', '15532074125@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤创面、疤痕预治及后期康复指导', 20, 'active');

-- 医生：张剑
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张剑', '$2b$12$1f4RoCFT4aw4cqcm0Xce4uFkAeAnTFSQ3sV4nnRmRLhf7EwKZ5vmi', 'F', '17236674237', '17236674237@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各种灼伤及创伤后疤痕增生的整形康复，以及各种难治性创面的诊治', 20, 'active');

-- 医生：刘琰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘琰', '$2b$12$TnezgnOEuZiuC/ZIcRfuvuJT/rruEdUqyy0m.2lJCdjvpQkjOXbLu', 'M', '16813962536', '16813962536@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '创面处理、慢性难愈创面（糖尿病足）治疗', 10, 'active');

-- 医生：杨惠忠
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨惠忠', '$2b$12$mpqW0hTJERLezQDHpz4CJ.teKtEIL35WPa8vv8DiFQLzXIMiK2anW', 'F', '18903303363', '18903303363@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤创面修复,疤痕修复', 20, 'active');

-- 医生：向军
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('向军', '$2b$12$pnf0McZx8/9jqTqgS8S9LOlrHAMR5CHGrJvo66eRwDmo46bouZlAm', 'M', '17711072118', '17711072118@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各类创伤创面的修复、疤痕防治', 20, 'active');

-- 医生：牛轶雯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('牛轶雯', '$2b$12$0VlskzZu5vaBdV0kKNBXVuOi7ZqRF1bz3MyiCOMyWp1AgQaB3b8L6', 'F', '16688343096', '16688343096@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各种类型创面修复及预防、瘢痕防治', 20, 'active');

-- 医生：乔亮
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('乔亮', '$2b$12$FYENbvKc3EgXO9sZWQxRH.pLg9RBaBv//rj/VVQOMZfnJ/YfDCrKi', 'M', '15408071259', '15408071259@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '大面积烧伤、儿童烧伤、疤痕治疗、慢性创面。', 10, 'active');

-- 医生：王志勇
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王志勇', '$2b$12$co6YQ/TUVPqV7tQNSN1k6eKio9LfFLZqD14AcMkrMi3nIRqSPzRxu', 'M', '16943702118', '16943702118@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤创面修复、慢性及难愈创面处理', 10, 'active');

-- 医生：窦懿
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('窦懿', '$2b$12$UjsR5hc7ug4T627Y8qHJKOpYnI4.DReVlAEac83aBBsUwQXlLy/S6', 'F', '16574364121', '16574364121@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '疑难复杂伤口修复、疤痕治疗、皮肤肿物治疗', 20, 'active');

-- 医生：黄晓琴
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄晓琴', '$2b$12$mFMzUPJt3uir/SH5K9QtlOvMdm52YIDpAwWqJh/zIIQGOvxdAEGC6', 'F', '18329460134', '18329460134@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤创面修复、烧伤瘢痕整形、瘢痕激光治疗', 10, 'active');

-- 医生：王西樵
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王西樵', '$2b$12$tgCiHgKtA7WUlLROSAxc2uM9GvjPtsGue1nFQ9pZN3ZOKKHAMHERu', 'F', '19954725078', '19954725078@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '擅长深度烧伤、创伤等难愈创面的非手术愈合，疤痕的修复治疗和激光治疗', 20, 'active');

-- 医生：施燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('施燕', '$2b$12$4rJ1IdHdq2cQzOTSfMP7Yeeg1tcwzdem4a89.Ud75QRxSHTZikDjy', 'M', '18191048514', '18191048514@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '瘢痕光电及综合治疗、小儿烧伤、创面修复。', 20, 'active');

-- 医生：唐佳俊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('唐佳俊', '$2b$12$GHHN1daFESIfyshDxf6vdu7jUqOQ38XyvPR30Nue/JtY19wNYl/Fy', 'F', '17812807629', '17812807629@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各类急慢性创面修复、增生性疤痕治疗', 20, 'active');

-- 医生：郇京宁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郇京宁', '$2b$12$JUuCXyQjdhiPVlExK3L0a.yBzz6e6v97pwJkA.UTjZYEAjkgKhqBS', 'F', '13978775497', '13978775497@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤创面、慢性、感染创面治疗，疤痕防治', 10, 'active');

-- 医生：李学川
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李学川', '$2b$12$cKXkIXxILQoCGRrhEUUrRep8LpgzC3FIQvd/N7oSYyRYtndZMthO2', 'M', '18433310361', '18433310361@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '瘢痕综合治疗；淋巴水肿手术治疗；精细缝合及复杂创面修复。', 20, 'active');

-- 医生：原博
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('原博', '$2b$12$dBf.IrlSM/otDrPFfEiJoeq4i8SrSqgaotxHMoGEGyM0qMk0E.N4e', 'M', '19313814138', '19313814138@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '小儿烧伤、瘢痕防治、创面的细胞治疗与再生。', 20, 'active');

-- 医生：周增丁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周增丁', '$2b$12$wT2Q7OEnElMNTlorrWsHDO91YiQ8PIaWmch2Pz7LCYFn2ThydDoWa', 'M', '13149621604', '13149621604@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '艾灸、暖宝宝等低温烫伤治疗；腋臭祛除、腹壁整形；手部指蹼瘢痕治疗；疤、痣、纹身及皮脂腺切除精细缝合。', 20, 'active');

-- 医生：易磊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('易磊', '$2b$12$BqSUmCAG0WkBD94i71gk.u7zivEEmnnNL.1VErMhH976WN8rxYGMC', 'M', '16756350429', '16756350429@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '瘢痕治疗，伤口修复，皮肤肿瘤与黑痣，整形，灼伤与外伤治疗。', 20, 'active');

-- 医生：冯少清
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯少清', '$2b$12$GRhzM0lqLyY/inBe6HOuYOoFmj8wsh/FJQVxk3.0EX2HRMmCOC/Te', 'M', '16545002643', '16545002643@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '特长：烧创伤瘢痕综合治疗、乳房腹壁整形、眼整形', 20, 'active');

-- 医生：穆雄铮
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('穆雄铮', '$2b$12$3AO./c0N/27uB/abr6IM4eeze0h.oZvKVkcw6XPPibzTLzm81T/Jm', 'M', '18847704681', '18847704681@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '各类面部轮廓整形及颅颌面畸形疾病。', 10, 'active');

-- 医生：林炜栋
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('林炜栋', '$2b$12$RXhUJZFDVk1nrJAyaIUKR.Nh5Z8VDzoVI1ywPLZ7GPYdjG1Dn.yTe', 'F', '16529123992', '16529123992@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '各类创面修复、疤痕防治及慢性难愈创面诊治', 10, 'active');

-- 二级科室：普胸外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '普胸外科', '圣心楼', 2, '普胸外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
-- 医生：陈中元
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈中元', '$2b$12$R04VwCcBC2U4Pgx895THruZLO6YYT14lOtaU8MdbgYXIzHqjJoP46', 'M', '18838328947', '18838328947@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '主任医师', '心脏，食管、贲门肿瘤、肺部，纵隔肿瘤', 10, 'active');

-- 医生：任健
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('任健', '$2b$12$RkQdS7Oj3vWv7Rsu/CpG4eacSgEcOKtnPwbbdfWaNvIlqsvozfPNq', 'M', '19926830795', '19926830795@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '胸腔镜微创手术，食管肿瘤、肺肿瘤 、纵膈肿瘤、手汗症', 20, 'active');

-- 医生：杭钧彪
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杭钧彪', '$2b$12$5O5i/3Oo8G/SoXIUNRHvuO57osnIBhXhSOJIefCZKVx4xfhQZAt2W', 'M', '19556497252', '19556497252@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '主任医师', '胸外科微创手术、食管贲门肿瘤手术、肺癌综合治疗、纵膈肿瘤及胸部疑难疾病诊治', 10, 'active');

-- 医生：车嘉铭  
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('车嘉铭  ', '$2b$12$ecYuH4Jl1TxOKrQLylUzb.cQFYfLzGxzvG4AcIEGZAdhr3CbRxO02', 'M', '14963406382', '14963406382@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺肿瘤、肺小结节、食管癌、贲门癌、纵膈肿瘤等的外科微创治疗', 20, 'active');

-- 医生：朱良纲
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱良纲', '$2b$12$vsPDXzVNkFU5nDlth.AOeebl5B4qQzPLJKxCy6KM1PPp/wVxYjZo.', 'F', '13698509918', '13698509918@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '食管、肺、纵隔疾病、冠心病、先心、瓣膜病', 20, 'active');

-- 医生：周翔
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周翔', '$2b$12$48Lgmn5B6HZJsJqfh1mwJu4rkpx8QILuAMQTtPUcbT5PYtf9RV6Dy', 'M', '19230307805', '19230307805@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '食管、肺部、纵膈肿瘤的手术及胸腔镜诊疗', 20, 'active');

-- 医生：杨孝清  
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨孝清  ', '$2b$12$DfnXMg4rzQEMRrZl0z56LeTb0GYRIaGpcL3Vm0NQOa6KculGN8KlC', 'F', '14960514523', '14960514523@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '食道肿瘤、肺肿瘤、纵膈肿瘤的外科手术及综合治疗。普胸外科疑难疾病诊疗', 20, 'active');

-- 医生：项捷
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('项捷', '$2b$12$FksMM.OSPGAy7dOnAJNWfeWSlvErSf4NPJX5makagchRFlY1l1xCK', 'F', '18670298515', '18670298515@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺、食管、纵隔肿瘤等常见胸部疾病以手术为主的综合治疗，熟练开展包括胸腔镜食管癌根治术，单孔胸腔镜肺癌根治术剑突下纵隔肿瘤切除术。巨大纵隔肿瘤切除术及达芬奇机器人在内的各类胸外科手术。', 20, 'active');

-- 医生：陈凯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈凯', '$2b$12$MRL3fB8KbjgOBwM2WjgebuPj7fyi4OEnfgKSEOhFGQSJdW6Ia5nua', 'F', '19575209597', '19575209597@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '微创治疗纵隔、肺、食管等胸部良恶性肿瘤。', 20, 'active');

-- 医生：杜海磊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杜海磊', '$2b$12$rlufepqFlNOG7KUoKEox3eXT0dgA8HOSiSFvUcf44NDHiEkG.IEty', 'F', '19688273030', '19688273030@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺结节、肺癌、食管癌、纵隔肿瘤、手汗症、气胸等微创治疗。', 20, 'active');

-- 医生：韩丁培
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('韩丁培', '$2b$12$RV.IDv9ezdxFfo7kUfpDU.eiJ57M6bzfFomkns8A0IBlOy6qXQgde', 'F', '14898493949', '14898493949@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺结节、磁导航定位、肺癌、食管贲门癌、胸腺、纵膈肿瘤微创外科治疗。', 20, 'active');

-- 医生：李鹤成
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李鹤成', '$2b$12$o.B9zLIHD.D8vkAZzE1mqOcDLS9OzZg.ySU51mGw.7OfnHVfrJr5a', 'F', '16378304807', '16378304807@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '主任医师', '肺癌、食管癌、纵隔病变、胸膜间皮瘤等以微创手术为主的综合治疗；肺移植外科治疗。', 10, 'active');

-- 医生：张亚杰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张亚杰', '$2b$12$XM1KyVm1HirKApdyxwJdWO5t4zk/..D2bcutN2Zm8W8Dg7m3K8G.m', 'M', '19784683755', '19784683755@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '胸部肿瘤机器人、胸腔镜等微创手术及综合治疗。', 20, 'active');

-- 医生：金润森
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('金润森', '$2b$12$vtwzwF347hjhzFbduukmJ.Tqmy1adHvcQLwvSgRVB4xFQ1I8XRTQm', 'F', '19935021998', '19935021998@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺小结节的鉴别诊断，肺癌、食管癌、纵隔疾病微创外科治疗。', 20, 'active');

-- 医生：陈学瑜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈学瑜', '$2b$12$W0G.ych4TeiqQCR6LnveJe3PQfxtAaTddhtOTOZxqxNUGRecCanh2', 'F', '18356888294', '18356888294@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺部良恶性病变、磨玻璃结节的微创手术治疗。', 20, 'active');

-- 一级科室：儿科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '儿科', '圣心楼', 3, '儿科专注于儿童健康成长，提供儿童常见病、多发病的诊疗服务，医护人员经验丰富，深受患儿家长信赖。');
-- 二级科室：小儿内科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '小儿内科', '圣心楼', 4, '小儿内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
-- 医生：李卫
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李卫', '$2b$12$lQYY17.jkFTx7LJVdPmgHu.5nKODJvvv5soWwfnIVqEIsJfm2x3Rm', 'M', '18406798268', '18406798268@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童血液及恶性肿瘤疾病的临床诊治、肝脾淋巴结肿大、儿童感染性疾病诊治', 10, 'active');

-- 医生：许春娣
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('许春娣', '$2b$12$NrYrH/9rYNIQ7fMB8n.VZezAVA31/.TUkDZD6e2GYcVJlIl/thmua', 'M', '15460613550', '15460613550@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童消化道疾病的诊治（便血、腹泻、腹痛、呕吐等疑难杂症）', 10, 'active');

-- 医生：董治亚
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('董治亚', '$2b$12$heCT4Wha/AZZhVurcJEOA.bzpaBV5sGePG9AmAVxk946/Smd16bXm', 'F', '17186519602', '17186519602@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童生长发育相关疾病诊疗，包括矮小症、早发育、性发育异常、肥胖症；儿童糖尿病、甲状腺疾病及遗传代谢病。', 10, 'active');

-- 医生：陆文丽
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陆文丽', '$2b$12$8HlkXJKk856yg4RP0Wv3uOjjZ/QtQ874xKmdf4aR9w2XEc.kblidi', 'M', '14348315126', '14348315126@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童生长发育评估（乳房早发育、性早熟、矮小、肥胖等）及甲状腺疾病、先天性肾上腺皮质增生症、McCune-Albrght综合征等。', 10, 'active');

-- 医生：肖园
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('肖园', '$2b$12$JjtWtiljoHEWUQCQ3UTZxO9QPmYs/sZ2Mv27KOfI8DsJcuzjIx4FS', 'F', '18264073400', '18264073400@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童内分泌疾病（矮小、性发育、糖尿病、甲状腺疾病等）、儿童胃肠病（慢性腹泻、炎症性肠病）和消化内镜诊治。', 10, 'active');

-- 医生：吴群
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴群', '$2b$12$SI1Y3./m/Mka.hgqlOhnnOlmQGj39AtpwizU2Srrist9fCSyWIEJ.', 'M', '13545461013', '13545461013@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童呼吸系统疾病，儿童哮喘，儿童过敏性鼻炎，婴儿湿疹，食物过敏。', 20, 'active');

-- 医生：余熠
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('余熠', '$2b$12$9q6DsHjqakKQXIAui1CKI.1cawUQaHlDclEE1ltH5l0Ymytt0QHXC', 'F', '15682624278', '15682624278@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿内科常见疾病诊治，擅长消化系统疾病。', 20, 'active');

-- 医生：王歆琼
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王歆琼', '$2b$12$wiDTQqLVJNT1s5pMfR0YTue9j.FHQzztS043HLfzsddfSuFIG9Vjm', 'F', '16166857303', '16166857303@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童胃肠疾病（炎症性肠病、急慢性腹泻、慢性胃炎、过敏性肠炎等）及其它儿内科常见病的诊治', 20, 'active');

-- 医生：马晓宇
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('马晓宇', '$2b$12$P0d9eznjtTrhl3.WStNCBOvmDsH4RpNKiW52eW6R4G5EX7GpvZ.72', 'M', '19551125671', '19551125671@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童内分泌遗传代谢，擅长矮小症、性早熟、糖尿病、肥胖、甲状腺疾病等儿童内分泌疾病的诊治。', 20, 'active');

-- 医生：曹玮
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曹玮', '$2b$12$8Mz7FaaGCFdNolkpm3c4W.SdS7YMWzJCQNQIa9xomGHPQWsF8amwm', 'F', '19727150493', '19727150493@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童生长发育评估（矮小、性早熟、肥胖）、甲状腺疾病及常见疾病。', 20, 'active');

-- 医生：倪继红
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('倪继红', '$2b$12$xOEejDRBJATCWY1g3EVIbO.Aj3gB1vnim2wyHNpJNqJ2TF/dSgnv.', 'M', '19922050912', '19922050912@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童矮小症、性早熟和性腺发育低下、甲状腺疾病、先天性肾上皮质增生', 10, 'active');

-- 医生：赵建琴
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵建琴', '$2b$12$c.4w/w2P0HmviXwUpIpHbeIPRAotFINZB0f90rzbUkg/PLAWocpsK', 'F', '16106330865', '16106330865@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '小儿常见多发病，擅长小儿呼吸道疾病、哮喘等疑难杂症', 10, 'active');

-- 医生：邱定众
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('邱定众', '$2b$12$6iRBPTFDhL0jLnGIzNKsLen73ghRzdhfXRQ2fe5/feZmtt3K.Duma', 'F', '15909037759', '15909037759@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '小儿感染性疾病、心血管与呼吸道疾病、自身免疫性疾病及疑难杂症', 20, 'active');

-- 医生：于意
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('于意', '$2b$12$v54hN5K9yempgW23OSXsAOLFmWX9DFik6z2DbyiUL8Ub6qdc5t3.G', 'F', '19997356828', '19997356828@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童内分泌疾病(矮小、肥胖、性早熟等)及儿内科常见病', 20, 'active');

-- 二级科室：小儿外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '小儿外科', '圣心楼', 4, '小儿外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
-- 医生：沈丽萍
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈丽萍', '$2b$12$N49vxUXf4YkhtBBDKyAlQOtea4F0sINq/qZq06thlWgbHV8QjB5eS', 'M', '16335602191', '16335602191@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '消化、新生儿、腹腔镜', 20, 'active');

-- 医生：陈建雯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈建雯', '$2b$12$YcVLUYO0FKpV/Z40B2uSVOVQ55mxUf646aQW.KXmlt9iWhglX.ezG', 'F', '16621454127', '16621454127@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '小儿的脐茸、包茎、各种肿瘤、儿童骨折及先天性骨性病变', 20, 'active');

-- 医生：周曙光
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周曙光', '$2b$12$bDAvecOb.vkxYP6z7lLkde5Rgwks11Zn/ZeL5c.7BEuub4WEHxtcK', 'M', '16460916347', '16460916347@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '小儿骨科、小儿泌尿外科、小儿脐茸诊治', 20, 'active');

-- 医生：刘德鸿
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘德鸿', '$2b$12$lwignjSaKn3YL.HaQw4Uy.yzbtkwnKqHCi2lIHWV74AbjpknW20Y.', 'F', '18277215428', '18277215428@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '小儿泌尿外科各类疾病的诊治', 20, 'active');

-- 一级科室：妇产科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '妇产科', '圣心楼', 4, '妇产科为广大女性提供全方位的医疗保健服务，包括孕期检查、分娩、妇科疾病诊疗等。');
-- 二级科室：妇科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '妇科', '圣心楼', 2, '妇科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
-- 医生：刘延
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘延', '$2b$12$5WXrFR4kFqHBbKqlC8oZBeDAp7n/KlKZMr9e1ZPQNwJYUPUDN7cK2', 'M', '16735540030', '16735540030@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '高危产科、月经异常等妇科疾病、围绝经期综合症、不孕症', 10, 'active');

-- 医生：黄健
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄健', '$2b$12$NWxQ/S9gXx/btjkkBoWelOrNMMdDAawlLEmSYppCw0PCTwvs/CdCu', 'M', '13790147183', '13790147183@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '慢性宫颈炎症、子宫肌瘤、月经失调、不孕症', 20, 'active');

-- 医生：滕宗荣
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('滕宗荣', '$2b$12$ts6qyK1hZIF6Pj8VsvA2BeHpJ0ZPNyxykHc1yZlQWgnkfQ.v0wFnq', 'F', '14595744935', '14595744935@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科内分泌疾病、妇科炎症', 20, 'active');

-- 医生：刘淳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘淳', '$2b$12$z1vO0gabbf8gu/aaRH90CewApxOvHkjGOEpMGr24TKvdSXH.Hka1.', 'M', '13379340713', '13379340713@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科炎症、妇科内分泌疾病 ', 20, 'active');

-- 医生：钟慧萍
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('钟慧萍', '$2b$12$PJYGksMSwkTfKBUWlkmd2uLr9JCFqvOOnuupL0B2/sbEA.oQw4sn2', 'F', '15327268503', '15327268503@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '妇科宫腔镜、腹腔镜手术，包括宫颈疾病、子宫肌瘤，卵巢囊肿的治疗。妊娠合并甲状腺疾病（甲亢、甲减、桥本甲状腺炎等），妊娠期糖尿病，妊娠合并甲旁亢等内分泌疾病的诊治，及产科危重疑难杂症的诊治。', 10, 'active');

-- 医生：薛梅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('薛梅', '$2b$12$8eUWpURS3V22qQlqOLCpx.OXxygkUIPCjh2PEswq9iVFz1BRaF7XC', 'F', '15462388455', '15462388455@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科肿瘤的综合治疗\月经失调、更年期综合征', 20, 'active');

-- 医生：沈立翡
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈立翡', '$2b$12$Its5KEwgFcExGTuYT7CxXufGFVPmjlbcHpq7cP0CnfRw9P8AJmupa', 'F', '15907464398', '15907464398@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '子宫肌瘤、子宫内膜异位症、卵巢良恶性肿瘤、子宫颈癌及子宫内膜癌的腹腔镜手术', 20, 'active');

-- 医生：龙雯晴
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('龙雯晴', '$2b$12$qfVvmlgA2oXLGLRZKiol6.NXXkWsfincp70B9SS/UW7ahIP51sqpO', 'F', '15996478772', '15996478772@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '女性不孕症、子宫内膜异位症、子宫肌瘤、卵巢囊肿等妇科良恶性肿瘤的宫腹腔镜治疗。', 10, 'active');

-- 医生：王敏敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王敏敏', '$2b$12$2oKxUOBZAlceKab9ngN4puVQYpxxEiVlXGJd/Ij2Vzk3159jham/S', 'M', '16120812641', '16120812641@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '绝经综合症、功能失调性子宫出血、妇科炎症', 20, 'active');

-- 医生：沈育红
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈育红', '$2b$12$.t/7PkA6M/W19MHH6usrO.aRhS74xdhSxoGrb1tVPG3yO2ZaZv8Lq', 'M', '15340756713', '15340756713@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科常见病如子宫内膜异位症、子宫肌瘤、卵巢肿瘤等宫腹腔镜手术治疗以及妇科恶性肿瘤的诊治。', 20, 'active');

-- 医生：朱岚
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱岚', '$2b$12$ufrH3uLBRwtZYE6YNmm7g.2EY19GZDzBPH9QSESlwg88/aJUXBzB2', 'M', '19799816232', '19799816232@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科疾病、妇科肿瘤、宫颈疾病的诊治，内窥镜治疗', 20, 'active');

-- 医生：陈晨
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈晨', '$2b$12$cFsMR510IZ9x5WQBWe8cQeYa3IAEnPpix.s8G0mfQGpMufGBObJlS', 'M', '19133318206', '19133318206@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '月经异常包括多囊卵巢综合征、不孕症、性腺及生长发育异常，卵巢早衰，围绝经期综合征。', 20, 'active');

-- 医生：蔡蕾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蔡蕾', '$2b$12$wmYFjtfDnclxesP4eCiCcO8XATftUzXKKLEP7tswXaIKN43CbKkia', 'M', '14121882877', '14121882877@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '宫颈疾病诊治，妇科肿瘤宫腹腔镜手术', 20, 'active');

-- 医生：沈健
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈健', '$2b$12$VVj6FXP2gq.tTKvFmSPZsOpjiXnNMjUDWFj66ifql00tdjJAbc/9m', 'M', '14235528741', '14235528741@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科内分泌、更年期综合症、不孕不育、生殖道感染', 20, 'active');

-- 医生：杨辰敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨辰敏', '$2b$12$gA.4q5YjKb/Q/uATurv.we8tlZikPWbCYH4jwhklzVjfLWwsVT.4K', 'F', '18222824732', '18222824732@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '阴道镜下宫颈疾病的诊治，宫腔镜下治疗内膜息肉、粘膜下肌瘤、子宫纵隔，腹腔镜下治疗不孕症、子宫肌瘤、内异症、卵巢肿瘤及子宫切除术等。', 20, 'active');

-- 医生：刘华
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘华', '$2b$12$.mmcqeu6BCYkDROE.RLxaOKjetv58MxQdaywd7OD0kZMktEtQFZn2', 'M', '16851044796', '16851044796@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '内膜异位症，妇科良恶性肿瘤，腹腔镜手术，宫腔镜手术', 10, 'active');

-- 医生：焦海宁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('焦海宁', '$2b$12$DvtqDHUWKtsykRV/r78fRuAd9i12DcF0deWk5tJd92fjKSnujLEiG', 'F', '19496098177', '19496098177@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '宫腔占位，子宫肌瘤，卵巢囊肿，子宫内膜异位症等良性肿瘤的诊治；产科危重疾病的综合治疗及妊娠合并糖尿病、高血压、甲状腺疾病和自身免疫性疾病的诊治。', 20, 'active');

-- 医生：陈慧
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈慧', '$2b$12$30qUalplS2PnRQndLsanMePtqdmgpxUstIooytjGgBi.BUfQgGD5a', 'M', '17752027025', '17752027025@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '各类妇产科疑难超声诊断：如妇科肿瘤（尤其是卵巢肿瘤）、子宫内膜异位症、女性生殖系统发育异常及高危妊娠产前超声诊断等。', 10, 'active');

-- 医生：许啸声
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('许啸声', '$2b$12$kK7Y9yhCnqURZ0x4NdnEnueZL1vMdggAeHvrL0vDEh2fy7G1cHN3S', 'M', '19979717936', '19979717936@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '子宫肌瘤、卵巢囊肿等妇科常见良性肿瘤的微创手术治疗，妇科恶性肿瘤的综合治疗，HPV感染防治及宫颈癌前病变的物理治疗。', 20, 'active');

-- 医生：冯炜炜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯炜炜', '$2b$12$dE/RPVF6WNWRO2mgERFzCOQSdBF51.dImJ2I91q40grZ1ZNfDDCQO', 'M', '15216070149', '15216070149@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '精通妇科良恶性肿瘤，子宫内膜异位症的腹腔镜微创手术，机器人手术，开腹肿瘤减灭手术等；以及恶性肿瘤的综合治疗。', 10, 'active');

-- 医生：程忠平
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('程忠平', '$2b$12$S4/V9Bu.m9kwiKsrbBHUwuRF33IDs8SXWXDZEDLI7ZWy3iXIu5cla', 'M', '15718257024', '15718257024@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '腺肌症/肌瘤(微创保宫)、妇科肿瘤微创与免疫治疗。', 10, 'active');

-- 医生：李菁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李菁', '$2b$12$vMpavgz4EJ/0hz910qJmIedsijZP/2msOpRC/4g4P/Y14lNOEVpMC', 'F', '16867741192', '16867741192@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科宫腹腔镜手术，包括内膜和宫颈疾病的诊治。', 20, 'active');

-- 一级科室：医学检验科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '医学检验科', '圣心楼', 2, '医学检验科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
-- 二级科室：核医学科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '核医学科', '圣心楼', 1, '核医学科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
-- 医生：李彪
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李彪', '$2b$12$NmLKj4s7d5S./zj5B1J7NeJiaxpjeOXxoj/wdDtJplmuPo8nuq932', 'M', '13848605284', '13848605284@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', 'PET/CT肿瘤临床诊断，甲亢、甲癌核素治疗。', 10, 'active');

-- 医生：李培勇
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李培勇', '$2b$12$mi4rMQuiyIKsSj62DfU.vuKpTYdscscMsF2UYELZ.NHhSVQkPhayO', 'F', '19834717549', '19834717549@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '甲亢和甲癌的碘-131治疗，桥本甲状腺炎的内科治疗', 10, 'active');

-- 医生：管樑
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('管樑', '$2b$12$gZOqIeYCqbfftEYc3gG5qub26rFXh0XARCJ.p5khduFiu5tddgtjq', 'M', '19707665448', '19707665448@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '碘131治疗甲亢，诊断和治疗甲减（妊娠期）、桥本、甲状腺结节等甲状腺良性疾病。', 10, 'active');

-- 医生：陈刚
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈刚', '$2b$12$3V8tLMYYOjq758PWkD10LOG41IX2dMb37eqbfjYvTXsLRuwoHAZUK', 'M', '15672030854', '15672030854@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '骨转移骨痛，甲亢、甲状腺癌131I治疗，甲减，甲状腺炎等甲状腺相关疾病。肿瘤心理支持治疗。', 20, 'active');

-- 医生：江旭峰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('江旭峰', '$2b$12$ZCv2RDDxDgKc124o8WQIduIJ4dsMCaDMwviVGbKgGqMqUNA5DJhJS', 'F', '18497897455', '18497897455@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '擅长甲状腺疾病（甲亢、甲状腺癌等）、肿瘤骨转移核素治疗； 骨质疏松诊治', 20, 'active');

-- 医生：张一帆
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张一帆', '$2b$12$oaO6JodM1MY7jno1oAc/iulg/qu5ZiSsprJ5VH1PajRECDpY61FjS', 'M', '17795294275', '17795294275@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '甲状腺癌碘-131治疗、甲亢碘-131治疗等', 10, 'active');

-- 医生：张敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张敏', '$2b$12$4xrlcazNj3.XtSu4P9VhmuO2cTjnSFbDgxNUDdQ4R2UAHHo3fxuG6', 'F', '13551034639', '13551034639@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '甲状腺功能亢进和甲状腺癌碘-131治疗，以及PET/MR，PET/CT，SPECT/CT多模态影像评估肿瘤、神经及心血管疾病。', 10, 'active');

-- 医生：胡佳佳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡佳佳', '$2b$12$9trFlKH4mwlNl6YIstOZ.uw5IozEdigkrRmG5R9moHyZLHgyYumXC', 'F', '13565492204', '13565492204@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '肿瘤分子影像诊断及甲亢、甲癌、骨痛核素治疗。', 20, 'active');

-- 医生：张淼
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张淼', '$2b$12$gvDdHKo3eoaofhqpzyGhrep4UkNXJC2MvTGiFFPWwXr2lhTMfnT4q', 'F', '18989817696', '18989817696@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '甲亢、甲状腺癌碘131治疗，肿瘤及记忆力减退PET/CT、PET/MR多模态影像评估', 20, 'active');

-- 医生：郭睿
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郭睿', '$2b$12$xHijRp3Pp11gjsqmmolIFeHwI2BXNRf0uZK2kidR9vLZWPEaa8Voi', 'F', '18264272995', '18264272995@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '甲状腺疾病(甲亢、甲癌等）核素治疗，淋巴瘤PET评估。', 20, 'active');

-- 医生：席云
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('席云', '$2b$12$YTbIHCN3.cIpDiV5qYHjsuNfJWi.v2u5yo10udpCID.kuGn9W2GjW', 'F', '14887906704', '14887906704@sx.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '甲亢碘-131治疗、甲癌碘-131治疗、肿瘤多模态分子影像评估。', 20, 'active');
