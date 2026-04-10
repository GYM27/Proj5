package aor.paj.projecto5.bean;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import aor.paj.projecto5.dao.TokenDao;

import java.io.Serial;
import java.io.Serializable;

@Singleton
@Startup // Garante que o agendamento é ativado assim que o servidor arranca
public class TokenCleanupBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Inject
    TokenDao tokenDao;

    /**
     * Limpeza automática de tokens expirados.
     * Configurado para correr a cada 3 horas.
     */
    @Schedule(hour = "*/3", minute = "0", second = "0", persistent = false)
    public void dailyCleanup() {
        // Chamamos o DAO para executar a NamedQuery que já definiste na Entity
        int invalidated = tokenDao.cleanUpExpiredTokens();

        if (invalidated > 0) {
            System.out.println("[MAINTENANCE] Tokens expirados desativados: " + invalidated);
        }
    }
}