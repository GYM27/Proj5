package aor.paj.projecto5.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDTO extends UserBaseDTO{

    @NotBlank(message = "A password não pode estar vazia")
    @Size(min = 8, message = "A password deve ter pelo menos 8 caracteres")
    private String password;

    public UserDTO() {
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
