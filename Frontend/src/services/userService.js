import api from './api';

/**
 * Serviço responsável por gerir todas as chamadas à API relacionadas com utilizadores.
 * Agrupa os métodos de consulta, edição e administração de contas.
 */
export const userService = {

    /**
     * Obtém os dados do perfil do utilizador atualmente autenticado.
     * * @returns {Promise<Object>} Uma promessa que resolve com os dados do utilizador ativo (UserDTO).
     */
    getMe: async () => {
        return await api("/users/me", "GET");
    },

    /**
     * Obtém a lista de todos os utilizadores registados no sistema.
     * Garante que o retorno é sempre um array, mesmo que a API falhe ou devolva um formato inesperado.
     * * @returns {Promise<Array>} Uma promessa que resolve com um array de utilizadores (UserBaseDTO).
     */
    getAllUsers: async () => {
        const response = await api("/users", "GET");
        return Array.isArray(response) ? response : [];
    },

    /**
     * Atualiza os dados do perfil do utilizador atualmente autenticado.
     * * @param {Object} userData - Objeto contendo os novos dados do perfil a serem atualizados.
     * @returns {Promise<Object>} Uma promessa que resolve com a resposta da API à atualização.
     */
    updateMyProfile: async (userData) => {
        return await api("/users/me", "PUT", userData);
    },

    /**
     * Obtém os dados básicos de um utilizador específico através do seu ID.
     * * @param {number|string} id - O identificador único do utilizador a consultar.
     * @returns {Promise<Object>} Uma promessa que resolve com os dados do utilizador (UserBaseDTO).
     */
    getUserById: async (id) => {
        return await api(`/users/${id}`, "GET");
    },

    /**
     * Obtém os dados de um utilizador específico através do seu username.
     * Ideal para visualização de perfis partilháveis através de rotas dinâmicas (/users/:username).
     * * @param {string} username - O nome de utilizador (username) exato a consultar.
     * @returns {Promise<Object>} Uma promessa que resolve com os dados do utilizador (UserBaseDTO).
     */
    getUserByUsername: async (username) => {
        return await api(`/users/username/${username}`, "GET");
    },

    /**
     * Altera o estado de um utilizador (Ativar ou Desativar / Soft Delete).
     * Ação restrita a Administradores.
     * * @param {number|string} id - O ID do utilizador alvo.
     * @param {string} action - A ação a executar ("activate" ou "deactivate").
     * @returns {Promise<Object>} Uma promessa que resolve com a confirmação da alteração de estado.
     */
    toggleUserStatus: async (id, action) => {
        return await api(`/users/${id}/${action}`, "PATCH");
    },

    /**
     * Remove permanentemente um utilizador do sistema (Hard Delete).
     * Os registos associados (ex: clientes, leads) serão transferidos para um utilizador de sistema.
     * Ação restrita a Administradores.
     * * @param {number|string} id - O ID do utilizador a eliminar fisicamente.
     * @returns {Promise<Object>} Uma promessa que resolve com a confirmação da eliminação.
     */
    deleteUserPermanent: async (id) => {
        return await api(`/users/${id}`, "DELETE");
    }
};