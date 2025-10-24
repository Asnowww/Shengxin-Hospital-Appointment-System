package org.example.backend;

import java.util.concurrent.CompletableFuture;
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
        CompletableFuture<Boolean> future = emailAPI.sendHtmlEmail(
                "Test Email",
                "<h1>This is a test email</h1>",
                "w15208422679@icloud.com"
        );
        System.out.println("Email send result: " + future.join());
    }
}
