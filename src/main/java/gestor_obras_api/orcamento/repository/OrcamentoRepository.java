package gestor_obras_api.orcamento.repository;

import gestor_obras_api.orcamento.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {
    List<Orcamento> findByFornecedorId(UUID fornecedorId);
    List<Orcamento> findByObraId(UUID obraId);
}
