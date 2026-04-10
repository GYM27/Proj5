import React from "react";

/**
 * COMPONENTE: Footer
 * -----------------
 * DESCRIÇÃO: Rodapé fixo ou posicionado na base da aplicação.
 * FUNCIONALIDADE: Exibe informações de direitos de autor (Copyright) e links
 * legais, garantindo que o ano é atualizado automaticamente.
 */
const Footer = () => {
  // LÓGICA DINÂMICA (CRITÉRIO: CÓDIGO LIMPO):
  // Captura o ano atual do sistema para que o copyright não precise de
  // manutenção manual todos os anos.
  const currentYear = new Date().getFullYear();

  return (
    /**
     * ESTRUTURA DO RODAPÉ:
     * - 'mt-auto': Garante que, num layout flexbox, o footer seja "empurrado" para a base.
     * - 'bg-light border-top': Estilo visual sóbrio que separa o conteúdo do rodapé.
     */
    <footer className="footer mt-auto py-3 bg-light border-top">
      <div className="container-fluid px-4">
        <div className="d-flex align-items-center justify-content-between small">
          {/* SECÇÃO DE COPYRIGHT:
                        Utiliza a variável 'currentYear' para exibir o ano em tempo real.
                    */}
          <div className="text-muted">
            &copy; {currentYear} CRM proj5 - Todos os direitos reservados.
          </div>

          {/* LINKS LEGAIS:
                        Padronizados com 'text-decoration-none' para um visual profissional.
                    */}
          <div>
            <a href="#" className="text-decoration-none text-muted me-3">
              Política de Privacidade
            </a>
            <a href="#" className="text-decoration-none text-muted">
              Termos & Condições
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
