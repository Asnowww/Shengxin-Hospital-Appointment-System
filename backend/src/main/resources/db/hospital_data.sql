-- 添加appointment_quota字段（如果不存在）
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS appointment_quota INT DEFAULT 30 COMMENT '预约数量配额' AFTER bio;
-- 将departments.room字段设为可空
ALTER TABLE departments MODIFY COLUMN room VARCHAR(20) NULL COMMENT '房间号/诊室号，如201';

-- 一级科室：内科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '内科', '圣心楼', 1, '内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '145', '圣心楼', 1, '内科主诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 二级科室：老年病科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '老年病科', '圣心楼', 2, '老年病科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '227', '圣心楼', 2, '老年病科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：徐志红
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐志红', '$2b$12$WQqTzhxm2JcjWS.ijTHD5eho1OVgyXiskLN89retfTIztF2dXkiZO', 'M', '18895285932', '徐志红_9935@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年呼吸系统及心血管常见病，包括肺部感染，慢阻肺，慢支，肺结节，睡眠呼吸暂停，高血压，冠心病及体检报告的分析解读等。', 40, 'active');

-- 医生：杜萱
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杜萱', '$2b$12$/lJYDSBvOZnVraHbjOLmbuXpiuKgDLRwHAz8DI15S.bmQmp3XbdqW', 'M', '17553035110', '杜萱_1520@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '老年高血压，冠心病，心律失常，心力衰竭等心血管疾病的诊治。', 50, 'active');

-- 医生：梁伟
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('梁伟', '$2b$12$ztH73/XrN9925HLLb1xgr.yvPEi41P/f3/BYFLNGUJRe0EkF6IRyO', 'M', '13334760738', '梁伟_4811@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '血脂紊乱，高血压病，冠状动脉粥样硬化性心脏病。

', 50, 'active');

-- 医生：庞小芬
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('庞小芬', '$2b$12$YT111CauOJMdRhS2DCAcmu4XZJcutQjeTNX8zDZlz42T52Mtri9ju', 'M', '17313500298', '庞小芬_9928@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '老年代谢性疾病：原发及继发性骨质疏松症的诊断、治疗，老年人的骨关节炎，老年高血压、高脂血症，老年糖尿病等老年性疾病。', 50, 'active');

-- 医生：曹久妹
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曹久妹', '$2b$12$4/zkJRzRywL5AU.qemxzyudoBzv7v5hwDAMrYCQbjtdzS6zcBX3Ku', 'F', '14582334538', '曹久妹_5557@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '主任医师', '高血压病、冠心病、血脂异常、心律失常等心血管疾病的诊断与治疗，慢性病的健康管理。', 50, 'active');

-- 医生：缪婕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('缪婕', '$2b$12$VoxIaWip0aBBBsNAvgg1reZ.YF.YgS8MFlIeM21ZMrB38ceTz0qJu', 'M', '19965241839', '缪婕_3615@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年内分泌疾病（甲状腺相关疾病、糖尿病等）', 40, 'active');

-- 医生：赵雅洁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵雅洁', '$2b$12$GMQ6WfTP0NcU4bx8B5YOPOmzkm8j5CH7zAISx.TcQR7jkoT9JS18.', 'F', '15398362082', '赵雅洁_3547@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年肌少症，老年糖尿病，老年骨质疏松，老年甲状腺疾病与老年相关性疾病的诊治。', 40, 'active');

-- 医生：苏征佳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('苏征佳', '$2b$12$gDhfrU8OBmswb36utugsiu7Sy7Rl/eUymb44SHwhGbOsEG70JRyPS', 'M', '19461415646', '苏征佳_2674@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='老年病科' LIMIT 1), '副主任医师', '老年血脂异常、冠心病、高血压的个体化治疗。', 40, 'active');

-- 二级科室：风湿免疫科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '风湿免疫科', '圣心楼', 1, '风湿免疫科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '422', '圣心楼', 1, '风湿免疫科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：杨程德
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨程德', '$2b$12$F6uV6f3zl8Jqen3d8PR5zOvIAzXypPuEM9XMH57aEwKIG4diSGEYC', 'F', '19469319644', '杨程德_5333@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '主任医师', '系统性红斑狼疮、抗磷脂综合征、强直性脊柱炎、类风湿关节炎', 50, 'active');

-- 医生：滕佳临
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('滕佳临', '$2b$12$vazYrwSGRU26hZAzmNpzZuE6Knp58t2SWKEAT0zjcHPqNyJO/o6ri', 'M', '18593303705', '滕佳临_9785@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '主任医师', '系统性红斑狼疮等多种风湿免疫性疾病的诊治', 50, 'active');

-- 医生：刘宏磊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘宏磊', '$2b$12$Ks9FjYgS9gKFNOsq3RkU5endZcOvEc0aaP6PMTS61qaaC4yf6AGvy', 'M', '16184611066', '刘宏磊_5803@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '系统性红斑狼疮、皮肌炎、类风湿性关节炎、强直性脊柱炎等', 40, 'active');

-- 医生：程笑冰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('程笑冰', '$2b$12$yZ33YDlkdoSI5UuQEDOR0uZb8nRKDEYrWyANA5FCYOFMP9/Obe9ge', 'F', '17306468299', '程笑冰_2139@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '关节痛及系统性红斑狼疮、皮肌炎等常见风湿病。', 40, 'active');

-- 医生：叶俊娜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶俊娜', '$2b$12$lYM88CX7mdcymo3XaXfJWe92E80iGnu3kHJdj51haENCKPlq4m8tO', 'M', '18344703907', '叶俊娜_5741@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '系统性红斑狼疮，干燥综合症，类风湿关节炎，强直性脊柱炎，皮肌炎，硬皮病等', 40, 'active');

-- 医生：苏禹同
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('苏禹同', '$2b$12$zm5ZAihdHdtdEGPgtCxBiOpNSXWve5eppU7NtoUD18CkrzYosZpdS', 'M', '19349957310', '苏禹同_2654@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '皮肌炎、系统性红斑狼疮、类风湿性关节炎、强直性脊柱炎、成人斯蒂尔病等多种风湿性疾病的诊治', 40, 'active');

-- 医生：石慧
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('石慧', '$2b$12$M7kHeVra6jly9ShtSTxBT./c0pcSOg3sKLZxUZQua4dlkqicPas2y', 'F', '15586845604', '石慧_6977@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='风湿免疫科' LIMIT 1), '副主任医师', '系统性红斑狼疮、抗磷脂综合征、类风湿关节炎', 40, 'active');

-- 二级科室：高血压科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '高血压科', '圣心楼', 2, '高血压科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '355', '圣心楼', 2, '高血压科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：高平进
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('高平进', '$2b$12$I.OOnvknDwYMUpv6BaoJOOVyBa2Q5bxdj92q2jjv4W9yoQxCPn7LK', 'M', '18386665249', '高平进_2169@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '并发症的诊断与治疗，单基因遗传性高血压（二级教授）', 50, 'active');

-- 医生：陈绍行
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈绍行', '$2b$12$2u5kFFz9w5mohrwdl8K65.Q/ijqhqUFxwMhoZLo1Q71b3v3rr0Ohu', 'M', '17882893941', '陈绍行_5010@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '降压药物合理应用、高血压及其相关疾病鉴别诊断和治疗。', 50, 'active');

-- 医生：孔燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孔燕', '$2b$12$4UYqcF9Hkok48xbf0xTCSuPb4PKKFLGz1GD4rGaUYo2b5jlyKdP76', 'M', '16507437181', '孔燕_5422@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '老年性及顽固性高血压、高血压合并糖尿病', 40, 'active');

-- 医生：龚艳春
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('龚艳春', '$2b$12$LZita8.0spOFDKROb1ctx.HLI7Tgmd899ogGLXrD2fbBGDd6UsxiW', 'M', '18448195935', '龚艳春_1916@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '难治性高血压及内分泌性高血压的诊断和治疗', 50, 'active');

-- 医生：胡亚蓉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡亚蓉', '$2b$12$x2khcC0chcUbL0mnc0HUJ.GycqnPt10Tx2Rh4.P2NtfKIKYYQ.MGS', 'M', '19134467368', '胡亚蓉_6168@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压合并肾脏损害的诊断和治疗', 40, 'active');

-- 医生：朱理敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱理敏', '$2b$12$WW74sjAVtHagz8NzrsKbDuvjl4MtNZmSvGVaKzG1KA1ytZoNLJEta', 'F', '15171069472', '朱理敏_4456@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '继发性高血压和难治性高血压的诊断与治疗', 50, 'active');

-- 医生：蔡凯愉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蔡凯愉', '$2b$12$1jjxeuxnvf6/jS75pmU39.gSfnHDqeksYLPXZFM6jhRh3OTJGAeEW', 'F', '14803771909', '蔡凯愉_9179@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压合并心脏病及糖尿病的诊治', 40, 'active');

-- 医生：唐晓峰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('唐晓峰', '$2b$12$NjdBxPTtMdH9ZxoC43haHu8XxukXwcHT3b6LMmfVYGtG7VuNhjzUC', 'F', '18592688426', '唐晓峰_3340@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '难治性高血压的诊断筛查和治疗', 40, 'active');

-- 医生：陶波
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陶波', '$2b$12$tkMUjx11AtuboGvV8URs9ul0h9m9QAZEufv3fHKrE1Mnu/QPeFDgW', 'F', '14364814270', '陶波_9830@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '肾动脉狭窄等继发性高血压的筛查、难治性高血压的治疗、血脂调控。', 40, 'active');

-- 医生：李燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李燕', '$2b$12$MdsTwnLAGQtWld9O6wNlmusPctCw.wXz2IudF6H1jI6cNAI/E5.7S', 'F', '18727694430', '李燕_8019@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主治医师', '高血压诊断和优化治疗', 30, 'active');

-- 医生：张瑾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张瑾', '$2b$12$LB.t20TtsZWLaU4ODsLvteMOy.kQJO41EcqA5BNBmGQ0B5G8ca8qS', 'F', '15335493870', '张瑾_3266@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '继发性及原发性高血压的诊治及合理用药。', 40, 'active');

-- 医生：王继光
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王继光', '$2b$12$fUG2p/ex2E7Ghur4K761j.26fuIPG8589XJ8i/6bseekFGo3Lu3B6', 'F', '13911514914', '王继光_1771@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主治医师', '难治性高血压、合并靶器官损害高血压', 30, 'active');

-- 医生：杨龑
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨龑', '$2b$12$NqbM3SmDKYE1IvI9YXP38OCnWf6.4I7CRVJvIV.NFekVqClgEyWEC', 'M', '14773715057', '杨龑_3621@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '特长：高血压心脏病的诊治、优化降压治疗', 40, 'active');

-- 医生：盛长生
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('盛长生', '$2b$12$ehk1GI.YhUsXxB4p9P2Tqeupr45BqYxMKHXi8XFU4LmAN3DCGrtmW', 'F', '17168212356', '盛长生_7304@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压及相关疾病的综合评估及病因诊治', 40, 'active');

-- 医生：黄绮芳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄绮芳', '$2b$12$2StXSLbwe/5GCxCIJ4nNcOZaf7jMux.eErxk/KNY2.IZx6bKW9sC.', 'F', '17602564736', '黄绮芳_9669@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '高血压及合并靶器官损害的诊治、降压药物合理使用', 40, 'active');

-- 医生：葛茜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('葛茜', '$2b$12$9YFpy75PypVgfAcMbkdXHuYFqBCVAKLrRanN.H2a4fYLgDtFkWcfa', 'F', '17112327652', '葛茜_2876@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '副主任医师', '难治性高血压及继发性高血压诊治，睡眠呼吸暂停相关高血压诊治。', 40, 'active');

-- 医生：王彦
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王彦', '$2b$12$NGACTKRSom9959c2wz.X5uzP82/ylK/EqCKBS7qzYta2w1rPLp2ca', 'F', '19788227492', '王彦_6573@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主治医师', '高血压病的诊断和优化治疗，遗传性高血压诊治', 30, 'active');

-- 医生：许建忠
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('许建忠', '$2b$12$rdSacBgM7S85PxPuw8QV4eZVtaremHSB/MTUnxbHk8DkM/Xilo0VS', 'M', '15566825638', '许建忠_3591@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', ' 难治性高血压肾神经消融治疗，肾血管性高血压介入治疗', 50, 'active');

-- 医生：徐婷嬿
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐婷嬿', '$2b$12$fIXzfbbx7FRRHE9dAxV58OQr/W5qqwnWBd/Uptd246TY22zTKn7ZW', 'F', '13875340444', '徐婷嬿_5315@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='高血压科' LIMIT 1), '主任医师', '高血压靶器官损害诊治及降压药物合理应用', 50, 'active');

