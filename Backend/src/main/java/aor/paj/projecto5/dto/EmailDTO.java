package aor.paj.projecto5.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Data Transfer Object (DTO) focado apenas no transporte de um endereço de email.
 * Ideal para operações simples como Convites de Registo ou Recuperação de Password.
 */
public class EmailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "O email é de preenchimento obrigatório.")
    @Email(message = "O formato do email é inválido.")
    private String email;

    public EmailDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}