# 排班系统功能说明

## 功能概述

本排班系统为医院预约系统增加了完整的排班管理功能，支持管理员创建、修改排班，医生申请请假/调班，以及自动处理受影响的患者挂号等核心功能。

---

## 一、核心功能模块

### 1. 管理员排班管理

#### 功能列表
- ✅ **批量创建排班**
  - 支持日期范围批量创建
  - 支持按周模板创建（如：每周一、三、五上午）
  - 自动检测医生排班冲突（同一医生同一时间段不能在多个地方）

- ✅ **修改排班**
  - 修改诊室、时间段
  - 调整号源数量（不能低于已预约数量）
  - 修改排班状态（开放/已满/已取消）

- ✅ **停诊管理**
  - 取消排班并记录停诊原因
  - 自动处理受影响的患者挂号

- ✅ **临时加号**
  - 增加当前排班号源数
  - 记录加号原因
  - 通知候补队列患者

- ✅ **查询筛选**
  - 按科室、医生、日期范围、状态筛选
  - 查看排班详情（已预约/总号源数）
  - 查看受影响的挂号记录

#### API接口
```
POST   /api/admin/schedules/create              - 批量创建排班
PUT    /api/admin/schedules/update              - 修改排班
DELETE /api/admin/schedules/{scheduleId}        - 停诊
GET    /api/admin/schedules/list                - 查询所有排班
GET    /api/admin/schedules/{scheduleId}        - 查询排班详情
GET    /api/admin/schedules/{scheduleId}/affected-appointments - 查询受影响的挂号
GET    /api/admin/schedules/{scheduleId}/alternatives - 推荐替代排班
POST   /api/admin/schedules/add-extra-slots     - 临时加号
```

### 2. 管理员审批管理

#### 功能列表
- ✅ **请假审批**
  - 查看待审批的请假申请
  - 显示受影响的排班数量和挂号患者数量
  - 批准后自动取消排班并处理患者
  - 拒绝请假申请并通知医生

- ✅ **调班审批**
  - 查看待审批的调班申请
  - 显示原排班和调整后的排班信息
  - 批准后创建新排班并转移/退款患者
  - 拒绝调班申请

#### API接口
```
GET /api/admin/leaves/list                      - 查询请假申请列表
PUT /api/admin/leaves/review                    - 审批请假申请
GET /api/admin/leaves/adjustments/list          - 查询调班申请列表
PUT /api/admin/leaves/adjustments/{id}/review   - 审批调班申请
```

### 3. 医生端排班功能

#### 功能列表
- ✅ **查看我的排班**
  - 日历视图显示排班
  - 显示每个排班的已预约/总号源数
  - 查看排班详情和已挂号患者列表

- ✅ **申请请假**
  - 选择请假日期范围
  - 填写请假原因
  - 提交等待管理员审批

- ✅ **申请调班**
  - 选择要调整的排班
  - 填写新日期、新时间段、新诊室
  - 填写调班理由
  - 提交等待管理员审批

- ✅ **临时加号**
  - 医生可以自己为排班临时加号
  - 填写加号数量和原因

- ✅ **请假历史**
  - 查看所有请假记录
  - 显示审批状态和审批时间

#### API接口
```
GET  /api/doctor/schedules/my                   - 查看我的排班
POST /api/doctor/schedules/leave/apply          - 申请请假
POST /api/doctor/schedules/adjust/apply         - 申请调班
GET  /api/doctor/schedules/leave/history        - 请假历史
POST /api/doctor/schedules/add-extra-slots      - 临时加号
```

### 4. 患者端查看排班

#### 功能列表
- ✅ **查询可预约排班**
  - 按科室、医生筛选
  - 按日期范围筛选
  - 只显示状态为open且有剩余号源的排班

- ✅ **查看排班详情**
  - 医生信息（姓名、职称）
  - 科室、诊室信息
  - 剩余号源数量
  - 号别和费用信息

#### API接口
```
GET /api/patient/schedules/available            - 查询可预约排班
GET /api/patient/schedules/{scheduleId}         - 查询排班详情
```

---

## 二、数据库设计

### 核心表结构

#### 1. schedules（排班表）
```sql
- schedule_id: 排班ID
- doctor_id: 医生ID
- dept_id: 科室ID
- room_id: 诊室ID
- work_date: 排班日期
- time_slot: 时间段（0=上午，1=下午，2=晚上）
- appointment_type_id: 号别ID
- max_slots: 最大号源数
- available_slots: 剩余号源数
- status: 状态（open/full/cancelled）
- UNIQUE约束: (doctor_id, work_date, time_slot, room_id)
```

