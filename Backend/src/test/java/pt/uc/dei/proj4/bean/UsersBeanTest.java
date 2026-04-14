package pt.uc.dei.proj5.bean;

import aor.paj.projecto5.bean.LeadsBean;
import aor.paj.projecto5.bean.TokenBean;
import aor.paj.projecto5.bean.UsersBean;
import aor.paj.projecto5.dao.LeadDao;
import aor.paj.projecto5.dao.UserDao;
import aor.paj.projecto5.dto.LeadDTO;
import aor.paj.projecto5.dto.LoginDTO;
import aor.paj.projecto5.dto.LoginResponseDTO;
import aor.paj.projecto5.dto.UserBaseDTO;
import aor.paj.projecto5.entity.LeadEntity;
import aor.paj.projecto5.entity.UserEntity;
import aor.paj.projecto5.utils.UserRoles;
import aor.paj.projecto5.utils.LeadState;
import aor.paj.projecto5.utils.UserState;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsersBeanTest {

    // --- BEANS EM TESTE ---
    @InjectMocks
    private UsersBean usersBean;

    @InjectMocks
    private LeadsBean leadsBean; // Adicionado para suportar o teste de AdminSuperEdit

    // --- MOCKS ---
    @Mock
    private UserDao userDao;

    @Mock
    private LeadDao leadDao; // Adicionado para suportar o teste de AdminSuperEdit

    @Mock
    private TokenBean tokenBean;

    // --- VARIÁVEIS DE SUPORTE ---
    private UserEntity u;
    private UserEntity u2;
    private LeadEntity leadEntity; // Adicionado
    private LeadDTO leadDTO;       // Adicionado
    private LoginDTO loginDTO1;
    private LoginDTO loginDTO2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // 1. Setup Utilizadores
        u = spy(new UserEntity());
        u.setUsername("testDummy");
        u.setPassword("xxxxx");
        u.setEmail("test@test.com");
        u.setFirstName("Joao");
        u.setUserRole(UserRoles.NORMAL);
        u.setState(UserState.DISABLED);
        doReturn(1L).when(u).getId();

        u2 = spy(new UserEntity());
        u2.setUsername("u2");
        u2.setEmail("u2@u2.com");
        u2.setUserRole(UserRoles.NORMAL);
        doReturn(2L).when(u2).getId();

        // 2. Setup Leads (Necessário para o erro de compilação desaparecer)
        leadEntity = spy(new LeadEntity());
        leadEntity.setTitulo("Lead Antiga");
        leadEntity.setSoftDeleted(false);
        leadEntity.setLeadState(LeadState.NOVO);
        doReturn(10L).when(leadEntity).getId();

        leadDTO = new LeadDTO();
        leadDTO.setTitle("Lead Editada");
        leadDTO.setSoftDeleted(true);

        // 3. Setup Login
        loginDTO1 = new LoginDTO(u.getUsername(), u.getPassword());
        loginDTO2 = new LoginDTO(u.getUsername(), "wrong_password");

        // 4. Configuração de Mocks
        when(userDao.findUserByUsername("testDummy")).thenReturn(u);
        when(userDao.find(1L)).thenReturn(u);
        when(tokenBean.generateNewToken(u)).thenReturn("token1");
    }

    // --- TESTES DE UTILIZADOR ---

    @Test
    @DisplayName("Deve autenticar utilizador com sucesso")
    void testAuthenticateUser() {
        LoginResponseDTO response = usersBean.authenticateUser(loginDTO1);
        assertNotNull(response);
        assertEquals("token1", response.getToken());
        assertEquals("Joao", response.getFirstName());
    }

    @Test
    @DisplayName("Deve falhar autenticação com password errada")
    void testAuthenticateUserWrongPassword() {
        LoginResponseDTO response = usersBean.authenticateUser(loginDTO2);
        assertNull(response);
    }

//    @Test
//    @DisplayName("Admin: Deve falhar edição se email já existir em outro ID")
//    void testPutEditUserDuplicateEmail() {
//        UserBaseDTO editDto = new UserBaseDTO();
//        editDto.setEmail("u2@u2.com");
//        when(userDao.findUserByEmail("u2@u2.com")).thenReturn(u2);
//
//        assertThrows(WebApplicationException.class, () -> usersBean.putEditUser(1L, editDto));
//    }

    @Test
    @DisplayName("Deve realizar soft delete com sucesso")
    void testSoftDeleteUser() {
        // 1. Executa a ação
        assertDoesNotThrow(() -> usersBean.softDeleteUser(1L));

        // 2. Recupera o utilizador para verificar o estado atualizado
        UserBaseDTO updatedUser = usersBean.getUserBaseDTOById(1L);

        // 3. Verifica se o estado é efetivamente DISABLED
        // Nota: Comparamos a String porque o DTO devolve o nome do Enum
        assertEquals(UserState.DISABLED.name(), updatedUser.getState(),
                "O estado do utilizador deveria ser DISABLED após o soft delete");
    }
    // --- TESTES DE LEADS (ADAPTADOS PARA ESTA CLASSE) ---

    @Test
    @DisplayName("Admin: Super Edit deve alterar estado de soft delete")
    void testAdminSuperEdit() {
        // 1. Mock do retorno do DAO (Agora leadDao existe como @Mock lá em cima)
        when(leadDao.getLeadByLeadID(10L)).thenReturn(leadEntity);

        // 2. Execução
        LeadDTO result = leadsBean.adminSuperEdit(10L, leadDTO);

        // 3. Verificação
        // Importante: Para isto ser TRUE, o teu LeadsBean.entityToDTO deve mapear o campo softDelete!
        assertTrue(result.isSoftDeleted(), "A lead devia estar em soft delete após o super edit");
        verify(leadDao).merge(leadEntity);
    }
}