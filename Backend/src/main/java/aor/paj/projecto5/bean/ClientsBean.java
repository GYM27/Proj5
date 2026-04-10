package aor.paj.projecto5.bean;

import java.util.List;
import java.util.stream.Collectors;
import aor.paj.projecto5.dao.ClientsDao;
import aor.paj.projecto5.dao.UserDao;
import aor.paj.projecto5.dto.ClientsDTO;
import aor.paj.projecto5.entity.ClientsEntity;
import aor.paj.projecto5.entity.UserEntity;
import aor.paj.projecto5.utils.UserRoles;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Stateless
public class ClientsBean {

    @Inject
    TokenBean tokenBean;

    @Inject
    ClientsDao clientsDao;

    @Inject
    UserDao userDao;

    public ClientsBean() {}

    // --- MÉTODOS DE CONVERSÃO (Helpers) ---

    private ClientsDTO toDTO(ClientsEntity entity) {
        ClientsDTO dto = new ClientsDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setOrganization(entity.getOrganization());
        dto.setSoftDeleted(entity.isSoftDelete());
        return dto;
    }

    private ClientsEntity toEntity(ClientsDTO dto, UserEntity owner) {
        ClientsEntity entity = new ClientsEntity();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setOrganization(dto.getOrganization());
        entity.setOwner(owner);
        // Use o valor do DTO se ele estiver disponível, caso contrário, use false
        entity.setSoftDelete(dto.isSoftDeleted());
        return entity;
    }

    private List<ClientsDTO> toDTOList(List<ClientsEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS DE ESCRITA (Individual) ---

    public ClientsDTO addClient(String token, ClientsDTO dto) {
        UserEntity owner = tokenBean.getUserEntityByToken(token);
        ClientsEntity newClient = toEntity(dto, owner);
        clientsDao.persist(newClient);
        return toDTO(newClient);
    }

    public ClientsDTO editClient(Long clientId, ClientsDTO dto) {
        ClientsEntity client = clientsDao.find(clientId);
        if (client == null || client.isSoftDelete()) {
            throw new WebApplicationException("Cliente não encontrado", Response.Status.NOT_FOUND);
        }
        client.setName(dto.getName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setOrganization(dto.getOrganization());
        clientsDao.merge(client);
        return toDTO(client);
    }

    public ClientsDTO softDeleteClient(Long clientId) {
        ClientsEntity client = clientsDao.find(clientId);
        if (client == null || client.isSoftDelete()) {
            throw new WebApplicationException("Cliente não encontrado", Response.Status.NOT_FOUND);
        }
        client.setSoftDelete(true);
        clientsDao.merge(client);
        return toDTO(client);
    }

    public ClientsDTO restoreClient(Long clientId) {
        ClientsEntity client = clientsDao.find(clientId);
        if (client == null || !client.isSoftDelete()) {
            throw new WebApplicationException("Cliente não encontrado ou já ativo", Response.Status.NOT_FOUND);
        }
        client.setSoftDelete(false);
        clientsDao.merge(client);
        return toDTO(client);
    }

    public ClientsDTO permanentDeleteClient(Long clientId) {
        ClientsEntity client = clientsDao.find(clientId);
        if (client == null || !client.isSoftDelete()) {
            throw new WebApplicationException("O cliente deve estar na lixeira para remoção permanente", Response.Status.CONFLICT);
        }
        ClientsDTO dto = toDTO(client);
        clientsDao.remove(client);
        return dto;
    }

    // --- MÉTODOS DE LISTAGEM DINÂMICA (Refatorados) ---

    /**
     * Lista clientes ativos. Se for Admin, pode filtrar por um utilizador específico ou ver todos.
     */
    public List<ClientsDTO> listClients(String token, Long userId) {
        UserEntity requester = tokenBean.getUserEntityByToken(token);
        if (requester == null) throw new WebApplicationException(401);

        // Segurança: Apenas Admin pode escolher o userId; utilizador comum vê apenas os seus
        Long filterId = (requester.getUserRole() == UserRoles.ADMIN) ? userId : requester.getId();

        return toDTOList(clientsDao.findClientsWithFilters(filterId, false));
    }

    /**
     * Lista clientes na lixeira (softDelete = true).
     */
    public List<ClientsDTO> listDeletedClientsDTO(String token, Long userId) {
        UserEntity requester = tokenBean.getUserEntityByToken(token);
        if (requester == null) throw new WebApplicationException(401);

        Long filterId = (requester.getUserRole() == UserRoles.ADMIN) ? userId : requester.getId();

        return toDTOList(clientsDao.findClientsWithFilters(filterId, true));
    }

    // --- AÇÕES EM MASSA (Otimizadas via Bulk Update no DAO) ---

    public int softDeleteAllClientsByUser(Long userId) {
        if (userDao.find(userId) == null) throw new WebApplicationException("Utilizador não encontrado", 404);
        return clientsDao.bulkUpdateSoftDelete(userId, true); // Move tudo para lixeira
    }

    public int unSoftDeleteAllClientsByUser(Long userId) {
        if (userDao.find(userId) == null) throw new WebApplicationException("Utilizador não encontrado", 404);
        return clientsDao.bulkUpdateSoftDelete(userId, false); // Restaura tudo
    }

    public boolean emptyTrash(Long userId) {
        try {
            // Obtemos a lista da lixeira e removemos fisicamente
            List<ClientsEntity> trashList = clientsDao.findClientsWithFilters(userId, true);
            for (ClientsEntity c : trashList) {
                clientsDao.remove(c);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // --- AUXILIARES E ADMIN ---

    public ClientsDTO createClientForUser(Long userId, ClientsDTO dto) {
        UserEntity targetOwner = userDao.find(userId);
        if (targetOwner == null) throw new WebApplicationException("Utilizador não encontrado", 404);

        ClientsEntity newClient = toEntity(dto, targetOwner);
        clientsDao.persist(newClient);
        return toDTO(newClient);
    }

    public List<ClientsDTO> listAllDeletedClients() {
        // Lixeira global: userId null e softDelete true
        return toDTOList(clientsDao.findClientsWithFilters(null, true));
    }


}