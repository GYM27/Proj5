package aor.paj.projecto5.pojo;


public class ClientsPojo {

    private int id;
    private String nome;
    private String email;
    private String telefone;
    private String organizacao;

    public ClientsPojo() {
    }

    public ClientsPojo(String organizacao, String nome, String email, String telefone, int id) {
        this.organizacao = organizacao;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getOrganizacao() {
        return organizacao;
    }

    public void setOrganizacao(String organizacao) {
        this.organizacao = organizacao;
    }
}

