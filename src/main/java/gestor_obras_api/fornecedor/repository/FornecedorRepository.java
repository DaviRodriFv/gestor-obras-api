package gestor_obras_api.fornecedor.repository;

import gestor_obras_api.fornecedor.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FornecedorRepository extends JpaRepository<Fornecedor, UUID> {
    boolean existsByEmail(String email);
}