-- 二级科室：呼吸内科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '呼吸内科', '圣心楼', 5, '呼吸内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '274', '圣心楼', 5, '呼吸内科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：李敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李敏', '$2b$12$ey8Pyh4RzHCSTXOkblZgxuEtu0jhpQc3ow65rikFQm1FKW5Jq3p.W', 'M', '19771410971', '李敏_5889@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '哮喘,慢性阻塞性肺病,睡眠呼吸障碍,肺部感染', 50, 'active');

-- 医生：高蓓莉  
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('高蓓莉  ', '$2b$12$sMKk54bj59/bCNVIZ8M/COevIdElzJdBxkZlv91q2z/eZRSnRhFge', 'M', '14501486939', '高蓓莉  _3646@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺癌、肺部感染和慢性阻塞性肺病等', 50, 'active');

-- 医生：时国朝  
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('时国朝  ', '$2b$12$kW5PniqjT0XgPYShehu6MOAxAB0NjdWJRRA/BL8aj3V.S2rl0LuBy', 'M', '17448059918', '时国朝  _9005@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '呼吸系统疾病的诊治', 50, 'active');

-- 医生：程齐俭
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('程齐俭', '$2b$12$YWrEYomFns7ApDcCbSGeq.iJd3NpxngpAEIICwmd41GVrCXdkVMH.', 'M', '13489747629', '程齐俭_6038@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部影像学诊断，急慢性下呼吸道阻塞性疾病、肺部感染性疾病、肺癌的诊断治疗。', 50, 'active');

-- 医生：朱雪梅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱雪梅', '$2b$12$AUUYHbY9O697iCvlGH7v6OdRuoS6g4iPzn.qvBtf4sGDiCUQPsjde', 'M', '13358633898', '朱雪梅_2290@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '肺弥漫性间质疾病、肺癌、肺内节结及呼吸系统其他疾病的诊断、鉴别及治疗。', 40, 'active');

-- 医生：汤葳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汤葳', '$2b$12$DFIg3mz7SckEgxCTPnlM3eFRU0rJukdNef83xNxj1A7yYbJOuNbZW', 'M', '18621828280', '汤葳_2133@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '哮喘，慢阻肺，呼吸道过敏，肺部肿瘤等相关呼吸系统疾病的诊治', 50, 'active');

-- 医生：项轶
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('项轶', '$2b$12$27TMrU3ln/zJca8LL83EfOg4x3hwL0dEaqeccf/I90P7/Hcm5huja', 'M', '14808402051', '项轶_8787@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '各种肺部恶性肿瘤的诊治（化疗、靶向治疗）、戒烟', 50, 'active');

-- 医生：李庆云
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李庆云', '$2b$12$0wuCNWChf69eV2mfBAjqROJiaSovhig6xR1wBIiiF2mY3I1zD3zma', 'M', '15666585408', '李庆云_7932@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '睡眠呼吸疾病，慢性咳嗽，哮喘，慢性阻塞性肺疾病，肺癌，无创通气，肺部感染', 50, 'active');

-- 医生：戴然然
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('戴然然', '$2b$12$oy7ya9AjnO/.NZLMyifSW.QfknRZrJQalNK49kf5KxuRzSg96iJqi', 'M', '17910959828', '戴然然_4295@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '慢性气道疾病及肺癌的诊治，戒烟', 40, 'active');

-- 医生：倪磊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('倪磊', '$2b$12$DMR7V11891G1jNQMJoCHwuqPfo16TvuIR3G7W97XfUzWBF7qHAMLe', 'F', '16821218382', '倪磊_7118@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部结节、肺部恶性肿瘤、间质性肺病、慢性气道疾病等呼吸系统疾病的诊治。', 50, 'active');

-- 医生：陈巍
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈巍', '$2b$12$XfO96/XKgWS.ufilT2ulle6YOcCM/.yyu4pvcd8I0kwpBVuKZR28O', 'F', '17584779555', '陈巍_2982@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺小结节、肺癌、哮喘、气管镜介入治疗', 50, 'active');

-- 医生：过依
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('过依', '$2b$12$yAl5AOu.b8PoFqRdvf/n1uohj6jjADO9aQ6oOwu4iKWW7BGT1pGeu', 'M', '14168747287', '过依_6539@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部感染，慢阻肺，哮喘，肺癌等呼吸系统疾病的诊治', 50, 'active');

-- 医生：周敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周敏', '$2b$12$Sn5KvXTwMU.yZp2mp/KMG.sVZEI4r70FlYLvVkaWJIrsZASwYCRCW', 'M', '17694768704', '周敏_4770@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '主任医师，正教授，博士研究生导师，法国居里研究所博士后；瑞金医院呼吸与危重症医学科副主任，上海市科委优秀学科带头人，中国医师协会呼吸病分会优秀中青年医师，上海市卫生系统三八红旗手和巾帼建功标兵；任中华医学会呼吸病分会呼吸治疗组副组长、中国医师协会呼吸分会慢阻肺委员会委员、上海市女医师协会肺癌专委会副主任委员等。主持科技部国家重点研发计划慢病专项和国家自然基金等多项科研项目，发表论文100余篇，其中SCI论文近50篇，参编或副主编专著多部，擅长重症哮喘、慢阻肺、肺部感染及肺癌等呼吸疾病的诊治。



专家门诊时间：周二下午，周四上午

特需专家门诊：周五上午

瑞金医院舟山分院：名医工作室（每月一次）', 50, 'active');

-- 医生：丁永杰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('丁永杰', '$2b$12$hcVFXixop8RsSj8BabwWn.iLrXqZXSNX..iyRn7anzS7swWApU1Le', 'M', '13176228245', '丁永杰_1964@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '肺栓塞、肺动脉高压、肺源性心脏病、肺部感染。', 40, 'active');

-- 医生：李宁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李宁', '$2b$12$wxUAqeF6sAUH8A8rFyqmTOf//xA6/pVKFduAc2bltUYAWzRtOBFiO', 'M', '13133729406', '李宁_6413@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '无创通气治疗、睡眠呼吸障碍、肺部结节、咳嗽、肺部感染', 40, 'active');

-- 医生：包志瑶
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('包志瑶', '$2b$12$uNDyzOIxgiQjRQY8fk5tae5s8aSkjQw9SDPTEyIdQvoZnq9XqxnYW', 'M', '17355555531', '包志瑶_5562@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '肺癌全程管理，肺部感染性疾病，慢性气道疾病规范化管理。', 40, 'active');

-- 医生：王晓斐
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王晓斐', '$2b$12$su2Hh0lmFfdiIvI5jFrS5.bPyi02z2PviS5Gyif0BuJemt1uS1F8W', 'F', '14678998028', '王晓斐_3167@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部恶性肿瘤的个体化诊疗', 50, 'active');

-- 医生：周剑平
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周剑平', '$2b$12$x2g/MBPFNR843cGRc.ifsuZC2R/GisOy7ZpX.0ffdPPy5BxFE58Bu', 'F', '14942478695', '周剑平_8749@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '哮喘、肺炎、咳嗽、肺结节读片、气管镜诊治、戒烟干预。', 40, 'active');

-- 医生：冯耘
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯耘', '$2b$12$0luoGP6UHd8y0KmU25U0.enhwj.N.6ojhYWI3PdqHk.QH5/yvaqhm', 'F', '14201281557', '冯耘_2588@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '肺部结节、咳嗽、支气管扩张、肺部感染、肺癌', 50, 'active');

-- 医生：孙娴雯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙娴雯', '$2b$12$3Uz9CBSaFs2VkSTGolq/D.FeU6KbMJcpwZoxVfeAef7i39bWBwH1e', 'F', '15554814084', '孙娴雯_7735@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '副主任医师', '慢性气道疾病及肺血管疾病的规范化诊治。', 40, 'active');

-- 医生：瞿介明
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('瞿介明', '$2b$12$vZxkPi.7Q3lsC6F3h7Z.gO3eSJItNH0S85k.750ioH47An8HKV6..', 'F', '19882839238', '瞿介明_1887@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='呼吸内科' LIMIT 1), '主任医师', '慢性咳嗽、哮喘、COPD和肺癌等呼吸科常见病及呼吸疑难疾病的诊治。', 50, 'active');

-- 二级科室：内分泌科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '内分泌科', '圣心楼', 1, '内分泌科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '161', '圣心楼', 1, '内分泌科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：陈瑛
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈瑛', '$2b$12$Ps5ep/.CAGn/KP.wtJfeZu6fKQXtRNCoECLEcPoXDlx35ivRD.Rva', 'F', '19217326729', '陈瑛_5073@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '尿崩症、水、电解质紊乱、内分泌与代谢疾病', 40, 'active');

-- 医生：汤正义
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汤正义', '$2b$12$.4ulZdATQFATDh3/Lk3PL.k1bLOGQGZU/rIhIL0dG7.2TiWqAizCm', 'M', '14675832441', '汤正义_8350@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病血糖控制与疑难并发症的诊治，甲状腺、肾上腺与垂体等内分泌病的疑难杂症', 50, 'active');

-- 医生：刘建民
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘建民', '$2b$12$OIfuHws4s9Zdww75LVqiYO01sB6Eq1CvuUrPv5tmw7XcRqyJpRWPe', 'M', '16297018781', '刘建民_5563@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲亢、肾上腺疾病、甲状旁腺疾病、骨质疏松', 50, 'active');

-- 医生：赵红燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵红燕', '$2b$12$CkcrKnigLO9H6QrFVt3p6u8x2WlemYSFlvGplj72HrM0fBs1jfOSC', 'F', '14180943908', '赵红燕_8260@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲状腺和甲状旁腺疾病以及骨质疏松症的诊治', 50, 'active');

-- 医生：陈宇红
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈宇红', '$2b$12$lkmZfM6ivyVzlyykac06Ser.gbdwEqX54R5mTp7j0KyQaMpz5aFMS', 'M', '13800235246', '陈宇红_9856@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病，甲状腺疾病的诊治', 50, 'active');

-- 医生：王曙
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王曙', '$2b$12$qERI1s/dTlO9aEeJJkOJvex5LXWODlGmuhG7uLufdKU1U84ngcvuu', 'M', '13909134796', '王曙_4872@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '内分泌疾病、骨质疏松、糖尿病、甲状腺等', 50, 'active');

-- 医生：毕宇芳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('毕宇芳', '$2b$12$ycdyPrz5bQsKIOeX1365K.qTSQ5k7lPeAtHMUDceDUnypb/kftwCu', 'M', '16621453189', '毕宇芳_8886@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病和甲状腺疾病的临床诊治（二级教授）', 50, 'active');

-- 医生：洪洁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('洪洁', '$2b$12$a8TA7gWy6Md.ZRtyDWKZlOqBHJrufwOHmUfMXkiblTzstLuMVuJRi', 'M', '19530613729', '洪洁_1960@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '肥胖和糖尿病的病因诊断和治疗。', 50, 'active');

-- 医生：孙首悦
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙首悦', '$2b$12$RwblPnCf80eS8f8UyhvDS.dXIlENbJtOHygFsQ.1DMwwYfOLmQ9Mu', 'M', '16102314313', '孙首悦_7396@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '内分泌科性腺发育异常', 50, 'active');

-- 医生：孙立昊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙立昊', '$2b$12$1xEbX7z1e7su5gjVrqpdluFL0dfWkwZS30lRNO5teXMRVsNS0JLOa', 'F', '19943026227', '孙立昊_8454@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '骨质疏松、甲状旁腺疾病等代谢性骨病', 40, 'active');

