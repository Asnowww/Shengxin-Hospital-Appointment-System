# 候补功能使用说明

## 功能概述

候补功能允许患者在目标科室或医生的号源已满时进行排队候补。当有号源释放时，系统将按优先级和申请时间自动为患者创建预约。

## 后端接口

### 1. 创建候补
- **接口**: `POST /api/waitlist/create`
- **需要认证**: 是（需要token）
- **参数**:
  ```json
  {
    "scheduleId": 1,        // 排班ID（必填）
    "priority": 0,          // 优先级：0-普通，1-紧急，2-非常紧急（选填，默认0）
    "notes": "备注说明"     // 备注（选填）
  }
  ```
- **返回**: 候补记录

### 2. 取消候补
- **接口**: `POST /api/waitlist/cancel/{waitId}`
- **需要认证**: 是（需要token）
- **参数**: 路径参数 `waitId`（候补ID）
- **返回**: 成功或失败消息

### 3. 获取我的候补详细列表
- **接口**: `GET /api/waitlist/my-detail`
- **需要认证**: 是（需要token）
- **返回**: 候补详细列表，包含排班信息和队列统计

### 4. 检查排班候补情况
- **接口**: `GET /api/waitlist/check/{scheduleId}`
- **需要认证**: 否
- **参数**: 路径参数 `scheduleId`（排班ID）
- **返回**: 候补统计信息（总人数、高优先级人数）

### 5. 查询已满排班（可候补）
- **接口**: `GET /api/waitlist/fully-booked-schedules`
- **需要认证**: 否
- **查询参数**:
  - `deptId`: 科室ID（可选）
  - `doctorId`: 医生ID（可选）
  - `startDate`: 开始日期（可选，默认今天）
  - `endDate`: 结束日期（可选，默认7天后）
  - `timeSlot`: 时间段（可选：0-上午，1-下午，2-晚上）
- **返回**: 已满排班列表

## 前端页面

### 1. 我的候补 (`/waitlist/my`)
**文件**: `frontend/src/views/MyWaitlist.vue`

**功能**:
- 查看所有候补记录
- 区分"候补中"和"历史记录"
- 查看候补详情（队列位置、总人数、优先级排名等）
- 取消候补
- 查看转正通知

**访问方式**:
```javascript
// 在Vue组件中
this.$router.push('/waitlist/my')

// 或直接访问
https://localhost:5173/waitlist/my
```

### 2. 可候补排班列表 (`/waitlist/fully-booked`)
**文件**: `frontend/src/views/FullyBookedSchedules.vue`

**功能**:
- 查看所有已满的排班
- 按科室、时间段、日期范围筛选
- 查看候补队列情况
- 申请候补（选择优先级、填写备注）

**访问方式**:
```javascript
// 在Vue组件中
this.$router.push('/waitlist/fully-booked')

// 或直接访问
https://localhost:5173/waitlist/fully-booked
```

### 3. 候补卡片组件
**文件**: `frontend/src/components/WaitlistCard.vue`

**用途**: 在我的候补页面中显示候补记录的卡片组件

## 使用流程

### 患者申请候补流程

1. **查找可候补排班**
   - 访问 `/waitlist/fully-booked` 页面
   - 使用筛选条件查找需要的排班
   - 查看排班详情和候补队列情况

2. **申请候补**
   - 点击"申请候补"按钮
   - 选择优先级（普通/紧急/非常紧急）
   - 填写备注说明（可选）
   - 确认申请

3. **查看候补状态**
   - 访问 `/waitlist/my` 页面
   - 查看"候补中"列表
   - 点击卡片查看详细信息（队列位置、优先级排名等）

4. **候补转正**
   - 有号源释放时，系统自动创建预约
   - 候补状态变为"已转正"
   - 可查看转正的预约ID
   - 前往"我的预约"页面查看预约详情

5. **取消候补**（可选）
   - 在"我的候补"页面点击候补记录
   - 点击"取消候补"按钮
   - 确认取消

