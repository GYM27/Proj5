package aor.paj.projecto5.service;

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
     * Regista um novo utilizador no sistema.
     * URL: POST /users/register
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(@Valid UserDTO user) {
        usersBean.registerUser(user);
        return Response.status(Response.Status.CREATED).entity(user).build();
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
}