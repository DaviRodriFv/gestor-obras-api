package gestor_obras_api.fornecedor.model;

import gestor_obras_api.obra.model.Obra;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fornecedor")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "tipo_servico", nullable = false)
    private String tipoServico;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String endereco;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private boolean ativo;

    @ManyToMany
    @JoinTable(
        name = "fornecedor_obra",
        joinColumns = @JoinColumn(name = "fornecedor_id"),
        inverseJoinColumns = @JoinColumn(name = "obra_id")
    )
    private Set<Obra> obras = new HashSet<>();

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
    }
}
