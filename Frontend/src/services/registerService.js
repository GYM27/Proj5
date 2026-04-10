import api from './api';

/**
 * SERVIÇO: registerService
 * -----------------------
 * DESCRIÇÃO: Gere o processo de criação de novas contas de utilizador.
 * FUNCIONALIDADE: Atua como ponte entre o formulário de registo (React)
 * e o endpoint JAX-RS especializado no Wildfly.
 */

/**
 * FUNÇÃO: registerUser
 * -------------------
 * @param {Object} registerData - Objeto contendo firstName, lastName, username,
 * email, password, cellphone e photoUrl (Mapeia o UserDTO no Java).
 * @returns {Object} Resposta estruturada de sucesso.
 */
export const registerUser = async (registerData) => {
  try {
    /** * 1. COMUNICAÇÃO COM O MOTOR CENTRAL (API.JS):
     * O pedido é enviado para '/users/register' usando o verbo POST.
     * Este endpoint no Java é habitualmente público (não exige token)
     * para permitir que novos visitantes se registem.
     */
    const response = await api("/users/register", "POST", registerData);

    /** * 2. RETORNO ESTRUTURADO (UX - 3%):
     * Devolvemos um objeto de sucesso que o componente 'Register.jsx'
     * utiliza para confirmar a operação e navegar para o Login.
     */
    return {
      success: true,
      message: "Utilizador registado com sucesso!",
      data: response
    };

  } catch (error) {
    /** * 3. PROPAGAÇÃO DE EXCEPÇÕES (SEGURANÇA - 2%):
     * Captura erros de validação do Hibernate ou do SQL (ex: 409 Conflict se
     * o username ou email já estiverem em uso na base de dados).
     */
    console.error("Erro no RegisterService:", error.message);
    // Lançamos o erro para que o bloco 'catch' do formulário mostre o Alert ao utilizador.
    throw error;
  }
};