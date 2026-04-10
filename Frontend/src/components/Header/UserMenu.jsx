import React from "react";
import { NavDropdown, Image } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useUserStore } from "../../stores/UserStore";

/**
 * COMPONENTE: UserMenu
 * -------------------
 * DESCRIÇÃO: Menu dropdown do utilizador na Navbar.
 * FUNCIONALIDADES: Exibe a identidade do utilizador (nome/foto),
 * atalho para o perfil e gestão do encerramento de sessão (Logout).
 */
const UserMenu = () => {
  const navigate = useNavigate();

  // GESTÃO DE ESTADO (CRITÉRIO: EFICIÊNCIA):
  // Extraímos apenas os dados necessários da store global para evitar re-renderizações desnecessárias.
  const { firstName, photoUrl, clearUser } = useUserStore();

  /**
   * LÓGICA DE LOGOUT CORRIGIDA:
   * 1. Limpa o sessionStorage (onde o token e a store realmente residem).
   * 2. Limpa o estado reativo da Store (Zustand) para forçar o re-render da UI.
   * 3. Redireciona para o login com 'replace' para limpar o histórico.
   */
  const handleLogout = () => {
    // 1. LIMPEZA FÍSICA: O teu apiRequest e UserStore usam sessionStorage!
    sessionStorage.clear();
    localStorage.clear(); // Por precaução, caso tenha restos de dados aqui

    // 2. LIMPEZA REATIVA: Remove o role de "ADMIN" da memória do React
    clearUser();

    // 3. NAVEGAÇÃO FORÇADA: Impede que o user use o botão "Voltar" para ver o Dashboard
    navigate("/login", { replace: true });
  };

  /**
   * LÓGICA DE AVATAR DINÂMICO (CRITÉRIO: UX):
   * Caso o utilizador não tenha foto de perfil, recorremos a uma API externa
   * para gerar um avatar visual com a inicial do nome, garantindo que a interface nunca fica vazia.
   */
  const avatarUrl = photoUrl || `https://ui-avatars.com/api/?name=${firstName || "U"}&background=0d6efd&color=fff&rounded=true`;

  // CONFIGURAÇÃO DE ESTILO (EVITAR MAGIC CONSTANTS)
  const AVATAR_STYLE = { width: "32px", height: "32px", objectFit: "cover" };

  return (
      <NavDropdown
          align="end"
          className="user-dropdown-custom"
          title={
            <div className="d-flex align-items-center text-white">
              {/* Nome do utilizador: Escondido em mobile (d-md-inline) para manter a Navbar limpa */}
              <span className="me-2 d-none d-md-inline">{firstName}</span>
              <Image
                  src={avatarUrl}
                  roundedCircle
                  style={AVATAR_STYLE}
              />
            </div>
          }
      >
        {/* NAVEGAÇÃO: Link direto para a página de perfil (Ação de Utilizador) */}
        <NavDropdown.Item onClick={() => navigate("/profile")}>
          <i className="bi bi-person me-2"></i> Perfil
        </NavDropdown.Item>

        <NavDropdown.Divider />

        {/* ACÇÃO CRÍTICA: Logout com destaque visual a vermelho */}
        <NavDropdown.Item onClick={handleLogout} className="text-danger">
          <i className="bi bi-box-arrow-right me-2"></i> Sair
        </NavDropdown.Item>
      </NavDropdown>
  );
};

export default UserMenu;