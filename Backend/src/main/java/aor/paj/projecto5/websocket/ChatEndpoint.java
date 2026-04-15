package aor.paj.projecto5.websocket;

import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

/**
 * Gestor central de comunicações WebSocket para o sistema de chat.
 * Esta classe funciona como um Singleton para garantir que todas as sessões
 * de utilizadores ativos sejam armazenadas e geridas num mapa único.
 * * O endpoint está configurado para capturar o token de autenticação diretamente no URL.
 */
@Singleton
@ServerEndpoint("/websocket/chat/{token}")
public class ChatEndpoint {

    private static final Logger logger = LogManager.getLogger(ChatEndpoint.class);
    //teste
    // Mapa para associar tokens de sessão às ligações WebSocket ativas
    private HashMap<String, Session> sessions = new HashMap<>();

    /**
     * Envia uma mensagem de texto para um utilizador específico através do seu token.
     * Se a sessão não for encontrada ou ocorrer um erro no envio, a sessão é removida.
     * * @param token O identificador único do utilizador destino.
     * @param msg O conteúdo da mensagem a enviar.
     */
    public void send(String token, String msg) {
        Session session = sessions.get(token);

        if (session != null) {
            try {
                session.getBasicRemote().sendText(msg);
                logger.debug("Mensagem enviada com sucesso para o token: {}", token);
            } catch (IOException e) {
                logger.error("Falha ao enviar mensagem para o token {}. A remover sessão: {}", token, e.getMessage());
                sessions.remove(token);
            }
        } else {
            logger.warn("Tentativa de envio para um utilizador que não está ligado. Token: {}", token);
        }
    }

    /**
     * Evento disparado quando um cliente estabelece uma nova ligação.
     * Adiciono o utilizador ao meu mapa de sessões ativo.
     * * @param session A instância da ligação WebSocket.
     * @param token O token capturado na path da ligação.
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        sessions.put(token, session);
        logger.info("Nova ligação WebSocket estabelecida para o utilizador com token: {}", token);
    }

    /**
     * Evento disparado quando uma ligação é fechada (pelo cliente ou servidor).
     * Garanto que o token é removido do mapa para limpar a memória.
     * * @param session A sessão que está a ser encerrada.
     * @param reason O motivo do fecho da ligação.
     * @param token O token do utilizador que se desligou.
     */
    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam("token") String token) {
        sessions.remove(token);
        logger.info("Ligação WebSocket terminada para o utilizador {}. Motivo: {}", token, reason.getReasonPhrase());
    }

    /**
     * Lida com mensagens recebidas diretamente pelo canal WebSocket.
     * Nota: O envio principal de mensagens de chat deve ser feito via REST (POST).
     * * @param message O texto recebido.
     * @param session A sessão que enviou a mensagem.
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("Mensagem recebida no WebSocket: {}", message);
        try {
            session.getBasicRemote().sendText("Recebido");
        } catch (IOException e) {
            logger.error("Erro ao enviar confirmação de receção via WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Captura erros inesperados que ocorram durante a comunicação.
     * * @param session A sessão onde o erro ocorreu.
     * @param error A exceção lançada.
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("Ocorreu um erro crítico no canal WebSocket: {}", error.getMessage());
    }
}