package com.ken.usermanager.Services;

import com.ken.usermanager.Domains.PasswordResetToken;
import com.ken.usermanager.Domains.User;
import com.ken.usermanager.Domains.VerificationToken;
import com.ken.usermanager.Repositories.PasswordResetTokenRepository;
import com.ken.usermanager.Repositories.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final VerificationTokenRepository verificationTokenRepository;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender javaMailSender;

    public EmailService(VerificationTokenRepository verificationTokenRepository, JavaMailSender javaMailSender, PasswordResetTokenRepository passwordResetTokenRepository){
        this.verificationTokenRepository = verificationTokenRepository;
        this.javaMailSender = javaMailSender;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }
    public void sendVerifyEmail(User user) throws MessagingException {
        VerificationToken verificationToken = verificationTokenRepository.findByUser(user);

        if(verificationToken != null){
            String token = verificationToken.getToken();
            String template =
                    "<h3>Email verification</h3> \n <p>Thank you for signing up. Please click on the link to verify your email address </p> \n <a href='http://localhost:8080/api/v1/auth/email-verify?token=" + token + "'>Verify</a>";

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("Email Verification");
            helper.setText(template, true);
            javaMailSender.send(message);
        }
    }

    public void sendResetPasswordEmail(User user) throws MessagingException {
        PasswordResetToken verificationToken = passwordResetTokenRepository.findByUser(user);

        if(verificationToken != null){
            String token = verificationToken.getToken();
            String template =
                    "<h3>Password reset</h3> \n <p>Please click on the link to reset your password.</p> \n <a href='http://localhost:8080/api/v1/password/password-reset?token=" + token + "'>Password Reset</a>";

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("Password reset");
            helper.setText(template, true);
            javaMailSender.send(message);
        }
    }
}
