package aor.paj.projecto5.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public class UserBaseDTO {

    // Campos do admin
    private Long id;
    @NotBlank(message = "O username não pode estar vazio")
    public String username;

    private String role;
    private boolean softDelete;

    //Campos Geral

    @NotBlank(message = "O email do utilizador não pode estar vazio.")
    @Email(message = "O email do utilizador não está num formato válido.")
    private String email;
    @NotBlank(message = "O primeiro nome do utilizador não pode estar vazio.")
    private String firstName;
    @NotBlank(message = "O último nome do utilizador não pode estar vazio.")
    private String lastName;
    @NotBlank(message = "O número de contacto não pode estar vazio.")
    @Pattern(regexp = "^(\\+\\d{1,3}( )?)?\\d{3,15}$", message = "O número introduzido não é um contacto válido")
    private String cellphone;
    @URL(message = "O link da foto deve ser um URL válido")
    private String photoUrl;

    public UserBaseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isSoftDelete() {
        return softDelete;
    }

    public void setSoftDelete(boolean softDelete) {
        this.softDelete = softDelete;
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

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String contact) {
        this.cellphone = contact;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

}
