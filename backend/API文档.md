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
