package gestor_obras_api.custo.repository;

import gestor_obras_api.custo.model.CategoriaCusto;
import gestor_obras_api.custo.model.Custo;
import gestor_obras_api.custo.model.StatusCusto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustoRepository extends JpaRepository<Custo, UUID> {

    Optional<Custo> findByIdAndObraId(UUID id, UUID obraId);

    /** Filtros opcionais: parâmetro nulo não restringe. buscaPattern já vem como '%termo%' em minúsculas. */
    @Query("SELECT c FROM Custo c LEFT JOIN FETCH c.fornecedor WHERE c.obra.id = :obraId " +
           "AND (:categoria IS NULL OR c.categoria = :categoria) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:buscaPattern IS NULL OR LOWER(c.descricao) LIKE :buscaPattern) " +
           "ORDER BY c.data DESC, c.criadoEm DESC")
    List<Custo> buscarPorObra(
        @Param("obraId") UUID obraId,
        @Param("categoria") CategoriaCusto categoria,
        @Param("status") StatusCusto status,
        @Param("buscaPattern") String buscaPattern
    );

    /**
     * Soma e conta os lançamentos da obra por categoria, sem os CANCELADO.
     * Categorias sem lançamento não aparecem no resultado.
     */
    @Query("SELECT new gestor_obras_api.custo.repository.TotalPorCategoria(c.categoria, SUM(c.valor), COUNT(c)) " +
           "FROM Custo c WHERE c.obra.id = :obraId " +
           "AND c.status <> gestor_obras_api.custo.model.StatusCusto.CANCELADO " +
           "GROUP BY c.categoria")
    List<TotalPorCategoria> somarPorCategoria(@Param("obraId") UUID obraId);

    /**
     * Chamado antes de excluir um fornecedor: os lançamentos são preservados, só perdem o vínculo.
     * Fornecedor usa soft delete com @SQLRestriction, então manter a referência quebraria o carregamento.
     */
    @Modifying
    @Query("UPDATE Custo c SET c.fornecedor = NULL WHERE c.fornecedor.id = :fornecedorId")
    int desvincularFornecedor(@Param("fornecedorId") UUID fornecedorId);
}
