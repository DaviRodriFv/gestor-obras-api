package gestor_obras_api.security;

import gestor_obras_api.auth.service.UserDetailsServiceImpl;
import gestor_obras_api.config.JwtAuthFilter;
import gestor_obras_api.config.JwtService;
import gestor_obras_api.config.SecurityConfig;
import gestor_obras_api.funcionario.model.Funcionario;
import gestor_obras_api.funcionario.model.TipoCargo;
import gestor_obras_api.funcionario.repository.FuncionarioRepository;
import gestor_obras_api.relatorio.controller.RelatorioController;
import gestor_obras_api.relatorio.service.RelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O relatório da obra traz dados financeiros, então segue a regra deles: só ADMINISTRADOR. */
@WebMvcTest(controllers = RelatorioController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class RelatorioControllerSecurityTest {

    private static final String RELATORIO = "/api/obras/" + UUID.randomUUID() + "/relatorio";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RelatorioService relatorioService;

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
    void administradorPodeEmitirRelatorio() throws Exception {
        mockMvc.perform(get(RELATORIO).with(user(criarFuncionario(TipoCargo.ADMINISTRADOR))))
                .andExpect(status().isOk());
    }

    @Test
    void equipeNaoPodeEmitirRelatorio() throws Exception {
        mockMvc.perform(get(RELATORIO).with(user(criarFuncionario(TipoCargo.EQUIPE))))
                .andExpect(status().isForbidden());
    }

    @Test
    void semAutenticacaoNaoPodeEmitirRelatorio() throws Exception {
        mockMvc.perform(get(RELATORIO))
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
