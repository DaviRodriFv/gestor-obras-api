package gestor_obras_api.security;

import gestor_obras_api.auth.service.UserDetailsServiceImpl;
import gestor_obras_api.config.JwtAuthFilter;
import gestor_obras_api.config.JwtService;
import gestor_obras_api.config.SecurityConfig;
import gestor_obras_api.funcionario.model.Funcionario;
import gestor_obras_api.funcionario.model.TipoCargo;
import gestor_obras_api.funcionario.repository.FuncionarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = gestor_obras_api.financeiro.controller.FinanceiroController.class)
@Import(SecurityConfig.class)
class FinanceiroControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void proprietarioPodeAcessarDashboardFinanceiro() throws Exception {
        mockMvc.perform(get("/api/financeiro/dashboard")
                        .with(user(criarFuncionario("Proprietario"))))
                .andExpect(status().isOk());
    }

    @Test
    void proprietarioNaoPodeCriarEntradaFinanceira() throws Exception {
        mockMvc.perform(post("/api/financeiro/entrada")
                        .with(user(criarFuncionario("Proprietario"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void equipePodeCriarEntradaFinanceira() throws Exception {
        mockMvc.perform(post("/api/financeiro/entrada")
                        .with(user(criarFuncionario("Equipe"))))
                .andExpect(status().isOk());
    }

    private Funcionario criarFuncionario(String role) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("Teste");
        funcionario.setEmail("teste@email.com");
        funcionario.setSenha("senha");
        funcionario.setTelefone("(11) 99999-9999");
        funcionario.setAtivo(true);
        funcionario.setRole(role);
        funcionario.setCargo(role.equals("Proprietario") ? TipoCargo.EQUIPE : TipoCargo.EQUIPE);
        return funcionario;
    }
}
