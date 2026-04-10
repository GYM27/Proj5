package aor.paj.projecto5.pojo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import aor.paj.projecto5.utils.UserRoles;

import java.util.ArrayList;

public class User {
    private Long id;
    @NotBlank(message = "O username não pode estar vazio.")
    @Size(min=4, message = "O username deve ter pelo menos 4 caracteres.")
    private String username;
    @NotBlank(message = "A password não pode estar vazia.")
    @Size(min = 8, message = "A password deve ter pelo menos 8 caracteres.")
    private String password;
    @NotBlank(message = "O email do utilizador não pode estar vazio.")
    @Email(message = "O email do utilizador não está num formato válido.")
    private String email;
    @NotBlank(message = "O primeiro nome do utilizador não pode estar vazio.")
    private String firstName;
    @NotBlank(message = "O último nome do utilizador não pode estar vazio.")
    private String lastName;
    @NotBlank(message = "O número de contacto não pode estar vazio.")
    @Pattern(regexp = "^(\\+\\d{1,3}( )?)?\\d{3,15}$", message = "O número introduzido não é um contacto válido")
    private String contact;
    @URL(message = "O link da foto deve ser um URL válido")
    private String photo;
    private UserRoles userRole;


    private ArrayList<Lead> leads = new ArrayList<>();
    //private ArrayList<Project> projects = new ArrayList<>();
    //private ArrayList<ClientsPojo> clientes = new ArrayList<>();

    public User() {
    }

    public User(String username, String password, String email, String firstName, String lastName, String contact, String photo) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contact = contact;
        this.photo = photo;
        this.userRole=UserRoles.NORMAL;
    }

    //Identificação
    public long getId() { return id; }
    //public void setId(long id) { this.id = id; }

    //Autenticação
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }


    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean validatePassword(String pass) {
        return pass != null && pass.equals(this.password);
    }

    //Perfil
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public UserRoles getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRoles userRole) {
        this.userRole = userRole;
    }

    //Metodos dos leads
    public ArrayList<Lead> getLeads() { return leads; }
    public void setLeads(ArrayList<Lead> leads) { this.leads = (leads != null) ? leads : new ArrayList<>(); }
    public boolean addLead(Lead lead) { return this.leads.add(lead); }

    public Lead getLeadById(String leadId) {
        if (leadId == null || leads == null) return null;
        return leads.stream().filter(l -> l.getId().equals(leadId)).findFirst().orElse(null);
    }

    public boolean removeLeadById(String leadId) {
        if (leads == null || leadId == null) return false;
        return leads.removeIf(l -> l.getId().equals(leadId));
    }

    public void setId(Long id) {
        this.id = id;
    }

    //Metodos dos projects
    /*
    public ArrayList<Project> getProjects() { return projects; }
    public void setProjects(ArrayList<Project> projects) { this.projects = (projects != null) ? projects : new ArrayList<>(); }
    public boolean addProject(Project project) { return this.projects.add(project); }

    public Project getProjectById(String projectId) {
        if (projectId == null || projects == null) return null;
        return projects.stream().filter(p -> p.getId().equals(projectId)).findFirst().orElse(null);
    }

    public boolean removeProjectById(String projectId) {
        if (projects == null || projectId == null) return false;
        return projects.removeIf(p -> p.getId().equals(projectId));
    }

    public int getProjectIndexById(String projectId) {
        for (int i = 0; i < this.projects.size(); i++) {
            if (this.projects.get(i).getId().equals(projectId)) {
                return i;
            }
        }
        return -1; // Não encontramos
    }
    */

    //Metodos dos clients
    /*
    public ArrayList<Cliente> getClientes() { return clientes; }
    public void setClientes(ArrayList<Cliente> clientes) { this.clientes = (clientes != null) ? clientes : new ArrayList<>(); }
    public boolean addCliente(Cliente cliente) { return this.clientes.add(cliente); }

    public Cliente getClientById(String clienteId) {
        if (clientes == null) return null;
        return clientes.stream().filter(c -> c.getId().equals(clienteId)).findFirst().orElse(null);
    }

    public boolean removeClienteById(String clienteId) {
        if (clientes == null) return false;
        return clientes.removeIf(c -> c.getId().equals(clienteId));
    }

    public int getClienteIndexById(String id) {
        for (int i = 0; i < this.clientes.size(); i++) {
            if (clientes.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }*/

    /*public int getLeadIndexById(String leadId) {
        for (int i = 0; i < this.leads.size(); i++) {
            if (this.leads.get(i).getId().equals(leadId)) {
                return i;
            }
        }
        return -1; // Não encontramos
    }*/




}