package gestor_obras_api.security;

import gestor_obras_api.auth.service.UserDetailsServiceImpl;
import gestor_obras_api.config.JwtAuthFilter;
import gestor_obras_api.config.JwtService;
import gestor_obras_api.config.SecurityConfig;
import gestor_obras_api.custo.controller.CustoController;
import gestor_obras_api.custo.service.CustoService;
import gestor_obras_api.financeiro.controller.DashboardFinanceiroController;
import gestor_obras_api.financeiro.service.DashboardFinanceiroService;
import gestor_obras_api.funcionario.model.Funcionario;
import gestor_obras_api.funcionario.model.TipoCargo;
import gestor_obras_api.funcionario.repository.FuncionarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Dados financeiros (custos, dashboard financeiro e orçamento da obra) são exclusivos do ADMINISTRADOR. */
@WebMvcTest(controllers = {CustoController.class, DashboardFinanceiroController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class FinanceiroControllerSecurityTest {

    private static final String OBRA = "/api/obras/" + UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustoService custoService;

    @MockitoBean
    private DashboardFinanceiroService dashboardFinanceiroService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private FuncionarioRepository funcionarioRepository;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void administradorPodeListarCustos() throws Exception {
        mockMvc.perform(get(OBRA + "/custos")
                        .with(user(criarFuncionario(TipoCargo.ADMINISTRADOR))))
                .andExpect(status().isOk());
    }

    @Test
    void administradorPodeAcessarDashboardFinanceiro() throws Exception {
        mockMvc.perform(get(OBRA + "/financeiro")
                        .with(user(criarFuncionario(TipoCargo.ADMINISTRADOR))))
                .andExpect(status().isOk());
    }

    @Test
    void equipeNaoPodeListarCustos() throws Exception {
        mockMvc.perform(get(OBRA + "/custos")
                        .with(user(criarFuncionario(TipoCargo.EQUIPE))))
                .andExpect(status().isForbidden());
    }

    @Test
    void equipeNaoPodeVerCustosPorCategoria() throws Exception {
        mockMvc.perform(get(OBRA + "/custos/por-categoria")
                        .with(user(criarFuncionario(TipoCargo.EQUIPE))))
                .andExpect(status().isForbidden());
    }

    @Test
    void equipeNaoPodeAcessarDashboardFinanceiro() throws Exception {
        mockMvc.perform(get(OBRA + "/financeiro")
                        .with(user(criarFuncionario(TipoCargo.EQUIPE))))
                .andExpect(status().isForbidden());
    }

    @Test
    void equipeNaoPodeDefinirOrcamento() throws Exception {
        mockMvc.perform(put(OBRA + "/orcamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\": 1000}")
                        .with(user(criarFuncionario(TipoCargo.EQUIPE))))
                .andExpect(status().isForbidden());
    }

    @Test
    void semAutenticacaoNaoPodeAcessarFinanceiro() throws Exception {
        mockMvc.perform(get(OBRA + "/financeiro"))
                .andExpect(status().isUnauthorized());
    }

    private Funcionario criarFuncionario(TipoCargo cargo) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("Teste");
        funcionario.setEmail("teste@email.com");
        funcionario.setSenha("senha");
        funcionario.setTelefone("(11) 99999-9999");
        funcionario.setAtivo(true);
        funcionario.setCargo(cargo);
        return funcionario;
    }
}
