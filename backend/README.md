# 后端说明文档
## 运行方式
1. 安装并运行Redis
2. 执行MySQL数据库建表脚本`src/main/resources/db/hospital.sql`
3. 在终端输入`run dev`或直接运行`BackendApplication.java`

## 数据导入
"backend\src\main\resources\db\hospital_data.sql"
  # 导入方法：
  在数据库中：
  -- 先设置客户端编码
  SET NAMES utf8mb4;
  --再导入
  SOURCE Shengxin-Hospital-Appointment-System/backend/src/main/resources/db/hospital_data.sql;  # 修改为你主机上的路径

# 医生账号密码统一为‘123456’ #


## 接口：
### (1) 用户注册
- **接口地址**：`/auth/register`（POST）
- **前端请求体（JSON）**：
  - username：用户名（字符串）
  - password：密码（字符串）
  - phone：手机号（必填，char）
  - email：邮箱（必填，字符串）
  - idCard：身份证号（字符串，可选）
  - emailCode：邮箱验证码（字符串）
  ```json
  {
    "username": "testuser",     
    "password": "123456",       
    "phone": "13800138000",     
    "email": "test@example.com",
    "idCard":"******************",
    "emailCode": "1628"         
  }
  ```
- **返回结果**：
  ```json
  {
    "code": 200,
    "msg": "注册成功",
    "data": null
  }
  ```
  ```json
  {
    "code": 409,
    "msg": "邮箱已注册，请直接登录",
    "data": null
  }
  ```
  ```json
  {
    "code": 409,
    "msg": "用户名已存在，请更换",
    "data": null
  }
  ```
  ```json
  {
    "code": 400,
    "msg": "验证码错误",
    "data": null
  }
  ```
  ```json
  {
    "code": 400,
    "msg": "验证码已过期或不存在",
    "data": null
  }
  ```


### (2) 发送验证码
- **接口地址**：`/auth/sendEmailCode`（POST）
- **前端请求体（JSON）**：
  ```json
  {
    "email": "test@example.com"
  }
  ```
- **返回结果**：
  ```json
  {
    "code": 409,
    "message": "该邮箱已被注册，请直接登录",
    "data": null
  }
  ```
  ```json
  {
    "code": 200,
    "msg": "验证码已发送，请查收邮箱",
    "data": null
  }
  ```
  ```json
  {
    "code": 500,
    "msg": "验证码发送失败，请稍后重试",
    "data": null
  }
  ```


### (3) 用户登录
- **接口地址**：`/auth/login`（POST）
- **前端请求体**：
  - account:可以是用户名、邮箱或手机号
- 
  ```json
  {
    "account": "testuser",     
    "password": "123456"       
  }
  ```
- **返回结果**：
  - account: userid
  - password: 返回空
  - token: 后端生成的 UUID token
  ```json
  {
    "code": 200,
    "msg": "登录成功",
    "data": {
      "account": 1,            
      "password": "",          
      "token": "uuid-token",   
      "email": "test@example.com"
    }
  }
  ```
  ```json
  {
    "code": 404,
    "msg": "用户不存在",
    "data": null
  }
  ```
  ```json
  {
    "code": 401,
    "msg": "密码错误",
    "data": null
  }
  ```
  ```json
  {
    "code": 400,
    "msg": "账号或密码不能为空",
    "data": null
  }
  ```