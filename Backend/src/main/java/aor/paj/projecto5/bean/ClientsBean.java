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

/**
 * Enterprise JavaBean (EJB) Stateless responsável pela lógica de negócio
 * associada à gestão de Clientes (Clients).
 * Intermedeia as operações entre a camada de apresentação (REST) e a camada de acesso a dados (DAO).
 */
@Stateless
public class ClientsBean {

    /**
     * Bean responsável pela validação e extração de dados através de tokens de autenticação.
     */
    @Inject
    TokenBean tokenBean;

    /**
     * Data Access Object (DAO) para operações na base de dados relacionadas com a entidade ClientsEntity.
     */
    @Inject
    ClientsDao clientsDao;

    /**
     * Data Access Object (DAO) para operações na base de dados relacionadas com a entidade UserEntity.
     */
    @Inject
    UserDao userDao;

    /**
     * Construtor padrão sem argumentos.
     */
    public ClientsBean() {}

    // --- MÉTODOS DE CONVERSÃO (Helpers) ---

    /**
     * Converte um objeto da camada de persistência (Entity) para um objeto de transferência de dados (DTO).
     *
     * @param entity A entidade {@link ClientsEntity} a ser convertida.
     * @return O {@link ClientsDTO} correspondente com os dados do cliente.
     */
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

    /**
     * Converte um objeto de transferência de dados (DTO) numa entidade da camada de persistência.
     *
     * @param dto O {@link ClientsDTO} contendo os dados do cliente.
     * @param owner A entidade {@link UserEntity} que representa o proprietário (criador/responsável) pelo cliente.
     * @return A entidade {@link ClientsEntity} preenchida.
     */
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

