package aor.paj.projecto5.entity;

import aor.paj.projecto5.utils.UserState;
import jakarta.persistence.*;
import aor.paj.projecto5.utils.UserRoles;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="users")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.findUserByEmail", query = "SELECT u FROM UserEntity u WHERE u.email = :email")
@NamedQuery(name="User.findUserByContact", query = "SELECT u FROM UserEntity u WHERE u.contact = :contact")
public class UserEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

      //definir um id autoincremantável
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, unique = true,updatable = false)
    private Long id;

    //@todo definir se o username é updatable ou não. Tendo em conta que o id é que conta em termos de identificação, pode ser updatable
    @Column(name="username", nullable = false, unique = true, updatable = false)
    private String username;

    @Column(name="password", nullable = false, unique = false, updatable = true)
    private String password;

    @Column(name="email",nullable = false,unique = true, updatable = true)
    private String email;

    @Column(name="firstName", nullable = false, unique = false, updatable = true)
    private String firstName;

    @Column(name="lastName", nullable = false,unique = false, updatable = true)
    private  String lastName;

    @Column(name="contact", nullable = false, unique = true, updatable = true)
    private String contact;

    @Column(name="photo", nullable = true, unique = false, updatable = true)
    private String photo;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private UserRoles userRole= UserRoles.NORMAL;

    @Enumerated(EnumType.STRING) // Guarda o nome do estado (ex: "PENDING") na BD em vez de um número
    @Column(name = "state", nullable = false)
    private UserState state = UserState.PENDING;

    @Column()


    @OneToMany(mappedBy = "owner", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<LeadEntity> leads= new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ClientsEntity> clients= new ArrayList<>();

    //todo verificar se não há problema com usarmos o cascade delete (aqui há a questão adicional de registos auditoria).
    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TokenEntity> tokens=new ArrayList<>();



    //construtor vazio, obrigatório
    public UserEntity() {
    }

    public void addLead(LeadEntity lead) {
        leads.add(lead);
        lead.setOwner(this); // Ensures the Lead knows who its owner is
    }
    public void addClient(ClientsEntity client) {
        this.clients.add(client);
        client.setOwner(this); // Garante que o cliente sabe quem é o dono
    }
    public void removeClient(ClientsEntity client) {
        if (client != null) {
            this.clients.remove(client);
            client.setOwner(null); // Remove a ligação
        }
    }

    public List<ClientsEntity> getClients() {
        return clients;
    }

    public void setClients(List<ClientsEntity> clients) {
        this.clients = clients;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public UserRoles getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRoles userRole) {
        this.userRole = userRole;
    }

    public List<LeadEntity> getLeads() {
        return leads;
    }

    public void setLeads(List<LeadEntity> leads) {
        this.leads = leads;
    }

    public Long getId() {
        return id;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }
}
