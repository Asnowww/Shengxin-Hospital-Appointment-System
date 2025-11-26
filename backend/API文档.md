# 接口文档

## 1 号别管理

Base URL: `/api/admin/appointment-types`

---

### 1️⃣ 获取号别列表（支持模糊搜索）

**URL:** `/list`
**Method:** `GET`

**Query Parameters:**

| 参数      | 类型      | 必填 | 说明                          |
| ------- | ------- | -- | --------------------------- |
| keyword | String  | 否  | 模糊搜索关键字（typeName 或 typeKey） |

**示例请求:**

```
GET /api/admin/appointment-types/list?keyword=专家
```

**示例响应:**

```json
{
  "records": [
    {
      "appointmentTypeId": 1,
      "typeKey": "专家",
      "typeName": "专家号",
      "feeAmount": 300,
      "maxSlots": 10,
      "description": "资深医生号",
      "createdAt": "2025-11-13T10:00:00",
      "updatedAt": "2025-11-13T10:00:00"
    }
  ]
}
```

---

### 2️⃣ 获取单个号别详情

**URL:** `/{id}`
**Method:** `GET`

**Path Parameters:**

| 参数 | 类型      | 必填 | 说明    |
| -- | ------- | -- | ----- |
| id | Integer | 是  | 号别 ID |

**示例请求:**

```
GET /api/admin/appointment-types/1
```

**示例响应:**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "appointmentTypeId": 1,
    "typeKey": "专家",
    "typeName": "专家号",
    "feeAmount": 300,
    "maxSlots": 10,
    "description": "资深医生号",
    "createdAt": "2025-11-13T10:00:00",
    "updatedAt": "2025-11-13T10:00:00"
  }
}
```

---

### 3️⃣ 新增号别

**URL:** `/`
**Method:** `POST`

**Request Body (JSON):**

```json
{
  "typeKey": "专家",
  "typeName": "专家号",
  "feeAmount": 300,
  "maxSlots": 5,
  "description": "资深医生号"
}
```

**响应示例：**

* 成功：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "appointmentTypeId": 1,
    "typeKey": "专家",
    "typeName": "专家号",
    "feeAmount": 300,
    "maxSlots": 5,
    "description": "资深医生号",
    "createdAt": "2025-11-13T10:00:00",
    "updatedAt": "2025-11-13T10:00:00"
  }
}
```

* typeName 为空：

```json
{
  "code": 400,
  "message": "typeName 不能为空",
  "data": null
}
```

* 数据重复：

```json
{
  "code": 409,
  "message": "数据已存在，请勿重复添加",
  "data": null
}
```

---

### 4️⃣ 修改号别

**URL:** `/{id}`
**Method:** `PUT`

**Path Parameters:**

| 参数 | 类型      | 必填 | 说明    |
| -- | ------- | -- | ----- |
| id | Integer | 是  | 号别 ID |

**Request Body (JSON):**

```json
{
  "typeKey": "专家",
  "typeName": "专家号",
  "feeAmount": 300,
  "maxSlots": 20,
  "description": "资深医生号"
}
```

**响应示例：**

* 成功修改：

```json
{
  "code": 200,
  "message": "修改成功",
  "data": {
    "appointmentTypeId": 1,
    "typeKey": "专家",
    "typeName": "专家号",
    "feeAmount": 300,
    "maxSlots": 20,
    "description": "资深医生号",
    "createdAt": "2025-11-13T10:00:00",
    "updatedAt": "2025-11-13T10:05:00"
  }
}
```

* 数据未改变：

```json
{
  "code": 400,
  "message": "数据未改变",
  "data": {
    "appointmentTypeId": 1,
    "typeKey": "专家",
    "typeName": "专家号",
    "feeAmount": 300,
    "maxSlots": 10,
    "description": "资深医生号",
    "createdAt": "2025-11-13T10:00:00",
    "updatedAt": "2025-11-13T10:05:00"
  }
}
```

* 数据重复：

