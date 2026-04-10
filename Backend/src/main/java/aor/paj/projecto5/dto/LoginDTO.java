package aor.paj.projecto5.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginDTO {

    @NotBlank(message = "O username não pode estar vazio.")
    @Size(min=4, message = "O username deve ter pelo menos 4 caracteres.")
    public String username;
    @NotBlank(message = "A password não pode estar vazia.")
    @Size(min = 8, message = "A password deve ter pelo menos 8 caracteres.")
    public String password;

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginDTO() { //é preciso para o json
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
}
