package com.ken.usermanager.Controllers;

import com.ken.usermanager.Domains.Authority;
import com.ken.usermanager.Domains.User;
import com.ken.usermanager.Domains.UserImage;
import com.ken.usermanager.Domains.VerificationToken;
import com.ken.usermanager.Exceptions.CustomException;
import com.ken.usermanager.Repositories.AuthorityRepository;
import com.ken.usermanager.Repositories.UserRepository;
import com.ken.usermanager.Repositories.VerificationTokenRepository;
import com.ken.usermanager.Requests.SignInRequest;
import com.ken.usermanager.Requests.SignUpRequest;
import com.ken.usermanager.Responses.EmailExistResponse;
import com.ken.usermanager.Responses.SignUpResponse;
import com.ken.usermanager.Responses.UserNotFoundResponse;
import com.ken.usermanager.Security.JWT.JWTResponse;
import com.ken.usermanager.Security.JWT.JWTService;
import com.ken.usermanager.Services.EmailService;
import com.ken.usermanager.Services.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final JWTService jwtService;

    private final FileUploadService fileUploadService;

    public AuthController(JWTService jwtService,
                          AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthorityRepository authorityRepository,
                          VerificationTokenRepository verificationTokenRepository,
                          EmailService emailService,
                          FileUploadService fileUploadService){
        this.jwtService = jwtService;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
        this.fileUploadService = fileUploadService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = JWTResponse.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserNotFoundResponse.class))
            })
    })
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid SignInRequest request) throws CustomException {
        if(!userRepository.existsByEmail(request.getEmail())){
            return ResponseEntity.status(404).body(new UserNotFoundResponse("User not found by this email."));
        }
        Authentication token = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authUser = authenticationManager.authenticate(token);

        String jwt = jwtService.generateToken((User) authUser.getPrincipal());

        return ResponseEntity.ok(new JWTResponse(jwt));
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SignUpResponse.class))
            }),
            @ApiResponse(responseCode = "500", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = EmailExistResponse.class))
            })
    })
    @PostMapping(value = "/sign-up", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> signUp(@ModelAttribute @Valid SignUpRequest request) throws MessagingException, IOException {
        if(userRepository.findUserByEmail(request.getEmail()) != null){
            return ResponseEntity.badRequest().body(new EmailExistResponse("This email already exists."));
        }

        Authority authority = authorityRepository.getReferenceById(2L);
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAuthority(authority);
        authority.getUsers().add(user);

        UserImage userImage = fileUploadService.fileUpload(request.getImage(), user);
        user.setUserImage(userImage);

        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);

        verificationTokenRepository.save(token);

        emailService.sendVerifyEmail(user);

        return ResponseEntity.ok().body(new SignUpResponse(userRepository.save(user)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404")
    })
    @GetMapping("/email-verify")
    public ResponseEntity<?> verifyEmail(@RequestParam(name = "token") String token){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken != null) {
            User user = verificationToken.getUser();
            if (user.getEmailVerifiedAt() == null) {
                user.setEmailVerifiedAt(LocalDate.now());
                verificationToken.setUser(null);
                verificationTokenRepository.delete(verificationToken);
                userRepository.save(user);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.notFound().build();
    }
}
