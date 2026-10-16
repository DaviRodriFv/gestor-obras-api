package gestor_obras_api.custo.model;

import gestor_obras_api.fornecedor.model.Fornecedor;
import gestor_obras_api.obra.model.Obra;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "custo")
@SQLDelete(sql = "UPDATE custo SET excluido_em = now() WHERE id = ?")
@SQLRestriction("excluido_em IS NULL")
public class Custo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    /** Opcional. Ao excluir o fornecedor, passa a null e o lançamento é preservado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaCusto categoria;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private String descricao;

    // Default no banco para que o ddl-auto consiga criar a coluna em tabelas que já têm linhas
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'PENDENTE'")
    private StatusCusto status;

    /** Nome e tipo do arquivo anexado; o conteúdo fica em {@link CustoAnexo}. Nulos quando não há anexo. */
    @Column(name = "anexo_nome")
    private String anexoNome;

    @Column(name = "anexo_tipo")
    private String anexoTipo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "excluido_em")
    private LocalDateTime excluidoEm;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
        if (status == null) {
            status = StatusCusto.PENDENTE;
        }
    }

    public boolean possuiAnexo() {
        return anexoNome != null;
    }
}
