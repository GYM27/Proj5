package aor.paj.projecto5.utils;


//Muito provavelmente não vai ser necessário neste projeto, não está no enunciado, não será implementado.
public enum ProjectState {
    PLANEAMENTO(1),
    EXECUCAO(2),
    REVISAO(3),
    CONCLUIDO(4);

    private final int stateId;

    ProjectState(int stateId) {
        this.stateId = stateId;
    }

    public int getStateId() {
        return stateId;
    }

    // Método utilitário para converter ID do Banco/API de volta para Enum
    public static ProjectState fromId(int id) {
        for (ProjectState state : values()) {
            if (state.stateId == id) return state;
        }
        return null;
    }
}
