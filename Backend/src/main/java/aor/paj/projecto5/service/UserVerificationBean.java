package aor.paj.projecto5.service;

import aor.paj.projecto5.bean.TokenBean;
import aor.paj.projecto5.entity.UserEntity;
import aor.paj.projecto5.utils.UserRoles;
import aor.paj.projecto5.entity.LeadEntity;
import aor.paj.projecto5.entity.ClientsEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@RequestScoped
public class UserVerificationBean {

    @Inject
    private TokenBean tokenBean;

    @PersistenceContext(unitName = "project5PU")
    private EntityManager em;

    /**
     * Validação Básica: Verifica se o token é válido e o utilizador está ATIVO.
     * Usado em: GET /me, PUT /me
     */
    public UserEntity verifyUser(String token) {
        if (token == null || !tokenBean.isTokenValid(token)) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Sessão inválida ou expirada.").build());
        }

        UserEntity user = tokenBean.getUserEntityByToken(token);

        if (user == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Utilizador não encontrado.").build());
        }

        // Bloqueia utilizadores que sofreram Soft Delete
        if (user.isSoftDelete()) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                    .entity("A sua conta está desativada. Contacte um administrador.").build());
        }

        return user;
    }

    /**
     * Validação de Administrador: Garante que o utilizador tem permissões de ADMIN.
     * Usado em: GET /users, PATCH /users/{id}/deactivate, DELETE /users/{id}, etc.
     */
    public void verifyAdmin(String token) {
        // Primeiro, verificamos se é um utilizador válido e ativo
        UserEntity user = verifyUser(token);

        // Depois, verificamos se o Role é ADMIN
        if (user.getUserRole() != UserRoles.ADMIN) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                    .entity("Acesso restrito a administradores.").build());
        }
    }

    // --- MÉTODOS DE PROPRIEDADE (OWNERSHIP) PARA LEADS E CLIENTES ---

    /**
     * Verifica se o utilizador é dono do Cliente ou se é Admin.
     */
    public void verifyOwnershipOrAdmin(String token, Long clientId) {
        if (clientId == null) {
            throw new WebApplicationException(Response.status(400).entity("ID do Cliente obrigatório.").build());
        }

        UserEntity user = verifyUser(token);
        if (user.getUserRole() == UserRoles.ADMIN) return;

        ClientsEntity client = em.find(ClientsEntity.class, clientId);
        if (client == null) {
            throw new WebApplicationException(Response.status(404).entity("Cliente não encontrado.").build());
        }

        if (!client.getOwner().getId().equals(user.getId())) {
            throw new WebApplicationException(Response.status(403).entity("Não tem permissão para este cliente.").build());
        }
    }

    /**
     * Verifica se o utilizador é dono da Lead ou se é Admin.
     */
    public void verifyLeadOwnership(String token, Long leadId) {
        if (leadId == null) {
            throw new WebApplicationException(Response.status(400).entity("ID da Lead obrigatório.").build());
        }

        UserEntity user = verifyUser(token);
        if (user.getUserRole() == UserRoles.ADMIN) return;

        LeadEntity lead = em.find(LeadEntity.class, leadId);
        if (lead == null) {
            throw new WebApplicationException(Response.status(404).entity("Lead não encontrada.").build());
        }

        if (!lead.getOwner().getId().equals(user.getId())) {
            throw new WebApplicationException(Response.status(403).entity("Não tem permissão para esta lead.").build());
        }
    }
}