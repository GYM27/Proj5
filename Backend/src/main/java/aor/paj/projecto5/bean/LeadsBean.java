package aor.paj.projecto5.bean;

import aor.paj.projecto5.utils.UserState;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import aor.paj.projecto5.dao.LeadDao;
import aor.paj.projecto5.dao.UserDao;
import aor.paj.projecto5.dto.LeadDTO;
import aor.paj.projecto5.entity.LeadEntity;
import aor.paj.projecto5.entity.UserEntity;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enterprise JavaBean (EJB) Stateless responsável pela lógica de negócio
 * associada à gestão de Leads (Oportunidades).
 * Centraliza as operações de conversão (DTO <-> Entidade), regras de negócio
 * para utilizadores normais e operações avançadas para administradores.
 */
@Stateless
public class LeadsBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Inject
    LeadDao leadDao;

    @Inject
    TokenBean tokenBean;

    @Inject
    UserDao userDao;

    // =================================================================================
    // HELPERS (Mapeamento DTO <-> Entidade)
    // =================================================================================

    /**
     * Converte uma entidade LeadEntity num LeadDTO para envio ao Frontend.
     * Mapeia os dados do dono (owner) caso a lead esteja associada a um utilizador.
     *
     * @param entity A entidade vinda da base de dados.
     * @return O DTO populado ou null caso a entidade seja nula.
     */
    public LeadDTO entityToDTO(LeadEntity entity) {
        if (entity == null) return null;
        LeadDTO dto = new LeadDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitulo());
        dto.setDescription(entity.getDescricao());
        dto.setState(entity.getLeadState().getStateId());
        dto.setDate(entity.getData());
        dto.setSoftDeleted(entity.isSoftDeleted());

        // Aceder aos dados através do Owner
        if (entity.getOwner() != null) {

            dto.setName(entity.getOwner().getFirstName() + " " + entity.getOwner().getLastName());

            dto.setFirstName(entity.getOwner().getFirstName());
            dto.setLastName(entity.getOwner().getLastName());
        }
        return dto;
    }

    /**
     * Converte um LeadDTO numa nova LeadEntity, associando-lhe um dono.
     *
     * @param dto Os dados submetidos pelo utilizador.
     * @param owner A entidade do utilizador que será o dono da Lead.
     * @return A nova entidade pronta a ser persistida.
     */
    private LeadEntity DTOToEntity(LeadDTO dto, UserEntity owner) {
        LeadEntity entity = new LeadEntity();
        entity.setTitulo(dto.getTitle());
        entity.setDescricao(dto.getDescription());
        entity.setLeadState(dto.getStateEnum());
        entity.setOwner(owner);
        entity.setSoftDeleted(dto.isSoftDeleted());
        return entity;
    }

    /**
     * Converte uma lista de Entidades numa lista de DTOs.
     *
     * @param entities Lista de LeadEntity.
     * @return Lista de LeadDTO correspondente.
     */
    private List<LeadDTO> toDTOList(List<LeadEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    // =================================================================================
    // FUNCIONALIDADES DE USER
    // =================================================================================

    /**
     * Cria uma nova lead associada ao utilizador autenticado pelo token.
     *
     * @param token Token de sessão do utilizador.
     * @param dto Dados da nova lead a criar.
     * @return O DTO da lead recém-criada (com ID gerado).
     */
    public LeadDTO addLead(String token, LeadDTO dto) {
        UserEntity owner = tokenBean.getUserEntityByToken(token);
        LeadEntity newLead = DTOToEntity(dto, owner);

        leadDao.persist(newLead);

        return entityToDTO(newLead);
    }

    /**
     * Lista as leads pertencentes ao utilizador autenticado.
     * Permite alternar entre leads ativas e leads na lixeira.
     *
     * @param token Token de sessão do utilizador.
     * @param softDeleted Define se a query deve buscar as leads apagadas (true) ou ativas (false).
     * @return Lista de leads formatadas em DTO.
     */
    public List<LeadDTO> getLeadsByToken(String token, Boolean softDeleted) {
        UserEntity user = tokenBean.getUserEntityByToken(token);

        List<LeadEntity> entities;

        // 1. Se o React enviou softDeleted=true (User clicou no botão da lixeira)
        if (softDeleted != null && softDeleted) {
            entities = leadDao.getTrashLeadsByUserId(user.getId());
        } else {
            // 2. Comportamento normal (vai buscar as leads ativas)
            entities = leadDao.getLeadsByUserId(user.getId());
        }

        // 3. A base de dados já fez o filtro, só precisamos de converter para DTO
        return entities.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtém uma lead específica através do seu ID.
     *
     * @param leadId O identificador único da lead.
     * @return O LeadDTO correspondente.
     */
    public LeadDTO getLeadById(Long leadId) {
        LeadEntity entity = leadDao.getLeadByLeadID(leadId);
        return entityToDTO(entity);
    }

    /**
     * Edita os dados base de uma lead existente (Título, Descrição e Estado).
     *
     * @param leadId O identificador único da lead a editar.
     * @param dto Os novos dados a aplicar.
     * @return O DTO da lead atualizada.
     */
    public LeadDTO editLead(Long leadId, LeadDTO dto) {
        LeadEntity lead = leadDao.getLeadByLeadID(leadId);

        lead.setTitulo(dto.getTitle());
        lead.setDescricao(dto.getDescription());
        lead.setLeadState(dto.getStateEnum());

        leadDao.merge(lead);

        return entityToDTO(lead);
    }

    /**
     * Move uma lead para a lixeira (Eliminação lógica).
     *
     * @param leadId O identificador único da lead a desativar.
     */
    public void softDeleteLead(Long leadId) {
        LeadEntity lead = leadDao.getLeadByLeadID(leadId);
        if (lead != null) {
            lead.setSoftDeleted(true);
            leadDao.merge(lead);
        }
    }

    // =================================================================================
    // FUNCIONALIDADES DE ADMIN
    // =================================================================================

    /**
     * Restaura uma lead que estava na lixeira, voltando a marcá-la como ativa.
     *
     * @param leadId O identificador único da lead a restaurar.
     */
    public void restoreLead(Long leadId) {
        LeadEntity lead = leadDao.getLeadByLeadID(leadId);
        if (lead != null) {
            // Mudamos o estado para false para que ela volte a ser "Ativa"
            lead.setSoftDeleted(false);
            leadDao.merge(lead);
        }
    }


    /**
     * Lista leads globalmente com base em múltiplos filtros dinâmicos.
     * Exclusivo para administradores.
     *
     * @param stateId ID do estado da lead (opcional).
     * @param userId ID do dono da lead (opcional).
     * @param softDeleted Estado de eliminação lógica da lead (opcional).
     * @return Lista filtrada de LeadDTOs.
     */
    public List<LeadDTO> getLeadsWithFilters(Integer stateId, Long userId, Boolean softDeleted) {
        List<LeadEntity> entities = leadDao.findLeadsWithFilters(stateId, userId, softDeleted);
        return toDTOList(entities);
    }

    /**
     * Edição com privilégios totais de Administrador.
     * Permite alterar propriedades exclusivas, como recuperar uma lead diretamente na edição.
     *
     * @param leadId O identificador único da lead alvo.
     * @param dto Os novos dados a aplicar.
     * @return O DTO da lead resultante.
     * @throws WebApplicationException Se a lead não existir (404) ou os dados forem inválidos (400).
     */
    public LeadDTO adminSuperEdit(Long leadId, LeadDTO dto) {
        if (dto == null) throw new WebApplicationException("Dados de edição inválidos", 400);

        LeadEntity lead = leadDao.getLeadByLeadID(leadId);
        if (lead == null) throw new WebApplicationException("Lead não encontrada", 404);

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            lead.setTitulo(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            lead.setDescricao(dto.getDescription());
        }
        if (dto.getState() > 0) {
            lead.setLeadState(dto.getStateEnum());
        }

        lead.setSoftDeleted(dto.isSoftDeleted());

        leadDao.merge(lead);
        return entityToDTO(lead);
    }

    /**
     * Administrador cria uma lead atribuindo-a diretamente a um utilizador específico.
     *
     * @param userId ID do utilizador destino (owner) que vai receber a lead.
     * @param dto Dados da nova lead a ser criada.
     * @return O DTO com a lead recém-criada.
     * @throws WebApplicationException 404 se o user não existir, 403 se o user não estiver ativo.
     */
    public LeadDTO addLeadToUser(Long userId, LeadDTO dto) {
        // 1. Busca o utilizador à base de dados
        UserEntity owner = userDao.find(userId);

        // 2. Validação 1: O utilizador existe?
        if (owner == null) {
            throw new WebApplicationException("Utilizador destino não encontrado.", 404);
        }

        // 3. Validação 2: A nova lógica de Estados
        // Impede o Admin de atribuir leads a contas DISABLED ou PENDING
        if (owner.getState() != UserState.ACTIVE) {
            throw new WebApplicationException("Não é possível atribuir leads: o utilizador encontra-se inativo ou pendente.", 403);
        }

        // 4. Criação e Persistência
        LeadEntity newLead = DTOToEntity(dto, owner);
        leadDao.persist(newLead);

        // 5. Retorna o DTO atualizado
        return entityToDTO(newLead);
    }

    /**
     * Remove permanentemente (Hard Delete) uma lead da base de dados.
     *
     * @param leadId ID da lead a remover.
     * @throws WebApplicationException 404 se a lead não existir.
     */
    public void hardDeleteLead(Long leadId) {
        LeadEntity lead = leadDao.getLeadByLeadID(leadId);
        if (lead == null) {
            throw new WebApplicationException("Lead não encontrada para remoção.", 404);
        }
        // Homogéneo: Uso do leadDao.remove herdado do AbstractDao
        leadDao.remove(lead);
    }

    /**
     * Move todas as leads de um determinado utilizador para a lixeira de uma só vez.
     *
     * @param userId O identificador do dono das leads.
     * @return O número de registos alterados na base de dados.
     * @throws WebApplicationException 404 se o utilizador não existir.
     */
    public int softDeleteAllFromUser(Long userId) {
        if (userDao.find(userId) == null) throw new WebApplicationException("User not found", 404);
        return leadDao.bulkUpdateSoftDelete(userId, true);
    }

    /**
     * Restaura em massa todas as leads de um utilizador que se encontravam na lixeira.
     *
     * @param userId O identificador do dono das leads.
     * @return O número de registos alterados na base de dados.
     * @throws WebApplicationException 404 se o utilizador não existir.
     */
    public int undeleteAllFromUser(Long userId) {
        if (userDao.find(userId) == null) throw new WebApplicationException("User not found", 404);
        return leadDao.bulkUpdateSoftDelete(userId, false);
    }

    /**
     * Elimina fisicamente todas as leads da lixeira de um determinado utilizador.
     * Ação irreversível.
     *
     * @param userId O identificador do dono das leads na lixeira.
     * @return O número de registos permanentemente apagados.
     * @throws WebApplicationException 404 se o utilizador não existir.
     */
    public int emptyTrash(Long userId) {
        if (userDao.find(userId) == null) throw new WebApplicationException("User not found", 404);
        // Delega para o DAO a execução da query de remoção física
        return leadDao.emptyTrashByUserId(userId);
    }
}