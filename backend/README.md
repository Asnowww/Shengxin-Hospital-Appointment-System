# 后端说明文档
## 运行方式
1. 安装并运行Redis
2. 执行MySQL数据库建表脚本`src/main/resources/db/hospital.sql`
3. 在终端输入`run dev`或直接运行`BackendApplication.java`

## 配置文件
1. `application.properties`中不要修改个人配置信息并推送（如MySQL配置、本地图片路径等）
2. 个人配置信息放在`application-local.properties`这个文件中：
   - 手动创建`application-local.properties`文件（与`application.properties`放在同一路径下）
   - 复制粘贴如下内容：
    ```
      spring.datasource.username=your_user_name    # 改成你的MySQL用户名
      spring.datasource.password=your_pwd     # 改成你的MySQL密码
      file.upload-dir=D:/hospital_uploads/verifications/    # 改成你本地的路径，用来存放照片
      file.access-path=/static/verifications/ #不变
    ```
   - 按照注释要求修改个人配置，并删除注释
   - 确保`application-local.properties`文件不被推送（已添加至gitignore）

## 数据导入
  ### 导入方法：
  在数据库中：backend/src/main/resources/db/hospital_data.sql
  -- 先设置客户端编码
  SET NAMES utf8mb4;
  --再导入
  SOURCE C:/Users/wang/IdeaProjects/Shengxin-Hospital-Appointment-System/backend/src/main/resources/db/hospital_data.sql; # 修改为你主机上的路径

#### 医生账号密码统一为‘123456’ 


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