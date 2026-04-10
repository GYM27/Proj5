package aor.paj.projecto5.entity;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="tokens")
@NamedQuery(name="token.invalidate", query = "update TokenEntity set active = false where tokenValue= :tokenValue")
@NamedQuery(name="token.cleanExpired", query = "update TokenEntity set active = false where expiresAt< :now")
@NamedQuery(name="token.findToken", query="select t from TokenEntity t where t.tokenValue= :tokenValue")
@NamedQuery(name="token.returnUserID", query="select t.owner.id from TokenEntity t where t.tokenValue=:tokenValue")
@NamedQuery(name="token.returnUserEntity", query="select t.owner from TokenEntity t where t.tokenValue=:tokenValue")

public class TokenEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    //diz ao JPA para não criar uma coluna para esta constante
    @Transient
    private final long TOKENVALIDITY=8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String tokenValue;

    @Column(nullable = false, unique = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false,unique = false,updatable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false,unique = false, updatable = true)
    private boolean active;

    @Column(nullable = false, unique = false, updatable = true)
    private LocalDateTime lastUsedAt;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    @PrePersist
    protected void onCreate(){
        this.createdAt=LocalDateTime.now();
        this.expiresAt=createdAt.plusHours(TOKENVALIDITY);
        this.active=true; //iniciar como verdadeiro qd se cria
        this.lastUsedAt=createdAt; //iniciar com o valor de quando é criado
    }


    public TokenEntity(){}



    public Long getId() {
        return id;
    }


    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

}
