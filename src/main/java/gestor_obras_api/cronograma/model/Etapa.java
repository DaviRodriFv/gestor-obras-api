package gestor_obras_api.cronograma.model;

import gestor_obras_api.obra.model.Obra;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "etapa")
@SQLDelete(sql = "UPDATE etapa SET excluido_em = now() WHERE id = ?")
@SQLRestriction("excluido_em IS NULL")
public class Etapa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    @Column(nullable = false, length = 150)
    private String nome;

    private String descricao;

    @Column(name = "data_prevista_inicio", nullable = false)
    private LocalDate dataPrevistaInicio;

    @Column(name = "data_prevista_fim", nullable = false)
    private LocalDate dataPrevistaFim;

    @Column(name = "data_real_inicio")
    private LocalDate dataRealInicio;

    @Column(name = "data_real_fim")
    private LocalDate dataRealFim;

    @Column(name = "percentual_progresso", nullable = false)
    private Integer percentualProgresso = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEtapa status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "excluido_em")
    private LocalDateTime excluidoEm;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
        if (status == null) {
            status = StatusEtapa.NAO_INICIADA;
        }
    }
}