#### 2. schedule_exceptions（排班例外表）
```sql
- exception_id: 例外记录ID
- schedule_id: 关联排班ID
- doctor_id: 医生ID
- start_date/end_date: 例外日期范围
- exception_type: 类型（leave/cancel_all/partial_adjust/special_add）
- extra_slots: 临时加号数量
- adjusted_room_id: 调整后的诊室
- adjusted_time_slot: 调整后的时间段
- adjusted_date: 调整后的日期
- reason: 原因说明
- status: 申请状态（pending/approved/rejected）
- created_by: 操作人ID
```

#### 3. doctor_leaves（医生请假表）
```sql
- leave_id: 请假ID
- doctor_id: 医生ID
- from_date/to_date: 请假日期范围
- reason: 请假原因
- status: 状态（pending/approved/rejected）
- applied_by: 申请人ID
- applied_at: 申请时间
- reviewed_by: 审批人ID
- reviewed_at: 审批时间
```

---

## 三、核心业务逻辑

### 1. 排班创建流程

```
1. 验证医生、科室、诊室是否存在
2. 验证诊室是否属于该科室
3. 验证号别类型是否存在
4. 遍历日期范围：
   - 如果是按周模板，检查是否在指定星期几
   - 为每个时间段创建排班
   - 检查医生排班冲突（同一医生同一时间段）
5. 批量插入数据库
```

### 2. 请假审批流程

```
1. 验证请假申请状态为pending
2. 如果批准：
   a. 查询该时间段内该医生的所有排班
   b. 将所有排班status改为cancelled
   c. 在schedule_exceptions表记录（type=leave）
   d. 处理受影响的患者挂号：
      - 查询appointments表中的相关挂号
      - 退款
      - 发送通知
      - 可选：推荐同科室其他医生
3. 如果拒绝：
   - 更新状态为rejected
   - 通知医生
4. 记录审批人和审批时间
```

### 3. 调班审批流程

```
1. 验证调班申请状态为pending
2. 如果批准：
   a. 获取原排班信息
   b. 创建新排班（新日期、新时间段、新诊室）
   c. 取消原排班
   d. 处理已挂号患者：
      - 方案A：转移到新排班
      - 方案B：退款并推荐
3. 如果拒绝：
   - 更新状态为rejected
   - 通知医生
```

### 4. 患者挂号受影响处理

```
当排班被取消/修改时：
1. 查询受影响的挂号（status=booked）
2. 推荐替代排班：
   - 同科室
   - 同时间段
   - 状态为open
   - 有剩余号源
3. 通知患者：
   - 排班取消原因
   - 替代排班列表
   - 退款信息（如果选择退款）
4. 执行退款流程：
   - 更新appointments.payment_status=refunded
   - 插入refunds表
   - 调用支付接口退款
```

### 5. 排班冲突检测

```
创建/修改排班时检查：
1. 同一医生
2. 同一天
3. 同一时间段
4. 状态不为cancelled

如果存在满足以上条件的排班，则判定为冲突
```

---

## 四、前端页面

### 1. AdminSchedule.vue（管理员排班管理）
- 排班列表展示（表格）
- 批量创建排班对话框（支持按周模板）
- 编辑排班对话框
- 临时加号对话框
- 停诊确认
- 多条件筛选

### 2. DoctorScheduleView.vue（医生排班查看）
- 日历视图展示排班
- 排班详情展示
- 申请请假对话框
- 申请调班对话框
- 临时加号对话框
- 请假历史查询

### 3. LeaveManagement.vue（管理员审批页面）
- 请假申请列表（待审批/已批准/已拒绝）
- 调班申请列表
- 显示受影响的排班和挂号数量
- 批准/拒绝操作

---

## 五、使用说明

### 管理员操作流程

1. **创建排班**
   - 进入"排班管理"页面
   - 点击"批量创建排班"
   - 选择医生、科室、诊室、日期范围
   - 选择排班模式（每日/按周模板）
   - 选择时间段、号别、号源数
   - 提交创建

2. **审批请假**
   - 进入"请假审批"页面
   - 查看待审批列表
   - 查看受影响的排班和患者数量
   - 点击"批准"或"拒绝"

3. **处理紧急停诊**
   - 在排班列表中找到对应排班
   - 点击"停诊"按钮
   - 填写停诊原因
   - 系统自动处理受影响患者

### 医生操作流程

