package aor.paj.projecto5.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Este é o meu Mapper para os erros "Forçados" ou "Planeados".
 * Serve para apanhar todas as WebApplicationExceptions que eu lanço à mão nos meus Beans
 * (ex: throw new WebApplicationException("Cliente não encontrado", 404);).
 * Garante que esses erros também vão no formato JSON certinho para o React.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        // 1. Primeiro, vou descobrir qual é o código de erro (401, 403, 404, etc.)
        // que eu defini quando lancei o erro lá no código do meu Bean.
        int status = exception.getResponse().getStatus();

        // 2. A seguir, crio o meu objeto de erro padronizado.
        // Vou buscar a mensagem exata que eu escrevi no 'throw' (exception.getMessage())
        // e junto-lhe o status code que extraí no passo acima.
        ErrorResponse error = new ErrorResponse(
                exception.getMessage(),
                status
        );

        // 3. Por fim, empacoto tudo e devolvo para o React.
        // O '.type(MediaType.APPLICATION_JSON)' garante que o Frontend (o meu api.js)
        // recebe isto como um JSON limpo e não como texto ou HTML.
        return Response.status(status)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}