package aor.paj.projecto5.entity;

import aor.paj.projecto5.utils.NotificationType;
import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;


@Entity
@Table(name= "notifications")

public class NotificationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    //Relaçao com destinatario
    @ManyToOne
    @JoinColumn(name= "receiver_id", nullable = false)
    private UserEntity receiver;

    @Column(name= "content" , nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name= "notification_type",nullable = false)
    private NotificationType type;

    @Column(name= "creat_at", nullable = false)
    private Timestamp creatAt;

    @Column(name= "is_read", nullable = false)
    private boolean isRead;

    // 1. Construtor Vazio (Obrigatório para o JPA/Hibernate)
    public NotificationEntity() {
    }

    /**
     * Este método garante que a data é gerada automaticamente
     * no momento em que a notificação é gravada.
     */
    @PrePersist
    public void onSave() {
        this.creatAt = new Timestamp(System.currentTimeMillis());
    }




    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Timestamp getCreatAt() {
        return creatAt;
    }

    public void setCreatAt(Timestamp creatAt) {
        this.creatAt = creatAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
