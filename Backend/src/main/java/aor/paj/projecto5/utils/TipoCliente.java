package aor.paj.projecto5.utils;

public enum TipoCliente {
    // associação de um id númérico
    VENDEDOR(1),
    COMPRADOR(2);

    private final int tipoId;

    TipoCliente(int tipoId) {
        this.tipoId = tipoId;
    }

    public int getTipoId() {
        return tipoId;
    }

    /**
     * Metodo Estatico (Utilitário): Faz o caminho inverso.
     * Recebe um número (ex: 2) e devolve a constante do Enum (COMPRADOR).
     * * @param id O ID numérico vindo do frontend ou base de dados
     * @return A constante TipoCliente correspondente
     * @throws IllegalArgumentException Caso o ID não exista na lista
     */
    public static TipoCliente fromId(int id) {
        // Percorre todos os valores definidos no Enum (VENDEDOR, COMPRADOR)
        for(TipoCliente tipo : values()) {
            if (tipo.tipoId == id) return tipo;
        }
        // Se o loop terminar sem encontrar nada, lança um erro para o sistema não falhar silenciosamente
        throw new IllegalArgumentException("ID de tipo inválido: " + id);
    }
}
