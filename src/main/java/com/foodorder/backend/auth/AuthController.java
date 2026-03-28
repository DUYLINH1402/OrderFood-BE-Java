package com.foodorder.backend.auth;

import com.foodorder.backend.auth.dto.request.ResetPasswordRequest;
import com.foodorder.backend.auth.dto.request.UserLoginRequest;
import com.foodorder.backend.auth.dto.request.UserRegisterRequest;
import com.foodorder.backend.auth.dto.request.ForgotPasswordRequest;
import com.foodorder.backend.auth.entity.UserToken;
import com.foodorder.backend.auth.entity.UserTokenType;
import com.foodorder.backend.user.dto.response.UserResponse;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.auth.repository.UserTokenRepository;
import com.foodorder.backend.auth.service.AuthService;
import com.foodorder.backend.user.service.UserService;
import com.foodorder.backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Controller xử lý các nghiệp vụ xác thực người dùng
 */
@Controller
@RequestMapping("/api/v1/public/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication and account management")
public class AuthController {
    @Autowired
    private  AuthService authService;
     @Autowired
    private UserService userService;

    @Autowired
    private  UserRepository userRepository;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordRedirectUrl;

    @Autowired
    private UserTokenRepository userTokenRepository;


    @Operation(summary = "Register account", description = "Register a new user account. A verification email will be sent to the registered email address upon successful registration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or email/username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegisterRequest request) {
        try {
            UserResponse response = authService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", e.getMessage()
            ));
        }
    }


    @Operation(summary = "Login", description = "Login with email/username and password. Returns a JWT token on success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid login credentials"),
            @ApiResponse(responseCode = "403", description = "Email not yet verified")
    })
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody @Valid UserLoginRequest request) {
        return ResponseEntity.ok(authService.loginUser(request));
    }


    @Operation(summary = "Verify email", description = "Verify user email via token sent by email. Token is valid for 24 hours.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification successful - Returns verify_success page"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token - Returns verify_failed page")
    })
    @GetMapping("/verify")
    public String verifyUser(
            @Parameter(description = "Verification token sent via email")
            @RequestParam("token") String token) {
        Optional<UserToken> tokenOpt = userTokenRepository.findByTokenAndUsedFalseAndType(token, UserTokenType.EMAIL_VERIFICATION);
        if (tokenOpt.isEmpty()) {
            return "verify_failed";
        }

        UserToken userToken = tokenOpt.get();

        // Kiểm tra hết hạn (dùng createdAt hoặc expiresAt đều được, em dùng createdAt như logic cũ)
        if (userToken.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            return "verify_failed";
        }

        // Đánh dấu token đã dùng
        userToken.setUsed(true);
        userTokenRepository.save(userToken);

        // Cập nhật user
        User user = userToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        return "verify_success";
    }




    @Operation(summary = "Forgot password", description = "Send a password reset link to the registered email address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent"),
            @ApiResponse(responseCode = "404", description = "Email not found in the system")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok("RESET_LINK_SENT");
    }

    @Operation(summary = "Reset password", description = "Reset password using the token sent via email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("PASSWORD_RESET_SUCCESS");
    }

    @Operation(summary = "Verify reset password token", description = "Validate the password reset token. Token is valid for 1 hour.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid token - Redirects to reset password page"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @GetMapping("/reset-password/verify")
    public String verifyResetPassword(
            @Parameter(description = "Password reset token")
            @RequestParam("token") String token, Model model) {
        Optional<UserToken> tokenOpt = userTokenRepository
                .findByTokenAndUsedFalseAndType(token, UserTokenType.PASSWORD_RESET);

        if (tokenOpt.isEmpty() || tokenOpt.get().getCreatedAt().isBefore(LocalDateTime.now().minusHours(1))) {
            return "verify_failed";
        }

        model.addAttribute("token", token);
        return "reset_redirect"; // Trả về template trung gian để kiểm tra Token xem có hợp lệ không rồi chuyển tiêsp đến
        // giao diên để người dùng có thể đặt lại mật khẩu

    }

    // Gửi lại email xác minh
    // Nếu người dùng đã xác minh email thì không cần gửi lại
    // nếu chưa xác minh thì gửi lại email xác minh l
    @Operation(summary = "Resend verification email", description = "Resend the verification email for users who have not yet verified their email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email resent"),
            @ApiResponse(responseCode = "400", description = "Email not found or already verified")
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        String emailOrUsername = request.get("email");

        try {
            authService.resendVerificationEmail(emailOrUsername);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Verification email resent successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", e.getMessage()
            ));
        }
    }


}

