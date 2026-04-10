package aor.paj.projecto5.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;

/**
 * Este é o meu Mapper para erros de validação de campos.
 * Serve para apanhar aquelas anotações que usei nos DTOs (tipo @NotNull ou @Email).
 * Se o utilizador preencher mal um formulário, este código extrai as mensagens de erro
 * e envia-as para o React de forma organizada.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // 1. Vou percorrer a lista de todas as violações que o Java encontrou.
        // Extraio a mensagem de cada uma (ex: "E-mail inválido") e junto-as todas
        // numa única String, separadas por vírgulas, para o utilizador ler tudo de uma vez.
        String errorMessage = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        // 2. Uso a minha classe ErrorResponse para garantir que o erro sai no formato
        // padrão que o meu frontend espera. Defino o status como 400 (Bad Request)
        // porque o erro foi do utilizador ao preencher os dados.
        ErrorResponse error = new ErrorResponse(
                "Erro de validação: " + errorMessage,
                400 // Bad Request
        );

        // 3. Devolvo a resposta ao Frontend com o código 400.
        // O '.type(MediaType.APPLICATION_JSON)' é fundamental para o meu 'api.js'
        // no React saber que tem de ler isto como um objeto JSON.
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}