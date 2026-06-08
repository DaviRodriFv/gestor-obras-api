package gestor_obras_api.obra.model;

import gestor_obras_api.funcionario.model.Funcionario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "obra")
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Funcionario usuario;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false)
    private String cliente;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "prazo_conclusao", nullable = false)
    private LocalDate prazoConclusao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusObra status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
        if (status == null) {
            status = StatusObra.EM_ANDAMENTO;
        }
    }
}
