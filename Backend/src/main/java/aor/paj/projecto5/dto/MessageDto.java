package aor.paj.projecto5.dto;

/**
 * Objeto de transferência de dados para mensagens.
 * Uso esta classe para capturar o que o utilizador escreve no frontend
 * e transportar esses dados de forma segura até ao meu serviço REST.
 * Adiciono o campo timestamp para registar o momento exato do envio.
 * * Isto ajuda-me a ordenar as mensagens no ecrã do destinatário.
 */
public class MessageDto {

    private String receiver; // Username ou ID do destino
    private String content; // O texto da mensagem
    private long timestamp; // hora da mensagem

    // Construtor vazio necessário para o Jackson (JSON)
    public MessageDto() {
    }

    /**
     * Getters e Setters para que o servidor consiga ler e escrever os dados.
     */
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}