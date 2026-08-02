package password_vault_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "credentials")
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String websiteName;
    private String username;

    // Stored ENCRYPTED (not hashed) - we need to be able to decrypt it
    // later to show the user their saved password.
    private String encryptedPassword;

    // Which user this credential belongs to
    @Column(name = "user_id")
    private Long userId;

    public Credential() {}

    public Credential(String websiteName, String username, String encryptedPassword, Long userId) {
        this.websiteName = websiteName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.userId = userId;
    }

    public Long getId() { return id; }

    public String getWebsiteName() { return websiteName; }
    public void setWebsiteName(String websiteName) { this.websiteName = websiteName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}