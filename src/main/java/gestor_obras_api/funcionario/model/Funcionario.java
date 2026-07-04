package gestor_obras_api.funcionario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "funcionarios")
public class Funcionario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCargo cargo;

    @Column(nullable = false)
    private String role = "EQUIPE";

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private Boolean ativo = true;

    @PrePersist
    @PreUpdate
    private void normalizeRole() {
        role = normalizeRoleValue(role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authorityRole = normalizeRoleValue(role != null ? role : (cargo != null ? cargo.name() : "EQUIPE"));
        return List.of(new SimpleGrantedAuthority("ROLE_" + authorityRole));
    }

    private String normalizeRoleValue(String value) {
        if (value == null || value.isBlank()) {
            return "EQUIPE";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
