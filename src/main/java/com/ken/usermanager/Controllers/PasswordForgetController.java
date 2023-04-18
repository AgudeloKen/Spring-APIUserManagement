package com.ken.usermanager.Controllers;

import com.ken.usermanager.Domains.PasswordResetToken;
import com.ken.usermanager.Domains.User;
import com.ken.usermanager.Repositories.PasswordResetTokenRepository;
import com.ken.usermanager.Repositories.UserRepository;
import com.ken.usermanager.Requests.EmailRequest;
import com.ken.usermanager.Requests.PasswordRequest;
import com.ken.usermanager.Responses.EmailResponse;
import com.ken.usermanager.Responses.PasswordResponse;
import com.ken.usermanager.Responses.UserNotFoundResponse;
import com.ken.usermanager.Services.EmailService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RequestMapping("/api/v1/password")
@RestController
public class PasswordForgetController {


    private final UserRepository userRepository;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;


    public PasswordForgetController(UserRepository userRepository,
                                    PasswordResetTokenRepository passwordResetTokenRepository,
                                    PasswordEncoder passwordEncoder,
                                    EmailService emailService){
        this.userRepository= userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = EmailResponse.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserNotFoundResponse.class))
            }),
            @ApiResponse(responseCode = "400", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PasswordResponse.class))
            })
    })
    @Parameter(in = ParameterIn.HEADER, name = "X-Authorization", required = true, schema = @Schema(type = "string"))
    @PostMapping("/send-password")
    public ResponseEntity<?> passwordResetEmail(@RequestBody EmailRequest request) throws MessagingException {
        if(!userRepository.existsByEmail(request.email())){
            return ResponseEntity.status(404).body(new UserNotFoundResponse("User not found by this email."));
        }
        UserDetails user = userRepository.findUserByEmail(request.email());

        if(passwordResetTokenRepository.existsByUser((User) user)){
            return ResponseEntity.badRequest().body(new PasswordResponse("An email has already been sent."));
        }

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(UUID.randomUUID().toString());
        passwordResetToken.setUser((User) user);
        passwordResetTokenRepository.save(passwordResetToken);

        emailService.sendResetPasswordEmail((User) user);

        return ResponseEntity.ok(new EmailResponse("Email send."));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PasswordResponse.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserNotFoundResponse.class))
            })
    })
    @Parameter(in = ParameterIn.HEADER, name = "X-Authorization", required = true, schema = @Schema(type = "string"))
    @Transactional
    @PostMapping("/password-reset")
    public ResponseEntity<?> resetPassword(@RequestParam(name = "token") String token, @RequestBody PasswordRequest request){
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if(passwordResetToken != null){
            User user = passwordResetToken.getUser();
            if(user.isEnabled()){
                user.setPassword(passwordEncoder.encode(request.password()));
                passwordResetToken.setUser(null);
                passwordResetTokenRepository.delete(passwordResetToken);
                userRepository.save(user);
                return ResponseEntity.ok(new PasswordResponse("Password has been changed."));
            }else{
                return ResponseEntity.status(404).body(new UserNotFoundResponse("User not found."));
            }
        }
        return ResponseEntity.notFound().build();
    }
}
