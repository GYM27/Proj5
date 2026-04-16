package aor.paj.projecto5.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import aor.paj.projecto5.entity.TokenEntity;
import aor.paj.projecto5.entity.UserEntity;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Stateless
public class TokenDao extends AbstractDao<TokenEntity> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public TokenDao() {
        super(TokenEntity.class);
    }

    /**
     * Este é o método que o teu TokenCleanupBean chama.
     * Ele usa a NamedQuery "token.cleanExpired" definida na tua Entity.
     */
    public int cleanUpExpiredTokens() {
        return em.createNamedQuery("token.cleanExpired")
                .setParameter("now", LocalDateTime.now())
                .executeUpdate();
    }

    public TokenEntity findToken(String tokenValue) {
        try {
            return em.createNamedQuery("token.findToken", TokenEntity.class)
                    .setParameter("tokenValue", tokenValue)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public int invalidateToken(String tokenValue) {
        return em.createNamedQuery("token.invalidate")
                .setParameter("tokenValue", tokenValue)
                .executeUpdate();
    }

    public UserEntity findUserEntityByToken(String tokenValue) {
        try {
            return em.createNamedQuery("token.returnUserEntity", UserEntity.class)
                    .setParameter("tokenValue", tokenValue)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public TokenEntity findActiveTokenByUser(UserEntity owner) {
        try {
            // Procuro o token que pertence ao owner e que ainda está marcado como active
            return em.createQuery(
                            "SELECT t FROM TokenEntity t WHERE t.owner = :owner AND t.active = true ORDER BY t.expiresAt DESC",
                            TokenEntity.class)
                    .setParameter("owner", owner)
                    .setMaxResults(1) // Garanto que só trago o mais recente
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Utilizador não tem sessões ativas
        }
    }
}