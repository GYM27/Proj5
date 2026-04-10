/**
 * Ponto de entrada base da API definido no ApplicationConfig do JAX-RS.
 * Este URL deve coincidir com o @ApplicationPath definido no Java.
 */
const BASE_URL = "http://localhost:8080/LuisF-proj5/rest";

/**
 * FUNÇÃO: apiRequest (INTERCEPTOR PADRÃO)
 * --------------------------------------
 * DESCRIÇÃO: Motor central de comunicação assíncrona (Fetch API).
 * OBJETIVO: Implementar o padrão 'Interceptor' para injetar segurança em todos
 * os pedidos e padronizar o tratamento de exceções vindas do servidor.
 * * @param {string} endpoint - Rota específica (ex: '/leads', '/users/me').
 * @param {string} method - Verbo HTTP (GET, POST, PUT, DELETE).
 * @param {Object} body - Dados a serem enviados no corpo do pedido.
 */
const apiRequest = async (endpoint, method = "GET", body = null) => {
  // 1. GESTÃO DE SESSÃO (SEGURANÇA - 2%):
  // Recupera o JWT Token do sessionStorage. Se o utilizador não estiver logado,
  // o token será 'null' e o Backend barrará o acesso via UserVerificationBean.
  const token = sessionStorage.getItem("token");

  // 2. CONFIGURAÇÃO DE CABEÇALHOS (PADRONIZAÇÃO):
  // Define que a comunicação é estritamente baseada em JSON (MIME Types).
  const headers = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };

  // 3. INJEÇÃO DE SEGURANÇA:
  // Injeta o cabeçalho personalizado 'token' que será validado pelo filtro do Wildfly.
  if (token) {
    headers["token"] = token;
  }

  // 4. CONFIGURAÇÃO ESTRUTURAL DO REQUEST:
  const config = {
    method: method,
    headers: headers,
  };

  // 5. SERIALIZAÇÃO DE DADOS (DATA MAPPING):
  // Converte objetos literais de JavaScript para Strings JSON interpretáveis pelo JAX-RS.
  if (body) {
    config.body = JSON.stringify(body);
  }

  try {
    // 6. EXECUÇÃO ASSÍNCRONA (PROMISES):
    // Realiza a chamada ao servidor Wildfly utilizando o URL absoluto.
    const response = await fetch(`${BASE_URL}${endpoint}`, config);

    // 7. TRATAMENTO DE ERROS DO SERVIDOR (EXCEPTION MAPPING):
    // Se a resposta não for 2xx, processamos o ErrorResponse enviado pelo Backend Java.
    if (!response.ok) {
      const errorData = await response.json();
      // Propaga a mensagem de erro específica definida nas Exceptions do Java.
      throw new Error(
        errorData.message || "Erro na comunicação com o servidor.",
      );
    }

    // 8. GESTÃO DE RESPOSTAS VAZIAS (204 NO CONTENT):
    // Comum em operações de Logout ou Delete onde não há objeto para retornar.
    if (response.status === 204) {
      return true;
    }

    // 9. DESSERIALIZAÇÃO (JSON -> JS OBJECT):
    // Transforma a resposta do servidor em objetos consumíveis pelo React/Zustand.
    return await response.json();
  } catch (error) {
    // 10. TRATAMENTO DE ERROS DE CONECTIVIDADE:
    // Captura cenários onde o servidor Java está offline ou há falha de rede.
    if (error.message === "Failed to fetch") {
      throw new Error(
        "Conexão recusada. Verifique se o servidor Wildfly está ativo.",
      );
    }
    // Re-lança o erro para ser capturado pela UI (ex: Alerts no Login ou Modais).
    throw error;
  }
};

export default apiRequest;
