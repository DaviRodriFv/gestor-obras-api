package gestor_obras_api.obra.repository;

import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.model.StatusObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ObraRepository extends JpaRepository<Obra, UUID> {

    List<Obra> findByStatus(StatusObra status);

    @Query("SELECT o FROM Obra o WHERE " +
           "LOWER(o.nome) LIKE LOWER(CONCAT('%', :busca, '%')) OR " +
           "LOWER(o.cliente) LIKE LOWER(CONCAT('%', :busca, '%'))")
    List<Obra> findByNomeOrClienteContainingIgnoreCase(@Param("busca") String busca);

    @Query("SELECT o FROM Obra o WHERE o.status = :status AND (" +
           "LOWER(o.nome) LIKE LOWER(CONCAT('%', :busca, '%')) OR " +
           "LOWER(o.cliente) LIKE LOWER(CONCAT('%', :busca, '%')))")
    List<Obra> findByStatusAndNomeOrClienteContaining(
        @Param("status") StatusObra status,
        @Param("busca") String busca
    );
}
