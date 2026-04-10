package aor.paj.projecto5.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.*;
import aor.paj.projecto5.entity.TokenEntity;
import aor.paj.projecto5.entity.UserEntity;
import java.io.Serial;
import java.io.Serializable;

@Stateless
public class UserDao extends AbstractDao<UserEntity> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public UserDao() {
        super(UserEntity.class);
    }

    /**
     * Procura um utilizador através de um token ativo usando Criteria API.
     */
    public UserEntity findUserByToken(String tokenValue) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = cb.createQuery(UserEntity.class);
        Root<TokenEntity> tokenRoot = query.from(TokenEntity.class);

        // Selecionamos o Owner do Token
        query.select(tokenRoot.get("owner"));

        // Condições: Valor do token coincide e está marcado como ativo
        Predicate equalToken = cb.equal(tokenRoot.get("tokenValue"), tokenValue);
        Predicate isActive = cb.equal(tokenRoot.get("active"), true);

        query.where(cb.and(equalToken, isActive));

        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // --- Consultas via Named Queries (Definidas na UserEntity) ---

    public UserEntity findUserByUsername(String username) {
        try {
            return em.createNamedQuery("User.findUserByUsername", UserEntity.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByEmail(String email) {
        try {
            return em.createNamedQuery("User.findUserByEmail", UserEntity.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByContact(String contact) {
        try {
            return em.createNamedQuery("User.findUserByContact", UserEntity.class)
                    .setParameter("contact", contact)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Transfere em massa a posse de Leads e Clientes.
     * É muito mais eficiente que fazer ciclos 'for' em Java.
     */
    public void transferOwnership(UserEntity oldOwner, UserEntity newOwner) {
        // Atualiza as Leads
        em.createQuery("UPDATE LeadEntity l SET l.owner = :newOwner WHERE l.owner = :oldOwner")
                .setParameter("newOwner", newOwner)
                .setParameter("oldOwner", oldOwner)
                .executeUpdate();

        // Atualiza os Clientes
        em.createQuery("UPDATE ClientsEntity c SET c.owner = :newOwner WHERE c.owner = :oldOwner")
                .setParameter("newOwner", newOwner)
                .setParameter("oldOwner", oldOwner)
                .executeUpdate();
    }

    /**
     * Remove fisicamente o utilizador da base de dados.
     * A reatribuição de dados deve ser chamada no Bean ANTES deste método.
     */
    public boolean hardDelete(Long id) {
        UserEntity userEntity = em.find(UserEntity.class, id);
        if (userEntity != null) {
            // Como o UsersBean já chamou o transferOwnership,
            // as listas de Leads e Clients já estarão vazias para este ID.
            em.remove(userEntity);
            return true;
        }
        return false;
    }
}