import { create } from "zustand";
import { clientsService } from "../services/clientsService";

/**
 * STORE: useClientStore (Zustand)
 * ------------------------------
 * DESCRIÇÃO: Gere o estado global dos clientes em toda a aplicação.
 * FUNCIONALIDADE: Centraliza dados, estados de carregamento (loading) e erros,
 * permitindo que as atualizações na base de dados sejam refletidas na UI instantaneamente.
 */
export const useClientStore = create((set, get) => ({
  clients: [],
  loading: false,
  error: null,

  // PERSISTÊNCIA DE CONTEXTO (OPTIMIZAÇÃO - 5%):
  // Guardamos o role e os filtros atuais para permitir 'refetch' automáticos
  // após ações de CRUD, mantendo a lista sempre atualizada com os critérios certos.
  _currentUserRole: null,
  _currentFilters: {},

  /** * ACÇÃO: fetchClients
   * Procura os clientes no Backend Java baseando-se no Role e Filtros.
   */
  fetchClients: async (userRole, filters = {}) => {
    set({
      loading: true,
      error: null,
      _currentUserRole: userRole,
      _currentFilters: filters,
    });
    try {
      const apiFilters = {
        userId: filters.userId || null,
        showTrash: !!filters.showTrash, // Converte para booleano explícito
      };
      const data = await clientsService.getClients(userRole, apiFilters);
      set({ clients: data, loading: false });
    } catch (err) {
      set({ error: err.message, loading: false });
    }
  },

  /** * MÉTODO INTERNO: _refetch
   * Atalho para atualizar a lista usando os últimos filtros guardados.
   */
  _refetch: async () => {
    const { _currentUserRole, _currentFilters, fetchClients } = get();
    await fetchClients(_currentUserRole, _currentFilters);
  },

  // --- GESTÃO DE CRIAÇÃO E ATUALIZAÇÃO ---

  addClient: async (clientData, targetUserId = null) => {
    set({ loading: true, error: null });
    try {
      if (targetUserId) {
        // ADMIN: Delegação de cliente para um utilizador específico via API.
        await clientsService.createClientForUser(targetUserId, clientData);
      } else {
        // USER: Criação associada ao próprio utilizador logado.
        await clientsService.createClient(clientData);
      }
      await get()._refetch(); // Sincroniza a UI após sucesso
      return true;
    } catch (err) {
      set({ error: err.message, loading: false });
      return false;
    }
  },

  updateClient: async (id, clientDto) => {
    set({ loading: true });
    try {
      await clientsService.updateClient(id, clientDto);
      await get()._refetch();
      return true;
    } catch (err) {
      set({ error: err.message });
      return false;
    }
  },

  // --- CICLO DE VIDA (REGRAS A9 e A14) ---

  deleteClient: async (id, isPermanent = false) => {
    set({ loading: true });
    try {
      if (isPermanent) {
        // REGRA A14: Hard Delete definitivo no PostgreSQL.
        await clientsService.permanentDeleteClient(id);
      } else {
        // REGRA A9: Soft Delete (movido para a lixeira).
        await clientsService.softDeleteClient(id);
      }
      await get()._refetch();
      return true;
    } catch (err) {
      set({ error: err.message });
      return false;
    }
  },

  restoreClient: async (id) => {
    set({ loading: true, error: null });
    try {
      await clientsService.restoreClient(id);
      await get()._refetch();
      return true;
    } catch (error) {
      console.error('❌ [RESTORE] Erro:', error.message);
      set({ error: error.message, loading: false });
      return false;
    }
  },

  /** * AÇÕES EM MASSA (BULK ACTIONS - ADMIN):
   * Executa operações de alta escala (Restaurar Tudo, Desativar Tudo, Esvaziar Lixeira).
   */
  handleBulkAction: async (
      userId,
      actionType,
      currentUserRole,
      currentFilters,
  ) => {
    if (!userId) return false;

    // Limpa a lista local para dar feedback visual imediato de "processamento"
    set({ loading: true, clients: [] });

    try {
      switch (actionType) {
        case "RESTORE_ALL":
          await clientsService.restoreAllFromUser(userId);
          break;
        case "DEACTIVATE_ALL":
          await clientsService.softDeleteAllFromUser(userId);
          break;
        case "EMPTY_TRASH":
          await clientsService.emptyTrashByUserId(userId);
          break;
        default:
          break;
      }

      /** * TIMEOUT TÉCNICO (ROBUSTEZ):
       * Garantimos 300ms de espera para que a transação no Wildfly/PostgreSQL
       * esteja 100% persistida antes de pedirmos a nova lista.
       */
      await new Promise((resolve) => setTimeout(resolve, 300));

      // Recarrega a lista com os filtros originais
      await get().fetchClients(currentUserRole, { ...currentFilters });

      return true;
    } catch (err) {
      set({ error: err.message, loading: false });
      return false;
    }
  },
}));