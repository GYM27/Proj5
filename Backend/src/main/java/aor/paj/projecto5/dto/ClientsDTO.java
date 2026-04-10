package aor.paj.projecto5.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * DTO para transferência de dados de Clientes entre o Frontend e a API REST.
 */
public class ClientsDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    //@NotNull
    private Long id;
    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @Email(message = "Formato de email inválido")
    @NotBlank(message = "O email é obrigatório")
    private String email;

    @NotBlank(message = "O telemóvel é obrigatório")
    @Pattern(regexp = "\\d{9,15}", message = "O telemóvel deve ter entre 9 e 15 dígitos")
    private String phone;

    private String organization;
    private boolean softDeleted;// Opcional segundo o enunciado

    // Construtor vazio obrigatório para a desserialização JSON
    public ClientsDTO() {
    }

    // Getters e Setters
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isSoftDeleted() {
        return softDeleted;
    }

    public void setSoftDeleted(boolean softDeleted) {
        this.softDeleted = softDeleted;
    }
}