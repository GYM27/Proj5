package aor.paj.projecto5.bean;

import aor.paj.projecto5.entity.UserEntity;
import aor.paj.projecto5.utils.UserRoles;
import aor.paj.projecto5.entity.LeadEntity;
import aor.paj.projecto5.entity.ClientsEntity;
import aor.paj.projecto5.utils.UserState;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.WebApplicationException;

@RequestScoped
public class UserVerificationBean {

    @Inject
    private TokenBean tokenBean;

    @PersistenceContext(unitName = "project5PU")
    private EntityManager em;

    /**
     * Validação Básica: Verifica se o token é válido e o utilizador está ATIVO.
     * Bloqueia automaticamente utilizadores PENDING ou DISABLED.
     */
    public UserEntity verifyUser(String token) {
        if (token == null || !tokenBean.isTokenValid(token)) {
            throw new WebApplicationException("Sessão inválida ou expirada.", 401);
        }

        UserEntity user = tokenBean.getUserEntityByToken(token);

        if (user == null) {
            throw new WebApplicationException("Sessão inválida. Utilizador não encontrado.", 401);
        }

        if (user.getState() != UserState.ACTIVE) {
            throw new WebApplicationException("A sua conta encontra-se desativada ou pendente. Contacte um administrador.", 403);
        }

        return user;
    }

    /**
     * Validação de Administrador: Garante que o utilizador tem permissões de ADMIN.
     */
    public void verifyAdmin(String token) {
        // Aproveitamos a validação básica (se não estiver ativo ou não existir, o verifyUser já lança erro)
        UserEntity user = verifyUser(token);

        // Limpeza feita aqui: lançamos a exceção de forma simples!
        if (user.getUserRole() != UserRoles.ADMIN) {
            throw new WebApplicationException("Acesso restrito a administradores.", 403);
        }
    }

    // --- MÉTODOS DE PROPRIEDADE (OWNERSHIP) PARA LEADS E CLIENTES ---

    /**
     * Verifica se o utilizador é dono do Cliente ou se tem privilégios de Admin.
     */
    public void verifyOwnershipOrAdmin(String token, Long clientId) {
        if (clientId == null) {
            throw new WebApplicationException("ID do Cliente obrigatório.", 400); // 400 Bad Request
        }

        UserEntity user = verifyUser(token);

        // Se for Admin, tem passe livre
        if (user.getUserRole() == UserRoles.ADMIN) return;

        ClientsEntity client = em.find(ClientsEntity.class, clientId);
        if (client == null) {
            throw new WebApplicationException("Cliente não encontrado.", 404); // 404 Not Found
        }

        // Valida se o ID do dono bate certo com o ID de quem está a fazer o pedido
        if (!client.getOwner().getId().equals(user.getId())) {
            throw new WebApplicationException("Não tem permissão para editar ou visualizar este cliente.", 403); // 403 Forbidden
        }
    }

    /**
     * Verifica se o utilizador é dono da Lead ou se tem privilégios de Admin.
     */
    public void verifyLeadOwnership(String token, Long leadId) {
        if (leadId == null) {
            throw new WebApplicationException("ID da Lead obrigatório.", 400);
        }

        UserEntity user = verifyUser(token);

        if (user.getUserRole() == UserRoles.ADMIN) return;

        LeadEntity lead = em.find(LeadEntity.class, leadId);
        if (lead == null) {
            throw new WebApplicationException("Lead não encontrada.", 404);
        }

        if (!lead.getOwner().getId().equals(user.getId())) {
            throw new WebApplicationException("Não tem permissão para gerir esta lead.", 403);
        }
    }
}