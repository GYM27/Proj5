package aor.paj.projecto5.bean;

import aor.paj.projecto5.dao.ConfirmationTokenDao;
import aor.paj.projecto5.entity.ConfirmationTokenEntity;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * EJB Stateless responsável pela gestão do ciclo de vida dos tokens de convite e registo.
 * Lida com a criação, validação e eliminação de tokens baseados em email.
 */
@Stateless
public class ConfirmationTokenBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ConfirmationTokenDao tokenDao;

    /**
     * Gera um token único (UUID) associado exclusivamente a um endereço de email.
     * Define uma validade de 24 horas a partir do momento da criação.
     *
     * @param email O email para o qual o convite será enviado.
     * @return A string UUID do token gerado para ser colocada no link do email.
     */
    public String createTokenForEmail(String email) {
        // 1. Gera a string alfanumérica única
        String tokenString = UUID.randomUUID().toString();

        // 2. Cria a entidade usando o novo construtor que fizemos (token, email, horas_validade)
        ConfirmationTokenEntity tokenEntity = new ConfirmationTokenEntity(tokenString, email, 24);

        // 3. Guarda na base de dados
        tokenDao.persist(tokenEntity);

        return tokenString;
    }

    /**
     * Valida um token quando o utilizador clica no link de registo.
     * Verifica se o token existe e se ainda não ultrapassou o prazo de validade.
     *
     * @param tokenString O valor do token extraído do URL.
     * @return A entidade do token se for válido; null caso não exista ou já tenha expirado.
     */
    public ConfirmationTokenEntity validateToken(String tokenString) {
        ConfirmationTokenEntity tokenEntity = tokenDao.findTokenByString(tokenString);

        if (tokenEntity == null) {
            return null; // O token não existe ou está incorreto
        }

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null; // O token existe mas as 24 horas já passaram
        }

        return tokenEntity;
    }

    /**
     * Remove um token da base de dados.
     * Deve ser chamado assim que o utilizador conclui o registo com sucesso,
     * para garantir que o mesmo link não pode ser reutilizado para criar outra conta.
     *
     * @param tokenEntity A entidade do token a eliminar.
     */
    public void deleteToken(ConfirmationTokenEntity tokenEntity) {
        // Encontra o token gerido pelo EntityManager atual e remove-o
        ConfirmationTokenEntity managedToken = tokenDao.find(tokenEntity.getId());
        if (managedToken != null) {
            tokenDao.remove(managedToken);
        }
    }
}