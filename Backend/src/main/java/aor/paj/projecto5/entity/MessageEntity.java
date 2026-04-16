package aor.paj.projecto5.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name= "messages")
public class MessageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    // O remetente da mensagem. Ligo esta coluna à tabela de utilizadores.
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    // O destinatário da mensagem. Também é uma relação com a UserEntity.
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    // O conteúdo da mensagem. Uso 'TEXT' para permitir mensagens longas no SQL.
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // O registo temporal de quando a mensagem foi gravada.
    @Column(name = "sent_at", nullable = false)
    private Timestamp sentAt;

    /**
     * Construtor padrão exigido pelo JPA.
     */
    public MessageEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public UserEntity getReceiver() {
        return receiver;
    }

    public void setReceiver(UserEntity receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }
}
