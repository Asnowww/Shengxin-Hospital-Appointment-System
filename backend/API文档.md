# 接口文档

## 号别管理

Base URL: `/api/admin/appointment-types`

---

## 1️⃣ 获取号别列表（分页 + 模糊搜索）

**URL:** `/list`
**Method:** `GET`

**Query Parameters:**

| 参数      | 类型      | 必填 | 说明                          |
| ------- | ------- | -- | --------------------------- |
| page    | Integer | 否  | 当前页，默认 1                    |
| size    | Integer | 否  | 每页数量，默认 10                  |
| keyword | String  | 否  | 模糊搜索关键字（typeName 或 typeKey） |

**示例请求:**

```
GET /api/admin/appointment-types/list?page=1&size=5&keyword=专家
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
      "description": "资深医生号",
      "createdAt": "2025-11-13T10:00:00",
      "updatedAt": "2025-11-13T10:00:00"
    }
  ],
  "total": 1,
  "size": 5,
  "current": 1,
  "pages": 1
}
```

---

## 2️⃣ 获取单个号别详情

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
    "description": "资深医生号",
    "createdAt": "2025-11-13T10:00:00",
    "updatedAt": "2025-11-13T10:00:00"
  }
}
```

---

## 3️⃣ 新增号别

**URL:** `/`
**Method:** `POST`

**Request Body (JSON):**

```json
{
  "typeKey": "专家",
  "typeName": "专家号",
  "feeAmount": 300,
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

## 4️⃣ 修改号别

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

## 5️⃣ 删除号别

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

## 6️⃣ 批量删除号别

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
