package password_vault_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_logs")
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    // true = successful login, false = failed attempt
    private boolean success;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt;

    // Reason for failure, e.g. "Invalid password", "User not found" - null on success
    private String reason;

    public LoginLog() {}

    public LoginLog(String email, boolean success, String ipAddress, String reason) {
        this.email = email;
        this.success = success;
        this.ipAddress = ipAddress;
        this.reason = reason;
        this.attemptedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public boolean isSuccess() { return success; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public String getReason() { return reason; }
}