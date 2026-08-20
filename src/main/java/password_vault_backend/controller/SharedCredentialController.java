package password_vault_backend.controller;

import password_vault_backend.model.Credential;
import password_vault_backend.model.SharedCredential;
import password_vault_backend.model.User;
import password_vault_backend.repository.CredentialRepository;
import password_vault_backend.repository.SharedCredentialRepository;
import password_vault_backend.repository.UserRepository;
import password_vault_backend.security.EncryptionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shared-credentials")
public class SharedCredentialController {

    @Autowired private SharedCredentialRepository sharedCredentialRepository;
    @Autowired private CredentialRepository credentialRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EncryptionUtil encryptionUtil;

    private String getCurrentUserEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Long getCurrentUserId() {
        User user = userRepository.findByEmail(getCurrentUserEmail());
        return user.getId();
    }

    // POST /api/shared-credentials
    // body: { "credentialId": 3, "shareWithEmail": "friend@example.com", "expiresInHours": 24 }
    // expiresInHours is optional - omit or null for a share that never expires
    @PostMapping
    public ResponseEntity<?> share(@RequestBody ShareRequest request) {
        Long ownerId = getCurrentUserId();

        // Confirm the credential exists and actually belongs to the person sharing it
        Credential credential = credentialRepository.findByIdAndUserId(request.getCredentialId(), ownerId)
                .orElse(null);
        if (credential == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Credential not found."));
        }

        // Confirm the recipient is an actual registered user
        User recipient = userRepository.findByEmail(request.getShareWithEmail());
        if (recipient == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No user found with that email."));
        }

        LocalDateTime expiresAt = request.getExpiresInHours() != null
                ? LocalDateTime.now().plusHours(request.getExpiresInHours())
                : null;

        SharedCredential shared = new SharedCredential(
                credential.getId(), ownerId, request.getShareWithEmail(), expiresAt);
        sharedCredentialRepository.save(shared);

        return ResponseEntity.ok(Map.of("message", "Credential shared successfully."));
    }

    // GET /api/shared-credentials/received - credentials other people have shared WITH me
    @GetMapping("/received")
    public ResponseEntity<?> received() {
        String myEmail = getCurrentUserEmail();
        List<SharedCredential> shares = sharedCredentialRepository.findBySharedWithEmail(myEmail);

        List<Map<String, Object>> response = shares.stream()
                .filter(SharedCredential::isActive)
                .map(share -> {
                    Credential credential = credentialRepository.findById(share.getCredentialId()).orElse(null);
                    if (credential == null) return null;

                    Map<String, Object> item = new HashMap<>();
                    item.put("shareId", share.getId());
                    item.put("websiteName", credential.getWebsiteName());
                    item.put("username", credential.getUsername());
                    item.put("password", encryptionUtil.decrypt(credential.getEncryptedPassword()));
                    item.put("expiresAt", share.getExpiresAt());
                    return item;
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // GET /api/shared-credentials/sent - credentials I have shared out, for managing/revoking
    @GetMapping("/sent")
    public ResponseEntity<?> sent() {
        Long ownerId = getCurrentUserId();
        List<SharedCredential> shares = sharedCredentialRepository.findByOwnerId(ownerId);

        List<Map<String, Object>> response = shares.stream().map(share -> {
            Credential credential = credentialRepository.findById(share.getCredentialId()).orElse(null);
            Map<String, Object> item = new HashMap<>();
            item.put("shareId", share.getId());
            item.put("websiteName", credential != null ? credential.getWebsiteName() : "(deleted)");
            item.put("sharedWithEmail", share.getSharedWithEmail());
            item.put("expiresAt", share.getExpiresAt());
            item.put("revoked", share.isRevoked());
            item.put("active", share.isActive());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // DELETE /api/shared-credentials/{shareId} - revoke a share you created
    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> revoke(@PathVariable Long shareId) {
        Long ownerId = getCurrentUserId();
        SharedCredential share = sharedCredentialRepository.findByIdAndOwnerId(shareId, ownerId).orElse(null);

        if (share == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Share not found."));
        }

        share.setRevoked(true);
        sharedCredentialRepository.save(share);

        return ResponseEntity.ok(Map.of("message", "Access revoked."));
    }

    public static class ShareRequest {
        private Long credentialId;
        private String shareWithEmail;
        private Long expiresInHours;

        public Long getCredentialId() { return credentialId; }
        public void setCredentialId(Long credentialId) { this.credentialId = credentialId; }
        public String getShareWithEmail() { return shareWithEmail; }
        public void setShareWithEmail(String shareWithEmail) { this.shareWithEmail = shareWithEmail; }
        public Long getExpiresInHours() { return expiresInHours; }
        public void setExpiresInHours(Long expiresInHours) { this.expiresInHours = expiresInHours; }
    }
}