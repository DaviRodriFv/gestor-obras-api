package gestor_obras_api.cronograma.repository;

import gestor_obras_api.cronograma.model.Etapa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EtapaRepository extends JpaRepository<Etapa, UUID> {
    List<Etapa> findByObraId(UUID obraId);
}