-- 医生：顾卫琼
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('顾卫琼', '$2b$12$vWndzQ4qW3aQ5lC0GZBgJujfiLp11/U8HheRIp/dEO1SoyHjb7kVe', 'F', '16847959430', '顾卫琼_8973@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '青少年糖尿病分型诊断、临床诊治与应用基础研究，以及干细胞新兴领域的探索和临床应用', 50, 'active');

-- 医生：汪启迪
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汪启迪', '$2b$12$2mrVrDOBqjQmqWXdGvfXCuR1NVXvgVNe.r0IOZoNCVs8TmvX5Sqve', 'M', '14418587604', '汪启迪_4566@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病，甲状腺疾病，及其他内分泌代谢疾病的临床诊疗', 50, 'active');

-- 医生：周瑜琳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周瑜琳', '$2b$12$62GhZwdZQbKWi/YnMiBJG.5peplczD7OD5GNsWTqlc6Axh1cOKUSC', 'M', '17889991777', '周瑜琳_9883@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '各种类型的甲状腺疾病及糖尿病诊治', 50, 'active');

-- 医生：苏颋为
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('苏颋为', '$2b$12$VEpOXLJiO0QSTp1VNYydGuwH/S3143GJlewJ7ESd2K6OqjlUVL/5C', 'M', '18436730606', '苏颋为_1936@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病，甲状腺，内分泌相关肿瘤，电解质紊乱，垂体和肾上腺疾病。', 50, 'active');

-- 医生：周丽斌
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周丽斌', '$2b$12$OVvLXwt8bTwCZ1j3.FUyFOOJKQAIGoGAlucS70AEWOGNWYFoQu2O6', 'M', '17611947770', '周丽斌_9238@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲状腺等内分泌代谢性疾病的诊疗', 50, 'active');

-- 医生：张翼飞
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张翼飞', '$2b$12$WShBTPAI09XDGJ58rPqyR.2izTN5bgxEMD42ud0/fFngVX6Bbsr/i', 'M', '13645276696', '张翼飞_2312@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '擅长肥胖、代谢综合症、糖尿病及相关疾病的临床诊治', 50, 'active');

-- 医生：顾燕云
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('顾燕云', '$2b$12$4pbOBm4iuyiDyuyXrovRpeHMnMqjHib1WPBbh0IXhp2afZXdUl1FO', 'M', '13738914080', '顾燕云_2113@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '各种内分泌代谢病相关常见疾病，包括2型糖尿病、胰岛素抵抗、高尿酸血症及常见甲状腺疾患等', 50, 'active');

-- 医生：陆洁莉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陆洁莉', '$2b$12$HVvwXbCVClWqZPLHnFKJM.pyS.eiWqkyPR4IVosL8BxzAQbQzVlUu', 'M', '16228727266', '陆洁莉_5033@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '糖尿病、甲状腺疾病及内分泌代谢病', 50, 'active');

-- 医生：姜蕾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('姜蕾', '$2b$12$AbDdfXQDdr6d.khBuy5L7e9YkooIAxX8iTDHz6lchHY560bYEcciO', 'M', '17188029013', '姜蕾_7868@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '尿崩症等垂体—肾上腺疾病，糖尿病，甲状腺', 40, 'active');

-- 医生：叶蕾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶蕾', '$2b$12$EXGroYeEunFp.rpjs7i9m.fgehw7vlU9dSH5x32exhjmT2vsAYO.u', 'F', '15319321646', '叶蕾_6147@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '甲状腺癌、多发内分泌肿瘤、疑难甲状腺疾病', 50, 'active');

-- 医生：陶蓓
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陶蓓', '$2b$12$spGLZyWHn6keDSCB.9ssEu/Si6uJmxBUsSBmPz2k8d56DH5GwWzRu', 'M', '15524971817', '陶蓓_3144@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '钙磷代谢、骨质疏松、代谢性骨病的诊治', 40, 'active');

-- 医生：周薇薇
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周薇薇', '$2b$12$i4htL3CabpOAetQ1lvC5K.ZwIjaP2ndkvtST1LPNsGUecQq.OBG9i', 'F', '16439492674', '周薇薇_2188@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '垂体肾上腺疾病', 40, 'active');

-- 医生：田景琰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('田景琰', '$2b$12$x0A9mM2Gbfbn03aXnjiywOQlqemTqn/bTvd7apuqvFrbk5dbhMS3C', 'M', '16766964667', '田景琰_2638@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '主任医师', '擅长糖尿病初发病的诊治和糖尿病的强化治疗', 50, 'active');

-- 医生：蒋怡然
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蒋怡然', '$2b$12$IvZoW6nA2flNg2mkt4PyOOlnnfNZ.mgzEXRgkZmICWxnqXxKc0HQa', 'M', '17328872698', '蒋怡然_9288@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '垂体肾上腺疾病', 40, 'active');

-- 医生：张翠
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张翠', '$2b$12$S80wKfKrb7OQEPDhsJxAHuJRlhjxpirMJ6tr539SecdrXAFweg/12', 'F', '14474745382', '张翠_2127@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '肾上腺疾病', 40, 'active');

-- 医生：沈力韵
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈力韵', '$2b$12$9EtmWQ3sxgWWyORMI0ItVe05LIQcvjX1rrL/HmNHqM8YeMCJBzvwq', 'M', '15406002884', '沈力韵_3584@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='内分泌科' LIMIT 1), '副主任医师', '甲状腺眼病', 40, 'active');

-- 二级科室：神经内科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '神经内科', '圣心楼', 4, '神经内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '548', '圣心楼', 4, '神经内科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：陈生弟
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈生弟', '$2b$12$kgkiH2lA2rXRfWVuHEXBxeFCXvcN1gFy/sSevHmZdW3W/D9UbZVUi', 'M', '18977358886', '陈生弟_5905@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病、运动障碍病、老年性痴呆、认知障碍、神经系统疑难杂症等各种神经系统疾病(二级教授)', 50, 'active');

-- 医生：刘建荣
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘建荣', '$2b$12$GKDEzVzAie/JojLnmVKDHu6y40fY0X6AaGd5ZWZoj.J7AxX1E7OJu', 'M', '14383968123', '刘建荣_2891@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '各种神经系统常见病、疑难和危重病人的诊断和治疗，以及脑血管病的诊断、治疗和预防', 50, 'active');

-- 医生：王瑛
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王瑛', '$2b$12$lshwvJYZFGUdiVxZWcbqdu9oGbqkOLVODG8yDtgQq7Zsxlrk4U2.i', 'M', '18694019365', '王瑛_3546@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病、记忆障碍、睡眠障碍、神经系统疾病伴抑郁焦虑', 50, 'active');

-- 医生：傅毅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('傅毅', '$2b$12$pSLH6ZfIEqJqImQr4HOPUOL.QY8Iw1TjupFgTZbwF4uw6aAcDkRa2', 'F', '15749431082', '傅毅_4450@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '脑血管病、偏头痛及头晕的诊断和治疗', 50, 'active');

-- 医生：汤荟冬
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('汤荟冬', '$2b$12$9B91i.9ZTu9oTKAGWLsmruOtgFEyDhTvAlM9PmgP0y9lFV.uLxrAm', 'F', '14838194420', '汤荟冬_5325@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '记忆力减退、癫痫', 50, 'active');

-- 医生：肖勤
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('肖勤', '$2b$12$U2T3PdMi0/.V7D0pjrQz.ug.GgF73GY2WGH/nBIOWZdXhDexzRbzO', 'F', '15154544899', '肖勤_2512@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森氏病、肌萎缩、神经内科常见疾病', 50, 'active');

-- 医生：邓钰蕾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('邓钰蕾', '$2b$12$jTs3iupNbtqSg/pIPX9HOerURc9F9Iel4ZNDB4OdRZ6PHJ7S3EF26', 'F', '19397083132', '邓钰蕾_1722@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '癫痫等发作性疾病、老年性痴呆、头痛、睡眠障碍、脑卒中等神经系统疾病。', 50, 'active');

-- 医生：马建芳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('马建芳', '$2b$12$K0Qt9Vd2tf7ZT3PGxVzbj.FkbQbVBIgMk/7xgW0BNNlfiCSyefeHW', 'M', '15927982963', '马建芳_3143@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森、失眠、不宁腿，打鼾等睡眠障碍，姿势障碍，抽动症和斜颈，偏头痛等。', 50, 'active');

-- 医生：任汝静
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('任汝静', '$2b$12$5/ya8tNKbb6WicFrjUNTAOIrTE8lpwvsw/WoSHEjoD7GZ9Kbi3fOu', 'F', '14895890631', '任汝静_8239@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '痴呆、帕金森病、脑血管病、头痛头晕、癫痫、睡眠障碍等神经疾病。', 50, 'active');

-- 医生：刘军
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘军', '$2b$12$rEoCZX/exbzT5/gIBilfQelwHCCehOdVyBjS.gyJScW/YPB6k1HFC', 'F', '17110382761', '刘军_2832@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病、痴呆、肌张力障碍、肌萎缩、睡眠障碍、脑血管病等神经内科常见病和多发病的诊治。', 50, 'active');

-- 医生：曾丽莉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曾丽莉', '$2b$12$BHbpII5FfkqFAuLdnoncXeU6MCGaxORwZB0oDPl8/63HtWJ9eqWdG', 'M', '18260045822', '曾丽莉_9938@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病及相关情感认知障碍的诊断及治疗', 40, 'active');

-- 医生：徐玮
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐玮', '$2b$12$TfZ7QIo/BRgnNN/SK3wT5uWOsE4z0pB8y32c21VOz/SW/KVbUH4KO', 'M', '19496442646', '徐玮_3426@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '记忆力障碍，睡眠障碍，头晕头痛及其他神经系统疾病的诊断和治疗。', 50, 'active');

-- 医生：辛晓瑜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('辛晓瑜', '$2b$12$r3XN9th6FvVSXORsF.dxSeY6P4rG2XavRe9wYwEGhpYLga0LuwBRe', 'F', '14144913399', '辛晓瑜_6050@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病及卒中后管理，血管性认知障碍，头晕', 40, 'active');

-- 医生：吴逸雯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴逸雯', '$2b$12$FuMVLmUKEG9Kpa3Cx8l4UO7XKBEuOBj6aPZC9YotKlmgNHZerhneK', 'F', '19142836793', '吴逸雯_6862@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '肌张力障碍、震颤、帕金森病、舞蹈、共济失调；肉毒毒素治疗；神经调控治疗', 50, 'active');

-- 医生：周海燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周海燕', '$2b$12$mSw7zUzXcqRGnmz2U58uqOnZKDMKyUs0wrLAcXD3Rapj1LYgVzt6u', 'M', '18367930513', '周海燕_2684@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森病，震颤，肌无力，肌萎缩，DBS程控，神经科常见病。', 40, 'active');

-- 医生：陈晟
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈晟', '$2b$12$TCP/LBkpJc5JbKlZ4aWZ9OFmj3ySF2PyaQgZnCVlP.fS.kpS4B0UC', 'F', '19701170349', '陈晟_7658@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '擅长神经科常见疾病，头痛，头晕，脑血管意外，肌无力，肌萎缩等诊治，尤其擅长神经免疫性疾病（自身免疫性脑炎、多发性硬化、视神经脊髓炎、重症肌无力）、疑难疾病诊治', 50, 'active');

-- 医生：潘静
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('潘静', '$2b$12$GSbYUpJKpxyqILfPZbWF9eD6Sr.t2vQacF8pmODSz9Qc5OqKPvpV.', 'M', '14274484941', '潘静_3900@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森病、特发性震颤、睡眠障碍、脑血管病', 40, 'active');

-- 医生：谭玉燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('谭玉燕', '$2b$12$23HnPcak7cBrMtEQ7hMsQ.zTPTou4ZnRhBVBCH65jkspEFkR56E5.', 'F', '13292587044', '谭玉燕_6442@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '帕金森病，多系统萎缩及其他运动障碍疾病。', 50, 'active');

-- 医生：李彬寅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李彬寅', '$2b$12$Y9gY4/UyI0J3divbu2w.Ae7ldzEdLm24iLREdVOIxTg/kuLrMTcwS', 'F', '19819307302', '李彬寅_5065@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '阿尔兹海默病、血管性痴呆等认知障碍类疾病。', 40, 'active');

-- 医生：尹豆
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('尹豆', '$2b$12$HqlhceobYc/uc3KNlPc8x.PwKFSySEBWHbS359jCkscEtQ26wIOsa', 'F', '14945436943', '尹豆_2771@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病，脑动脉狭窄，神经血管介入治疗。', 40, 'active');

-- 医生：李媛媛
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李媛媛', '$2b$12$G9cVTc2e2YuKM0tTuGRK8.qVWD4LJNPTfqO/xSRuhy0Qz/.phWPX6', 'F', '19141580226', '李媛媛_8711@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森病，震颤及睡眠行为障碍。', 40, 'active');

-- 医生：杨晓东
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨晓东', '$2b$12$Uwy/mqeyFYcLp5vpiCF6DeJNtDUHYFvUb6fUzzaINhIH0Dg33Ykgq', 'M', '14976803188', '杨晓东_8541@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '帕金森，脑血管病，头晕，失眠等神经内科常见病。', 40, 'active');

