import React from "react";
import { Navbar, Image } from "react-bootstrap";
import logo from "../../assets/logo.jpeg";

/**
 * COMPONENTE: HeaderLogo
 * ---------------------
 * DESCRIÇÃO: Renderiza a identidade visual da marca na barra de navegação.
 * FUNCIONALIDADE: Atua como um link de retorno rápido para o Dashboard.
 */
const HeaderLogo = () => {

    // CONFIGURAÇÃO DE IMAGEM (CRITÉRIO: EVITAR MAGIC CONSTANTS)
    // Centralizamos as dimensões do logo para facilitar ajustes de design futuros.
    const LOGO_STYLE = {
        width: "40px",
        height: "40px",
        objectFit: "contain"
    };

    return (
        <Navbar.Brand href="/dashboard" className="d-flex align-items-center p-0 m-0">
            {/* Logotipo da Empresa:
          Utilizamos o componente Image do Bootstrap para garantir a responsividade.
      */}
            <Image
                src={logo}
                alt="Bridge CRM"
                className="me-2"
                style={LOGO_STYLE}
            />

            {/* Nome da Aplicação:
          - 'fw-bold': Destaque visual.
          - 'd-none d-sm-inline': Esconde o texto em ecrãs muito pequenos (Mobile)
            para poupar espaço, mantendo apenas o ícone.
      */}
            <span className="fw-bold text-white d-none d-sm-inline">
        Bridge.
      </span>
        </Navbar.Brand>
    );
};

export default HeaderLogo;