package org.example.backend.util;

public enum EmailTemplateEnum {
    // 验证码邮件
    VERIFICATION_CODE_EMAIL_HTML("<html><body>用户你好，你的验证码是:<h1>%s</h1>请在五分钟内完成注册</body></html>","登录验证"),

    // 用户被封禁邮件通知
    USER_BANNED_EMAIL("用户你好，你已经被管理员封禁，封禁原因:%s", "封禁通知"),
    // 密码重置验证码邮件
    PASSWORD_RESET_CODE_EMAIL_HTML(
    "<html><body>"
            + "<h3>密码重置验证码</h3>"
            + "<p>您好，您正在请求修改密码。</p>"
            + "<p>您的验证码是：<h2>%s</h2></p>"
            + "<p>验证码有效期为 5 分钟，请勿泄露给他人。</p>"
            + "<p style=\"color:gray;font-size:12px;\">（此邮件为系统自动发送，请勿回复）</p>"
            + "</body></html>",
            "密码重置验证码"
    );



    private final String template;
    private final String subject;

    EmailTemplateEnum(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }

    public String getTemplate(){
        return this.template;
    }

    public String set(String captcha) {
        return String.format(this.template, captcha);
    }


    public String getSubject() {
        return this.subject;
    }
}
