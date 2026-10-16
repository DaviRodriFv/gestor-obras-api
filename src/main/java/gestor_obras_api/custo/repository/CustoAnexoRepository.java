package gestor_obras_api.custo.repository;

import gestor_obras_api.custo.model.CustoAnexo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustoAnexoRepository extends JpaRepository<CustoAnexo, UUID> {

    Optional<CustoAnexo> findByCustoId(UUID custoId);
}
