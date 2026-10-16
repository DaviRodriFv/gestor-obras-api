package gestor_obras_api.custo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Conteúdo do arquivo anexado a um lançamento (orçamento, nota fiscal, recibo).
 * Fica em tabela própria para que listar custos não carregue os bytes dos arquivos;
 * nome e tipo ficam no próprio {@link Custo}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "custo_anexo")
public class CustoAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custo_id", nullable = false, unique = true)
    private Custo custo;

    @Lob
    @Column(nullable = false)
    private byte[] conteudo;
}
