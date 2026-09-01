package gestor_obras_api.config;

import gestor_obras_api.funcionario.model.Funcionario;
import gestor_obras_api.funcionario.model.TipoCargo;
import gestor_obras_api.funcionario.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (funcionarioRepository.findByEmail("admgestor@gmail.com").isEmpty()) {
            Funcionario admin = new Funcionario();
            admin.setNome("Administrador");
            admin.setEmail("admgestor@gmail.com");
            admin.setSenha(passwordEncoder.encode("adm@1234"));
            admin.setCargo(TipoCargo.ADMINISTRADOR);
            admin.setTelefone("(00) 00000-0000");
            admin.setAtivo(true);
            funcionarioRepository.save(admin);
            System.out.println(">>> Usuário admin criado: admgestor@gmail.com");
        }

        if (funcionarioRepository.findByEmail("equipe@gestor.com").isEmpty()) {
            Funcionario equipe = new Funcionario();
            equipe.setNome("Funcionário Padrão");
            equipe.setEmail("equipe@gestor.com");
            equipe.setSenha(passwordEncoder.encode("equipe@1234"));
            equipe.setCargo(TipoCargo.EQUIPE);
            equipe.setTelefone("(00) 11111-1111");
            equipe.setAtivo(true);
            funcionarioRepository.save(equipe);
            System.out.println(">>> Usuário equipe criado: equipe@gestor.com");
        }
    }
}
