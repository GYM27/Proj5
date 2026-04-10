package aor.paj.projecto5.service;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import aor.paj.projecto5.bean.LoginBean;
import aor.paj.projecto5.dto.LoginDTO;
import aor.paj.projecto5.dto.LoginResponseDTO;

/**
 * Serviço dedicado à Autenticação.
 * Separamos do UserService para manter a lógica de sessão isolada.
 */
@Path("/auth")
public class LoginService {

    @Inject
    LoginBean loginBean;

    /**
     * Realiza o login e devolve o token de acesso.
     * URL: POST /auth/login
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Valid LoginDTO loginDTO) { // Adicionado @Valid!
        // O Bean agora lança WebApplicationException se falhar
        LoginResponseDTO loginResponseDTO = loginBean.login(loginDTO);
        return Response.ok(loginResponseDTO).build();
    }

    /**
     * Finaliza a sessão invalidando o token.
     * URL: POST /auth/logout
     */
    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("token") String token) {
        // Invalidar o token no Bean/Dao
        boolean success = loginBean.logout(token);

        if (success) {
            return Response.noContent().build(); // 204: Sucesso sem corpo
        } else {
            // Se o token já não era válido, retornamos 200 avisando o cliente
            return Response.ok("{\"message\":\"Sessão já se encontrava expirada.\"}").build();
        }
    }
}