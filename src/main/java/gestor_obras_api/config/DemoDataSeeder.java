package gestor_obras_api.config;

import gestor_obras_api.cronograma.model.Etapa;
import gestor_obras_api.cronograma.model.StatusEtapa;
import gestor_obras_api.cronograma.repository.EtapaRepository;
import gestor_obras_api.custo.model.CategoriaCusto;
import gestor_obras_api.custo.model.Custo;
import gestor_obras_api.custo.model.StatusCusto;
import gestor_obras_api.custo.repository.CustoRepository;
import gestor_obras_api.fornecedor.model.Fornecedor;
import gestor_obras_api.fornecedor.repository.FornecedorRepository;
import gestor_obras_api.funcionario.model.Funcionario;
import gestor_obras_api.funcionario.model.TipoCargo;
import gestor_obras_api.funcionario.repository.FuncionarioRepository;
import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.model.StatusObra;
import gestor_obras_api.obra.repository.ObraRepository;
import gestor_obras_api.orcamento.model.Orcamento;
import gestor_obras_api.orcamento.model.OrcamentoItem;
import gestor_obras_api.orcamento.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static gestor_obras_api.custo.model.CategoriaCusto.MAO_DE_OBRA;
import static gestor_obras_api.custo.model.CategoriaCusto.MATERIAL;
import static gestor_obras_api.custo.model.CategoriaCusto.SERVICO_TERCEIRIZADO;

