import api from './api';

/**
 * SERVIÇO: loginService
 * --------------------
 * DESCRIÇÃO: Gere o ciclo de vida da autenticação do utilizador.
 * FUNCIONALIDADE: Comunica com os endpoints JAX-RS para emissão e
 * invalidação de tokens de acesso (JWT).
 */

/**
 * FUNÇÃO: loginUser
 * ----------------
 * @param {Object} credentials - Contém 'username' e 'password'.
 * @returns {Object} LoginResponseDTO - Dados do perfil e token.
 */
export const loginUser = async (credentials) => {
    try {
        // 1. COMUNICAÇÃO COM BACKEND (SEGURANÇA - 2%):
        // Faz o pedido POST para o endpoint '/auth/login' definido no Java.
        // O wrapper 'api.js' trata da serialização JSON e dos cabeçalhos.
        const data = await api("/auth/login", "POST", credentials);

        /** * 2. PERSISTÊNCIA DE SESSÃO:
         * Se o login for bem-sucedido, o Java devolve o LoginResponseDTO.
         * Guardamos o token e os dados básicos no localStorage para persistir
         * a sessão mesmo após um refresh da página (F5).
         */
        if (data && data.token) {
            localStorage.setItem("token", data.token);
            localStorage.setItem("userName", data.firstName);
            localStorage.setItem("userRole", data.userRole);
        }

        // 3. RETORNO: Devolve os dados para o 'Login.jsx' atualizar a Store (Zustand).
        return data;

    } catch (error) {
        // 4. TRATAMENTO DE EXCEPÇÕES:
        // Captura erros como 401 Unauthorized (Password errada) ou 404 (User não existe).
        console.error("Erro no serviço de autenticação:", error.message);
        throw error;
    }
}

/**
 * FUNÇÃO: logoutUser
 * -----------------
 * DESCRIÇÃO: Encerra a sessão de forma segura (Server-side e Client-side).
 */
export const logoutUser = async () => {
    // 1. RECUPERAÇÃO DO TOKEN:
    const token = localStorage.getItem("token");

    try {
        if (token) {
            /** * 2. INVALIDAÇÃO NO SERVIDOR (BOA PRÁTICA):
             * Chamamos o endpoint de logout para que o Backend possa, opcionalmente,
             * colocar o token numa 'blacklist' ou destruir a sessão no Wildfly.
             */
            await api("/auth/logout", "POST");
            console.log("Sessão encerrada com sucesso no servidor.");
        }
    } catch (error) {
        // Mesmo que a rede falhe ou o servidor esteja offline, o logout local deve prosseguir.
        console.error("Erro ao invalidar token no servidor:", error.message);
    } finally {
        /** * 3. LIMPEZA TOTAL (CLEANUP):
         * Garante que os dados sensíveis são removidos do browser,
         * impedindo acessos indevidos após a saída.
         */
        localStorage.removeItem("token");
        localStorage.removeItem("userName");
        localStorage.removeItem("userRole");

        // Dica: Após isto, o 'MainLayout.jsx' detetará a falta do token e enviará o user para o Login.
    }
};