package pt.uc.dei.proj5.bean;

import aor.paj.projecto5.bean.LeadsBean;
import aor.paj.projecto5.bean.TokenBean;
import aor.paj.projecto5.dao.LeadDao;
import aor.paj.projecto5.dao.UserDao;
import aor.paj.projecto5.dto.LeadDTO;
import aor.paj.projecto5.entity.LeadEntity;
import aor.paj.projecto5.entity.UserEntity;
import aor.paj.projecto5.utils.LeadState;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LeadsBeanTest {

    @InjectMocks
    private LeadsBean leadsBean;

    @Mock
    private LeadDao leadDao;

    @Mock
    private UserDao userDao;

    @Mock
    private TokenBean tokenBean;

    private UserEntity spyUser;
    private LeadEntity leadEntity;
    private LeadDTO leadDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // 1. Setup do Utilizador via Spy (ID 1)
        UserEntity u = new UserEntity();
        u.setFirstName("Joao");
        spyUser = spy(u);
        doReturn(1L).when(spyUser).getId();

        // 2. Setup da Lead com ID simulado (ID 10) via Spy se necessário,
        // ou apenas mockando o retorno do DAO
        leadEntity = spy(new LeadEntity());
        leadEntity.setTitulo("Lead Antiga");
        leadEntity.setOwner(spyUser);
        leadEntity.setLeadState(LeadState.NOVO);
        doReturn(10L).when(leadEntity).getId();

        // 3. Setup do DTO
        leadDTO = new LeadDTO();
        leadDTO.setTitle("Lead Nova");
        leadDTO.setDescription("Descricao Nova");
        leadDTO.setState(1); // NOVO
    }

    // --- TESTES DE USER ---

    @Test
    @DisplayName("Deve adicionar lead com sucesso via token")
    void testAddLead() {
        when(tokenBean.getUserEntityByToken("valid-token")).thenReturn(spyUser);

        LeadDTO result = leadsBean.addLead("valid-token", leadDTO);

        assertNotNull(result);
        assertEquals("Lead Nova", result.getTitle());
        verify(leadDao, times(1)).persist(any(LeadEntity.class));
    }

    @Test
    @DisplayName("Deve editar lead existente")
    void testEditLead() {
        when(leadDao.getLeadByLeadID(10L)).thenReturn(leadEntity);

        LeadDTO result = leadsBean.editLead(10L, leadDTO);

        assertNotNull(result);
        assertEquals("Lead Nova", result.getTitle());
        verify(leadDao, times(1)).merge(leadEntity);
    }

    @Test
    @DisplayName("Deve realizar soft delete de uma lead")
    void testSoftDeleteLead() {
        when(leadDao.getLeadByLeadID(10L)).thenReturn(leadEntity);

        leadsBean.softDeleteLead(10L);

        assertTrue(leadEntity.isSoftDeleted());
        verify(leadDao, times(1)).merge(leadEntity);
    }

    // --- TESTES DE ADMIN ---

    @Test
    @DisplayName("Admin: Deve adicionar lead diretamente a um utilizador")
    void testAddLeadToUserSuccess() {
        when(userDao.find(1L)).thenReturn(spyUser);

        LeadDTO result = leadsBean.addLeadToUser(1L, leadDTO);

        assertNotNull(result);
        verify(leadDao, times(1)).persist(any(LeadEntity.class));
    }

    @Test
    @DisplayName("Admin: Deve falhar ao adicionar lead se utilizador for inexistente")
    void testAddLeadToUserNotFound() {
        when(userDao.find(99L)).thenReturn(null);

        assertThrows(WebApplicationException.class, () -> {
            leadsBean.addLeadToUser(99L, leadDTO);
        });
    }

    @Test
    @DisplayName("Admin: Deve realizar hard delete")
    void testHardDeleteLead() {
        when(leadDao.getLeadByLeadID(10L)).thenReturn(leadEntity);

        leadsBean.hardDeleteLead(10L);

        verify(leadDao, times(1)).remove(leadEntity);
    }

    @Test
    @DisplayName("Admin: Deve falhar hard delete se lead não existir")
    void testHardDeleteNotFound() {
        when(leadDao.getLeadByLeadID(99L)).thenReturn(null);

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> {
            leadsBean.hardDeleteLead(99L);
        });
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    @DisplayName("Admin: Deve atualizar em massa (Soft Delete)")
    void testSoftDeleteAllFromUser() {
        when(userDao.find(1L)).thenReturn(spyUser);
        when(leadDao.bulkUpdateSoftDelete(1L, true)).thenReturn(5);

        int result = leadsBean.softDeleteAllFromUser(1L);

        assertEquals(5, result);
        verify(leadDao).bulkUpdateSoftDelete(1L, true);
    }

    @Test
    @DisplayName("Admin: Super Edit deve alterar estado de soft delete")
    void testAdminSuperEdit() {
        when(leadDao.getLeadByLeadID(10L)).thenReturn(leadEntity);
        leadDTO.setSoftDeleted(true);

        LeadDTO result = leadsBean.adminSuperEdit(10L, leadDTO);

        assertTrue(result.isSoftDeleted());
        verify(leadDao).merge(leadEntity);
    }
}