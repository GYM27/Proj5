package aor.paj.projecto5.bean;

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

    // HELPERS -------------------------------------------------------------

    public LeadDTO entityToDTO(LeadEntity entity) {
        if (entity == null) return null;
        LeadDTO dto = new LeadDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitulo());
        dto.setDescription(entity.getDescricao());
        dto.setState(entity.getLeadState().getStateId());
        dto.setDate(entity.getData());
        dto.setSoftDeleted(entity.isSoftDeleted());
        // Aceder ao firstName através do Owner
        if (entity.getOwner() != null) {
            dto.setName(entity.getOwner().getFirstName());
            dto.setName(entity.getOwner().getLastName());
        }
        dto.setFirstName(entity.getOwner().getFirstName());
        dto.setLastName(entity.getOwner().getLastName());
        return dto;
    }

    private LeadEntity DTOToEntity(LeadDTO dto, UserEntity owner) {
        LeadEntity entity = new LeadEntity();
        entity.setTitulo(dto.getTitle());
        entity.setDescricao(dto.getDescription());
        entity.setLeadState(dto.getStateEnum());
        entity.setOwner(owner);
        entity.setSoftDeleted(dto.isSoftDeleted());
        return entity;
    }

    private List<LeadDTO> toDTOList(List<LeadEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    // FUNCIONALIDADES DE USER ------------------------------------------

    /**
     * Adicionar Lead via Token
     */
    public LeadDTO addLead(String token, LeadDTO dto) {
        UserEntity owner = tokenBean.getUserEntityByToken(token);
        LeadEntity newLead = DTOToEntity(dto, owner);

        leadDao.persist(newLead);

        return entityToDTO(newLead);
    }

    /**
     * Listar leads do utilizador autenticado (Ativas ou na Lixeira)
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
     * Obter lead por ID
     */
    public LeadDTO getLeadById(Long leadId) {
        LeadEntity entity = leadDao.getLeadByLeadID(leadId);
        return entityToDTO(entity);
    }

    /**
     * Editar Lead existente
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
     * Eliminação lógica (Soft Delete)
     */
    public void softDeleteLead(Long leadId) {
        LeadEntity lead = leadDao.getLeadByLeadID(leadId);
        if (lead != null) {
            lead.setSoftDeleted(true);
            leadDao.merge(lead);
        }
    }

    // FUNCIONALIDADES DE ADMIN ----------------------------------------

    /**
     * Restaura uma lead que estava na lixeira (Undelete).
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
     * Listagem com filtros para Administrador
     */
    public List<LeadDTO> getLeadsWithFilters(Integer stateId, Long userId, Boolean softDeleted) {
        List<LeadEntity> entities = leadDao.findLeadsWithFilters(stateId, userId, softDeleted);
        return toDTOList(entities);
    }

    /**
     * Edição total por Administrador
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
     * Admin adiciona lead diretamente a um utilizador
     */
    public LeadDTO addLeadToUser(Long userId, LeadDTO dto) {
        // Homogéneo: Uso do userDao.find herdado do AbstractDao
        UserEntity owner = userDao.find(userId);

        if (owner == null || owner.isSoftDelete()) {
            throw new WebApplicationException("Utilizador destino não encontrado ou inativo", 404);
        }

        LeadEntity newLead = DTOToEntity(dto, owner);
        leadDao.persist(newLead);

        return entityToDTO(newLead);
    }

    /**
     * Eliminação física do registo
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
     * Soft Delete em massa por utilizador
     */
    public int softDeleteAllFromUser(Long userId) {
        // Homogéneo: Uso do userDao.find
        if (userDao.find(userId) == null) throw new WebApplicationException("User not found", 404);

        return leadDao.bulkUpdateSoftDelete(userId, true);
    }

    /**
     * Recuperação em massa por utilizador (Undelete)
     */
    public int undeleteAllFromUser(Long userId) {
        // Homogéneo: Uso do userDao.find
        if (userDao.find(userId) == null) throw new WebApplicationException("User not found", 404);

        return leadDao.bulkUpdateSoftDelete(userId, false);
    }

    /**
     * Esvaziar a lixeira (Hard Delete em massa) por utilizador
     */
    public int emptyTrash(Long userId) {
        // Homogéneo: Uso do userDao.find
        if (userDao.find(userId) == null) throw new WebApplicationException("User not found", 404);

        // Delega para o DAO a execução da query de remoção física
        return leadDao.emptyTrashByUserId(userId);
    }
}