```json
{
  "code": 409,
  "message": "数据已存在（唯一约束冲突），请检查 typeKey 或 typeName",
  "data": null
}
```

---

### 5️⃣ 删除号别

**URL:** `/{id}`
**Method:** `DELETE`

**响应示例：**

* 成功：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

* 号别不存在：

```json
{
  "code": 404,
  "message": "号别不存在或删除失败",
  "data": null
}
```

---

### 6️⃣ 批量删除号别

**URL:** `/batch`
**Method:** `DELETE`

**Request Body (JSON):**

```json
[1,2,3]
```

**响应示例：**

* 成功：

```json
{
  "code": 200,
  "message": "批量删除成功",
  "data": null
}
```

* 删除列表为空：

```json
{
  "code": 400,
  "message": "删除列表不能为空",
  "data": null
}
```

* 部分号别不存在：

```json
{
  "code": 404,
  "message": "部分号别不存在或删除失败",
  "data": null
}
```
## 2 费用管理


**POST /api/fee/{appointmentId}**

### **接口描述**

根据挂号记录 ID 计算费用，并将计算结果写入数据库。费用计算依据挂号类别基础费用和患者身份折扣比例。

---

### **请求方式**

| 项目 | 内容                         |
| -- | -------------------------- |
| 方法 | `POST`                     |
| 路径 | `/api/fee/{appointmentId}` |

---

### **路径参数**

| 参数            | 类型   | 必填 | 描述      |
| ------------- | ---- | -- | ------- |
| appointmentId | Long | 是  | 挂号记录 ID |

---

### **请求示例**

```
POST /api/fee/1024
```

---

### **成功响应示例**

```json
{
  "code": 200,
  "message": "费用已计算",
  "data": 12.50
}
```

---

### **失败响应示例**

（挂号、患者或挂号类型不存在时均可能出现）

```json
{
  "code": 400,
  "message": "挂号记录不存在",
  "data": null
}
```

```json
{
  "code": 400,
  "message": "患者信息不存在",
  "data": null
}
```

```json
{
  "code": 400,
  "message": "挂号类别不存在",
  "data": null
}
```

---

### **返回字段说明**

| 字段      | 类型              | 说明              |
| ------- | --------------- | --------------- |
| code    | int             | 状态码，成功一般为 `200` |
| message | string          | 操作提示信息          |
| data    | BigDecimal/null | 计算后的费用金额        |

---

### **计费规则说明**

| 身份类型    | 折扣           |
| ------- | ------------ |
| student | 95% 费用（5%折扣） |
| teacher | 90% 费用       |
| staff   | 85% 费用       |
| 其他      | 无折扣          |

公式：

```
finalFee = baseFee × (1 - discountRate)
```

## 3  医生端患者管理 ##
1. 获取指定日期所有排班的患者列表：
URL: /api/doctor/patient/schedules-line
Method: GET
返回参考：
{
"code": 200,
"message": "获取成功",
"data": [
        {
            "scheduleId": 7219,
            "workDate": "2025-11-24",
            "timeSlot": 0,
            "timeSlotName": "上午",
            "deptName": "老年病科",
            "roomName": "101",
            "patients": [
                    {
                    "appointmentId": 6,
                    "patientId": 1,
                    "patientName": "patient1",
                    "gender": "F",
                    "age": 25,
                    "phone": "p",
                    "queueNumber": 1,
                    "waitingNumber": 0,
                    "appointmentStatus": "completed",
                    "bookingTime": 0,
                    "appointmentTypeName": "普通号"
                    }
            ]
        }
    ]
}

2. 患者已就诊：
URL：/api/doctor/patient/{appointmentId}/completed
Method：PUT
返回参考：
   {
   "code": 200,
   "message": "操作成功",
   "data": "已标记为已就诊"
   }

3. 患者过号：
URL：/api/doctor/patient/{appointmentId}/missed
Method：PUT
返回参考：
    {
    "code": 200,
    "message": "操作成功",
    "data": "已标记为过号"
    }
