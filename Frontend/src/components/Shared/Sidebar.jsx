import React from "react";
import { NavLink } from "react-router-dom";
import { OverlayTrigger, Tooltip } from "react-bootstrap";
import { useUserStore } from "../../stores/UserStore.js";
import "../../App.css";

/**
 * COMPONENTE: Sidebar
 * -------------------
 * DESCRIÇÃO: Menu de navegação lateral principal da aplicação.
 * FUNCIONALIDADE: Implementa navegação reativa, ocultação automática de itens
 * restritos a administradores e suporte visual para estados aberta/fechada.
 * @param {boolean} isOpen - Estado que controla a expansão visual da barra.
 */
const Sidebar = ({ isOpen }) => {
  // ACESSO AO ESTADO GLOBAL:
  // Recuperamos o cargo (role) do utilizador para validar permissões de menu.
  const { userRole } = useUserStore();
  const isAdmin = userRole === "ADMIN";

  /** * CONFIGURAÇÃO DO MENU (DATA MAPPING):
   * Centralizamos as rotas num array de objetos para facilitar a manutenção.
   * 'adminOnly: true' marca as rotas sensíveis que requerem privilégios elevados.
   */
  const allItems = [
    { to: "/dashboard", icon: "bi-speedometer2", label: "Dashboard" },
    { to: "/leads", icon: "bi-clipboard2-plus", label: "Leads" },
    { to: "/clients", icon: "bi-people", label: "Clientes" },
    { to: "/users", icon: "bi-gear", label: "Utilizadores", adminOnly: true },
  ];

  /** * FILTRAGEM DE SEGURANÇA (REGRA DE NEGÓCIO):
   * Garante que um utilizador comum nunca veja o link para a gestão de utilizadores.
   */
  const menuItems = allItems.filter(item => !item.adminOnly || isAdmin);

  return (
      /**
       * CONTAINER DA BARRA:
       * Utiliza classes dinâmicas (is-open/is-closed) para animações CSS de expansão/contração.
       */
      <nav className={`sidebar-nav border-end d-flex flex-column ${isOpen ? "is-open" : "is-closed"}`}>
        <div className="pt-5">
          <ul className="nav flex-column">
            {menuItems.map((item) => (
                <li className="nav-item w-100" key={item.to}>

                  {/* ACESSIBILIDADE E UX (3%):
                      O Tooltip só é ativado quando a Sidebar está fechada (ícones apenas),
                      ajudando o utilizador a identificar a rota.
                  */}
                  <OverlayTrigger
                      placement="right"
                      disabled={isOpen}
                      overlay={<Tooltip id={`t-${item.to}`}>{item.label}</Tooltip>}
                  >
                    {/* NAVEGAÇÃO REATIVA (6%):
                        'NavLink' do react-router-dom gere automaticamente a classe 'active-link'
                        quando o URL coincide com o destino, dando feedback visual de onde o utilizador está.
                    */}
                    <NavLink
                        to={item.to}
                        className={({ isActive }) =>
                            `nav-link py-3 d-flex align-items-center ${isActive ? "active-link" : "text-dark"}`
                        }
                    >
                      <div className="nav-icon-wrapper">
                        <i className={`bi ${item.icon} fs-4`}></i>
                      </div>
                      <span className="nav-label">{item.label}</span>
                    </NavLink>
                  </OverlayTrigger>
                </li>
            ))}
          </ul>
        </div>
      </nav>
  );
};

export default Sidebar;