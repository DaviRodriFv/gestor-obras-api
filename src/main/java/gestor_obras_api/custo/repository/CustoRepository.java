package gestor_obras_api.custo.repository;

import gestor_obras_api.custo.model.Custo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustoRepository extends JpaRepository<Custo, UUID> {
    List<Custo> findByObraId(UUID obraId);
}