-- 医生：沈帆霞
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈帆霞', '$2b$12$xIdcDyH3Kp7/a6p0oCEAqOK1UOYUBYtwwKKQLgWU0Z4AEqYRZapMW', 'F', '15981044229', '沈帆霞_4728@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '各种脑血管病（如脑小血管病、血管炎、脑白质病）、血管性痴呆、头痛及其它神经系统常见与疑难病诊治。', 50, 'active');

-- 医生：康文岩
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('康文岩', '$2b$12$IQDDQGrL3PUcQNPCdP3e8uDVwYcVC.w08M31s.AZdwVtBES1lcSKm', 'M', '13808705238', '康文岩_4164@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '主任医师', '诊治帕金森、智能减退、脑血管病、头痛、头晕、睡眠障碍、肢体麻木、面肌抽搐', 50, 'active');

-- 医生：江静雯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('江静雯', '$2b$12$p.ITtKLZUEB0rqwZR.QlKuCduIubyWNvsO31/kBVWexI7ge57laEO', 'F', '15399147754', '江静雯_2137@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '头晕、腔梗、脑白质病、记忆减退及神经科常见病。', 40, 'active');

-- 医生：胡震
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡震', '$2b$12$EsHVJ.VrPgV7aHPehffvVe3VG6l8imfVFUr7XOFylF0NhrkEd57DS', 'F', '15788785773', '胡震_9346@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑梗塞，脑血管病的微创介入治疗，脑动脉支架植入术', 40, 'active');

-- 医生：刘斌
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘斌', '$2b$12$xs7HgKi75M6KOzM2.9rQ4O.xThN94oaQJUEe6QixWwtkxKFV3D6i.', 'F', '18675757257', '刘斌_6425@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经内科' LIMIT 1), '副主任医师', '脑血管病的诊治，脑血管介入治疗。', 40, 'active');

-- 一级科室：外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '外科', '圣心楼', 1, '外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '143', '圣心楼', 1, '外科主诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 二级科室：骨科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '骨科', '圣心楼', 2, '骨科专业诊治各类骨科疾病，包括骨折、关节疾病、脊柱疾病等，拥有先进的骨科治疗技术。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '543', '圣心楼', 2, '骨科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：王亚梓
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王亚梓', '$2b$12$s7K/a5yGDdjB6lxnUfXby.kHYe.VJBOEcQ9PaV6nALWbU2UcPtvwK', 'M', '13740563507', '王亚梓_8119@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '四肢关节周围骨折、骨折愈合障碍、膝关节和肘关节屈伸功能障碍的治疗', 40, 'active');

-- 医生：梁裕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('梁裕', '$2b$12$3GFB.UA4Lt8GenBfKDkEiOQzyRLNsxZgVdlD6yVT199sOEhv3m5QO', 'F', '18944420824', '梁裕_6139@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '脊柱骨折脱位伴神经损伤；脊柱畸形，包括青少年特发性脊柱侧弯、先天性脊柱侧弯、成人脊柱侧弯以及创伤后脊柱畸形等；脊柱退行性疾病，包括椎间盘突出，椎管狭窄，腰椎滑脱等', 50, 'active');

-- 医生：张伟滨
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张伟滨', '$2b$12$wECfJEieyyoT0A9InqIGv.vqyUzA3NrTLUcDuvfF36859Mv2hD09C', 'F', '17649136331', '张伟滨_2894@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '各类骨肿瘤保肢治疗，转移性骨肿瘤的综合治疗，骨质疏松诊治、各类复杂骨折', 50, 'active');

-- 医生：冯建民
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯建民', '$2b$12$upAxFxeb..ENbuHu3IGuEOaD8elnnTHpcV/TJdHiQiKKKHaWVH58G', 'F', '17304095531', '冯建民_5173@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '关节外科，骨关节病，人工关节', 50, 'active');

-- 医生：王蕾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王蕾', '$2b$12$CixPihUD1nbO750URwZltOGK6t2w7kSC6RFtKXDUsSttSy96.Zfue', 'M', '18568213197', '王蕾_1027@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '肩关节外科、肩关节外伤、肩关节疼痛等疑难病例，四肢关节周围创伤、骨质疏松骨折、骨折后并发症', 50, 'active');

-- 医生：曹鹏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曹鹏', '$2b$12$YyE5dqZ9wdd9Ai2iTwqe1.5S6WQ6nYSwI8VXWEgWfPYhTPowfVCBC', 'M', '15563102056', '曹鹏_2146@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '临床常见颈肩痛、腰腿痛等脊柱退行性疾病、脊柱损伤的微创手术治疗；复杂脊柱疾病（如：脊柱畸形、脊柱肿瘤、脊柱炎症）的综合诊断和治疗。', 50, 'active');

-- 医生：刘志宏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘志宏', '$2b$12$cc.USmZiSquLwPCPkfcEHOLUnALgtwjkgaUA0.FKIVzdQ1xrHUTaW', 'F', '17437064353', '刘志宏_3041@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '髋膝关节外科，包括髋、膝关节骨关节炎，股骨头坏死，髋关节发育不良的人工关节置换，关节周围截骨手术以及髋膝关节置换术后的翻修手术治疗。', 50, 'active');

-- 医生：刘津浩
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘津浩', '$2b$12$B9DagXp9VyqIB0BfA537u.RxJAYPTxA6yaxrKiQJ.P5sKTtnfCGpO', 'F', '17432091877', '刘津浩_7691@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝部畸形（包括先天性、后天性、创伤性）矫正、运动损伤、四肢骨折及其并发症的治疗', 40, 'active');

-- 医生：徐向阳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐向阳', '$2b$12$mVPIwduxCgV9MgLqJG6OzOxWOYKwUhUM.PVOWz4qOCmGobUnu6z0q', 'F', '16848621833', '徐向阳_5844@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '足踝外科各类疾病', 50, 'active');

-- 医生：张兴凯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张兴凯', '$2b$12$6xvSO9fthGPmMOxZ/1OtgOkN7M8iZB501NhEh0p4u7eZzKEdR4fEK', 'M', '14551450815', '张兴凯_7211@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '脊柱疾病， 颈腰椎间盘突出，椎管狭窄，脊柱骨折，脊髓损伤，脊柱畸形，骨质疏松症', 50, 'active');

-- 医生：万荣
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('万荣', '$2b$12$M8VwhrQpprAhVfKtQ/Cl1OOvbp4U1qtzBK1F/xBsfLXolY/NfxRd6', 'M', '17711072118', '万荣_5930@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '骨与软组织肿瘤诊治，骨质疏松症及各类关节内骨折。', 50, 'active');

-- 医生：吴文坚
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴文坚', '$2b$12$MLUE2DII5nHszxoA6AqWQuG6/ZVWBA.aisYMSsPsYfcwCaqzkFM9q', 'F', '17995205588', '吴文坚_1006@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '脊柱微创手术，颈椎病，腰腿痛，脊柱损伤、畸形', 40, 'active');

-- 医生：张炅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张炅', '$2b$12$nDhk/tfcrw/ddrAE2wnncO.4wHW8WAv6s3Cl8j8wWYKy.H5/lnISy', 'F', '15325681936', '张炅_8043@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '髋关节膝关节骨关节炎诊治；先天性髋臼发育不良诊治；股骨头无菌性坏死诊治；髋膝关节人工关节置换手术', 40, 'active');

-- 医生：庄澄宇
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('庄澄宇', '$2b$12$M4n6EqZ3vFpT.b2ZK5fY2evvVWSEH4X9hgeGQUGLGbdzJnIZGSvwC', 'F', '16574364121', '庄澄宇_8244@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '肩关节外科及运动伤病的治疗', 40, 'active');

-- 医生：沈宇辉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈宇辉', '$2b$12$QuxfYqv9PnMhTH0jqM5qjuZiWxr6.lwi1mhtK2bf4LYgEsVOk9.bK', 'M', '17608079792', '沈宇辉_3780@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '良恶性骨与软组织肿瘤、转移性骨肿瘤、髋膝肩关节疾病、各种代谢性骨病、骨质疏松。', 50, 'active');

-- 医生：朱渊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱渊', '$2b$12$sumbl2ZnzPQm1R0VyFr1OuaaxKBDyAaph.6tCjZvdO8Wpki4sWUQK', 'M', '15653462378', '朱渊_6491@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝外科各种畸形矫形、足踝运动医学、创伤', 40, 'active');

-- 医生：何川
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('何川', '$2b$12$9..xyS5DDruWgTFiFXx0G.BOBjxM8pyRkMjPRiPPjFtbDl46Y8dkC', 'M', '19906528433', '何川_4848@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '髋、膝关节疾患的诊断和治疗，关节置换和关节镜手术', 50, 'active');

-- 医生：王碧菠
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王碧菠', '$2b$12$b6ZYA3XAy0/GNbpKGNYrke4Sy18bCCbZUQqV0HEvX9bDwWujKGnl6', 'F', '14966040317', '王碧菠_4262@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '四肢骨折及运动损伤，手足外科各类疾病。', 40, 'active');

-- 医生：叶庭均
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶庭均', '$2b$12$G1jR.GqJFsn0.MtQR4V72eIEj929h4IWFAbdH3.v3raPj/A3qZkdu', 'M', '13149621604', '叶庭均_5011@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '运动损伤，膝关节半月板，韧带损伤，骨膝关节疾病。', 40, 'active');

-- 医生：王弘毅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王弘毅', '$2b$12$J9Tqd/U9Q0gLwKQ11.Y9POefTuWc3Q9i1Ag1b7zwind3nVjW13jqu', 'F', '17925159146', '王弘毅_2193@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '髋、膝关节疾病的诊治，包括骨关节炎，创伤性关节炎，股骨头坏死，髋关节发育不良等微创和人工关节置换手术', 40, 'active');

-- 医生：虞佩
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('虞佩', '$2b$12$KVhKXr0ruonH7MUMMMZunOeIG60mAPAqLX3RAzJg1ozTmMwWeZpTW', 'F', '16776205431', '虞佩_4185@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '复杂骨折微创治疗，运动损伤、慢性损伤', 40, 'active');

-- 医生：温竣翔
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('温竣翔', '$2b$12$a14Z2.BGqAxSFrmWSfbNLuuWPFfqafmBRqIihlB7xMHy1OH4fA72q', 'F', '16529123992', '温竣翔_4997@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '骨与软组织肿瘤的综合治疗，转移性骨肿瘤的综合治疗，代谢性骨病，骨质疏松的综合治疗。', 40, 'active');

-- 医生：于涛
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('于涛', '$2b$12$IYuwkag6IDn.7GpKdbBMTO6NcKu0T2iqGEcsyL1vH/HioGRTJe2Q2', 'M', '18838328947', '于涛_1090@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝疾病、畸形、关节退变及骨折的诊治。', 40, 'active');

-- 医生：杨云峰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨云峰', '$2b$12$iZm2j8BUtcPUDNJ7cqSWl.OzIaQWgqw/K675TZCgLr.cuWQfwIKbO', 'M', '19556497252', '杨云峰_4585@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '主任医师', '骨科、足踝外科、创伤骨科', 50, 'active');

-- 医生：李兵
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李兵', '$2b$12$xn0nk9Y.lyEyz94P2G0vq.SgHJUM4iVmBPWNi0CJyi4rKoPzHhSva', 'M', '19847229579', '李兵_9486@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝外科相关疾病（骨折，外翻，手足高强运动损伤，退变等）', 40, 'active');

-- 医生：杨军
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨军', '$2b$12$gdqdnommSZ39cc90vaUuY.F.XYZBPDwjhzR8tDl2dh4b.1i70TTwe', 'F', '13698509918', '杨军_5082@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '脊柱肿瘤，颈、腰椎病，上颈椎脱位，脊柱侧弯畸形，脊柱结核感染。', 40, 'active');