4. 患者历史就诊记录：
URL：/api/doctor/patient/{patientId}/history
Method：GET
返回参考：
{
"code": 200,
"message": "获取成功",
"data": [
        {
            "appointmentId": 7,
            "patientId": 1,
            "patientName": "patient1",
            "doctorName": "庞小芬",
            "doctorTitle": "主任医师",
            "doctorInfo": "老年代谢性疾病：原发及继发性骨质疏松症的诊断、治疗，老年人的骨关节炎，老年高血压、高脂血症，老年糖尿病等老年性疾病。",
            "deptName": "老年病科",
            "building": "圣心楼",
            "roomName": "101",
            "typeName": "普通号",
            "appointmentTime": "2025年11月24日 上午",
            "bookingTime": "2025-11-19 23:26:51",
            "status": "no_show",
            "feeFinal": 0.50,
            "remarks": null
        }
    ]
}

## 4 医生端排班管理

**接口统一前缀：**

```
/api/doctor/schedules
```

---

### 1. 查看我的排班

| 项目   | 内容                         |
| ---- | -------------------------- |
| 接口描述 | 查询医生某时间段内的排班信息             |
| 请求方式 | **GET**                    |
| URL  | `/api/doctor/schedules/my` |

#### 🔹请求参数

| 参数名       | 类型                 | 必填 | 说明         |
| --------- | ------------------ | -- | ---------- |
| doctorId  | Long               | ✔  | 医生ID       |
| startDate | Date(`yyyy-MM-dd`) | ✖  | 开始日期，默认为全部 |
| endDate   | Date(`yyyy-MM-dd`) | ✖  | 结束日期，默认为全部 |

#### 🔹请求示例

```
GET /api/doctor/schedules/my?doctorId=1&startDate=2025-11-20&endDate=2025-11-30
```

#### 🔹响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "scheduleId": 10,
      "doctorId": 1,
      "doctorName": "李医生",
      "deptName": "内科",
      "roomName": "诊室 105",
      "workDate": "2025-11-22",
      "timeSlot": 0,
      "timeSlotName": "上午",
      "appointmentTypeName": "普通号",
      "maxSlots": 20,
      "availableSlots": 5,
      "bookedSlots": 15,
      "status": "open"
    }
  ]
}
```


###  2. 申请请假

| 项目   | 内容                                  |
| ---- | ----------------------------------- |
| 接口描述 | 医生提交请假申请                            |
| 请求方式 | **POST**                            |
| URL  | `/api/doctor/schedules/leave/apply` |

#### 🔹请求体参数（JSON）

| 参数名      | 类型                 | 必填 | 说明     |
| -------- | ------------------ | -- | ------ |
| doctorId | Long               | ✔  | 医生ID   |
| fromDate | Date(`yyyy-MM-dd`) | ✔  | 请假开始时间 |
| toDate   | Date(`yyyy-MM-dd`) | ✔  | 请假结束时间 |
| reason   | String             | ✔  | 请假原因   |

#### 🔹请求示例

```json
{
  "doctorId": 1,
  "fromDate": "2025-11-21",
  "toDate": "2025-11-23",
  "reason": "身体不适休息"
}
```

#### 🔹响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```


### 3. 申请调班

| 项目   | 内容                                   |
| ---- | ------------------------------------ |
| 接口描述 | 提交排班调整申请                             |
| 请求方式 | **POST**                             |
| URL  | `/api/doctor/schedules/adjust/apply` |

#### 🔹请求体参数（JSON）

| 参数名              | 类型      | 必填 | 说明              |
| ---------------- | ------- | -- | --------------- |
| scheduleId       | Integer | ✔  | 原排班ID           |
| adjustedDate     | Date    | ✔  | 新排班日期           |
| adjustedTimeSlot | Integer | ✔  | 时间段：0上午，1下午，2晚上 |
| adjustedRoomId   | Integer | ✔  | 新诊室ID           |
| reason           | String  | ✔  | 调班理由            |
| appliedBy        | Long    | ✔  | 申请人             |

#### 🔹请求示例

