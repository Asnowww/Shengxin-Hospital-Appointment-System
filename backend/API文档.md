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
