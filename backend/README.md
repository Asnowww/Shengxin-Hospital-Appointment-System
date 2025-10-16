1.安装并运行Redis
2.执行MySQL数据库建表脚本src/main/resources/db/hospital.sql
3.在终端输入run dev或直接运行BackendApplication.java

接口：
(1) /auth/register(POST)：
前端请求体(JSON)：
{
"username": "testuser",     // 用户名
"password": "123456",       // 密码
"name": "张三",             // 必填
"phone": "13800138000",     // 必填
"email": "test@example.com",// 必填
"emailCode": "1628"         //验证码
}
返回结果：
{
"code": 200,
"msg": "注册成功",
"data": null
},
{
"code": 409,
"msg": "邮箱已注册，请直接登录"
"data": null
},
{
"code": 409,
"msg": "用户名已存在，请更换"
"data": null
},
{
"code": 400,
"msg": "验证码错误"
"data": null
},
{
"code": 400,
"msg": "验证码已过期或不存在",
"data": null
}



(2) /auth/sendEmailCode(POST)发送验证码:
前端请求体(JSON)：
{
"email": "test@example.com"
}
返回结果：
{
"code": 409,
"message": "该邮箱已被注册，请直接登录",
"data": null
},
{
"code": 200,
"msg": "验证码已发送，请查收邮箱",
"data": null
},
{
"code": 500,
"msg": "验证码发送失败，请稍后重试",
"data": null
}

(3) /auth/login(POST)
前端请求体：
{
"account": "testuser",     // 可以是用户名、邮箱或手机号
"password": "123456"       // 密码
}
返回结果：
{
"code": 200,
"msg": "登录成功",
    "data": {
    "account": 1,            // userId
    "password": "",          // 返回空
    "token": "uuid-token",   // 后端生成的 UUID token
    "email": "test@example.com"
    }
},
{
"code": 404,
"msg": "用户不存在",
"data": null
},
{
"code": 401,
"msg": "密码错误",
"data": null
},
{
"code": 400,
"msg": "账号或密码不能为空",
"data": null
}





