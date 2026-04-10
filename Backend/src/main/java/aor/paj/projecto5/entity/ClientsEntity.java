package aor.paj.projecto5.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "clientes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "owner_id"})
},
        indexes = {
                @Index(name = "idx_owner_softdelete", columnList = "owner_id, softDelete")
        }
)


@NamedQueries({
        // Query para o Administrador ver TODOS os clientes ativos do sistema (de todos os users)
        @NamedQuery(
                name = "ClientsEntity.findAdminActive",
                query = "SELECT c FROM ClientsEntity c WHERE c.softDelete = false"
        ),
        // Query para o User ver apenas os seus ativos
        @NamedQuery(
                name = "ClientsEntity.findActiveByOwner",
                query = "SELECT c FROM ClientsEntity c WHERE c.owner = :owner AND c.softDelete = false"
        ),
        @NamedQuery(
                name = "ClientsEntity.findByEmailAndOwner",
                query = "SELECT c FROM ClientsEntity c WHERE c.email = :email AND c.owner = :owner"
        ),
        @NamedQuery(
                name = "ClientsEntity.findDeletedByOwner",
                query = "SELECT c FROM ClientsEntity c WHERE c.owner.id = :ownerId AND c.softDelete = true"
        )
})
public class ClientsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "organization")
    private String organization;

    @Column(name = "softDelete", nullable = false)
    private boolean softDelete = false;

    // Relação: Muitos Clientes pertencem a 1 Utilizador
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    public ClientsEntity() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public boolean isSoftDelete() {
        return softDelete;
    }

    public void setSoftDelete(boolean softDelete) {
        this.softDelete = softDelete;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }
}