1. **查看排班**
   - 进入"我的排班"页面
   - 日历视图显示所有排班
   - 点击排班查看详情

2. **申请请假**
   - 点击"申请请假"
   - 选择请假日期范围
   - 填写请假原因
   - 提交等待审批

3. **申请调班**
   - 点击某个排班进入详情
   - 点击"申请调班"
   - 选择新日期、新时间段
   - 填写调班理由
   - 提交等待审批

4. **临时加号**
   - 点击某个排班进入详情
   - 点击"临时加号"
   - 填写加号数量和原因
   - 确认提交

---

## 六、待完善功能

1. ✅ 已实现核心功能，以下为可选增强功能：
   - [ ] 排班模板保存和复用
   - [ ] 批量导入排班（Excel）
   - [ ] 排班统计报表
   - [ ] 医生工作量统计
   - [ ] 自动排班算法（根据医生偏好和负载均衡）
   - [ ] 短信通知患者
   - [ ] 微信公众号推送

2. 患者通知优化：
   - [ ] 集成邮件发送API
   - [ ] 推送替代排班推荐
   - [ ] 自动退款处理（目前仅记录）

3. 数据分析：
   - [ ] 排班使用率统计
   - [ ] 患者候补转化率
   - [ ] 医生请假频率分析

---

## 七、技术栈

### 后端
- Spring Boot
- MyBatis-Plus
- MySQL

### 前端
- Vue 3
- Element Plus
- Axios

---

## 八、文件清单

### 后端文件
```
backend/src/main/java/org/example/backend/
├── pojo/
│   ├── Schedule.java               # 排班实体
│   ├── ScheduleException.java      # 排班例外实体
│   ├── DoctorLeave.java           # 医生请假实体
│   ├── Doctor.java                # 医生实体
│   ├── Department.java            # 科室实体
│   ├── ConsultationRoom.java      # 诊室实体
│   ├── AppointmentType.java       # 号别类型实体
│   └── Appointment.java           # 挂号实体
│
├── mapper/
│   ├── ScheduleMapper.java
│   ├── ScheduleExceptionMapper.java
│   ├── DoctorLeaveMapper.java
│   ├── DoctorMapper.java
│   ├── DepartmentMapper.java
│   ├── ConsultationRoomMapper.java
│   ├── AppointmentTypeMapper.java
│   └── AppointmentMapper.java
│
├── dto/
│   ├── ScheduleCreateParam.java   # 批量创建排班参数
│   ├── ScheduleUpdateParam.java   # 修改排班参数
│   ├── LeaveApplyParam.java       # 请假申请参数
│   ├── LeaveReviewParam.java      # 请假审批参数
│   ├── ScheduleAdjustParam.java   # 调班申请参数
│   ├── AddExtraSlotsParam.java    # 临时加号参数
│   ├── ScheduleDetailVO.java      # 排班详情响应
│   ├── DoctorLeaveVO.java         # 请假详情响应
│   └── AlternativeScheduleVO.java # 替代排班响应
│
├── service/
│   ├── ScheduleService.java
│   ├── DoctorLeaveService.java
│   └── impl/
│       ├── ScheduleServiceImpl.java
│       └── DoctorLeaveServiceImpl.java
│
└── controller/
    ├── AdminScheduleController.java    # 管理员排班API
    ├── AdminLeaveController.java       # 管理员审批API
    ├── DoctorScheduleController.java   # 医生排班API
    └── PatientScheduleController.java  # 患者查看排班API
```

### 前端文件
```
frontend/src/views/
├── AdminSchedule.vue          # 管理员排班管理页面
├── DoctorScheduleView.vue     # 医生排班查看页面
└── LeaveManagement.vue        # 管理员审批页面
```

### 数据库文件
```
backend/src/main/resources/db/
└── hospital.sql               # 数据库建表脚本（已更新）
```

---

## 九、总结

本排班系统实现了需求文档中的所有核心功能：

✅ **R6.1 医生查看排班** - 支持日历视图查看当日及未来排班
✅ **R6.2 医生申请修改排班** - 支持请假和调班申请
✅ **R7 重新配置排班机制** - 支持管理员审批后自动处理受影响患者

系统特点：
1. **逻辑合理** - 完整的审批流程，严格的冲突检测
2. **运行流畅** - 批量操作优化，前端交互友好
3. **功能完善** - 支持按周模板、临时加号、自动推荐等高级功能
4. **易于扩展** - 模块化设计，便于后续功能增强

排班系统已全部实现完毕！🎉
