package aor.paj.projecto5.bean;

import aor.paj.projecto5.dao.UserDao;
import aor.paj.projecto5.entity.UserEntity;
import aor.paj.projecto5.utils.UserRoles;
import aor.paj.projecto5.utils.UserState;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
public class DatabaseBootstrap {

    @EJB
    private UserDao userDao;

    @PostConstruct
    public void init() {
        // Verifica se o admin já existe para não criar duplicados
        UserEntity existingAdmin = userDao.findUserByUsername("admin");

        if (existingAdmin == null) {
            UserEntity admin = new UserEntity();
            
            // Preenchimento dos campos obrigatórios conforme a tua UserEntity
            admin.setUsername("admin");
            admin.setPassword("admin123"); // Idealmente usar hash aqui depois
            admin.setEmail("admin@paj.com");
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setContact("900000000"); // Campo unique e nullable=false
            admin.setUserRole(UserRoles.ADMIN); // Usando o teu Enum
            admin.setState(UserState.ACTIVE);

            try {
                userDao.persist(admin);
                System.out.println(">>> [BOOTSTRAP] Utilizador ADMIN criado com sucesso.");
            } catch (Exception e) {
                System.err.println(">>> [BOOTSTRAP] Erro ao criar admin: " + e.getMessage());
            }
        } else {
            System.out.println(">>> [BOOTSTRAP] Utilizador ADMIN já existe.");
        }
    }
}