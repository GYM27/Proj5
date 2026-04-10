package aor.paj.projecto5.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Esta é a minha rede de segurança máxima (o "apanha-tudo").
 * Se der algum erro estranho no meu Java (tipo variáveis nulas, erros de ficheiros, etc)
 * e eu não tiver criado um Mapper específico para isso, o código vai cair aqui dentro
 * em vez de enviar um ecrã de erro feio em HTML para o React.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        // 1. Vou imprimir o erro na consola do meu servidor (Wildfly).
        // Assim, se alguém se queixar que o site deu erro, eu posso ir ler a consola
        // e perceber exatamente em que linha o meu código estoirou.
        exception.printStackTrace();

        // 2. Vou criar uma mensagem genérica para o meu Frontend.
        // Nunca devo mandar a mensagem real do Java ('exception.getMessage()') para o ecrã
        // porque pode expor dados da base de dados ou falhas de segurança a hackers.
        ErrorResponse error = new ErrorResponse(
                "Ocorreu um erro interno inesperado no servidor. Por favor, tente novamente mais tarde.",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() // Status 500 (Erro de Servidor)
        );

        // 3. Pego na mensagem que criei ali em cima e devolvo para o meu React
        // formatada como um JSON limpinho. Assim o meu 'api.js' consegue ler isto perfeitamente!
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}