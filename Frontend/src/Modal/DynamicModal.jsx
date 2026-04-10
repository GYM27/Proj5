import React from "react";
import { Modal } from "react-bootstrap";

/**
 * COMPONENTE: DynamicModal
 * -----------------------
 * DESCRIÇÃO: Contentor genérico para janelas modais (pop-ups).
 * OBJETIVO: Centralizar a estrutura visual e o comportamento dos modais.
 * BENEFÍCIO: Garante consistência visual em toda a aplicação (CRM proj5)
 * e facilita a manutenção do código (Princípio DRY - Don't Repeat Yourself).
 * * @param {boolean} show - Controla a visibilidade do modal (aberto/fechado).
 * @param {Function} onHide - Função de callback para fechar o modal.
 * @param {string} title - Título exibido no cabeçalho do modal.
 * @param {ReactNode} children - Conteúdo dinâmico a ser injetado no corpo do modal.
 */
const DynamicModal = ({ show, onHide, title, children }) => {
  return (
    /**
     * CONFIGURAÇÃO DO MODAL (BOOTSTRAP):
     * - 'centered': Garante que o modal aparece sempre no centro vertical e horizontal
     * do ecrã, melhorando a focagem do utilizador (UX - 3%).
     */
    <Modal show={show} onHide={onHide} centered>
      {/* CABEÇALHO: Inclui o título e o botão de fechar (X) padrão */}
      <Modal.Header closeButton>
        <Modal.Title>{title}</Modal.Title>
      </Modal.Header>

      {/* CORPO DO MODAL (COMPOSIÇÃO):
          - Este é o ponto de flexibilidade do componente.
          - O React injeta aqui qualquer conteúdo passado via 'children'
            (ex: ConfirmActionContent ou EditLeadForm).
      */}
      <Modal.Body>
        {/* O HTML/Componentes injetados pelo componente pai vão aparecer aqui */}
        {children}
      </Modal.Body>
    </Modal>
  );
};

export default DynamicModal;
