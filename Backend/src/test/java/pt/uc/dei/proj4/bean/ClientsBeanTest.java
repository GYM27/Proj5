package pt.uc.dei.proj5.bean;

import aor.paj.projecto5.bean.ClientsBean;
import aor.paj.projecto5.bean.TokenBean;
import aor.paj.projecto5.dao.ClientsDao;
import aor.paj.projecto5.dao.UserDao;
import aor.paj.projecto5.dto.ClientsDTO;
import aor.paj.projecto5.entity.ClientsEntity;
import aor.paj.projecto5.entity.UserEntity;
import aor.paj.projecto5.utils.UserRoles;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientsBeanTest {

    @InjectMocks
    @Spy
    private ClientsBean clientsBean;

    @Mock
    private ClientsDao clientsDao;

    @Mock
    private UserDao userDao;

    @Mock
    private TokenBean tokenBean;

    private UserEntity spyUser;
    private UserEntity otherUser;
    private ClientsEntity myClient;
    private ClientsDTO clientsDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Setup do Utilizador Principal (ID 1)
        UserEntity u = new UserEntity();
        u.setUserRole(UserRoles.NORMAL);
        spyUser = spy(u);
        doReturn(1L).when(spyUser).getId();

        // Setup de Outro Utilizador (ID 2)
        UserEntity u2 = new UserEntity();
        otherUser = spy(u2);
        doReturn(2L).when(otherUser).getId();

        // Setup do Cliente
        myClient = new ClientsEntity();
        myClient.setId(100L);
        myClient.setName("Cliente Original");
        myClient.setOwner(spyUser);
        myClient.setSoftDelete(false);

        // Setup do DTO
        clientsDTO = new ClientsDTO();
        clientsDTO.setName("Novo Nome");

        // Configuração de Mocks Globais
        when(tokenBean.getUserEntityByToken("token-valido")).thenReturn(spyUser);
    }

    // --- TESTES DE LISTAGEM (MÉTODO DINÂMICO) ---

    @Test
    @DisplayName("Deve listar clientes ativos usando o novo método findClientsWithFilters")
    void listClientsSuccess() {
        List<ClientsEntity> list = new ArrayList<>();
        list.add(myClient);

        // Ajuste: Agora o Bean chama findClientsWithFilters(ownerId, softDelete)
        when(clientsDao.findClientsWithFilters(1L, false)).thenReturn(list);

        List<ClientsDTO> result = clientsBean.listClients("token-valido", null);

        assertNotNull(result);
        verify(clientsDao).findClientsWithFilters(1L, false);
    }

    @Test
    @DisplayName("Admin deve conseguir listar a lixeira global (userId null)")
    void listGlobalTrashAsAdmin() {
        spyUser.setUserRole(UserRoles.ADMIN);

        // Na lixeira global, o ownerId vai null e softDelete vai true
        clientsBean.listDeletedClientsDTO("token-valido", null);

        verify(clientsDao).findClientsWithFilters(null, true);
    }

    // --- TESTES DE AÇÕES EM MASSA (BULK UPDATE) ---

    @Test
    @DisplayName("Deve usar Bulk Update do DAO para apagar tudo de um utilizador")
    void softDeleteAllBulkSuccess() {
        when(userDao.find(2L)).thenReturn(otherUser);

        clientsBean.softDeleteAllClientsByUser(2L);

        // Ajuste: O Bean não faz mais loops de merge, chama o BulkUpdate no DAO
        verify(clientsDao, times(1)).bulkUpdateSoftDelete(2L, true);
        verify(clientsDao, never()).merge(any());
    }

    @Test
    @DisplayName("Deve usar Bulk Update do DAO para restaurar tudo de um utilizador")
    void restoreAllBulkSuccess() {
        when(userDao.find(2L)).thenReturn(otherUser);

        clientsBean.unSoftDeleteAllClientsByUser(2L);

        // Ajuste: Status false para restauro
        verify(clientsDao, times(1)).bulkUpdateSoftDelete(2L, false);
    }

    // --- TESTES DE ELIMINAÇÃO INDIVIDUAL ---

    @Test
    @DisplayName("Soft delete individual deve continuar a usar merge")
    void softDeleteIndividualSuccess() {
        when(clientsDao.find(100L)).thenReturn(myClient);

        clientsBean.softDeleteClient(100L);

        assertTrue(myClient.isSoftDelete());
        verify(clientsDao).merge(myClient);
    }

    @Test
    @DisplayName("Esvaziar lixeira deve remover clientes fisicamente um a um")
    void emptyTrashSuccess() {
        List<ClientsEntity> trashList = new ArrayList<>();
        trashList.add(myClient);

        when(clientsDao.findClientsWithFilters(1L, true)).thenReturn(trashList);

        clientsBean.emptyTrash(1L);

        // Verificamos se usou o remove do DAO para cada item da lista
        verify(clientsDao).remove(myClient);
    }
}