    /**
     * Converte uma lista de entidades {@link ClientsEntity} numa lista de {@link ClientsDTO}.
     *
     * @param entities A lista de entidades a converter.
     * @return Uma lista de objetos DTO correspondentes.
     */
    private List<ClientsDTO> toDTOList(List<ClientsEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS DE ESCRITA (Individual) ---

    /**
     * Adiciona um novo cliente associado ao utilizador autenticado pelo token.
     *
     * @param token O token de autenticação do utilizador que está a criar o cliente.
     * @param dto Os dados do novo cliente a ser criado.
     * @return O {@link ClientsDTO} do cliente recém-criado persistido na base de dados.
     */
    public ClientsDTO addClient(String token, ClientsDTO dto) {
        UserEntity owner = tokenBean.getUserEntityByToken(token);
        ClientsEntity newClient = toEntity(dto, owner);
        clientsDao.persist(newClient);
        return toDTO(newClient);
    }

    /**
     * Edita as informações de um cliente existente.
     *
     * @param clientId O ID do cliente a ser atualizado.
     * @param dto Os novos dados do cliente.
     * @return O {@link ClientsDTO} atualizado.
     * @throws WebApplicationException Se o cliente não existir ou se estiver na lixeira (soft deleted).
     */
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

    /**
     * Move um cliente ativo para a lixeira (Soft Delete).
     * O registo é mantido na base de dados, mas marcado como apagado.
     *
     * @param clientId O ID do cliente a ser movido para a lixeira.
     * @return O {@link ClientsDTO} atualizado refletindo o estado de apagado.
     * @throws WebApplicationException Se o cliente não existir ou já estiver na lixeira.
     */
    public ClientsDTO softDeleteClient(Long clientId) {
        ClientsEntity client = clientsDao.find(clientId);
        if (client == null || client.isSoftDelete()) {
            throw new WebApplicationException("Cliente não encontrado", Response.Status.NOT_FOUND);
        }
        client.setSoftDelete(true);
        clientsDao.merge(client);
        return toDTO(client);
    }

    /**
     * Restaura um cliente que estava na lixeira, voltando a marcá-lo como ativo.
     *
     * @param clientId O ID do cliente a ser restaurado.
     * @return O {@link ClientsDTO} atualizado refletindo o estado ativo.
     * @throws WebApplicationException Se o cliente não existir ou não estiver na lixeira.
     */
    public ClientsDTO restoreClient(Long clientId) {
        ClientsEntity client = clientsDao.find(clientId);
        if (client == null || !client.isSoftDelete()) {
            throw new WebApplicationException("Cliente não encontrado ou já ativo", Response.Status.NOT_FOUND);
        }
        client.setSoftDelete(false);
        clientsDao.merge(client);
        return toDTO(client);
    }

    /**
     * Remove permanentemente um cliente da base de dados.
     * O cliente tem de estar obrigatoriamente na lixeira (softDelete = true) antes de ser removido.
     *
     * @param clientId O ID do cliente a ser removido fisicamente.
     * @return O {@link ClientsDTO} contendo os dados do cliente apagado.
     * @throws WebApplicationException Se o cliente não for encontrado ou não estiver marcado como soft delete.
     */
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
     * Lista clientes ativos (não apagados).
     * A listagem é filtrada com base no papel (role) do utilizador que faz o pedido.
     * - Administradores podem visualizar clientes de qualquer utilizador (passando o userId) ou todos (se userId for nulo).
     * - Utilizadores regulares apenas veem os seus próprios clientes.
     *
     * @param token O token do utilizador que faz a requisição.
     * @param userId Opcional. O ID do utilizador cujos clientes se pretende listar (aplicável apenas a Administradores).
     * @return Uma lista de {@link ClientsDTO} ativos.
     * @throws WebApplicationException Se o token for inválido (HTTP 401).
     */
    public List<ClientsDTO> listClients(String token, Long userId) {
        UserEntity requester = tokenBean.getUserEntityByToken(token);
        if (requester == null) throw new WebApplicationException(401);

        // Segurança: Apenas Admin pode escolher o userId; utilizador comum vê apenas os seus
        Long filterId = (requester.getUserRole() == UserRoles.ADMIN) ? userId : requester.getId();

        return toDTOList(clientsDao.findClientsWithFilters(filterId, false));
    }

    /**
     * Lista clientes que se encontram na lixeira (softDelete = true).
     * Partilha a mesma lógica de segurança baseada em papéis do método listClients().
     *
     * @param token O token do utilizador que faz a requisição.
     * @param userId Opcional. O ID do utilizador cuja lixeira se pretende consultar.
     * @return Uma lista de {@link ClientsDTO} na lixeira.
     * @throws WebApplicationException Se o token for inválido (HTTP 401).
     */
    public List<ClientsDTO> listDeletedClientsDTO(String token, Long userId) {
        UserEntity requester = tokenBean.getUserEntityByToken(token);
        if (requester == null) throw new WebApplicationException(401);

        Long filterId = (requester.getUserRole() == UserRoles.ADMIN) ? userId : requester.getId();

        return toDTOList(clientsDao.findClientsWithFilters(filterId, true));
    }

    // --- AÇÕES EM MASSA (Otimizadas via Bulk Update no DAO) ---

    /**
     * Move todos os clientes de um utilizador específico para a lixeira.
     * Operação otimizada no lado da base de dados através do DAO.
     *
     * @param userId O ID do utilizador dono dos clientes a apagar.
     * @return O número de registos alterados.
     * @throws WebApplicationException Se o utilizador não for encontrado na base de dados.
     */
    public int softDeleteAllClientsByUser(Long userId) {
        if (userDao.find(userId) == null) throw new WebApplicationException("Utilizador não encontrado", 404);
        return clientsDao.bulkUpdateSoftDelete(userId, true); // Move tudo para lixeira
    }

    /**
     * Restaura todos os clientes de um utilizador específico que estavam na lixeira.
     *
     * @param userId O ID do utilizador dono dos clientes a restaurar.
     * @return O número de registos alterados.
     * @throws WebApplicationException Se o utilizador não for encontrado na base de dados.
     */
    public int unSoftDeleteAllClientsByUser(Long userId) {
        if (userDao.find(userId) == null) throw new WebApplicationException("Utilizador não encontrado", 404);
        return clientsDao.bulkUpdateSoftDelete(userId, false); // Restaura tudo
    }

    /**
     * Esvazia a lixeira de um utilizador, apagando permanentemente todos os clientes que nela constam.
     *
     * @param userId O ID do utilizador cuja lixeira será esvaziada.
     * @return true se a operação for concluída com sucesso, false caso contrário.
     */
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

    /**
     * Método administrativo para a criação de um cliente e atribuição direta a um utilizador específico.
     * Usado tipicamente quando um Administrador quer alocar um novo cliente a outro membro da equipa.
     *
     * @param userId O ID do utilizador que será o proprietário do cliente.
     * @param dto Os dados do novo cliente a ser criado.
     * @return O {@link ClientsDTO} do cliente recém-criado persistido na base de dados.
     * @throws WebApplicationException Se o utilizador alvo não for encontrado.
     */
    public ClientsDTO createClientForUser(Long userId, ClientsDTO dto) {
        UserEntity targetOwner = userDao.find(userId);
        if (targetOwner == null) throw new WebApplicationException("Utilizador não encontrado", 404);

        ClientsEntity newClient = toEntity(dto, targetOwner);
        clientsDao.persist(newClient);
        return toDTO(newClient);
    }

    /**
     * Recupera todos os clientes do sistema (de qualquer utilizador) que estejam atualmente marcados como apagados.
     * Tipicamente utilizado numa vista global de lixeira para administradores.
     *
     * @return Uma lista contendo todos os {@link ClientsDTO} do sistema que tenham softDelete = true.
     */
    public List<ClientsDTO> listAllDeletedClients() {
        // Lixeira global: userId null e softDelete true
        return toDTOList(clientsDao.findClientsWithFilters(null, true));
    }
}