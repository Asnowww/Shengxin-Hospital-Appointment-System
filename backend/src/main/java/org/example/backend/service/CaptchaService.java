package org.example.backend.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface CaptchaService {
    boolean sendCaptcha(String email);
    void createGraphCaptcha(HttpServletResponse response) throws IOException;
    boolean verifyGraphCaptcha(String uuid, String inputCode);
}