-- 医生：倪明
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('倪明', '$2b$12$dGL2JkGdUx8D5035.UVusuRRisz0gHqjGwsuuaUWlZZW/duF830By', 'M', '16243171768', '倪明_8612@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '肢体骨折、足踝畸形矫正、运动损伤与康复。', 40, 'active');

-- 医生：徐建强
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐建强', '$2b$12$lmV8lWGc/JrYKSgbUIg9i.SfuxLehG4dKU5T68QoSAi5dX2x0jujK', 'F', '19575209597', '徐建强_9270@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '骨肿瘤、脊柱肿瘤，各种骨与关节损伤，骨与关节炎症及风湿性关节病，骨质疏松等疑难杂症', 40, 'active');

-- 医生：郭常军
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郭常军', '$2b$12$NFj2SdzDW3QdM/s0qLXQI.J9c4eMGjHSyH1IuP0eANjy.Lf5wg7/C', 'F', '19688273030', '郭常军_8305@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='骨科' LIMIT 1), '副主任医师', '足踝外科疾病（踇外翻，扁平足，高弓足等），足踝运动损伤和四肢创伤。
', 40, 'active');

-- 二级科室：功能神经外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '功能神经外科', '圣心楼', 2, '功能神经外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '467', '圣心楼', 2, '功能神经外科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：占世坤
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('占世坤', '$2b$12$vVhWeFkKcwozRgjDGpEuH.Rb30Fiu1aUf/66cxt.cb0OG8CnzgdaC', 'F', '19365471644', '占世坤_5543@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '主任医师', '帕金森、肌张力障碍、疼痛、癫痫、精神疾病、脑肿瘤、脑瘫的外科手术及伽马刀治疗.', 50, 'active');

-- 医生：孙伯民
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙伯民', '$2b$12$pDBKWevVorqMaePqK.m8Ouddas0vwfVZtJ6uVkdZT0D2pjzPT/l8G', 'F', '18356888294', '孙伯民_5499@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '主任医师', '癫痫、帕金森、肌张力障碍、疼痛、神经性厌食症、精神分裂症、强迫症、焦虑症、面肌痉挛、三叉神经痛', 50, 'active');

-- 医生：李殿友
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李殿友', '$2b$12$zbduAG1KRSfN.lF6IDvJl.pO21Iloi7zBUWxZGEvBr9p7CGS/z4qq', 'F', '13866162966', '李殿友_5681@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '主任医师', '帕金森、震颤、肌张力障碍、疼痛、抑郁症的外科治疗', 50, 'active');

-- 医生：潘宜新
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('潘宜新', '$2b$12$GryzpIT9jyUngI41JNjMfuHlFx4vLflIFCWFRebdcGMO5JhScmmFu', 'M', '15460613550', '潘宜新_6238@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '副主任医师', '肌张力障碍、帕金森、糖足、疼痛、震颤、截瘫、抑郁症及精神疾病的外科治疗', 40, 'active');

-- 医生：刘伟
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘伟', '$2b$12$GI1lyT0v/XFECZNwHKxijezpWlV8ifZy43S3EDJr/wjF29KDhUEzO', 'M', '14261953189', '刘伟_4788@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '副主任医师', '疼痛、震颤、糖足、肌张力障碍、帕金森、厌食症、精神疾病的微创治疗', 40, 'active');

-- 医生：胡柯嘉
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡柯嘉', '$2b$12$16q18p/RMMLY7tONfiiqfO3jCcHlULzItPSlySro.dV5Dtn7yeKzu', 'F', '18264073400', '胡柯嘉_4505@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='功能神经外科' LIMIT 1), '副主任医师', '疼痛、肌张力障碍、糖足、帕金森、脊髓、周围神经病变。', 40, 'active');

-- 二级科室：心脏外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '心脏外科', '圣心楼', 1, '心脏外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '462', '圣心楼', 1, '心脏外科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：陈海涛
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈海涛', '$2b$12$CO.2roPiC3SycSHvbzcLzuYZcle2rzbW4lvqd57S/6CCvwYL/YO0W', 'F', '17600282414', '陈海涛_7812@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '胸腔镜手术，风湿性心脏病，冠心病', 40, 'active');

-- 医生：裘佳培
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('裘佳培', '$2b$12$4B/nR5Cqkc3/btxfDLOrzOeFn5XGZaV9KUj/hsZFkfLFJcGFVF4HW', 'M', '14994310083', '裘佳培_7883@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '特长：微创冠脉搭桥术及各类冠心病、瓣膜病、大血管疾病的外科治疗', 40, 'active');

-- 医生：赵强
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵强', '$2b$12$rAwtnfNcCxm9.pReKxgkuO7zFUlLXDWti/Cjcwj0oIGBQMA2c/f1K', 'F', '19727150493', '赵强_1320@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '冠状动脉搭桥术，微创冠状动脉搭桥术、微创心脏手术，机器人辅助心脏手术、心脏瓣膜成形术、
主动脉瘤的外科治疗、
心衰的外科治疗，心脏移植，辅助循环
', 50, 'active');

-- 医生：王哲
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王哲', '$2b$12$TZ.4oQct.vYdKi.N7rSl2e5evgfOVx03LiEvWlxScdrTsq41K1rtG', 'F', '16106330865', '王哲_6763@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '瓣膜病、冠心病、先心病、胸部动脉瘤微创手术', 50, 'active');

-- 医生：叶晓峰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('叶晓峰', '$2b$12$YemEnyTlIzu/l6A6GrcVS.2kXze2eNnd61Kak604dfiiuRAiDi6v2', 'F', '19518744974', '叶晓峰_7865@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '对心脏病的外科诊断和手术治疗有着丰富经验，擅长微创介入瓣膜修补、微创冠脉搭桥和大血管手术。', 50, 'active');

-- 医生：周密
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周密', '$2b$12$oB7ffXDA8JlUYCoCGDXsJOH3W/ljaFbJuk5Zu3smLpsacnUciq2Fa', 'M', '16335602191', '周密_5471@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '主任医师', '心脏移植、人工心脏、冠脉搭桥及瓣膜手术、微创手术、肥厚型心肌病手术', 50, 'active');

-- 医生：李海清
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李海清', '$2b$12$fZWdw6wgjs/waO3NXcwdpuusMTB6Nknr7jPI25no6qRemg1OVfsnG', 'F', '16131165166', '李海清_7371@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '冠心病、心脏瓣膜病、先心病、心力衰竭等外科治疗', 40, 'active');

-- 医生：徐洪
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('徐洪', '$2b$12$IWeFrcwjxHsMC2QUf6/U.ulpc/Omu5m5xXoPaXVvJu.BkXCGwXmU.', 'F', '18829223044', '徐洪_7624@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '各类成人心脏病如复杂冠心病、瓣膜病、大血管疾病、以及微创介入治疗、终末期心衰综合治疗。', 40, 'active');

-- 医生：朱云鹏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱云鹏', '$2b$12$zjNUCWNCQwfHimhzTW1x5uc7vV1ub.LsLM.vap7AWo2DG4AIHJOIa', 'M', '19601856351', '朱云鹏_3091@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='心脏外科' LIMIT 1), '副主任医师', '冠心病心脏搭桥术及心脏外科其他常见病诊疗', 40, 'active');

-- 二级科室：神经外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '神经外科', '圣心楼', 5, '神经外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '513', '圣心楼', 5, '神经外科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：赵卫国
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵卫国', '$2b$12$l/Gw/Azt/huH6Z/O/rY9ou8qo0ThOr/ZjbOolrLFG2JvD/hiWxaoa', 'F', '17706011277', '赵卫国_1444@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '神经外科疑难疾病的诊断和微创手术治疗。特别是垂体瘤和颅内各类肿瘤；面肌痉挛，三叉神经痛，舌咽神经痛；颈动脉狭窄和脑血管病的微创外科治疗。', 50, 'active');

-- 医生：王健
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王健', '$2b$12$pckS8jVITS7CuouppxqTJ.Eti3bm0xGc2IpbDs6VoU.Eogm7zVIYq', 'M', '18560219456', '王健_3223@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '颅脑外伤的诊治，预后康复，各类颅内肿瘤的手术和放化疗方案。脊髓椎管内肿瘤的诊治', 40, 'active');

-- 医生：濮春华
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('濮春华', '$2b$12$R.3BPhj.9hOQqeM4ZqmWqOqPbfodyq0Sakv9zjo.ltOVoj8oqlyJu', 'F', '14153992714', '濮春华_5262@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '颅神经疾病治疗（面肌痉挛、三叉神经痛、舌咽神经痛的微创微血管减压术）、颅内及椎管内肿瘤手术', 40, 'active');

-- 医生：李云峰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李云峰', '$2b$12$K5tkOcZTa1.wbNDy14nnu.UKsAKnUqh84fepMqGnvaHEDFD59rNBi', 'F', '15327268503', '李云峰_8449@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '微创手术、内窥镜、颅脑肿瘤综合治疗', 40, 'active');

-- 医生：林东
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('林东', '$2b$12$SxjgyXBoxcaDsD2PtmKHWOFsrZXbVshxv9wBFOHi0oY15afGjvRm6', 'F', '15917364173', '林东_7211@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑、脊髓血管病的血管内治疗、颅内肿瘤', 40, 'active');

-- 医生：胡锦清
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡锦清', '$2b$12$Czm9pSXxKAY.v7CD7uVxQeTYFHjcHCspxnAZ/JNMtsQjR4ZN9mCTm', 'F', '19993271900', '胡锦清_7906@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑血管病（颅内外动脉狭窄、脑动脉瘤、脑血管畸形等）介入和外科治疗，脑外伤、脑肿瘤等。', 40, 'active');

-- 医生：蔡瑜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蔡瑜', '$2b$12$nbH9hG0eLNQ30qHyZb0/Q.1tiEhSMgsY2OPrwgk129QmSiIcw.IzO', 'F', '19187946147', '蔡瑜_8705@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '脑血管病（颈动脉狭窄的手术治疗）、颅神经疾病（面肌痉挛，三叉神经痛）', 50, 'active');

-- 医生：孙青芳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙青芳', '$2b$12$t4BHY4bx3oDA.4R3RZb6z.cvh/caLke6PWq89KobeyQCmwH/2EHKm', 'M', '18679196799', '孙青芳_1853@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅内肿瘤、垂体瘤、脑积水、神经脊柱脊髓外科、脑血管病', 50, 'active');

-- 医生：卞留贯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('卞留贯', '$2b$12$dcWr/VWaO1uCA7Y9GxTRq.kUkZs4IbzZp.tmqghoDowm3fmRHL1Ai', 'F', '14798086909', '卞留贯_2124@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅内肿瘤及脑血管病', 50, 'active');

-- 医生：江泓
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('江泓', '$2b$12$MzYVN9FhrtrL5GvN1ViTFOsaxDT3xUDRsqRpta.on/Dszb3udyEFO', 'M', '19133318206', '江泓_5051@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅内动脉瘤，脑动静脉畸形，颅内动脉狭窄，颈动脉狭窄，椎动脉狭窄，脑梗，脑出血，蛛网膜下腔出血等颅内出血性和缺血性疾病。搏动性耳鸣介入治疗。', 50, 'active');

-- 医生：朱军
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱军', '$2b$12$GY27U.FaniUYt/EqC9akLugL8YA87pRBDWjZYsqQ16MaMzxLt/ale', 'M', '19121882877', '朱军_3496@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑血管病微创治疗及脑出血、脑梗死、颅脑肿瘤诊治', 40, 'active');

