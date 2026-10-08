package gestor_obras_api.custo.repository;

import gestor_obras_api.custo.model.CategoriaCusto;

import java.math.BigDecimal;

/** Linha do agrupamento de CustoRepository.somarPorCategoria. */
public record TotalPorCategoria(CategoriaCusto categoria, BigDecimal total, Long quantidade) {
}
