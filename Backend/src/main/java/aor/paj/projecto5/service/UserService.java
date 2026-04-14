package aor.paj.projecto5.service;

import aor.paj.projecto5.bean.UserVerificationBean;
import aor.paj.projecto5.dto.EmailDTO;
import aor.paj.projecto5.dto.LoginDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import aor.paj.projecto5.bean.UsersBean;
import aor.paj.projecto5.dto.UserBaseDTO;
import aor.paj.projecto5.dto.UserDTO;
import java.util.List;

/**
 * Serviço unificado para gestão de Utilizadores (Perfil e Administração).
 */
@Path("/users")
public class UserService {

    @Inject
    UsersBean usersBean;

    @Inject
    UserVerificationBean verifier;

    // =========================================================================
    // SEÇÃO DE UTILIZADOR COMUM (Perfil Próprio e Registo)
    // =========================================================================

    /**
     * PASSO 1 (ADMIN): Envia um convite de registo para um novo email.
     * Apenas Administradores podem aceder a este endpoint.
     * URL: POST /users/invite
     */
    @POST
    @Path("/invite")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response inviteUser(@Valid EmailDTO emailDTO, @HeaderParam("token") String adminToken) {

        // 1. Garante que quem está a tentar enviar o convite é um Administrador
        verifier.verifyAdmin(adminToken);

        // 2. Chama o Passo 1 do nosso Bean (que vai validar se já existe e gerar o Token)
        usersBean.requestRegistration(emailDTO.getEmail());

        return Response.ok("{\"message\":\"Convite enviado com sucesso para " + emailDTO.getEmail() + "\"}").build();
    }

    /**
     * Conclui o registo usando o token recebido por email.
     * Endpoint público (não exige verifier).
     * URL: POST /users/register?token=123-abc-456
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeRegistration(@QueryParam("token") String token, @Valid UserDTO userDTO) {

        // 1. Valida se o token foi enviado no link
        if (token == null || token.isEmpty()) {
            return Response.status(400).entity("{\"error\":\"Token de convite em falta.\"}").build();
        }

        // 2. Chama o Passo 2 do nosso Bean (valida token, valida email, cria utilizador ACTIVE)
        usersBean.completeRegistration(token, userDTO);

        return Response.status(Response.Status.CREATED)
                .entity("{\"message\":\"Conta criada com sucesso! Já pode fazer login.\"}")
                .build();
    }

    /**
     * Retorna o perfil completo do próprio utilizador (via token).
     * URL: GET /users/me
     */
    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyProfile(@HeaderParam("token") String token) {
        verifier.verifyUser(token);
        UserDTO user = usersBean.getUserDTOByToken(token);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }

    /**
     * Edita o perfil do próprio utilizador (via token).
     * URL: PUT /users/me
     */
    @PUT
    @Path("/me")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putEditMyProfile(@HeaderParam("token") String token, @Valid UserDTO userDTO) {
        verifier.verifyUser(token);
        usersBean.putEditOwnUser(token, userDTO);
        return Response.ok(userDTO).build();
    }


    // =========================================================================
    // SEÇÃO DE ADMINISTRADOR (Gestão de Terceiros)
    // =========================================================================

    /**
     * Lista todos os utilizadores (apenas Admin).
     * URL: GET /users
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(@HeaderParam("token") String token) {
        verifier.verifyAdmin(token);
        List<UserBaseDTO> users = usersBean.getAllUsers();
        return Response.ok(users).build();
    }

    /**
     * Obtém um utilizador específico por ID (apenas Admin).
     * URL: GET /users/{id}
     */
    @GET
    @Path("/{id:[0-9]+}") // REGEX ADICIONADA
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("id") Long id, @HeaderParam("token") String token) {
        verifier.verifyAdmin(token);
        UserBaseDTO u = usersBean.getUserBaseDTOById(id);
        if (u == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(u).build();
    }

//    /**
//     * Edita qualquer utilizador por ID (apenas Admin).
//     * URL: PUT /users/{id}
//     */
//    @PUT
//    @Path("/{id:[0-9]+}") // REGEX ADICIONADA
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response adminEditUser(
//            @PathParam("id") Long id,
//            @HeaderParam("token") String token,
//            @Valid UserBaseDTO userBaseDTO) {
//
//        verifier.verifyAdmin(token);
//        usersBean.putEditUser(id, userBaseDTO);
//        return Response.ok(userBaseDTO).build();
//    }

    /**
     * Desativa um utilizador (Soft Delete).
     * URL: PATCH /users/{id}/deactivate
     */
    @PATCH
    @Path("/{id:[0-9]+}/deactivate") // REGEX ADICIONADA
    @Produces(MediaType.APPLICATION_JSON)
    public Response deactivateUser(@PathParam("id") Long id, @HeaderParam("token") String token) {
        verifier.verifyAdmin(token);
        usersBean.softDeleteUser(id);
        return Response.ok("{\"message\":\"Utilizador desativado.\"}").build();
    }

    /**
     * Reativa um utilizador.
     * URL: PATCH /users/{id}/activate
     */
    @PATCH
    @Path("/{id:[0-9]+}/activate") // REGEX ADICIONADA
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateUser(@PathParam("id") Long id, @HeaderParam("token") String token) {
        verifier.verifyAdmin(token);
        usersBean.softUnDeleteUser(id);
        return Response.ok("{\"message\":\"Utilizador reativado.\"}").build();
    }

    /**
     * Remove permanentemente e reatribui leads ao user 999.
     * URL: DELETE /users/{id}
     */
    @DELETE
    @Path("/{id:[0-9]+}") // REGEX ADICIONADA
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") Long id, @HeaderParam("token") String token) {
        verifier.verifyAdmin(token);
        usersBean.deleteUser(id);
        return Response.ok("{\"message\":\"Utilizador removido permanentemente.\"}").build();
    }

    @GET
    @Path("/username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByUsername(@PathParam("username") String username) {
        UserBaseDTO user = usersBean.getUserBaseDTOByUsername(username);
        return Response.ok(user).build();
    }


    /**
     * Endpoint para solicitar a recuperação de password.
     * URL: POST /users/forgot-password
     */
    @POST
    @Path("/forgot-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response forgotPassword(@Valid EmailDTO emailDTO) {
        usersBean.requestPasswordReset(emailDTO.getEmail());
        // Retornamos sempre sucesso por segurança (Privacy)
        return Response.ok("{\"message\":\"Se o email existir, receberá um link de recuperação.\"}").build();
    }

    /**
     * Endpoint para definir a nova password.
     * URL: POST /users/reset-password?token=...
     */
    @POST
    @Path("/reset-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetPassword(@QueryParam("token") String token, LoginDTO loginDTO) {
        // Usamos o LoginDTO porque já tem o campo password, ou podes criar um NewPasswordDTO
        if (token == null || loginDTO.getPassword() == null) {
            throw new WebApplicationException("Token ou Password em falta.", 400);
        }

        usersBean.resetPassword(token, loginDTO.getPassword());
        return Response.ok("{\"message\":\"Password atualizada com sucesso.\"}").build();
    }

}