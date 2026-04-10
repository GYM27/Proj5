package aor.paj.projecto5.utils;
import jakarta.json.bind.annotation.JsonbCreator;

public enum LeadState {
    NOVO(1),
    ANALISE(2),
    PROPOSTA(3),
    GANHO(4),
    PERDIDO(5);

    private final int leadStateId;

    LeadState(int leadStateId) {
        this.leadStateId = leadStateId;

    }

    public int getStateId() {
        return leadStateId;
    }

    // Método utilitário para converter ID do Banco/API de volta para Enum
    @JsonbCreator
    public static LeadState fromId(int id) {
        for (LeadState state : values()) {
            if (state.leadStateId == id) return state;
        }
        // Lançar uma exceção que o JAX-RS reconhece como erro do cliente (400)
        throw new jakarta.ws.rs.BadRequestException("ID de estado inválido: " + id);
    }
}

