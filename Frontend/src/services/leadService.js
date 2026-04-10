import api from './api';

/**
 * SERVIÇO: leadService
 * -------------------
 * DESCRIÇÃO: Gere a comunicação com os endpoints JAX-RS para a entidade Lead.
 * FUNCIONALIDADE: Suporta operações de CRUD, transições de estado no Kanban,
 * filtragem administrativa e gestão de lixeira (Soft/Hard Delete).
 */
export const leadService = {

  /**
   * CONSULTA DE LEADS (FILTRAGEM DINÂMICA - 3%):
   * Constrói a QueryString com base no Role e nos filtros ativos (Estado, Utilizador, Lixeira).
   * Diferencia os endpoints '/leads' (Self) e '/leads/admin' (Global/Staff).
   */
  getLeads: async (role, filters = {}) => {
    const isAdmin = role === "ADMIN";
    const endpoint = isAdmin ? "/leads/admin" : "/leads";

    const params = new URLSearchParams();

    // FILTRO DE ESTADO: Essencial para a separação das colunas no Kanban (Novo, Proposta, etc.)
    if (filters.state) params.append("state", filters.state);

    // FILTRO DE LIXEIRA (REGRA A9):
    // Boolean que define se o Java deve retornar leads ativas ou em Soft Delete.
    if (filters.softDeleted !== undefined) {
      params.append("softDeleted", filters.softDeleted);
    }

    // FILTRO DE ATRIBUIÇÃO (EXCLUSIVO ADMIN):
    // Permite que o Admin veja as leads de um colaborador específico no seu painel.
    if (isAdmin && filters.userId) {
      params.append("userId", filters.userId);
    }

    const queryString = params.toString();
    const finalUrl = queryString ? `${endpoint}?${queryString}` : endpoint;

    return await api(finalUrl);
  },

  /** * CRIAÇÃO E ATRIBUIÇÃO (REGRA DE NEGÓCIO):
   * Permite criar leads para o próprio ou delegar para outro utilizador (se Admin).
   */
  createLead: async (leadData, role, targetUserId = null) => {
    if (role === "ADMIN" && targetUserId) {
      // Endpoint especializado para delegação de leads por parte do Administrador.
      return await api(`/leads/admin/${targetUserId}`, "POST", leadData);
    }
    return await api("/leads", "POST", leadData);
  },

  /** * ATUALIZAÇÃO (EDIT):
   * Suporta a edição de dados e a mudança de coluna (state) no Kanban.
   */
  updateLead: async (id, leadData, role) => {
    const endpoint = role === "ADMIN" ? `/leads/admin/${id}` : `/leads/${id}`;
    return await api(endpoint, "PUT", leadData);
  },

  /** * ELIMINAÇÃO (REGRAS A9 e A14):
   * Implementa a lógica de decisão entre Soft Delete (Lixeira) e Hard Delete (Permanente).
   */
  deleteLead: async (id, role, permanent = false) => {
    // REGRA A14: Apenas Admin pode realizar o Hard Delete definitivo da base de dados PostgreSQL.
    if (role === "ADMIN" && permanent) {
      return await api(`/leads/admin/${id}`, "DELETE");
    }
    // REGRA A9: Soft Delete padrão enviando o pedido para a rota protegida de eliminação lógica.
    return await api(`/leads/${id}`, "DELETE");
  },

  /** * GESTÃO MANUAL DE ESTADO DE LIXEIRA:
   * Funções que utilizam o 'adminSuperEdit' do Java para inverter o bit 'softDeleted'.
   */
  softDeleteLead: async (leadId) => {
    const payload = { softDeleted: true };
    // O Java ignora campos nulos, atualizando apenas a flag de desativação.
    return await api(`/leads/admin/${leadId}`, "PUT", payload);
  },

  restoreLead: async (leadId) => {
    const payload = { softDeleted: false };
    return await api(`/leads/admin/${leadId}`, "PUT", payload);
  },

  /** * OPERAÇÕES EM MASSA (ADMIN - BULK ACTIONS):
   * Métodos de conveniência para limpar ou restaurar volumes de dados de um utilizador.
   */
  softDeleteAllFromUser: async (userId) => {
    return await api(`/leads/admin/${userId}/softdeleteall`, "POST");
  },

  restoreAllFromUser: async (userId) => {
    return await api(`/leads/admin/${userId}/softundeleteall`, "POST");
  },

  emptyTrashByUserId: async (userId) => {
    return await api(`/leads/admin/${userId}/trash`, "DELETE");
  }
};