package aor.paj.projecto5.dao;

import aor.paj.projecto5.entity.LeadEntity;
import aor.paj.projecto5.utils.LeadState;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.WebApplicationException;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Stateless
public class LeadDao extends AbstractDao<LeadEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "project5PU")
    private EntityManager em;

    public LeadDao() {
        super(LeadEntity.class);
    }

    /**
     * Vai buscar APENAS as leads que estão na lixeira de um utilizador normal
     */
    public List<LeadEntity> getTrashLeadsByUserId(Long userId) {
        return em.createNamedQuery("lead.findSoftDelUserLeads", LeadEntity.class)
                .setParameter("id", userId)
                .getResultList();
    }

    /**
     * 1. SUPER GET (Pesquisa Dinâmica)
     * Centraliza todas as listagens do Admin com filtros opcionais.
     */
    public List<LeadEntity> findLeadsWithFilters(Integer stateId, Long userId, Boolean softDeleted) {
        StringBuilder sb = new StringBuilder("SELECT l FROM LeadEntity l WHERE 1=1");

        // Construção dinâmica da query (Append)
        if (stateId != null) sb.append(" AND l.leadState = :state");
        if (userId != null) sb.append(" AND l.owner.id = :userId");

        if (softDeleted != null) {
            sb.append(" AND l.softDeleted = :softDeleted");
        } else {
            sb.append(" AND l.softDeleted = false"); // Padrão: não mostrar lixo
        }

        TypedQuery<LeadEntity> query = em.createQuery(sb.toString(), LeadEntity.class);

        // Atribuição segura de parâmetros (Set)
        if (stateId != null) {
            try {
                query.setParameter("state", LeadState.fromId(stateId));
            } catch (Exception e) {
                throw new WebApplicationException("Estado de lead inválido: " + stateId, 400);
            }
        }
        if (userId != null) query.setParameter("userId", userId);
        if (softDeleted != null) query.setParameter("softDeleted", softDeleted);

        return query.getResultList();
    }

    /**
     * 2. BULK UPDATE (Ação em Massa)
     * Altera o estado de softDelete de todas as leads de um user numa única transação.
     */
    public int bulkUpdateSoftDelete(Long userId, boolean newStatus) {
        return em.createQuery("UPDATE LeadEntity l SET l.softDeleted = :newStatus " +
                        "WHERE l.owner.id = :userId AND l.softDeleted = :oldStatus")
                .setParameter("newStatus", newStatus)
                .setParameter("oldStatus", !newStatus)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    /**
     * 3. NAMED QUERIES (Performance)
     * Usa as definições que já tens na LeadEntity para casos fixos.
     */
    public List<LeadEntity> getLeadsByUserId(Long userId) {
        return em.createNamedQuery("lead.findLeadsByUserId", LeadEntity.class)
                .setParameter("id", userId)
                .getResultList();
    }

    public LeadEntity getLeadByLeadID(Long leadId) {
        try {
            return em.createNamedQuery("lead.findLeadByLeadID", LeadEntity.class)
                    .setParameter("leadId", leadId)
                    .getSingleResult();
        } catch (Exception e) {
            return null; // O Verifier no Service tratará o 404
        }
    }

    /**
     * Elimina fisicamente todas as leads de um utilizador que estejam na lixeira
     */
    public int emptyTrashByUserId(Long userId) {
        // JPQL para apagar as leads onde o owner é o utilizador e estão marcadas como softDeleted
        return em.createQuery("DELETE FROM LeadEntity l WHERE l.owner.id = :userId AND l.softDeleted = true")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}