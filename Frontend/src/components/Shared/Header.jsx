import React from "react";
import { Navbar, Container, Nav } from "react-bootstrap";
import HeaderLogo from "../Header/HeaderLogo.jsx";
import UserMenu from "../Header/UserMenu.jsx";

/**
 * COMPONENTE: Header
 * -----------------
 * DESCRIÇÃO: Barra de navegação superior (TopBar).
 * FUNCIONALIDADE: Atua como um "Layout Wrapper" que organiza o acesso à Sidebar,
 * o Logotipo da aplicação e as opções de conta do utilizador.
 * @param {Function} onToggleMenu - Função que controla a abertura/fecho da Sidebar lateral.
 */
const Header = ({ onToggleMenu }) => {
  return (
    /**
     * CONFIGURAÇÃO DA NAVBAR:
     * - 'fixed-top': Garante que a navegação está sempre acessível durante o scroll (UX - 3%).
     * - 'shadow-sm': Adiciona profundidade visual para separar o header do conteúdo.
     * - 'backgroundColor': Cor primária do CRM proj5 (#1e2a78).
     */
    <Navbar
      expand="lg"
      variant="dark"
      className="shadow-sm fixed-top"
      style={{ backgroundColor: "#1e2a78", height: "56px" }}
    >
      <Container fluid>
        <div className="d-flex align-items-center">
          {/* CONTROLADOR DA SIDEBAR:
              Botão de estilo "Hambúrguer" que dispara o evento 'onToggleMenu'
              para otimizar o espaço de trabalho no ecrã (Regra de Design).
          */}
          <button
            className="btn text-white me-3 border-0"
            onClick={onToggleMenu}
            aria-label="Toggle Sidebar"
          >
            <i className="bi bi-list fs-4"></i>
          </button>

          {/* COMPONENTE MODULAR: Identidade visual (Logo + Nome) */}
          <HeaderLogo />
        </div>

        {/* SECÇÃO DIREITA:
            - 'ms-auto': Alinha o Menu do Utilizador totalmente à direita.
        */}
        <Nav className="ms-auto">
          {/* COMPONENTE MODULAR: Gere a foto, nome e logout do utilizador */}
          <UserMenu />
        </Nav>
      </Container>
    </Navbar>
  );
};

export default Header;
