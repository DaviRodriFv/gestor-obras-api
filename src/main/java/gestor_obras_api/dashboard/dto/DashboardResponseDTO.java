package gestor_obras_api.dashboard.dto;

import gestor_obras_api.financeiro.model.SituacaoFinanceira;
import gestor_obras_api.obra.model.StatusObra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Visão consolidada de todas as obras (tela inicial): contadores, alertas e o resumo de cada obra.
 * Os campos financeiros vêm nulos para o perfil Equipe.
 */
public record DashboardResponseDTO(
    int totalObras,
    int obrasEmAndamento,
    int obrasPausadas,
    int obrasConcluidas,
    int obrasCanceladas,
    int obrasComPrazoVencido,
    int obrasComPrazoProximo,
    int obrasComEtapasAtrasadas,
    /** Obras EM_ALERTA ou ORCAMENTO_EXCEDIDO. Nulo para o perfil Equipe. */
    Integer obrasComOrcamentoEmRisco,
    List<ObraResumoDTO> obras
) {

    public record ObraResumoDTO(
        UUID id,
        String nome,
        String cliente,
        StatusObra status,
        LocalDate dataInicio,
        LocalDate prazoConclusao,
        /** Dias até o prazo (negativo = vencido). Nulo para obras concluídas ou canceladas. */
        Long diasParaPrazo,
        int progressoGeral,
        int totalEtapas,
        int etapasAtrasadas,
        /** Próximo passo: primeira etapa não concluída pela data prevista de início. Nulo se não houver. */
        String proximaEtapa,
        LocalDate proximaEtapaInicio,
        SituacaoFinanceira situacaoFinanceira,
        BigDecimal percentualOrcamentoConsumido
    ) {
    }
}