-- 医生：尚寒冰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('尚寒冰', '$2b$12$OjTbMNnZWE/yU6KXGQ59oOozAB36zm4G.yZhPjUSuNH2NdJb1ZX4u', 'M', '14608482316', '尚寒冰_2874@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '颅神经疾病（三叉神经痛、面肌痉挛）微创手术、脑血管病、脑肿瘤外科治疗、神经系统疑难罕见疾病新技术治疗咨询。', 50, 'active');

-- 医生：潘斯俭
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('潘斯俭', '$2b$12$eeO9sJezDLIaoMsxu/L7Hut1vlvg1/EoXstEJ1ptmhpRQqXUfyT8a', 'M', '16851044796', '潘斯俭_5198@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '三叉神经痛的MVD，PBC及伽玛刀；脑肿瘤，脑血管畸形及癫痫的伽玛刀', 50, 'active');

-- 医生：吴哲褒
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴哲褒', '$2b$12$igsDIOGtQnptgzhFCZc2fefDizMzzsA37rE6ghY9cyrERkmLgK8Cm', 'F', '14750572325', '吴哲褒_2876@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '主任医师', '垂体腺瘤的内镜经蝶微创手术；颅脑肿瘤（脑膜瘤、胶质瘤、听神经瘤、颅咽管瘤等）显微外科微创手术治疗；垂体腺瘤的药物和个体化治疗。', 50, 'active');

-- 医生：孙昱皓
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('孙昱皓', '$2b$12$4sP00Gqg7Gz1fNSKOhTKBez4ir514gM2pFKZ65lUouPn62rkqxDjS', 'M', '15216070149', '孙昱皓_1420@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '脑肿瘤内镜微创手术（垂体瘤、颅咽管瘤等）；颈腰椎微创手术（神经鞘瘤、椎管狭窄等）；其他各类神经疾病的内镜微创治疗（脑积水、腕管综合征等）', 40, 'active');

-- 医生：顾威庭
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('顾威庭', '$2b$12$AKSxCQ5Bqr6ihwmOhQEPGOGyoDqXmvKhXi5qnsfDSoTQHh4BODyyW', 'F', '17827296111', '顾威庭_7149@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='神经外科' LIMIT 1), '副主任医师', '擅长脑血管疾病、颅内肿瘤和脑积水等疾病的外科治疗', 40, 'active');

-- 二级科室：烧伤整形科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '烧伤整形科', '圣心楼', 4, '烧伤整形科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '219', '圣心楼', 4, '烧伤整形科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：袁克俭
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('袁克俭', '$2b$12$aOpcY0/pRY9ZL51ewd1o4epQsJlgFwjoVDCBUY2NZ5R4K.hZrght6', 'M', '13848605284', '袁克俭_5941@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '大面积烧伤,难治性创面处理及疤痕早期防治', 50, 'active');

-- 医生：郑捷新
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郑捷新', '$2b$12$CO3F1MEa5/72iIVYLKqXRu.5.7bH1kSWeoIj2lchArDpnprycXYZa', 'M', '19707665448', '郑捷新_1672@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤后期疤痕预防和治疗，难愈性创面的治疗', 40, 'active');

-- 医生：张勤
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张勤', '$2b$12$wQQUg1.5T9SWIDLZW10Jz.Qy6ZkJooM4q9x99keziLR/SC4EJ2wQS', 'F', '17559967386', '张勤_7071@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤疤痕防治，难治烧伤创面的处理和治疗', 50, 'active');

-- 医生：黄伯高
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄伯高', '$2b$12$SEl6nW1pi0ijRJffEk3tIemZvYWOwgavfscM2l7jIAYknK5glHR8.', 'M', '17795294275', '黄伯高_6590@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤创面修复、抗感染、疤痕防治', 40, 'active');

-- 医生：王文奎
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王文奎', '$2b$12$AImaxiy0iopTuJA7mFGzL.Ri.LOz31f9dzhLY6MX6g2roVuDxjBx2', 'M', '19551034639', '王文奎_9031@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤创面、疤痕预治及后期康复指导', 40, 'active');

-- 医生：张剑
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张剑', '$2b$12$md.Ti2IDv4tjO7Dde8NLhem0XCGyxri1.1ospYcVtQn3jzbZxtNKq', 'M', '16488896443', '张剑_8532@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各种灼伤及创伤后疤痕增生的整形康复，以及各种难治性创面的诊治', 40, 'active');

-- 医生：刘琰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘琰', '$2b$12$y93lOGZKHq7QOHzyma9AneMYyEV9XasQ.cGV7.eKCpMCmr2qLH35W', 'M', '16289125618', '刘琰_9548@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '创面处理、慢性难愈创面（糖尿病足）治疗', 50, 'active');

-- 医生：杨惠忠
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨惠忠', '$2b$12$zV9nrQsjYi81.CzrEnauLOQQ7V03Tx4fH8yBB77DuOpa1um96U9g6', 'F', '17967797300', '杨惠忠_9817@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '烧伤创面修复,疤痕修复', 40, 'active');

-- 医生：向军
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('向军', '$2b$12$sshXc/QWbctnz30qEHxa6e7I6XwnoyEr/ndL09dFC0gbx/pmNG4ya', 'F', '16567694547', '向军_5397@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各类创伤创面的修复、疤痕防治', 40, 'active');

-- 医生：牛轶雯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('牛轶雯', '$2b$12$Du79bd/kPDk3EmLSU0ecre7RugAdb9wsETGSCNpUsD0j33Az5yYW.', 'F', '19363598427', '牛轶雯_2419@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各种类型创面修复及预防、瘢痕防治', 40, 'active');

-- 医生：乔亮
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('乔亮', '$2b$12$jVftSwnop.Ik4atui7bQKu4W3wb6JCRrkblyvSHMGA688LO1La/bu', 'F', '16361846370', '乔亮_8613@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '大面积烧伤、儿童烧伤、疤痕治疗、慢性创面。', 50, 'active');

-- 医生：王志勇
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王志勇', '$2b$12$O.UeVkftiVF6umQMLlGP3.5g4yxtOgK28AVJL52KbBZISjTS8t7k6', 'F', '15130816384', '王志勇_9098@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤创面修复、慢性及难愈创面处理', 50, 'active');

-- 医生：窦懿
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('窦懿', '$2b$12$gK2lCoAica5Urg51gKQRveIJbBT9rXM9l6hWv9xgMzkm48yT1UuEy', 'F', '14623502759', '窦懿_4475@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '疑难复杂伤口修复、疤痕治疗、皮肤肿物治疗', 40, 'active');

-- 医生：黄晓琴
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄晓琴', '$2b$12$CP27dRKnSfUKmGNZtnrwkeRo4LQGN.Onv0kCucD60.wI4CQJyskOe', 'F', '19377403790', '黄晓琴_6576@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤创面修复、烧伤瘢痕整形、瘢痕激光治疗', 50, 'active');

-- 医生：王西樵
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王西樵', '$2b$12$25kDllCccdjia7tP7mLzPuRjD/p4yIKztre4zDBzusXlU4Ybvah4O', 'F', '17852890318', '王西樵_5526@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '擅长深度烧伤、创伤等难愈创面的非手术愈合，疤痕的修复治疗和激光治疗', 40, 'active');

-- 医生：施燕
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('施燕', '$2b$12$2GJ3Ic1Op1cjSueer/I9P.byEc5XR2HbaCybY/mD5OOUMQwpqkChi', 'M', '17305145276', '施燕_2402@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '瘢痕光电及综合治疗、小儿烧伤、创面修复。', 40, 'active');

-- 医生：唐佳俊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('唐佳俊', '$2b$12$4xj1AxNL1shximdL3PupqOUFWwwDbHFrWsCMKhDHzF/UvEYbWHvNy', 'M', '18536398872', '唐佳俊_9004@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '各类急慢性创面修复、增生性疤痕治疗', 40, 'active');

-- 医生：郇京宁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郇京宁', '$2b$12$GaZaH6mduQ.Pzy0s.JC4se92snkjPR5dy5BRWs20Z5RahqMMpSGni', 'M', '18611201082', '郇京宁_9041@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '烧伤创面、慢性、感染创面治疗，疤痕防治', 50, 'active');

-- 医生：李学川
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李学川', '$2b$12$kYxBsKGdfYdTDQuZ9JMAGum3hZ23jvw05ZFzdOfgEMkK60VQoSmKq', 'F', '19118514813', '李学川_2524@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '瘢痕综合治疗；淋巴水肿手术治疗；精细缝合及复杂创面修复。', 40, 'active');

-- 医生：原博
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('原博', '$2b$12$vftSf56F3cqPf/OvyOxgEeFFSp3ivACdr4QMZnM8MgM0Cs6SB4ptS', 'F', '14534222401', '原博_4986@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '小儿烧伤、瘢痕防治、创面的细胞治疗与再生。', 40, 'active');

-- 医生：周增丁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周增丁', '$2b$12$loDG0v3vrhY7zKT5/jVK3etPyc90qShIaBDz6mONRmcIyAh1GeNfG', 'F', '18724440558', '周增丁_7046@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '艾灸、暖宝宝等低温烫伤治疗；腋臭祛除、腹壁整形；手部指蹼瘢痕治疗；疤、痣、纹身及皮脂腺切除精细缝合。', 40, 'active');

-- 医生：易磊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('易磊', '$2b$12$sA.8WSMcW6nKJ9dMr7FG6ezRuoEggn2b2Y6jb1K9luP4Q1/4xfWxu', 'F', '17670073085', '易磊_6632@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '瘢痕治疗，伤口修复，皮肤肿瘤与黑痣，整形，灼伤与外伤治疗。', 40, 'active');

-- 医生：冯少清
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯少清', '$2b$12$8OlWFdR7i/HTmPI8JX.W5OclpWvPWumQ7zrNjuRQuSjVI.qXDC7OK', 'F', '18690975123', '冯少清_6419@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '副主任医师', '特长：烧创伤瘢痕综合治疗、乳房腹壁整形、眼整形', 40, 'active');

-- 医生：穆雄铮
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('穆雄铮', '$2b$12$sJ2gEXxmeexB8.FPKjHmnuBp1A5ezgNBIdQUizG0pVZ4sMWcz7hY6', 'F', '18587205269', '穆雄铮_5438@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '各类面部轮廓整形及颅颌面畸形疾病。', 50, 'active');

-- 医生：林炜栋
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('林炜栋', '$2b$12$KZoI5qZHkg7D1B9oLzzL/e2/hcYqMGZZGcqfHlOTjWKxNqelMTB6e', 'F', '15347538338', '林炜栋_2976@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='烧伤整形科' LIMIT 1), '主任医师', '各类创面修复、疤痕防治及慢性难愈创面诊治', 50, 'active');

-- 二级科室：普胸外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '普胸外科', '圣心楼', 2, '普胸外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '325', '圣心楼', 2, '普胸外科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：陈中元
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈中元', '$2b$12$1OPzog/5sgGHgEOZu89hOONx1b3cb/030m.oep.sEq1p6kzyJuaNq', 'M', '14332341238', '陈中元_8933@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '主任医师', '心脏，食管、贲门肿瘤、肺部，纵隔肿瘤', 50, 'active');

-- 医生：任健
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('任健', '$2b$12$IXyXtLDbVuK6H9UK9zeuiedDqQGW4RWbk4YZtNISn./9LOgOpKFg.', 'F', '18733064117', '任健_9595@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '胸腔镜微创手术，食管肿瘤、肺肿瘤 、纵膈肿瘤、手汗症', 40, 'active');

-- 医生：杭钧彪
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杭钧彪', '$2b$12$tPfhxzaxb3EKOz1NSlyx/eqmSeJMooVLOw1OGPDCfqRIzxwCGVs.S', 'F', '13994027897', '杭钧彪_4180@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '主任医师', '胸外科微创手术、食管贲门肿瘤手术、肺癌综合治疗、纵膈肿瘤及胸部疑难疾病诊治', 50, 'active');

-- 医生：车嘉铭  
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('车嘉铭  ', '$2b$12$CPVl2n90siDiTpwb/rXuwecRLGnrHfmxmSpw.KMr7M0dag0KpNlDy', 'F', '14487493096', '车嘉铭  _3939@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺肿瘤、肺小结节、食管癌、贲门癌、纵膈肿瘤等的外科微创治疗', 40, 'active');

-- 医生：朱良纲
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱良纲', '$2b$12$aFVzErz0NfX7LjHEcuwLLedA2MXfm4Ix4VgyszWuhj/xJiokc8n86', 'F', '13860229059', '朱良纲_9751@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '食管、肺、纵隔疾病、冠心病、先心、瓣膜病', 40, 'active');

-- 医生：周翔
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周翔', '$2b$12$VykAtI26jjxL.xC.l1ryue1v0/Z2YfrIzs9p5.P6pQtn2ttJbyWXq', 'M', '15148872110', '周翔_1893@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '食管、肺部、纵膈肿瘤的手术及胸腔镜诊疗', 40, 'active');

-- 医生：杨孝清  
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨孝清  ', '$2b$12$Mvb9Vk2Y7HIqVdyGe9jbsew2bgu06tOUPqWD4Jmn57nCKTR2b3vYy', 'F', '18235591568', '杨孝清  _9042@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '食道肿瘤、肺肿瘤、纵膈肿瘤的外科手术及综合治疗。普胸外科疑难疾病诊疗', 40, 'active');

-- 医生：项捷
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('项捷', '$2b$12$FY6QKbNvjoCkVXIIyKej.e3r2NnVJGz4ACSaxf72Ch9i3oqSHTeBu', 'M', '19113169872', '项捷_5658@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺、食管、纵隔肿瘤等常见胸部疾病以手术为主的综合治疗，熟练开展包括胸腔镜食管癌根治术，单孔胸腔镜肺癌根治术剑突下纵隔肿瘤切除术。巨大纵隔肿瘤切除术及达芬奇机器人在内的各类胸外科手术。', 40, 'active');

-- 医生：陈凯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈凯', '$2b$12$a1qu2IQd75iDQq94HbWAcOvHBpa7ipQkptveOD.x1dFkZ0DX335a.', 'F', '16572949775', '陈凯_6582@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '微创治疗纵隔、肺、食管等胸部良恶性肿瘤。', 40, 'active');

