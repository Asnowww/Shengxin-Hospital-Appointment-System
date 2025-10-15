package org.example.backend;

import org.example.backend.util.EmailAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailAPITest {

    @Autowired
    private EmailAPI emailAPI;

    @Test
    void sendEmail() throws Exception {
        boolean result = emailAPI.sendHtmlEmail(
                "测试邮件",
                "<h1>这是一封测试邮件</h1>",
                "w15208422679@icloud.com"
        );
        System.out.println("发送结果：" + result);
    }
}
