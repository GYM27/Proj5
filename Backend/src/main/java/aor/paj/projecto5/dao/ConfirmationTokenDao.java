package aor.paj.projecto5.dao;

import aor.paj.projecto5.entity.ConfirmationTokenEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.io.Serializable;

/**
 * Data Access Object (DAO) para a entidade ConfirmationTokenEntity.
 * Responsável pelas operações de base de dados dos tokens de registo.
 */
@Stateless
public class ConfirmationTokenDao extends AbstractDao<ConfirmationTokenEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    // Construtor obrigatório porque estendemos o AbstractDao
    public ConfirmationTokenDao() {
        super(ConfirmationTokenEntity.class);
    }

    /**JPQL (Java Persistence Query Language)
     *
     * Procura um token de confirmação na base de dados através da sua string única (UUID).
     * * @param tokenString O valor alfanumérico do token gerado e enviado por email.
     * @return A entidade {@link ConfirmationTokenEntity} se existir; null caso não seja encontrada.
     */
    public ConfirmationTokenEntity findTokenByString(String tokenString) {
        try {
            return em.createQuery(
                            "SELECT t FROM ConfirmationTokenEntity t WHERE t.token = :token",
                            ConfirmationTokenEntity.class)
                    .setParameter("token", tokenString)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Retorna nulo de forma segura se o token for inválido ou não existir
        }
    }
}