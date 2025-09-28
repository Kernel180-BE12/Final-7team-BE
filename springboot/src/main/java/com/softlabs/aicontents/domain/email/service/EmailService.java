package com.softlabs.aicontents.domain.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  @Autowired
  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendVerificationEmail(String to, String verificationCode) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject("이메일 인증코드");
      helper.setText(createVerificationEmailContent(verificationCode), true);

      mailSender.send(message);
      log.info("인증 이메일 발송 완료: {}", to);
    } catch (MessagingException e) {
      log.error("이메일 발송 실패: {}", e.getMessage());
      throw new RuntimeException("이메일 발송에 실패했습니다.", e);
    }
  }

  private String createVerificationEmailContent(String verificationCode) {
    return String.format(
        """
        <html>
        <body>
        <h2>이메일 인증</h2>
        <p>아래 인증코드를 입력해주세요:</p>
        <h3 style="color: #007bff;">%s</h3>
        <p>인증코드는 5분간 유효합니다.</p>
        </body>
        </html>
        """,
        verificationCode);
  }
}
