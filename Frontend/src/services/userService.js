import api from './api';

/**
 * SERVIÇO: userService
 * --------------------
 * DESCRIÇÃO: Gere a comunicação com os endpoints JAX-RS para a entidade User.
 * FUNCIONALIDADE: Suporta a consulta de perfil próprio, gestão de staff por Admin
 * e operações de manutenção de conta (Ativar/Desativar/Eliminar).
 */
export const userService = {
    /**
     * CONSULTA DE PERFIL PRÓPRIO (AUTOCONSULTA):
     * Utilizado no 'Profile.jsx' quando o utilizador quer ver ou editar os seus dados.
     * O Backend identifica o utilizador através do Token JWT enviado no Header.
     */
    getMe: async () => {
        return await api("/users/me", "GET");
    },

    /**
     * LISTAGEM GLOBAL (EXCLUSIVO ADMIN - 2%):
     * Mapeia para @GET /users no Java.
     * Retorna a lista de todos os colaboradores para a página 'Users.jsx'.
     */
    getAllUsers: async () => {
        const response = await api("/users", "GET");
        // Garantia de robustez: assegura que o componente recebe sempre um array,
        // mesmo que a API devolva um erro ou uma resposta vazia.
        return Array.isArray(response) ? response : [];
    },

    /**
     * EDITA O PRÓPRIO PERFIL:
     * Envia as alterações do utilizador para a base de dados.
     */
    updateMyProfile: async (userData) => {
        return await api("/users/me", "PUT", userData);
    },

    /**
     * CONSULTA POR ID (ADMIN VIEW):
     * Permite que o Admin carregue os detalhes de um colaborador específico
     * ao clicar num UserCard ou ao navegar para /profile?userId=X.
     */
    getUserById: async (id) => {
        return await api(`/users/${id}`, "GET");
    },

    /**
     * GESTÃO DE ESTADO (REGRA A9 - SOFT DELETE):
     * Utiliza o verbo PATCH para uma alteração parcial de estado.
     * @param {string} id - ID do utilizador alvo.
     * @param {string} action - "deactivate" (Desativar) ou "activate" (Reativar).
     */
    toggleUserStatus: async (id, action) => {
        return await api(`/users/${id}/${action}`, "PATCH");
    },

    /**
     * ELIMINAÇÃO FÍSICA (REGRA A14 - HARD DELETE):
     * Remove permanentemente o registo do utilizador da base de dados PostgreSQL.
     * Mapeia para @DELETE /users/{id} no Java.
     */
    deleteUserPermanent: async (id) => {
        return await api(`/users/${id}`, "DELETE");
    }
};