package aor.paj.projecto5.bean;

import aor.paj.projecto5.dao.UserDao;
import aor.paj.projecto5.dto.MessageDto;
import aor.paj.projecto5.entity.MessageEntity;
import aor.paj.projecto5.entity.UserEntity;
import aor.paj.projecto5.websocket.ChatEndpoint;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.Timestamp;

@Stateless
public class MessageBean {
    @Inject
    private UserDao userDao;
    @Inject
    private TokenBean tokenBean;
    @Inject
    private ChatEndpoint chatEndpoint;
    @PersistenceContext(unitName = "project5PU")
    private EntityManager em;

    public void saveAndSendMessage(UserEntity sender, MessageDto messageDto) {
        // 1. Busca cirúrgica do destinatário
        UserEntity receiver = userDao.findUserByUsername(messageDto.getReceiver());
        if (receiver == null) return;

        // 2. Persistência mínima: criamos a Entity apenas com o necessário
        MessageEntity message = new MessageEntity();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageDto.getContent());
        message.setSentAt(new Timestamp(System.currentTimeMillis()));

        em.persist(message);

        // Só tentamos enviar se o user estiver online
        // O método getActiveTokenValueByUser no TokenBean deve ser uma query indexada
        String receiverToken = tokenBean.getActiveTokenValueByUser(receiver);

        if (receiverToken != null) {
            // Enviamos apenas o conteúdo (Payload mínimo)
            chatEndpoint.send(receiverToken, messageDto.getContent());
        }
    }
}