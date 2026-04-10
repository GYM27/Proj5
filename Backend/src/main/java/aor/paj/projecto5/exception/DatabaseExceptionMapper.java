package aor.paj.projecto5.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Mapeador de Exceções de Base de Dados (Estratégia de Inspeção Recursiva).
 * -----------------------------------------------------------------------
 * Este Mapper intercepa exceções que ocorrem durante a persistência (JPA/Hibernate).
 * É fundamental para converter erros de integridade do SQL em mensagens amigáveis.
 */
@Provider
public class DatabaseExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        // 1. ANÁLISE RECURSIVA (O "Escavador"):
        // Muitas vezes o erro real (Unique Constraint) está escondido sob várias camadas
        // de exceções do Java (EJBException -> RollbackException -> ConstraintViolation).
        Throwable cause = exception;

        // Limitamos a 10 níveis para evitar ciclos infinitos e garantir performance.
        for (int i = 0; i < 10 && cause != null; i++) {
            String message = cause.getMessage();

            // 2. DETEÇÃO DE CONFLITOS (REGRA A9 / UNICIDADE):
            // Procuramos por palavras-chave que indicam violação de campos ÚNICOS
            // definidos na UserEntity (username, email ou contact).
            if (message != null && (
                    message.contains("duplicate key") ||
                            message.contains("ConstraintViolation") ||
                            message.contains("Unique index") ||
                            message.contains("violates unique constraint") ||
                            message.contains("UK_") // Comum em nomes de índices gerados pelo Hibernate
            )) {

                // 3. RESPOSTA DE CONFLITO (409):
                // Encontramos um duplicado. Criamos o ErrorResponse que o React espera ler.
                ErrorResponse error = new ErrorResponse(
                        "Conflito de dados: O utilizador (username, email ou contacto) já se encontra registado no sistema.",
                        409
                );

                return Response.status(Response.Status.CONFLICT)
                        .entity(error)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }

            // Se não encontrou nesta camada, mergulha na causa seguinte.
            cause = cause.getCause();
        }

        /**
         * 4. DELEGAÇÃO:
         * Se o ciclo terminar e não for um erro de duplicado, retornamos 'null'.
         * No JAX-RS, retornar 'null' num ExceptionMapper faz com que a exceção
         * continue a propagar-se, sendo capturada pelo próximo Mapper da lista
         * (neste caso, o GlobalExceptionMapper para dar erro 500).
         */
        return null;
    }
}