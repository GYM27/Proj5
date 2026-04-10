import api from "./api";

/**
 * SERVIÇO: clientsService
 * ----------------------
 * DESCRIÇÃO: Encapsula todas as chamadas à API JAX-RS para a entidade Client.
 * FUNCIONALIDADE: Implementa a lógica de negócio de clientes, incluindo
 * filtragem por utilizador, gestão de lixeira (Soft Delete) e remoção permanente.
 */
export const clientsService = {

  /**
   * ROTEADOR CENTRAL DE LISTAGEM (PADRÃO DE ESTRATÉGIA):
   * Este método decide qual o endpoint chamar baseando-se no Role e no estado (Lixeira ou Ativo).
   * Resolve a complexidade de rotas múltiplas num único ponto de entrada.
   */
  getClients: async (userRole, filters = {}) => {
    const { userId, showTrash } = filters;

    // --- 1. GESTÃO DE LIXEIRA (REGRA A9): ---
    if (showTrash) {
      if (userRole === "ADMIN") {
        if (userId) {
          // Admin consulta a lixeira de um colaborador específico (@GET /clients/user/{userId}/trash)
          return await api(`/clients/user/${userId}/trash`, "GET");
        } else {
          // Admin consulta a lixeira global do sistema (@GET /clients/trash)
          return await api("/clients/trash", "GET");
        }
      } else {
        // Utilizador Normal consulta apenas os seus próprios dados eliminados (@GET /clients/me-trash)
        return await api("/clients/me-trash", "GET");
      }
    }

    // --- 2. GESTÃO DE CLIENTES ATIVOS: ---
    const params = new URLSearchParams();
    if (userRole === "ADMIN" && userId) {
      // Injeta o ID do utilizador alvo como Query Parameter para filtragem no SQL.
      params.append("userId", userId);
    }

    const queryString = params.toString();
    const url = queryString ? `/clients?${queryString}` : "/clients";

    return await api(url, "GET");
  },

  /** * OPERAÇÕES CRUD (CREATE, READ, UPDATE, DELETE):
   * Mapeamento direto para os métodos do ClientResource no Java.
   */

  // Registo de novo cliente pelo próprio utilizador
  createClient: async (clientDto) => {
    return await api("/clients", "POST", clientDto);
  },

  // Criação de cliente atribuído a outrem (Exclusivo Admin)
  createClientForUser: async (userId, clientDto) => {
    return await api(`/clients/user/${userId}`, "POST", clientDto);
  },

  // Atualização de dados (Mapeia para @PUT /clients/{id})
  updateClient: async (id, clientDto) => {
    return await api(`/clients/${id}`, "PUT", clientDto);
  },

  /** * CICLO DE VIDA E ELIMINAÇÃO (REGRAS A9 e A14): */

  // Soft Delete: Marca o cliente como 'deleted' sem o remover da base de dados.
  softDeleteClient: async (id) => {
    return await api(`/clients/${id}`, "DELETE");
  },

  // Restaurar: Reverte o Soft Delete (Mapeia para @PATCH no Java conforme as boas práticas REST).
  restoreClient: async (id) => {
    return await api(`/clients/${id}/restore`, "PATCH");
  },

  // Hard Delete: Remoção definitiva do registo no PostgreSQL (Restrito a Admin).
  permanentDeleteClient: async (id) => {
    return await api(`/clients/${id}/permanent`, "DELETE");
  },

  /** * OPERAÇÕES EM MASSA (BULK ACTIONS):
   * Implementam a gestão rápida de grandes volumes de dados para Administradores.
   */

  // Desativa todos os clientes de um utilizador específico de uma só vez.
  softDeleteAllFromUser: async (userId) => {
    return await api(`/clients/user/${userId}/status/deactivate-all`, "PATCH");
  },

  // Restaura todos os clientes eliminados de um utilizador.
  restoreAllFromUser: async (userId) => {
    return await api(`/clients/user/${userId}/status/activate-all`, "PATCH");
  },

  // Limpa definitivamente a lixeira de um colaborador.
  emptyTrashByUserId: async (userId) => {
    return await api(`/clients/user/${userId}/trash`, "DELETE");
  },

  // Consulta administrativa da lixeira de todo o CRM.
  getAllTrash: async () => {
    return await api("/clients/trash", "GET");
  }
};