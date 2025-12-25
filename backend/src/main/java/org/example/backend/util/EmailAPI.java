package org.example.backend.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Email sending helper.
 */
@Component
@Slf4j
public class EmailAPI {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from; // sender address

    /**
     * Send a plain text email.
     */
    public boolean sendGeneralEmail(String subject, String content, String... to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
        log.info("Plain text email sent successfully");
        return true;
    }

    /**
     * Send an HTML email asynchronously.
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> sendHtmlEmail(String subject, String content, String... to) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully");
            return CompletableFuture.completedFuture(true);
        } catch (Exception ex) {
            log.error("Failed to send HTML email", ex);
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * Send an email with attachments.
     */
    @SneakyThrows
    public boolean sendAttachmentsEmail(String subject, String content, String[] to, String[] filePaths) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        if (filePaths != null) {
            for (String filePath : filePaths) {
                FileSystemResource file = new FileSystemResource(new File(filePath));
                helper.addAttachment(Objects.requireNonNull(file.getFilename()), file);
            }
        }

        mailSender.send(mimeMessage);
        log.info("Attachment email sent successfully");
        return true;
    }

    /**
     * Send an email with inline resources.
     */
    @SneakyThrows
    public boolean sendInlineResourceEmail(String subject, String content, String to, String rscPath, String rscId) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);

        String contentHtml = "<html><body>This email contains an inline image:<img src='cid:" + rscId + "'>" + content
                + "</body></html>";
        helper.setText(contentHtml, true);

        FileSystemResource res = new FileSystemResource(new File(rscPath));
        helper.addInline(rscId, res);

        mailSender.send(mimeMessage);
        log.info("Inline resource email sent successfully");
        return true;
    }
}