```json
{
  "scheduleId": 10,
  "adjustedDate": "2025-11-25",
  "adjustedTimeSlot": 1,
  "adjustedRoomId": 3,
  "reason": "会议冲突",
  "appliedBy": 1
}
```

### 4. 查看请假历史

| 项目   | 内容                                    |
| ---- | ------------------------------------- |
| 接口描述 | 查询医生所有历史请假记录                          |
| 请求方式 | **GET**                               |
| URL  | `/api/doctor/schedules/leave/history` |

#### 🔹请求参数

| 参数名      | 类型   | 必填 | 说明   |
| -------- | ---- | -- | ---- |
| doctorId | Long | ✔  | 医生ID |

#### 🔹请求示例

```
GET /api/doctor/schedules/leave/history?doctorId=1
```

#### 🔹响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "leaveId": 5,
      "doctorId": 1,
      "fromDate": "2025-11-21",
      "toDate": "2025-11-23",
      "reason": "身体不适",
      "status": "pending",
      "appliedAt": "2025-11-19T10:22:00"
    }
  ]
}
```


### 5. 临时加号

| 项目   | 内容                                      |
| ---- | --------------------------------------- |
| 接口描述 | 为某排班增加号源数量                              |
| 请求方式 | **POST**                                |
| URL  | `/api/doctor/schedules/add-extra-slots` |

#### 🔹请求体参数（JSON）

| 参数名        | 类型      | 必填 | 说明   |
| ---------- | ------- | -- | ---- |
| scheduleId | Integer | ✔  | 排班ID |
| extraSlots | Integer | ✔  | 增加数量 |
| reason     | String  | ✔  | 原因   |
| createdBy  | Long    | ✔  | 操作人  |

#### 🔹请求示例

```json
{
  "scheduleId": 10,
  "extraSlots": 5,
  "reason": "患者量增加",
  "createdBy": 1
}
```

#### 统一响应结构格式定义

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段      | 含义               |
| ------- | ---------------- |
| code    | 状态码（200成功，500失败） |
| message | 描述信息             |
| data    | 返回业务数据           |

---
当然可以，我给你生成一份 **清晰可交付的接口文档**（REST风格、适合前端/测试/后端协作）。

---

## 提交擅长领域修改申请接口
### 一、医生端提交申请
#### **1️⃣ 接口描述**

医生提交修改其擅长科室简介，提交后进入管理员审核流程。

---

#### **2️⃣ 请求方式**

```
POST /api/doctor/bio/request
```

---

#### **3️⃣ 请求参数**

| 参数名    | 类型     | 是否必填 | 说明         |
|--------| ------ | ---- |------------|
| userId | Long   | 是    | 用户ID       |
| newBio | String | 是    | 修改后的擅长领域内容 |

---

#### **4️⃣ 请求格式**

#### Form Data 或 `application/x-www-form-urlencoded`

```
userId=1&newBio=擅长神经外科脑肿瘤微创治疗
```

---

#### **5️⃣ 返回示例**

##### ✔ 成功返回：

```json
{
  "code": 200,
  "msg": "申请提交成功，等待管理员审核",
  "data": null
}
```

##### ❌ 失败示例：

```json
{
  "code": 500,
  "msg": "提交失败：请稍后再试",
  "data": null
}
```

---

---

###  二、管理端查看申请列表


* **URL**: `/api/admin/doctors/bio/pending`
* **方法**: `GET`
* **说明**: 查询所有待审批的医生擅长领域修改申请。

#### 请求参数

无

#### 请求示例

```http
GET /api/admin/doctors/bio/pending HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
```

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "doctorId": 101,
      "oldBio": "心血管疾病",
      "newBio": "心血管疾病, 高血压",
      "status": "pending",
      "reason": null,
      "createdAt": "2025-11-21T07:30:00",
      "reviewedAt": null
    },
    {
      "id": 2,
      "doctorId": 102,
      "oldBio": "内分泌",
      "newBio": "内分泌, 糖尿病",
      "status": "pending",
      "reason": null,
      "createdAt": "2025-11-21T07:32:00",
      "reviewedAt": null
    }
  ]
}
```

