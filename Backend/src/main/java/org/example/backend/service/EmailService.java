package org.example.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.backend.enums.ErrorCode;
import org.example.backend.exception.AppException;
import org.example.backend.utils.VerificationCodeGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    JavaMailSender javaMailSender;
    StringRedisTemplate stringRedisTemplate;

    public void sendVerificationCode(String toEmail) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        String verificationCode = VerificationCodeGenerator.generateCode();
        mimeMessageHelper.setTo(toEmail);
        mimeMessageHelper.setSubject("[PetWorld] Verification Code");

        String body = String.format(
                "<p>Your verification code is: <b>%s</b></p>" +
                "<p>This code is valid for 5 minutes.</p>" +
                "<p>Best regards,<br/>PetWorld</p>",
                verificationCode
        );
        mimeMessageHelper.setText(body, true);

        try {
            javaMailSender.send(mimeMessage);
            stringRedisTemplate.opsForValue().set(
                    "activation:" + toEmail,
                    verificationCode,
                    5 * 60
            );
        } catch (MailException e) {
            throw new AppException(ErrorCode.ERROR_SENDING_EMAIL);
        }
    }

}
