package aor.paj.projecto5.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import aor.paj.projecto5.utils.LeadState;

import java.time.LocalDateTime;

public class Lead {

    private Long id;

    @NotNull(message = "O estado do lead é obrigatório")
    private LeadState estado = LeadState.NOVO;

    @NotBlank(message = "O Título do lead não pode estar vazio")
    private String titulo;

    @NotBlank(message = "A Descrição do lead não pode estar vazio")
    private String descricao;

    private LocalDateTime data;


    public Lead(Long id, String titulo, String descricao) {
        this.id=id;
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public Lead() {} // Essential for JSON-B!

    public void setId(Long id) { // Essential for JSON-B!
        this.id = id;
    }

    public LeadState getEstado() {
        return estado;
    }

    public void setEstado(LeadState estado) {
        this.estado = estado;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getId(){
        return id;
    }


    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }
}
