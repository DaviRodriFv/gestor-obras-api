package gestor_obras_api.orcamento.model;

import gestor_obras_api.fornecedor.model.Fornecedor;
import gestor_obras_api.obra.model.Obra;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orcamento")
@SQLDelete(sql = "UPDATE orcamento SET excluido_em = now() WHERE id = ?")
@SQLRestriction("excluido_em IS NULL")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    private String descricao;

    @Column(name = "data_orcamento", nullable = false)
    private LocalDate dataOrcamento;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "arquivo_nome")
    private String arquivoNome;

    @Column(name = "arquivo_tipo")
    private String arquivoTipo;

    @Lob
    @Column(name = "arquivo_conteudo")
    private byte[] arquivoConteudo;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrcamentoItem> itens = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "excluido_em")
    private LocalDateTime excluidoEm;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
    }

    public boolean possuiArquivo() {
        return arquivoConteudo != null && arquivoConteudo.length > 0;
    }
}