-- 医生：杜海磊
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杜海磊', '$2b$12$tEtKLKlzCiMqoisA4y5rPOY6StVAqNWFhoK1b9b.LuAno1qQPTqJu', 'M', '13371096600', '杜海磊_8827@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺结节、肺癌、食管癌、纵隔肿瘤、手汗症、气胸等微创治疗。', 40, 'active');

-- 医生：韩丁培
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('韩丁培', '$2b$12$RqR14s9LgM.Tp173YSJ2GurZv.Y3uhPo//5jJLKeANVEiYykQsmYy', 'M', '19170161976', '韩丁培_7565@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺结节、磁导航定位、肺癌、食管贲门癌、胸腺、纵膈肿瘤微创外科治疗。', 40, 'active');

-- 医生：李鹤成
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李鹤成', '$2b$12$J43phFqnoTTYfRCuNbIDF.L0sX8ExUI/9pM3PrXTVOp1dthXjg.m2', 'F', '13719571745', '李鹤成_1878@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '主任医师', '肺癌、食管癌、纵隔病变、胸膜间皮瘤等以微创手术为主的综合治疗；肺移植外科治疗。', 50, 'active');

-- 医生：张亚杰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张亚杰', '$2b$12$DacOEVBtsPy/ca0/FDh8QekZG6qufr0fi3wi4R430lnjnz69bIfvW', 'M', '14970957171', '张亚杰_5978@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '胸部肿瘤机器人、胸腔镜等微创手术及综合治疗。', 40, 'active');

-- 医生：金润森
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('金润森', '$2b$12$NR.ncvRM5h1fGOu.X.feFOnSJIpG6NeHXNkJZ2RpKJuqA.BtlAqFC', 'M', '14227186396', '金润森_7818@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺小结节的鉴别诊断，肺癌、食管癌、纵隔疾病微创外科治疗。', 40, 'active');

-- 医生：陈学瑜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈学瑜', '$2b$12$VkmMCiJ1laxAppaFJYjG1./Zi/6lBoSb9WgS.LS5BVYPZTao7JiOq', 'M', '19661107986', '陈学瑜_7232@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='普胸外科' LIMIT 1), '副主任医师', '肺部良恶性病变、磨玻璃结节的微创手术治疗。', 40, 'active');

-- 一级科室：儿科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '儿科', '圣心楼', 4, '儿科专注于儿童健康成长，提供儿童常见病、多发病的诊疗服务，医护人员经验丰富，深受患儿家长信赖。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '448', '圣心楼', 4, '儿科主诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 二级科室：小儿内科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '小儿内科', '圣心楼', 5, '小儿内科是我院重点科室之一，拥有先进的医疗设备和经验丰富的医疗团队，致力于为患者提供优质的医疗服务。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '449', '圣心楼', 5, '小儿内科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：李卫
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李卫', '$2b$12$4BdBl/QLiqzKV.9IVmtNGuF4rsV8KixgaBGpaXyytOR2k5/Pyq2YW', 'M', '17894664845', '李卫_2625@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童血液及恶性肿瘤疾病的临床诊治、肝脾淋巴结肿大、儿童感染性疾病诊治', 50, 'active');

-- 医生：许春娣
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('许春娣', '$2b$12$DlfZxKSLL.nbNCZrAoZz6.0wWxEWqemCzzwolBLtrH31bTo4e.5zC', 'M', '18326586581', '许春娣_5335@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童消化道疾病的诊治（便血、腹泻、腹痛、呕吐等疑难杂症）', 50, 'active');

-- 医生：董治亚
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('董治亚', '$2b$12$2qx.a89oyN/LeJqra.EtUu29LB6D9VqFMTwe6PA6xCviYrZADbf/2', 'M', '14357553274', '董治亚_3847@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童生长发育相关疾病诊疗，包括矮小症、早发育、性发育异常、肥胖症；儿童糖尿病、甲状腺疾病及遗传代谢病。', 50, 'active');

-- 医生：陆文丽
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陆文丽', '$2b$12$/5rOIW9zbYoI2QecUQCzM.m89Y6fH0gnlRsVo5ZQr3CEluC7XjFtq', 'M', '14102873038', '陆文丽_7693@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童生长发育评估（乳房早发育、性早熟、矮小、肥胖等）及甲状腺疾病、先天性肾上腺皮质增生症、McCune-Albrght综合征等。', 50, 'active');

-- 医生：肖园
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('肖园', '$2b$12$7UoPcDPsf0d5McJXpmP4b.HbYO4E40pSxby/jIWHSR0C.cltWdz5e', 'F', '18737599912', '肖园_8699@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童内分泌疾病（矮小、性发育、糖尿病、甲状腺疾病等）、儿童胃肠病（慢性腹泻、炎症性肠病）和消化内镜诊治。', 50, 'active');

-- 医生：吴群
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('吴群', '$2b$12$GW26Gs8jhK9yyIIEfFX0qe/iu32fV44ViusLzwvSftegUiFnCYEDO', 'F', '13348545713', '吴群_5720@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童呼吸系统疾病，儿童哮喘，儿童过敏性鼻炎，婴儿湿疹，食物过敏。', 40, 'active');

-- 医生：余熠
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('余熠', '$2b$12$4BKyws9tp.W6nihrEWEDkOwXiY2Svh3XaZLopDvAl1HI6tXi8aXum', 'F', '18587512424', '余熠_2166@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿内科常见疾病诊治，擅长消化系统疾病。', 40, 'active');

-- 医生：王歆琼
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王歆琼', '$2b$12$viARSWfa4xu4ZL6R7NQue.b0O.39MG3ogo9Fd6DstrBG2p1kLXYDO', 'M', '15945923902', '王歆琼_4241@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童胃肠疾病（炎症性肠病、急慢性腹泻、慢性胃炎、过敏性肠炎等）及其它儿内科常见病的诊治', 40, 'active');

-- 医生：马晓宇
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('马晓宇', '$2b$12$28sjFNWQU/jYrv4F7rOyAOK9MHJmFAA3NQuFwNwMacxuIWPTW5XI.', 'F', '13684719405', '马晓宇_4683@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童内分泌遗传代谢，擅长矮小症、性早熟、糖尿病、肥胖、甲状腺疾病等儿童内分泌疾病的诊治。', 40, 'active');

-- 医生：曹玮
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('曹玮', '$2b$12$693IUClU3mXqpUyT2YWvG.iUT6u8r76OYR9Ua1eJhUzSYLPlfY39u', 'M', '15987588450', '曹玮_3330@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童生长发育评估（矮小、性早熟、肥胖）、甲状腺疾病及常见疾病。', 40, 'active');

-- 医生：倪继红
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('倪继红', '$2b$12$ctX4dkjJwjODqCKTioBkFOZboCAClJjpJuDiCKitHdZwqChCVbE3q', 'M', '13278158234', '倪继红_6039@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '儿童矮小症、性早熟和性腺发育低下、甲状腺疾病、先天性肾上皮质增生', 50, 'active');

-- 医生：赵建琴
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('赵建琴', '$2b$12$rX9.2pK4t.olmdxHXJxCpuA3LKSxBElGfMW42f9zF.LXTd0MtXzHG', 'F', '16233523720', '赵建琴_8679@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '主任医师', '小儿常见多发病，擅长小儿呼吸道疾病、哮喘等疑难杂症', 50, 'active');

-- 医生：邱定众
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('邱定众', '$2b$12$WUS7pbev0d2.V0AFSf4tlOUJoOKhGxPt.MR2smCazSEvqRRSnKrx.', 'F', '18532190231', '邱定众_5460@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '小儿感染性疾病、心血管与呼吸道疾病、自身免疫性疾病及疑难杂症', 40, 'active');

-- 医生：于意
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('于意', '$2b$12$xSM7LOcp/qv1aLt2EDZGIutMTCdeaJJE2sFmDIlfzs4Q2g9OTyTum', 'F', '16186373782', '于意_1653@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿内科' LIMIT 1), '副主任医师', '儿童内分泌疾病(矮小、肥胖、性早熟等)及儿内科常见病', 40, 'active');

-- 二级科室：小儿外科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '小儿外科', '圣心楼', 4, '小儿外科配备先进的手术设施和专业的医护团队，专注于各类外科疾病的诊疗，手术成功率高。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '387', '圣心楼', 4, '小儿外科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：沈丽萍
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈丽萍', '$2b$12$nSY36yueGT2/hyavrPB8wud0CY1SMBI3rMASNFCHehkdzCaAu.5Ma', 'F', '13198061499', '沈丽萍_4750@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '消化、新生儿、腹腔镜', 40, 'active');

-- 医生：陈建雯
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈建雯', '$2b$12$hPA431pAVnuodsibX1zKyuwXXDVGrVHpcfk.ye1FOgxD1q/fZ.HMi', 'M', '19821856853', '陈建雯_5415@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '小儿的脐茸、包茎、各种肿瘤、儿童骨折及先天性骨性病变', 40, 'active');

-- 医生：周曙光
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('周曙光', '$2b$12$NWNLWtmn.JDVR2I3cqYTS.f55UgJRnyXjiN8dE9L/WKWjv/I0lmGS', 'M', '19912037475', '周曙光_3870@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '小儿骨科、小儿泌尿外科、小儿脐茸诊治', 40, 'active');

-- 医生：刘德鸿
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘德鸿', '$2b$12$6cnOf8H1sGvwWbSO9I.Wv.6hVljAq5/todPqkJco6bz7NguMDGvz2', 'F', '17799552991', '刘德鸿_8245@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='小儿外科' LIMIT 1), '副主任医师', '小儿泌尿外科各类疾病的诊治', 40, 'active');

-- 一级科室：妇产科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '妇产科', '圣心楼', 3, '妇产科为广大女性提供全方位的医疗保健服务，包括孕期检查、分娩、妇科疾病诊疗等。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '284', '圣心楼', 3, '妇产科主诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 二级科室：妇科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '妇科', '圣心楼', 4, '妇科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '421', '圣心楼', 4, '妇科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：刘延
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘延', '$2b$12$A/NXXcm1Qp/TYvrrb.pDXeoQuQka3INCA/EPM17cLwlZY/2TJ0L2S', 'F', '15538460431', '刘延_6460@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '高危产科、月经异常等妇科疾病、围绝经期综合症、不孕症', 50, 'active');

