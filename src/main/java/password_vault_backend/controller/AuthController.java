package password_vault_backend.controller;

import password_vault_backend.model.User;
import password_vault_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JavaMailSender mailSender;

    // ---- existing register/login stay exactly as they are ----

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getName() == null || request.getName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please fill all fields (password min 6 characters)."));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered."));
        }
        String hashed = passwordEncoder.encode(request.getPassword());
        userRepository.save(new User(request.getName(), request.getEmail(), hashed));
        return ResponseEntity.ok(Map.of("message", "Account created successfully."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password."));
        }
        return ResponseEntity.ok(Map.of("name", user.getName(), "email", user.getEmail()));
    }

    // ---- NEW: forgot password / OTP ----

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        // Always respond the same way whether or not the email exists
        if (user == null) {
            return ResponseEntity.ok(Map.of("message", "If that email exists, an OTP has been sent."));
        }

        String otp = String.valueOf(100000 + new Random().nextInt(900000)); // 6-digit code
        long expiry = System.currentTimeMillis() + (10 * 60 * 1000); // 10 minutes

        user.setOtp(otp);
        user.setOtpExpiry(expiry);
        userRepository.save(user);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Vaultkeep - Password Reset Code");
            message.setText("Your Vaultkeep password reset code is: " + otp
                    + "\n\nThis code expires in 10 minutes. If you didn't request this, you can ignore this email.");
            mailSender.send(message);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Could not send email. Check server email configuration."));
        }

        return ResponseEntity.ok(Map.of("message", "If that email exists, an OTP has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null || user.getOtp() == null) {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP."));
        }
        if (!user.getOtp().equals(request.getOtp())) {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP."));
        }
        if (System.currentTimeMillis() > user.getOtpExpiry()) {
            return ResponseEntity.status(400).body(Map.of("message", "OTP has expired. Please request a new one."));
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return ResponseEntity.status(400).body(Map.of("message", "Password must be at least 6 characters."));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now sign in."));
    }

    // ---- request body classes ----

    public static class RegisterRequest {
        private String name, email, password;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        private String email, password;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ForgotPasswordRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ResetPasswordRequest {
        private String email, otp, newPassword;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}