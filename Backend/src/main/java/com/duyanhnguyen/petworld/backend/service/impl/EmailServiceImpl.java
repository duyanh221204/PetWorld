package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.service.EmailService;
import com.duyanhnguyen.petworld.backend.utils.RedisKeyGenerator;
import com.duyanhnguyen.petworld.backend.utils.VerificationCodeGenerator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {

    JavaMailSender javaMailSender;
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendVerificationCode(String toEmail) {
        try {
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

            stringRedisTemplate.opsForValue().set(
                    RedisKeyGenerator.generateVerificationCodeKey(toEmail),
                    verificationCode,
                    Duration.ofSeconds(300)
            );
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException e) {
            throw new AppException(ErrorCode.ERROR_SENDING_EMAIL);
        }
    }

}
