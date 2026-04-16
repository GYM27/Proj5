package aor.paj.projecto5.bean;

import aor.paj.projecto5.utils.UserState;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import aor.paj.projecto5.dao.TokenDao;
import aor.paj.projecto5.entity.TokenEntity;
import aor.paj.projecto5.entity.UserEntity;
import aor.paj.projecto5.utils.UserRoles;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import static aor.paj.projecto5.utils.UserState.DISABLED;

@Stateless
public class TokenBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Inject
    TokenDao tokenDao;

    /**
     * Gera um novo token. A validade (8h) e datas são tratadas pelo @PrePersist da Entity.
     */
    public String generateNewToken(UserEntity owner) {
        String randomValue = UUID.randomUUID().toString();
        TokenEntity newToken = new TokenEntity();
        newToken.setTokenValue(randomValue);
        newToken.setOwner(owner);

        tokenDao.persist(newToken);
        return randomValue;
    }

    /**
     * Valida se o token existe, está ativo e dentro do prazo de expiração.
     */
    public boolean isTokenValid(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) return false;

        TokenEntity token = tokenDao.findToken(tokenValue);

        // 1. Verifica existência e estado booleano
        if (token == null || !token.isActive()) {
            return false;
        }

        // 2. Verifica expiração temporal
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            invalidateToken(tokenValue); // "Mata" o token na base de dados
            return false;
        }

        return true;
    }

    public UserEntity getUserEntityByToken(String token) {
        if (!isTokenValid(token)) return null;
        return tokenDao.findUserEntityByToken(token);
    }

    public UserRoles getUserRoleByToken(String token) {
        UserEntity user = getUserEntityByToken(token);
        return (user != null) ? user.getUserRole() : null;
    }

    public boolean getUserSoftDelete(String token) {
        UserEntity user = getUserEntityByToken(token);

        // Se não encontrar user ou o token for inválido, retornamos true (bloqueado)
        return (user == null) || (user.getState() == UserState.DISABLED);
    }

    public boolean invalidateToken(String tokenValue) {
        return tokenDao.invalidateToken(tokenValue) > 0;
    }

    /**
     * Procura o valor do token ativo de um utilizador específico.
     * Este método é crucial para o MessageBean saber se o destinatário está "online"
     * e para que canal WebSocket deve enviar a mensagem.
     */
    public String getActiveTokenValueByUser(UserEntity receiver) {
        // 1. Peço ao DAO para procurar o token ativo associado a este UserEntity
        TokenEntity token = tokenDao.findActiveTokenByUser(receiver);

        // 2. Valido se o token existe e se ainda é válido (não expirou)
        // Uso o método isTokenValid que já criámos para reaproveitar a lógica de expiração
        if (token != null && isTokenValid(token.getTokenValue())) {
            return token.getTokenValue();
        }

        // Se não encontrar ou estiver expirado, retorno null (destinatário offline)
        return null;
    }
}