---

### 三、获取单条申请详情

* **URL**: `/api/admin/doctors/bio/{requestId}`
* **方法**: `GET`
* **说明**: 根据 `requestId` 查询单条申请详情。

#### 请求参数

| 参数        | 类型   | 必填 | 说明    |
| --------- | ---- | -- | ----- |
| requestId | Long | 是  | 申请 ID |

#### 请求示例

```http
GET /api/admin/doctors/bio/1 HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
```

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "doctorId": 101,
    "oldBio": "心血管疾病",
    "newBio": "心血管疾病, 高血压",
    "status": "pending",
    "reason": null,
    "createdAt": "2025-11-21T07:30:00",
    "reviewedAt": null
  }
}
```
### 四、管理员审批申请
#### **1️⃣ 接口描述**

管理员审核医生提交的擅长修改申请，可选择通过或驳回。

---

#### **2️⃣ 请求方式**

```
POST /api/admin/doctors/bio/review/{requestId}
```

---

#### **3️⃣ 路径参数**

| 参数名       | 类型   | 是否必填 | 说明     |
| --------- | ---- | ---- | ------ |
| requestId | Long | 是    | 申请记录ID |

---

#### **4️⃣ 请求参数**

| 参数名      | 类型      | 是否必填 | 说明                       |
| -------- | ------- | ---- | ------------------------ |
| approved | Boolean | 是    | 是否审核通过（true=通过，false=驳回） |
| reason   | String  | 否    | 若拒绝，可填写理由                |

---

#### **5️⃣ 请求示例**

##### 审核通过：

```
POST /api/admin/doctors/bio/review/12
```

Form Data：

```
approved=true
```

##### 审核拒绝：

```
POST /api/admin/doctors/bio/review/12
```

Form Data：

```
approved=false
reason=内容不符合格式规范，请重新编辑
```

---

#### **6️⃣ 返回示例**

##### ✔ 审核通过：

```json
{
  "code": 200,
  "msg": "审核通过",
  "data": null
}
```

##### ❌ 审核拒绝：

```json
{
  "code": 200,
  "msg": "已拒绝",
  "data": null
}
```

### 📌 备注说明

* 修改申请提交后不可再次提交，直到管理员审核完成。
* 管理员审核后，若通过，将同步更新到医生信息表。


已按你的分页接口**格式统一整理好**👇
你直接可以复制到项目文档。

---

## 5 医生账号管理

Base URL: `/api/admin/doctors`

---

###  分页查询医生列表（支持条件过滤）

**URL:** `/list`
**Method:** `GET`

---

#### Query Parameters（全部可选）：

| 参数           | 类型      | 必填 | 说明                         |
| ------------ | ------- | -- | -------------------------- |
| pageNum      | Integer | 否  | 页码（默认 1）                   |
| pageSize     | Integer | 否  | 每页数量（默认 10）                |
| deptId       | Long    | 否  | 按科室筛选                      |
| username     | String  | 否  | 按用户名模糊搜索                   |
| status       | String  | 否  | 用户账号状态（enabled/disabled 等） |
| doctorStatus | String  | 否  | 医生执业状态（active/inactive 等）  |

---

#### 示例请求:

```
GET /api/admin/doctors/list?pageNum=1&pageSize=10&username=张&deptId=5
```

---

#### 示例响应:

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 15,
    "records": [
      {
        "doctorId": 1,
        "userId": 3,
        "deptId": 5,
        "deptName": "骨科",
        "title": "主任医师",
        "bio": "擅长骨科创伤与关节置换",
        "doctorStatus": "active",
        "createdAt": "2025-11-20T09:30:00",
        "updatedAt": "2025-11-22T14:05:00",
        "username": "张三",
        "phone": "13800000000",
        "email": "test@example.com",
        "gender": "男",
        "userStatus": "enabled"
      }
    ],
    "pageNum": 1,
    "pageSize": 10
  }
}
```
