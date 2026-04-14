package aor.paj.projecto5.utils;

public enum UserState {
    PENDING,    // Aguardando confirmação (bloqueado)
    ACTIVE,     // Conta confirmada e ativa
    DISABLED    // Conta desativada pelo admin (soft delete)
}