/**
 * Popula o banco com dados de demonstração interligados: funcionários, fornecedores, obras com
 * cronograma, orçamento, lançamentos de custo e propostas de fornecedor.
 * <p>
 * Só roda com {@code app.seed-demo=true} e uma única vez (pula se o usuário do André já existir).
 * As datas são relativas ao dia em que roda, para que alertas de prazo e etapas atrasadas façam sentido.
 * Progresso, situação financeira e alertas não são gravados — saem calculados dos dados abaixo.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo", havingValue = "true")
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private static final String EMAIL_MARCADOR = "andre.paulino@gestorobras.com.br";
    private static final String SENHA_PADRAO = "gestor@1234";

    private final FuncionarioRepository funcionarioRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ObraRepository obraRepository;
    private final EtapaRepository etapaRepository;
    private final CustoRepository custoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final PasswordEncoder passwordEncoder;

    // Semente fixa só para os valores saírem parecidos a cada vez que o banco é recriado
    private final Random random = new Random(42);
    private final LocalDate hoje = LocalDate.now();

    private record Gasto(String descricao, CategoriaCusto categoria, String fornecedor, double fracao) {}

    private record EtapaModelo(String nome, String descricao, int peso, List<Gasto> gastos) {}

    private record ItemModelo(String descricao, double qtdMin, double qtdMax, double preco) {}

    /**
     * @param inicio     dias a partir de hoje (negativo = no passado)
     * @param referencia data em que a obra "parou" (pausa, cancelamento ou conclusão), em dias a partir de hoje; nulo = hoje
     * @param base       valor de referência para os custos (100% de progresso com fator 1 gasta exatamente isso)
     * @param fator      multiplicador do gasto real sobre o previsto — acima de 1 a obra estoura o orçamento
     * @param atrasos    quantas das etapas que já deveriam ter terminado ficam como Atrasada
     */
    private record ObraModelo(String nome, String endereco, String cliente, int inicio, int duracao,
                              StatusObra status, Integer referencia, Long orcamento, long base, double fator,
                              int atrasos, List<EtapaModelo> etapas, boolean lancamentoCancelado) {}

    private static final List<EtapaModelo> CONSTRUCAO = List.of(
        new EtapaModelo("Projeto e aprovações", "Projetos executivos, alvará e aprovação no condomínio", 4, List.of(
            new Gasto("Projeto arquitetônico e executivo", SERVICO_TERCEIRIZADO, "projetos", 0.75),
            new Gasto("Taxas de aprovação e alvará", SERVICO_TERCEIRIZADO, null, 0.25))),
        new EtapaModelo("Terraplenagem e fundações", "Limpeza do terreno, escavação, sapatas e baldrames", 11, List.of(
            new Gasto("Terraplenagem e escavação", SERVICO_TERCEIRIZADO, "terraplenagem", 0.3),
            new Gasto("Concreto para fundação", MATERIAL, "concreto", 0.35),
            new Gasto("Aço para sapatas e baldrames", MATERIAL, "aco", 0.2),
            new Gasto("Mão de obra — fundação", MAO_DE_OBRA, null, 0.15))),
        new EtapaModelo("Estrutura", "Pilares, vigas e lajes em concreto armado", 17, List.of(
            new Gasto("Concreto usinado — lajes e pilares", MATERIAL, "concreto", 0.4),
            new Gasto("Vergalhões e telas — estrutura", MATERIAL, "aco", 0.25),
            new Gasto("Locação de escoramento metálico", SERVICO_TERCEIRIZADO, "locacao", 0.1),
            new Gasto("Mão de obra — estrutura", MAO_DE_OBRA, null, 0.25))),
        new EtapaModelo("Alvenaria", "Vedação em blocos cerâmicos e vergas", 11, List.of(
            new Gasto("Blocos cerâmicos e argamassa", MATERIAL, "deposito", 0.55),
            new Gasto("Mão de obra — alvenaria", MAO_DE_OBRA, null, 0.45))),
        new EtapaModelo("Instalações elétricas", "Infraestrutura, fiação e quadros", 7, List.of(
            new Gasto("Instalação elétrica (material e serviço)", SERVICO_TERCEIRIZADO, "eletrica", 0.75),
            new Gasto("Quadro de distribuição e disjuntores", MATERIAL, "deposito", 0.25))),
        new EtapaModelo("Instalações hidráulicas", "Água fria/quente, esgoto e águas pluviais", 7, List.of(
            new Gasto("Instalação hidrossanitária", SERVICO_TERCEIRIZADO, "hidraulica", 0.7),
            new Gasto("Tubos, conexões e registros", MATERIAL, "deposito", 0.3))),
        new EtapaModelo("Cobertura", "Estrutura do telhado, telhas, calhas e rufos", 8, List.of(
            new Gasto("Estrutura de madeira e telhas", SERVICO_TERCEIRIZADO, "telhado", 0.8),
            new Gasto("Calhas e rufos", MATERIAL, "deposito", 0.2))),
        new EtapaModelo("Gesso e forros", "Forro de gesso, sancas e drywall", 4, List.of(
            new Gasto("Forro de gesso e sancas", SERVICO_TERCEIRIZADO, "gesso", 1.0))),
        new EtapaModelo("Revestimentos e pisos", "Contrapiso, porcelanato e revestimentos de áreas molhadas", 11, List.of(
            new Gasto("Porcelanato e argamassa", MATERIAL, "revestimentos", 0.6),
            new Gasto("Mão de obra — assentamento", MAO_DE_OBRA, null, 0.4))),
        new EtapaModelo("Esquadrias e vidros", "Janelas, portas de correr e box", 7, List.of(
            new Gasto("Esquadrias de alumínio e vidros temperados", SERVICO_TERCEIRIZADO, "esquadrias", 1.0))),
        new EtapaModelo("Pintura", "Massa corrida, pintura interna e externa", 6, List.of(
            new Gasto("Pintura interna e externa", SERVICO_TERCEIRIZADO, "pintura", 0.8),
            new Gasto("Tintas e massa corrida", MATERIAL, "deposito", 0.2))),
        new EtapaModelo("Paisagismo e área externa", "Jardim, calçadas e iluminação externa", 4, List.of(
            new Gasto("Projeto e execução de paisagismo", SERVICO_TERCEIRIZADO, "paisagismo", 1.0))),
        new EtapaModelo("Limpeza final e entrega", "Limpeza pós-obra, vistoria e entrega das chaves", 3, List.of(
            new Gasto("Limpeza pós-obra", MAO_DE_OBRA, null, 0.6),
            new Gasto("Caçambas para entulho", SERVICO_TERCEIRIZADO, "locacao", 0.4)))
    );

    private static final List<EtapaModelo> REFORMA = List.of(
        new EtapaModelo("Demolição e retirada", "Remoção de revestimentos antigos e entulho", 10, List.of(
            new Gasto("Caçambas para entulho", SERVICO_TERCEIRIZADO, "locacao", 0.4),
            new Gasto("Mão de obra — demolição", MAO_DE_OBRA, null, 0.6))),
        new EtapaModelo("Instalações elétricas", "Troca da fiação e novos pontos", 15, List.of(
            new Gasto("Revisão e ampliação elétrica", SERVICO_TERCEIRIZADO, "eletrica", 1.0))),
        new EtapaModelo("Instalações hidráulicas", "Troca de tubulação dos banheiros e cozinha", 15, List.of(
            new Gasto("Revisão hidráulica", SERVICO_TERCEIRIZADO, "hidraulica", 1.0))),
        new EtapaModelo("Gesso e forros", "Rebaixamento de forro e iluminação embutida", 10, List.of(
            new Gasto("Forro de gesso", SERVICO_TERCEIRIZADO, "gesso", 1.0))),
        new EtapaModelo("Revestimentos e pisos", "Novo porcelanato em toda a área social", 25, List.of(
            new Gasto("Porcelanato e argamassa", MATERIAL, "revestimentos", 0.6),
            new Gasto("Mão de obra — assentamento", MAO_DE_OBRA, null, 0.4))),
        new EtapaModelo("Pintura", "Pintura geral interna", 15, List.of(
            new Gasto("Pintura interna", SERVICO_TERCEIRIZADO, "pintura", 1.0))),
        new EtapaModelo("Limpeza final e entrega", "Limpeza e entrega ao cliente", 10, List.of(
            new Gasto("Limpeza pós-obra", MAO_DE_OBRA, null, 1.0)))
    );

    /** Itens das propostas (orçamentos de fornecedor), por fornecedor. Quantidades valem para uma casa de porte médio. */
    private static final Map<String, List<ItemModelo>> ITENS_PROPOSTA = Map.of(
        "concreto", List.of(
            new ItemModelo("Concreto usinado fck 30 MPa (m³)", 60, 110, 545.00),
            new ItemModelo("Concreto magro fck 15 MPa (m³)", 8, 18, 420.00),
            new ItemModelo("Bombeamento de concreto (diária)", 4, 9, 1850.00)),
        "aco", List.of(
            new ItemModelo("Vergalhão CA-50 10 mm — barra 12 m", 350, 800, 58.90),
            new ItemModelo("Vergalhão CA-50 8 mm — barra 12 m", 250, 550, 38.50),
            new ItemModelo("Tela soldada Q-138 (painel)", 30, 70, 289.00),
            new ItemModelo("Arame recozido 18 (kg)", 60, 140, 19.80)),
        "revestimentos", List.of(
            new ItemModelo("Porcelanato 90x90 retificado (m²)", 220, 450, 164.90),
            new ItemModelo("Argamassa AC-III (saco 20 kg)", 180, 380, 42.50),
            new ItemModelo("Rejunte epóxi (kg)", 30, 70, 68.00),
            new ItemModelo("Rodapé em porcelanato (m)", 140, 280, 37.90)),
        "esquadrias", List.of(
            new ItemModelo("Janela de alumínio com vidro temperado (un)", 8, 16, 3250.00),
            new ItemModelo("Porta de correr pele de vidro (un)", 2, 5, 9800.00),
            new ItemModelo("Box de vidro temperado (un)", 3, 6, 1890.00))
    );

    @Override
    @Transactional
    public void run(String... args) {
        if (funcionarioRepository.findByEmail(EMAIL_MARCADOR).isPresent()) {
            System.out.println(">>> Dados de demonstração já existem — seed ignorado");
            return;
        }

        Funcionario responsavel = criarFuncionarios();
        Map<String, Fornecedor> fornecedores = criarFornecedores();
        List<ObraModelo> modelos = obras();
        for (ObraModelo modelo : modelos) {
            criarObra(modelo, responsavel, fornecedores);
        }

        System.out.println(">>> Dados de demonstração criados: " + modelos.size() + " obras, "
            + fornecedores.size() + " fornecedores. Senha dos usuários: " + SENHA_PADRAO);
    }

    private Funcionario criarFuncionarios() {
        Funcionario andre = funcionario("André Paulino de Oliveira", EMAIL_MARCADOR, TipoCargo.ADMINISTRADOR, "(11) 99812-4410", true);
        funcionario("Beatriz Campos", "beatriz.campos@gestorobras.com.br", TipoCargo.ADMINISTRADOR, "(11) 98455-1023", true);
        funcionario("Rafael Monteiro", "rafael.monteiro@gestorobras.com.br", TipoCargo.EQUIPE, "(11) 97321-5588", true);
        funcionario("Larissa Fontes", "larissa.fontes@gestorobras.com.br", TipoCargo.EQUIPE, "(11) 99104-7731", true);
        funcionario("Diego Carvalho", "diego.carvalho@gestorobras.com.br", TipoCargo.EQUIPE, "(11) 98877-2290", true);
        funcionario("Tatiane Ribeiro", "tatiane.ribeiro@gestorobras.com.br", TipoCargo.EQUIPE, "(11) 97650-3314", true);
        funcionario("Henrique Sales", "henrique.sales@gestorobras.com.br", TipoCargo.EQUIPE, "(11) 99230-8845", true);
        funcionario("Vanessa Lopes", "vanessa.lopes@gestorobras.com.br", TipoCargo.EQUIPE, "(11) 98012-6672", true);
        funcionario("Camila Andrade", "camila.andrade@gestorobras.com.br", TipoCargo.EQUIPE, "(11) 97789-4103", true);
        funcionario("Rodrigo Pires", "rodrigo.pires@gestorobras.com.br", TipoCargo.EQUIPE, "(11) 99567-0018", false);
        return andre;
    }

    private Funcionario funcionario(String nome, String email, TipoCargo cargo, String telefone, boolean ativo) {
        Funcionario f = new Funcionario();
        f.setNome(nome);
        f.setEmail(email);
        f.setSenha(passwordEncoder.encode(SENHA_PADRAO));
        f.setCargo(cargo);
        f.setTelefone(telefone);
        f.setAtivo(ativo);
        return funcionarioRepository.save(f);
    }

    private Map<String, Fornecedor> criarFornecedores() {
        Map<String, Fornecedor> f = new LinkedHashMap<>();
        f.put("concreto", fornecedor("Concreteira Pedra Forte Ltda", "Concreto usinado", "(11) 4012-3300", "comercial@pedraforte.com.br", "Rod. Dom Pedro I, km 88 — Atibaia/SP", true));
        f.put("aco", fornecedor("Ferro & Aço Paulista Distribuidora", "Aço e ferragens", "(11) 4587-2210", "vendas@ferroacopaulista.com.br", "Av. Industrial, 1450 — Jundiaí/SP", true));
        f.put("deposito", fornecedor("Depósito Construmais", "Materiais de construção", "(11) 4411-9087", "pedidos@construmais.com.br", "Rua das Indústrias, 320 — Bragança Paulista/SP", true));
        f.put("terraplenagem", fornecedor("Terra Firme Terraplenagem", "Terraplenagem", "(11) 98123-4567", "contato@terrafirme.com.br", "Estrada Municipal, 900 — Atibaia/SP", true));
        f.put("eletrica", fornecedor("Volt Instalações Elétricas", "Instalações elétricas", "(11) 97456-1122", "orcamento@voltinstalacoes.com.br", "Rua Benedito Lopes, 77 — Atibaia/SP", true));
        f.put("hidraulica", fornecedor("Hidroserv Instalações Hidráulicas", "Instalações hidráulicas", "(11) 98654-3321", "contato@hidroserv.com.br", "Av. São João, 1200 — Atibaia/SP", true));
        f.put("telhado", fornecedor("Telhados Bom Teto", "Telhados e coberturas", "(11) 99321-7788", "bomteto@bomteto.com.br", "Rua Lucas Nogueira, 45 — Nazaré Paulista/SP", true));
        f.put("gesso", fornecedor("Gesso Arte Acabamentos", "Gesso e drywall", "(11) 98777-1209", "gessoarte@gessoarte.com.br", "Rua José Bim, 210 — Atibaia/SP", true));
        f.put("revestimentos", fornecedor("Casa do Porcelanato Revestimentos", "Revestimentos e pisos", "(11) 4416-5500", "vendas@casadoporcelanato.com.br", "Av. Jerônimo de Camargo, 3100 — Atibaia/SP", true));
        f.put("esquadrias", fornecedor("Vidraçaria e Esquadrias Cristal", "Esquadrias e vidros", "(11) 4402-8811", "cristal@esquadriascristal.com.br", "Rua Siqueira Campos, 560 — Atibaia/SP", true));
        f.put("pintura", fornecedor("Pinta Bem Serviços de Pintura", "Pintura", "(11) 99876-5432", "pintabem@pintabem.com.br", "Rua Clóvis Soares, 18 — Atibaia/SP", true));
        f.put("paisagismo", fornecedor("Verde Vivo Paisagismo", "Paisagismo", "(11) 98234-9900", "projetos@verdevivo.com.br", "Estrada do Rosário, 2500 — Atibaia/SP", true));
        f.put("projetos", fornecedor("Studio Linha Arquitetura", "Projetos de arquitetura", "(11) 3456-7812", "studio@linhaarquitetura.com.br", "Av. Paulista, 1842, cj. 155 — São Paulo/SP", true));
        f.put("locacao", fornecedor("Loca Obra Equipamentos e Caçambas", "Locação de equipamentos", "(11) 4418-3030", "locacao@locaobra.com.br", "Rod. Fernão Dias, km 37 — Atibaia/SP", true));
        f.put("madeireira", fornecedor("Madeireira Santa Rita", "Madeiras", "(11) 4413-2671", "vendas@madeireirasantarita.com.br", "Rua do Comércio, 99 — Piracaia/SP", false));
        return f;
    }

    private Fornecedor fornecedor(String nome, String tipo, String telefone, String email, String endereco, boolean ativo) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome);
        f.setTipoServico(tipo);
        f.setTelefone(telefone);
        f.setEmail(email);
        f.setEndereco(endereco);
        f.setAtivo(ativo);
        return fornecedorRepository.save(f);
    }

    private List<ObraModelo> obras() {
        return List.of(
            new ObraModelo("Residência Reserva das Palmeiras — Lote 12", "Cond. Reserva das Palmeiras, Lote 12 — Atibaia/SP",
                "Ricardo e Fernanda Almeida", -300, 420, StatusObra.EM_ANDAMENTO, null, 1_850_000L, 1_850_000, 0.97, 0, CONSTRUCAO, true),
            new ObraModelo("Residência Alto da Serra — Lote 7", "Cond. Alto da Serra, Lote 7 — Atibaia/SP",
                "Marcelo Tavares", -200, 480, StatusObra.EM_ANDAMENTO, null, 2_400_000L, 2_400_000, 1.0, 0, CONSTRUCAO, false),
            // Prazo termina em ~20 dias e já passou de 80% do orçamento
            new ObraModelo("Casa Jardim das Acácias — Lote 3", "Cond. Jardim das Acácias, Lote 3 — Bragança Paulista/SP",
                "Patrícia Moura", -380, 400, StatusObra.EM_ANDAMENTO, null, 1_300_000L, 1_300_000, 1.08, 0, CONSTRUCAO, false),
            // Prazo vencido, etapas atrasadas e orçamento estourado
            new ObraModelo("Residência Vila Toscana — Lote 21", "Cond. Vila Toscana, Lote 21 — Atibaia/SP",
                "Eduardo e Camila Rezende", -420, 390, StatusObra.EM_ANDAMENTO, null, 1_650_000L, 1_650_000, 1.3, 2, CONSTRUCAO, true),
            new ObraModelo("Residência Quinta do Lago — Lote 5", "Cond. Quinta do Lago, Lote 5 — Nazaré Paulista/SP",
                "Gustavo Lima Ferraz", -90, 450, StatusObra.EM_ANDAMENTO, null, 2_950_000L, 2_950_000, 0.95, 1, CONSTRUCAO, false),
            // Sem orçamento definido no Dashboard Financeiro
            new ObraModelo("Casa Bosque dos Ipês — Lote 15", "Cond. Bosque dos Ipês, Lote 15 — Atibaia/SP",
                "Juliana Prado", -30, 400, StatusObra.EM_ANDAMENTO, null, null, 1_600_000, 1.0, 0, CONSTRUCAO, false),
            new ObraModelo("Residência Recanto Verde — Lote 9", "Cond. Recanto Verde, Lote 9 — Piracaia/SP",
                "Antônio Carlos Bastos", -260, 420, StatusObra.PAUSADA, -45, 1_500_000L, 1_500_000, 1.0, 0, CONSTRUCAO, false),
            new ObraModelo("Residência Portal do Sol — Lote 2", "Cond. Portal do Sol, Lote 2 — Atibaia/SP",
                "Sérgio e Luana Martins", -700, 450, StatusObra.CONCLUIDA, -250, 1_400_000L, 1_400_000, 0.96, 0, CONSTRUCAO, true),
            new ObraModelo("Casa Villa Bella — Lote 18", "Cond. Villa Bella, Lote 18 — Bragança Paulista/SP",
                "Renata Siqueira", -600, 360, StatusObra.CONCLUIDA, -240, 980_000L, 980_000, 1.04, 0, CONSTRUCAO, false),
            new ObraModelo("Reforma Residência Mirante — Lote 30", "Cond. Mirante da Serra, Lote 30 — Atibaia/SP",
                "Paulo Henrique Duarte", -60, 120, StatusObra.EM_ANDAMENTO, null, 320_000L, 320_000, 1.0, 0, REFORMA, false),
            new ObraModelo("Residência Colinas do Vale — Lote 11", "Cond. Colinas do Vale, Lote 11 — Piracaia/SP",
                "Fábio Nogueira", -150, 300, StatusObra.CANCELADA, -100, 1_100_000L, 1_100_000, 1.0, 0, CONSTRUCAO, true),
            // Começa daqui a 20 dias: cronograma todo "Não Iniciada" e nenhum gasto
            new ObraModelo("Residência Parque das Águas — Lote 4", "Cond. Parque das Águas, Lote 4 — Atibaia/SP",
                "Cláudia e Roberto Vieira", 20, 480, StatusObra.EM_ANDAMENTO, null, 3_200_000L, 3_200_000, 1.0, 0, CONSTRUCAO, false),
            new ObraModelo("Casa Terras Altas — Lote 26", "Cond. Terras Altas, Lote 26 — Nazaré Paulista/SP",
                "Leonardo Assis", -500, 420, StatusObra.CONCLUIDA, -80, 1_750_000L, 1_750_000, 0.92, 0, CONSTRUCAO, false),
            new ObraModelo("Residência Solar das Hortênsias — Lote 8", "Cond. Solar das Hortênsias, Lote 8 — Atibaia/SP",
                "Mariana Castro", -150, 360, StatusObra.PAUSADA, -20, 1_200_000L, 1_200_000, 1.0, 0, CONSTRUCAO, false)
        );
    }

    private void criarObra(ObraModelo m, Funcionario responsavel, Map<String, Fornecedor> fornecedores) {
        LocalDate inicio = hoje.plusDays(m.inicio());

        Obra obra = new Obra();
        obra.setUsuario(responsavel);
        obra.setNome(m.nome());
        obra.setEndereco(m.endereco());
        obra.setCliente(m.cliente());
        obra.setDataInicio(inicio);
        obra.setPrazoConclusao(inicio.plusDays(m.duracao()));
        obra.setStatus(m.status());
        obra.setOrcamento(m.orcamento() == null ? null : BigDecimal.valueOf(m.orcamento()).setScale(2));
        obraRepository.save(obra);

        LocalDate referencia = m.referencia() == null ? hoje : hoje.plusDays(m.referencia());
        List<Etapa> etapas = criarCronograma(m, obra, inicio, referencia);
        criarCustos(m, obra, etapas, referencia, fornecedores);
        criarPropostas(m, obra, etapas, fornecedores);
    }

    private List<Etapa> criarCronograma(ObraModelo m, Obra obra, LocalDate inicio, LocalDate referencia) {
        int pesoTotal = m.etapas().stream().mapToInt(EtapaModelo::peso).sum();
        int sobreposicao = m.duracao() / 50;

        // Etapas em sequência, com leve sobreposição, cada uma com duração proporcional ao peso
        List<LocalDate[]> previstas = new ArrayList<>();
        int acumulado = 0;
        for (int k = 0; k < m.etapas().size(); k++) {
            long ini = Math.round((double) acumulado / pesoTotal * m.duracao()) - (k > 0 ? sobreposicao : 0);
            acumulado += m.etapas().get(k).peso();
            long fim = Math.round((double) acumulado / pesoTotal * m.duracao());
            previstas.add(new LocalDate[]{inicio.plusDays(Math.max(0, ini)), inicio.plusDays(fim)});
        }

        // As que já deveriam ter terminado formam um prefixo; as últimas "atrasos" delas ficam atrasadas
        int vencidas = (int) previstas.stream().filter(p -> p[1].isBefore(referencia)).count();

        List<Etapa> etapas = new ArrayList<>();
        for (int k = 0; k < m.etapas().size(); k++) {
            EtapaModelo modelo = m.etapas().get(k);
            LocalDate ini = previstas.get(k)[0];
            LocalDate fim = previstas.get(k)[1];

            Etapa e = new Etapa();
            e.setObra(obra);
            e.setNome(modelo.nome());
            e.setDescricao(modelo.descricao());
            e.setDataPrevistaInicio(ini);
            e.setDataPrevistaFim(fim);

            if (m.status() == StatusObra.CONCLUIDA || (fim.isBefore(referencia) && k < vencidas - m.atrasos())) {
                LocalDate realInicio = ini.plusDays(entre(0, 4));
                LocalDate realFim = minimo(fim.plusDays(entre(-3, 6)), referencia);
                e.setDataRealInicio(minimo(realInicio, realFim));
                e.setDataRealFim(realFim);
                e.setPercentualProgresso(100);
                e.setStatus(StatusEtapa.CONCLUIDA);
            } else if (fim.isBefore(referencia)) {
                e.setDataRealInicio(ini.plusDays(entre(0, 5)));
                e.setPercentualProgresso(50 + 5 * entre(0, 7));
                e.setStatus(StatusEtapa.ATRASADA);
            } else if (!ini.isAfter(referencia)) {
                long total = Math.max(1, ChronoUnit.DAYS.between(ini, fim));
                long decorridos = ChronoUnit.DAYS.between(ini, referencia);
                int progresso = (int) Math.round(100.0 * decorridos / total / 5) * 5;
                e.setDataRealInicio(minimo(ini.plusDays(entre(0, 3)), referencia));
                e.setPercentualProgresso(Math.min(95, Math.max(5, progresso)));
                e.setStatus(StatusEtapa.EM_ANDAMENTO);
            } else {
                e.setPercentualProgresso(0);
                e.setStatus(StatusEtapa.NAO_INICIADA);
            }
            etapas.add(etapaRepository.save(e));
        }
        return etapas;
    }

    /** Gasto de cada etapa proporcional ao seu peso e ao progresso, multiplicado pelo fator da obra. */
    private void criarCustos(ObraModelo m, Obra obra, List<Etapa> etapas, LocalDate referencia,
                             Map<String, Fornecedor> fornecedores) {
        Custo primeiroMaterial = null;

        for (int k = 0; k < etapas.size(); k++) {
            Etapa etapa = etapas.get(k);
            if (etapa.getPercentualProgresso() == 0) continue;

            EtapaModelo modelo = m.etapas().get(k);
            double gastoEtapa = m.base() * modelo.peso() / 100.0 * etapa.getPercentualProgresso() / 100.0 * m.fator();
            LocalDate de = etapa.getDataRealInicio();
            LocalDate ate = minimo(etapa.getDataRealFim() != null ? etapa.getDataRealFim() : referencia, hoje);

            for (Gasto g : modelo.gastos()) {
                long valor = Math.round(gastoEtapa * g.fracao() * (0.9 + random.nextDouble() * 0.2) / 10) * 10;
                if (valor <= 0) continue;

                LocalDate data = de.plusDays(entre(0, (int) Math.max(0, ChronoUnit.DAYS.between(de, ate))));
                Fornecedor fornecedor = g.fornecedor() == null ? null : fornecedores.get(g.fornecedor());
                StatusCusto status = m.status() == StatusObra.CONCLUIDA || data.isBefore(hoje.minusDays(20))
                    ? StatusCusto.PAGO
                    : StatusCusto.PENDENTE;

                Custo c = custo(obra, fornecedor, g.categoria(), BigDecimal.valueOf(valor), data, g.descricao(), status);
                if (primeiroMaterial == null && g.categoria() == MATERIAL && fornecedor != null) {
                    primeiroMaterial = c;
                }
            }
        }

        // Um pedido lançado em duplicidade e cancelado, para o filtro de status e a regra "sem cancelados"
        if (m.lancamentoCancelado() && primeiroMaterial != null) {
            custo(obra, primeiroMaterial.getFornecedor(), MATERIAL, primeiroMaterial.getValor(),
                primeiroMaterial.getData(), "Pedido em duplicidade — " + primeiroMaterial.getDescricao(), StatusCusto.CANCELADO);
        }
    }

    private Custo custo(Obra obra, Fornecedor fornecedor, CategoriaCusto categoria, BigDecimal valor,
                        LocalDate data, String descricao, StatusCusto status) {
        Custo c = new Custo();
        c.setObra(obra);
        c.setFornecedor(fornecedor);
        c.setCategoria(categoria);
        c.setValor(valor.setScale(2));
        c.setData(data);
        c.setDescricao(descricao);
        c.setStatus(status);
        if (fornecedor != null) {
            fornecedor.getObras().add(obra);
        }
        return custoRepository.save(c);
    }

    /** Proposta enviada ~15 dias antes da primeira etapa que usa o fornecedor, se essa data já chegou. */
    private void criarPropostas(ObraModelo m, Obra obra, List<Etapa> etapas, Map<String, Fornecedor> fornecedores) {
        double porte = m.base() / 1_800_000.0;

        for (Map.Entry<String, List<ItemModelo>> entrada : ITENS_PROPOSTA.entrySet()) {
            String chave = entrada.getKey();
            int k = indiceDaPrimeiraEtapaCom(m, chave);
            if (k < 0) continue;

            LocalDate data = etapas.get(k).getDataPrevistaInicio().minusDays(15);
            if (data.isAfter(hoje)) continue;

            Fornecedor fornecedor = fornecedores.get(chave);
            Orcamento orcamento = new Orcamento();
            orcamento.setFornecedor(fornecedor);
            orcamento.setObra(obra);
            orcamento.setDescricao("Proposta — " + m.etapas().get(k).nome());
            orcamento.setDataOrcamento(data);

            BigDecimal total = BigDecimal.ZERO;
            for (ItemModelo im : entrada.getValue()) {
                double qtd = Math.max(1, Math.round((im.qtdMin() + random.nextDouble() * (im.qtdMax() - im.qtdMin())) * porte));
                OrcamentoItem item = new OrcamentoItem();
                item.setOrcamento(orcamento);
                item.setDescricaoMaterial(im.descricao());
                item.setQuantidade(BigDecimal.valueOf(qtd).setScale(3));
                item.setPrecoUnitario(BigDecimal.valueOf(im.preco()).setScale(2, RoundingMode.HALF_UP));
                item.setSubtotal(item.getQuantidade().multiply(item.getPrecoUnitario()).setScale(2, RoundingMode.HALF_UP));
                orcamento.getItens().add(item);
                total = total.add(item.getSubtotal());
            }
            orcamento.setValorTotal(total);
            orcamentoRepository.save(orcamento);
            fornecedor.getObras().add(obra);
        }
    }

    private int indiceDaPrimeiraEtapaCom(ObraModelo m, String fornecedor) {
        for (int k = 0; k < m.etapas().size(); k++) {
            if (m.etapas().get(k).gastos().stream().anyMatch(g -> fornecedor.equals(g.fornecedor()))) {
                return k;
            }
        }
        return -1;
    }

    private int entre(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static LocalDate minimo(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
