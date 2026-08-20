package password_vault_backend.controller;

import password_vault_backend.model.*;
import password_vault_backend.repository.*;
import password_vault_backend.security.EncryptionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private TeamCredentialRepository teamCredentialRepository;
    @Autowired private CredentialRepository credentialRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EncryptionUtil encryptionUtil;

    private Long getCurrentUserId() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(email);
        return user.getId();
    }

    // Checks whether the current user has at least the given role in a team.
    // OWNER outranks ADMIN outranks MEMBER.
    private boolean hasRole(Long teamId, Long userId, TeamMember.Role minimumRole) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId).orElse(null);
        if (member == null) return false;

        return switch (minimumRole) {
            case MEMBER -> true; // any role qualifies
            case ADMIN -> member.getRole() == TeamMember.Role.ADMIN || member.getRole() == TeamMember.Role.OWNER;
            case OWNER -> member.getRole() == TeamMember.Role.OWNER;
        };
    }

    // POST /api/teams - create a new team. Creator automatically becomes OWNER.
    // body: { "name": "Engineering Team" }
    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Team name is required."));
        }

        Long userId = getCurrentUserId();
        Team team = teamRepository.save(new Team(name, userId));
        teamMemberRepository.save(new TeamMember(team.getId(), userId, TeamMember.Role.OWNER));

        return ResponseEntity.ok(Map.of("message", "Team created.", "teamId", team.getId()));
    }

    // GET /api/teams - list teams the current user belongs to
    @GetMapping
    public ResponseEntity<?> myTeams() {
        Long userId = getCurrentUserId();
        List<TeamMember> memberships = teamMemberRepository.findByUserId(userId);

        List<Map<String, Object>> response = memberships.stream().map(m -> {
            Team team = teamRepository.findById(m.getTeamId()).orElse(null);
            Map<String, Object> item = new HashMap<>();
            item.put("teamId", m.getTeamId());
            item.put("teamName", team != null ? team.getName() : "(deleted)");
            item.put("role", m.getRole());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // POST /api/teams/{teamId}/members - add a member. Requires ADMIN or OWNER.
    // body: { "email": "person@example.com", "role": "MEMBER" }  (role: MEMBER or ADMIN)
    @PostMapping("/{teamId}/members")
    public ResponseEntity<?> addMember(@PathVariable Long teamId, @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        if (!hasRole(teamId, userId, TeamMember.Role.ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("message", "Only team admins or the owner can add members."));
        }

        String email = body.get("email");
        User newUser = userRepository.findByEmail(email);
        if (newUser == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No user found with that email."));
        }

        if (teamMemberRepository.findByTeamIdAndUserId(teamId, newUser.getId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "User is already a team member."));
        }

        String roleStr = body.getOrDefault("role", "MEMBER");
        TeamMember.Role role = roleStr.equalsIgnoreCase("ADMIN") ? TeamMember.Role.ADMIN : TeamMember.Role.MEMBER;

        teamMemberRepository.save(new TeamMember(teamId, newUser.getId(), role));
        return ResponseEntity.ok(Map.of("message", "Member added."));
    }

    // DELETE /api/teams/{teamId}/members/{memberUserId} - remove a member. Requires ADMIN or OWNER.
    @DeleteMapping("/{teamId}/members/{memberUserId}")
    public ResponseEntity<?> removeMember(@PathVariable Long teamId, @PathVariable Long memberUserId) {
        Long userId = getCurrentUserId();
        if (!hasRole(teamId, userId, TeamMember.Role.ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("message", "Only team admins or the owner can remove members."));
        }

        TeamMember target = teamMemberRepository.findByTeamIdAndUserId(teamId, memberUserId).orElse(null);
        if (target == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Member not found."));
        }
        if (target.getRole() == TeamMember.Role.OWNER) {
            return ResponseEntity.status(403).body(Map.of("message", "Cannot remove the team owner."));
        }

        teamMemberRepository.delete(target);
        return ResponseEntity.ok(Map.of("message", "Member removed."));
    }

    // POST /api/teams/{teamId}/credentials - share one of my credentials with the whole team. Requires ADMIN or OWNER.
    // body: { "credentialId": 8 }
    @PostMapping("/{teamId}/credentials")
    public ResponseEntity<?> shareToTeam(@PathVariable Long teamId, @RequestBody Map<String, Long> body) {
        Long userId = getCurrentUserId();
        if (!hasRole(teamId, userId, TeamMember.Role.ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("message", "Only team admins or the owner can share credentials to the team."));
        }

        Long credentialId = body.get("credentialId");
        Credential credential = credentialRepository.findByIdAndUserId(credentialId, userId).orElse(null);
        if (credential == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Credential not found."));
        }

        teamCredentialRepository.save(new TeamCredential(teamId, credentialId, userId));
        return ResponseEntity.ok(Map.of("message", "Credential shared with team."));
    }

    // GET /api/teams/{teamId}/credentials - view all credentials shared with this team. Requires being a member.
    @GetMapping("/{teamId}/credentials")
    public ResponseEntity<?> teamCredentials(@PathVariable Long teamId) {
        Long userId = getCurrentUserId();
        if (!hasRole(teamId, userId, TeamMember.Role.MEMBER)) {
            return ResponseEntity.status(403).body(Map.of("message", "You are not a member of this team."));
        }

        List<TeamCredential> links = teamCredentialRepository.findByTeamId(teamId);

        List<Map<String, Object>> response = links.stream().map(link -> {
            Credential credential = credentialRepository.findById(link.getCredentialId()).orElse(null);
            if (credential == null) return null;

            Map<String, Object> item = new HashMap<>();
            item.put("teamCredentialId", link.getId());
            item.put("websiteName", credential.getWebsiteName());
            item.put("username", credential.getUsername());
            item.put("password", encryptionUtil.decrypt(credential.getEncryptedPassword()));
            item.put("sharedAt", link.getSharedAt());
            return item;
        }).filter(item -> item != null).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}