-- 医生：黄健
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('黄健', '$2b$12$6TLmSsWT00Ao0v1pL4j2.OdjIpNrBaFEjln9oU0bHWsqOcY3gM9K.', 'F', '18212313264', '黄健_3634@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '慢性宫颈炎症、子宫肌瘤、月经失调、不孕症', 40, 'active');

-- 医生：滕宗荣
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('滕宗荣', '$2b$12$yK5sF1zzzw3ovsh8GikqA.jAu7Z6te9eKd.z3fF7om7C4b7mcceWG', 'F', '16844826434', '滕宗荣_9117@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科内分泌疾病、妇科炎症', 40, 'active');

-- 医生：刘淳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘淳', '$2b$12$NXUQ9Z9J/VNcVQXC2nEWVuJkAQxRT5t7FdsVxQBJMxhKKgKtXEdHe', 'F', '18529982328', '刘淳_1601@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科炎症、妇科内分泌疾病 ', 40, 'active');

-- 医生：钟慧萍
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('钟慧萍', '$2b$12$wtEwW14NPtfGieZESQXbuuK3/ROV/Eml1kfJ3uXLyPID5rcexThNu', 'F', '13437710224', '钟慧萍_5135@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '妇科宫腔镜、腹腔镜手术，包括宫颈疾病、子宫肌瘤，卵巢囊肿的治疗。妊娠合并甲状腺疾病（甲亢、甲减、桥本甲状腺炎等），妊娠期糖尿病，妊娠合并甲旁亢等内分泌疾病的诊治，及产科危重疑难杂症的诊治。', 50, 'active');

-- 医生：薛梅
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('薛梅', '$2b$12$XXhZgq87B.fR6e4aeFZb6eW7aJPdgXqXXQFUas1iSiEwSAHOPH1BK', 'F', '13929517022', '薛梅_7622@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科肿瘤的综合治疗\月经失调、更年期综合征', 40, 'active');

-- 医生：沈立翡
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈立翡', '$2b$12$/puz03v5poqk4zMtGg3oXOBqOUuX.aYG1wincl1o9RO/oBHrRxza6', 'M', '18682605435', '沈立翡_8569@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '子宫肌瘤、子宫内膜异位症、卵巢良恶性肿瘤、子宫颈癌及子宫内膜癌的腹腔镜手术', 40, 'active');

-- 医生：龙雯晴
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('龙雯晴', '$2b$12$0woHO9pJLYm.kjOw76GYuOnsMgsl.7W9yA5g7rrw8FXAuIFv88UcK', 'F', '13301415220', '龙雯晴_9494@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '女性不孕症、子宫内膜异位症、子宫肌瘤、卵巢囊肿等妇科良恶性肿瘤的宫腹腔镜治疗。', 50, 'active');

-- 医生：王敏敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('王敏敏', '$2b$12$JrbmDIfSNtYjmVPkK13zROL.dEpGTQzPzcEe3dT.TiMBO8hXrz3M2', 'F', '17912224654', '王敏敏_9167@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '绝经综合症、功能失调性子宫出血、妇科炎症', 40, 'active');

-- 医生：沈育红
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈育红', '$2b$12$HQSygVYa5PORfhGP3DqiFuy66m7NKd2.hQ8J.xGslayJG/N8AsEPa', 'F', '19155417727', '沈育红_4335@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科常见病如子宫内膜异位症、子宫肌瘤、卵巢肿瘤等宫腹腔镜手术治疗以及妇科恶性肿瘤的诊治。', 40, 'active');

-- 医生：朱岚
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('朱岚', '$2b$12$PNBpTmcX.rTXqi0bAfKZVObQvAg/hJm2A9ippDbV24s6ddlMnLVo2', 'F', '17240674381', '朱岚_5719@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科疾病、妇科肿瘤、宫颈疾病的诊治，内窥镜治疗', 40, 'active');

-- 医生：陈晨
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈晨', '$2b$12$bXYSX1vCRKFMsNpd7LprGONki8ZShKQjf3fuocJxzITwV5OLCiMNO', 'F', '18620455331', '陈晨_2989@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '月经异常包括多囊卵巢综合征、不孕症、性腺及生长发育异常，卵巢早衰，围绝经期综合征。', 40, 'active');

-- 医生：蔡蕾
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('蔡蕾', '$2b$12$QJsR5H78GBlmEo68RtCvYOAUoxg/JAaOSwvibO4AA5/zFIo7yTPhu', 'M', '18753770973', '蔡蕾_4920@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '宫颈疾病诊治，妇科肿瘤宫腹腔镜手术', 40, 'active');

-- 医生：沈健
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('沈健', '$2b$12$K0aFBn7MKv8t6TjcsCyYee4A0sOivNB14rIyO9O5fDb23mR17XRvO', 'M', '15691458887', '沈健_1224@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科内分泌、更年期综合症、不孕不育、生殖道感染', 40, 'active');

-- 医生：杨辰敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('杨辰敏', '$2b$12$Hfvy8XBsBG8cBjnsoKfcZu/9IGvyCG1dJp3wWprZuDFP5Jr0LBJUG', 'F', '13341270488', '杨辰敏_2858@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '阴道镜下宫颈疾病的诊治，宫腔镜下治疗内膜息肉、粘膜下肌瘤、子宫纵隔，腹腔镜下治疗不孕症、子宫肌瘤、内异症、卵巢肿瘤及子宫切除术等。', 40, 'active');

-- 医生：刘华
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('刘华', '$2b$12$etQgba8rWDkKLscMVpt6rutXxhtJdG.TCnpbD58eQneCWiAuFWgNe', 'F', '13795472980', '刘华_3522@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '内膜异位症，妇科良恶性肿瘤，腹腔镜手术，宫腔镜手术', 50, 'active');

-- 医生：焦海宁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('焦海宁', '$2b$12$QJLrk/QHVl4Cd4fSn4B1BeFqv3nQ8m7JmlnTRD0fi25hGRfqstroa', 'F', '18413383963', '焦海宁_9337@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '宫腔占位，子宫肌瘤，卵巢囊肿，子宫内膜异位症等良性肿瘤的诊治；产科危重疾病的综合治疗及妊娠合并糖尿病、高血压、甲状腺疾病和自身免疫性疾病的诊治。', 40, 'active');

-- 医生：陈慧
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈慧', '$2b$12$5aNtmIHCZOksp.5jNN0/Yu3hl65ZS537oEddjePFzhMbh9TJWrHdK', 'F', '16996278008', '陈慧_8905@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '各类妇产科疑难超声诊断：如妇科肿瘤（尤其是卵巢肿瘤）、子宫内膜异位症、女性生殖系统发育异常及高危妊娠产前超声诊断等。', 50, 'active');

-- 医生：许啸声
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('许啸声', '$2b$12$rMU51T4k/K5m4nJUxdAfN.BZizcDrOZ8.Yo3p0iiks1jUimTMH6gu', 'F', '14590470737', '许啸声_3369@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '子宫肌瘤、卵巢囊肿等妇科常见良性肿瘤的微创手术治疗，妇科恶性肿瘤的综合治疗，HPV感染防治及宫颈癌前病变的物理治疗。', 40, 'active');

-- 医生：冯炜炜
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('冯炜炜', '$2b$12$S8ERKkSI9Aqw1vgVOm7dBuomUZUXzWr7Io/u0c.b.g5G9K1qBZ4xG', 'F', '14743576792', '冯炜炜_9327@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '精通妇科良恶性肿瘤，子宫内膜异位症的腹腔镜微创手术，机器人手术，开腹肿瘤减灭手术等；以及恶性肿瘤的综合治疗。', 50, 'active');

-- 医生：程忠平
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('程忠平', '$2b$12$tu91AAyKLJxngvBY///cXedsEdq1pgwYrAa2Hzv4ZR1feZkO.Pvsa', 'M', '19174958005', '程忠平_5526@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '主任医师', '腺肌症/肌瘤(微创保宫)、妇科肿瘤微创与免疫治疗。', 50, 'active');

-- 医生：李菁
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李菁', '$2b$12$DRp/JV9ABPKClTUKH8aVquYhTCHt0/WBsHafOCIXYg1OqABGo7TEe', 'F', '15945805127', '李菁_9318@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='妇科' LIMIT 1), '副主任医师', '妇科宫腹腔镜手术，包括内膜和宫颈疾病的诊治。', 40, 'active');

-- 一级科室：医学检验科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '医学检验科', '圣心楼', 3, '医学检验科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '146', '圣心楼', 3, '医学检验科主诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 二级科室：核医学科
INSERT IGNORE INTO departments (parent_dept_id, dept_name, building, floor, description) VALUES (NULL, '核医学科', '圣心楼', 3, '核医学科拥有专业的医疗团队和先进的医疗设备，为患者提供高质量的医疗服务，深受患者好评。');
INSERT INTO consultation_rooms (dept_id, room_name, building, floor, description) VALUES (LAST_INSERT_ID(), '584', '圣心楼', 3, '核医学科诊室');
UPDATE departments SET room = LAST_INSERT_ID() WHERE dept_id = LAST_INSERT_ID();

-- 医生：李彪
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李彪', '$2b$12$NGFSEmRL2itY1XhpNbH3rOc5qXHG8TeBo60m.pXFC3XqoM79/gPHG', 'F', '19259553112', '李彪_8316@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', 'PET/CT肿瘤临床诊断，甲亢、甲癌核素治疗。', 50, 'active');

-- 医生：李培勇
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('李培勇', '$2b$12$71P.xf3VVUBl2xO9ND6gLOwxsoO8mQW5OQGaXABhu6Ez1ZIFWNYjG', 'F', '15456954682', '李培勇_9903@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '甲亢和甲癌的碘-131治疗，桥本甲状腺炎的内科治疗', 50, 'active');

-- 医生：管樑
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('管樑', '$2b$12$ET6qPZbf6PwL1Np/jlu7QuyW56FAwHGyiyxaoa2WymQozz.Zx28M2', 'F', '16445505799', '管樑_4090@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '碘131治疗甲亢，诊断和治疗甲减（妊娠期）、桥本、甲状腺结节等甲状腺良性疾病。', 50, 'active');

-- 医生：陈刚
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('陈刚', '$2b$12$3bMOpqOPZI9izHXjo3cV/erYrqCm9k0piAiklFxKwuH5fM7ojpqUG', 'M', '17511211819', '陈刚_4826@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '骨转移骨痛，甲亢、甲状腺癌131I治疗，甲减，甲状腺炎等甲状腺相关疾病。肿瘤心理支持治疗。', 40, 'active');

-- 医生：江旭峰
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('江旭峰', '$2b$12$1.gLx1hSRLOFw3zvQgKCA.CwB.7k/VBgCYWrsyNo0n4rZ4RjRJb6q', 'F', '13441639230', '江旭峰_8749@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '擅长甲状腺疾病（甲亢、甲状腺癌等）、肿瘤骨转移核素治疗； 骨质疏松诊治', 40, 'active');

-- 医生：张一帆
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张一帆', '$2b$12$YK8yg8R.fuSkpvjrhaTsFOoc7FStXbLAWsOZbogpBQq.Z4emIGX/W', 'F', '16812630453', '张一帆_3492@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '甲状腺癌碘-131治疗、甲亢碘-131治疗等', 50, 'active');

-- 医生：张敏
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张敏', '$2b$12$dGicyMcXqT2BE1rAtKw80uTikkoejB4e0GXcRqMemiuLDf44hlUPO', 'F', '13235555899', '张敏_9229@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '主任医师', '甲状腺功能亢进和甲状腺癌碘-131治疗，以及PET/MR，PET/CT，SPECT/CT多模态影像评估肿瘤、神经及心血管疾病。', 50, 'active');

-- 医生：胡佳佳
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('胡佳佳', '$2b$12$kTyENGStt6x2CpawAw8c1OwsA46RFPb46QGrZWaewBU1U9gG.Olpi', 'F', '19207765664', '胡佳佳_8213@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '肿瘤分子影像诊断及甲亢、甲癌、骨痛核素治疗。', 40, 'active');

-- 医生：张淼
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('张淼', '$2b$12$dslAJYUu4zoQIy4NzO/vOuOJ6a1abY1Bwu0edD1PZ2O6vydGJfDMa', 'M', '17590622789', '张淼_1251@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '甲亢、甲状腺癌碘131治疗，肿瘤及记忆力减退PET/CT、PET/MR多模态影像评估', 40, 'active');

-- 医生：郭睿
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('郭睿', '$2b$12$zC0CiYmCiuClhVVYlvDyXOebW/bKmyhjOBl1Yupy7FWTAyOWmgj/G', 'M', '16803066091', '郭睿_3529@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '甲状腺疾病(甲亢、甲癌等）核素治疗，淋巴瘤PET评估。', 40, 'active');

-- 医生：席云
INSERT IGNORE INTO users (username, password, gender, phone, email, role_type, status) VALUES ('席云', '$2b$12$DHc/FAvDpjYmHNDBwiAVIeoO8H/VthsSO4.4U.bi1DnykEOqg6cei', 'M', '16939156387', '席云_5342@hospital.com', 'doctor', 'verified');
INSERT INTO doctors (user_id, dept_id, title, bio, appointment_quota, status) VALUES (LAST_INSERT_ID(), (SELECT dept_id FROM departments WHERE dept_name='核医学科' LIMIT 1), '副主任医师', '甲亢碘-131治疗、甲癌碘-131治疗、肿瘤多模态分子影像评估。', 40, 'active');
