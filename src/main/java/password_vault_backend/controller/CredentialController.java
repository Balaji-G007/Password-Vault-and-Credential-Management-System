package password_vault_backend.controller;

import password_vault_backend.model.Credential;
import password_vault_backend.model.User;
import password_vault_backend.repository.CredentialRepository;
import password_vault_backend.repository.UserRepository;
import password_vault_backend.security.EncryptionUtil;
import password_vault_backend.Service.AuditLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/credentials")
public class CredentialController {

    @Autowired private CredentialRepository credentialRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EncryptionUtil encryptionUtil;
    @Autowired private AuditLogService auditLogService;

    private Long getCurrentUserId() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(email);
        return user.getId();
    }

    private String getCurrentUserEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // GET /api/credentials - list all of the logged-in user's saved credentials
    @GetMapping
    public ResponseEntity<?> getAll() {
        Long userId = getCurrentUserId();
        List<Credential> credentials = credentialRepository.findByUserId(userId);

        List<Map<String, Object>> response = credentials.stream().map(c -> {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", c.getId());
            item.put("websiteName", c.getWebsiteName());
            item.put("username", c.getUsername());
            item.put("password", encryptionUtil.decrypt(c.getEncryptedPassword()));
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // POST /api/credentials - save a new website credential
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CredentialRequest request) {
        if (request.getWebsiteName() == null || request.getWebsiteName().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Website name and password are required."));
        }

        Long userId = getCurrentUserId();
        String encrypted = encryptionUtil.encrypt(request.getPassword());

        Credential credential = new Credential(request.getWebsiteName(), request.getUsername(), encrypted, userId);
        credentialRepository.save(credential);

        auditLogService.log(getCurrentUserEmail(), "CREDENTIAL_CREATED", "Added credential for " + request.getWebsiteName());

        return ResponseEntity.ok(Map.of("message", "Credential saved."));
    }

    // PUT /api/credentials/{id} - update an existing credential
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CredentialRequest request) {
        Long userId = getCurrentUserId();

        Credential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElse(null);

        if (credential == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Credential not found."));
        }

        credential.setWebsiteName(request.getWebsiteName());
        credential.setUsername(request.getUsername());
        credential.setEncryptedPassword(encryptionUtil.encrypt(request.getPassword()));
        credentialRepository.save(credential);

        auditLogService.log(getCurrentUserEmail(), "CREDENTIAL_UPDATED", "Updated credential for " + request.getWebsiteName());

        return ResponseEntity.ok(Map.of("message", "Credential updated."));
    }

    // DELETE /api/credentials/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();

        Credential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElse(null);

        if (credential == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Credential not found."));
        }

        credentialRepository.delete(credential);

        auditLogService.log(getCurrentUserEmail(), "CREDENTIAL_DELETED", "Deleted credential for " + credential.getWebsiteName());

        return ResponseEntity.ok(Map.of("message", "Credential deleted."));
    }

    public static class CredentialRequest {
        private String websiteName;
        private String username;
        private String password;

        public String getWebsiteName() { return websiteName; }
        public void setWebsiteName(String websiteName) { this.websiteName = websiteName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}