## 候补规则

1. **排班验证**:
   - 排班必须处于"open"状态
   - 排班必须已满（availableSlots = 0）
   - 患者不能重复候补同一排班
   - 患者不能候补已预约的排班

2. **队列限制**:
   - 每个排班最多5人候补
   - 队列满时无法加入

3. **优先级规则**:
   - 优先级：0-普通，1-紧急，2-非常紧急
   - 转正顺序：优先级高者优先，同优先级按申请时间排序

4. **自动转正**:
   - 有号源释放时（取消预约/加号），系统自动处理候补队列
   - 按优先级和时间顺序依次转为正式预约
   - 转正后发送通知

## 界面展示功能

### 我的候补页面特点
- ✅ 候补中/历史记录分类展示
- ✅ 显示队列位置和总人数
- ✅ 显示优先级和排名
- ✅ 转正通知和预约ID
- ✅ 取消候补功能
- ✅ 响应式设计，支持移动端

### 可候补排班页面特点
- ✅ 多条件筛选（科室、时间段、日期范围）
- ✅ 显示候补队列情况
- ✅ 候补申请表单（优先级选择、备注）
- ✅ 候补规则说明
- ✅ 响应式设计，支持移动端

## 测试建议

### 前端测试
1. 启动前端服务：
   ```bash
   cd frontend
   npm run dev
   ```

2. 访问测试页面：
   - https://localhost:5173/waitlist/my
   - https://localhost:5173/waitlist/fully-booked

3. 测试功能点：
   - [ ] 查看可候补排班列表
   - [ ] 筛选排班（科室、时间段、日期）
   - [ ] 申请候补（不同优先级）
   - [ ] 查看我的候补列表
   - [ ] 查看候补详情
   - [ ] 取消候补
   - [ ] 响应式布局（调整浏览器窗口大小）

### 后端测试
1. 启动后端服务
2. 使用Postman或其他工具测试API接口
3. 测试候补转正逻辑（取消预约后自动转正）

## 前后端对接验证

### API调用格式
```javascript
// 示例：创建候补
const response = await axios.post(
  '/api/waitlist/create',
  {
    scheduleId: 1,
    priority: 0,
    notes: '希望能够就诊'
  },
  {
    headers: { Authorization: `Bearer ${token}` }
  }
)
```

### 数据格式
确保前后端数据格式一致：
- 日期格式：`YYYY-MM-DD`
- 时间格式：`HH:mm:ss`
- 优先级：0/1/2（数字）
- 状态：'waiting'/'converted'/'cancelled'/'expired'

## 注意事项

1. **认证要求**: 大部分候补操作需要用户登录并提供token
2. **权限验证**: 后端会验证候补记录是否属于当前用户
3. **状态同步**: 候补转正后，前端应刷新数据显示最新状态
4. **错误处理**: 前端需要处理各种错误情况（队列已满、重复候补等）
5. **用户体验**: 使用loading状态、成功/失败提示等提升用户体验

## 文件清单

### 后端文件
- `backend/src/main/java/org/example/backend/controller/WaitlistController.java`
- `backend/src/main/java/org/example/backend/service/WaitlistService.java`
- `backend/src/main/java/org/example/backend/service/impl/WaitlistServiceImpl.java`
- `backend/src/main/java/org/example/backend/dto/WaitlistDetailVO.java`
- `backend/src/main/java/org/example/backend/dto/WaitlistCreateParam.java`
- `backend/src/main/java/org/example/backend/mapper/WaitlistMapper.java`

### 前端文件
- `frontend/src/views/MyWaitlist.vue` - 我的候补页面
- `frontend/src/views/FullyBookedSchedules.vue` - 可候补排班列表页面
- `frontend/src/components/WaitlistCard.vue` - 候补卡片组件
- `frontend/src/router/index.js` - 路由配置（已更新）

## 更新日志
- 2025-10-31: 创建候补功能前端界面，与后端接口对接完成
