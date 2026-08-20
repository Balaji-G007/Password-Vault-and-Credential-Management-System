package password_vault_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "team_members")
public class TeamMember {

    public enum Role { OWNER, ADMIN, MEMBER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private Role role;

    public TeamMember() {}

    public TeamMember(Long teamId, Long userId, Role role) {
        this.teamId = teamId;
        this.userId = userId;
        this.role = role;
    }

    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public Long getUserId() { return userId; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}