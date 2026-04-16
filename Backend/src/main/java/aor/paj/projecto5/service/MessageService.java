package aor.paj.projecto5.service;

import aor.paj.projecto5.bean.MessageBean;
import aor.paj.projecto5.bean.UserVerificationBean;
import aor.paj.projecto5.dto.MessageDto;
import aor.paj.projecto5.entity.UserEntity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Serviço REST otimizado para a gestão de mensagens.
 * Atua apenas como porta de entrada, validando a segurança antes de delegar a lógica.
 */
@Path("/messages")
public class MessageService {

    private static final Logger logger = LogManager.getLogger(MessageService.class);

    @Inject
    UserVerificationBean userVerificationBean; // O meu "segurança" (valida token e estado)

    @Inject
    MessageBean messageBean; // O meu "maestro" (grava e envia)

    /**
     * Recebe e processa uma nova mensagem.
     * @param token Token de autenticação passado no Header (Segurança).
     * @param messageDto Payload mínimo com o destino e conteúdo.
     * @return Response 200 OK ou erro via ExceptionMapper.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage(@HeaderParam("token") String token, MessageDto messageDto) {

        logger.info("Pedido de envio de mensagem recebido para o destinatário: {}", messageDto.getReceiver());

        // 1. Validação de Segurança (Escalável e Centralizada)
        // Se o token for inválido ou o user não estiver ACTIVE, lança exceção automaticamente.
        UserEntity sender = userVerificationBean.verifyUser(token);

        // 2. Execução da Lógica de Negócio (Assíncrona para o Service)
        // Passo a Entity do sender já validada para evitar novas procuras desnecessárias.
        messageBean.saveAndSendMessage(sender, messageDto);

        return Response.ok("Mensagem processada").build();
    }
}