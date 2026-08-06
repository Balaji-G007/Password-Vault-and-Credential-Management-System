package password_vault_backend.controller;

import password_vault_backend.model.User;
import password_vault_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Figures out who's logged in from the JWT (same trick as CredentialController)
    private User getCurrentUser() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(email);
    }

    // GET /api/users/me - view my own profile
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        User user = getCurrentUser();
        return ResponseEntity.ok(Map.of("name", user.getName(), "email", user.getEmail()));
    }

    // PUT /api/users/me - update my own profile
    // body: { "name": "...", "email": "..." }
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody UpdateProfileRequest request) {
        User user = getCurrentUser();

        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name is required."));
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
        }

        // If they're changing email, make sure the new one isn't already taken
        if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "That email is already in use."));
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Profile updated.", "name", user.getName(), "email", user.getEmail()));
    }

    public static class UpdateProfileRequest {
        private String name, email;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}