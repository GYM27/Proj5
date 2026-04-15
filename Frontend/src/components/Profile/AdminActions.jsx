import React from "react";
import { Button } from "react-bootstrap";

/**
 * COMPONENTE: AdminActions
 * -----------------------
 * DESCRIÇÃO: Painel de controlo exclusivo para Administradores.
 * FUNCIONALIDADE: Gere o estado de ativação e a permanência de utilizadores no sistema.
 * @param {boolean} isDeleted - Estado atual do utilizador (se está em soft delete ou não).
 * @param {Function} onToggleStatus - Callback para alternar entre Ativo/Inativo (Regra A9).
 * @param {Function} onHardDelete - Callback para eliminar permanentemente (Regra A14).
 */
const AdminActions = ({ isDeleted, onToggleStatus, onHardDelete }) => {

    return (
        <div className="d-flex flex-column gap-2 mt-4 border-top pt-4">
            {/* Título da secção para separar claramente as ações comuns das de Admin */}
            <h5 className="text-secondary mb-3">Ações de Administração</h5>

            {/* BOTÃO DESATIVAR / REATIVAR:
                - Implementa o conceito de 'Soft Delete'.
                - Altera visualmente (cor e ícone) consoante o estado 'isDeleted'.
                - 'Success' para reativar e 'Warning' para desativar, dando feedback visual de segurança.
            */}
            <Button
                variant={isDeleted ? "success" : "warning"}
                onClick={onToggleStatus}
            >
                <i className={`bi ${isDeleted ? 'bi-check-circle' : 'bi-person-dash'} me-2`}></i>
                {isDeleted ? "Reativar Utilizador" : "Desativar Utilizador (Soft Delete)"}
            </Button>

            {/* BOTÃO ELIMINAR PERMANENTE (REGRA A14):
                - Aciona a remoção definitiva do registo da base de dados PostgreSQL.
                - Utiliza a variante 'danger' para alertar o Administrador sobre a gravidade da ação.
            */}
            <Button
                variant="danger"
                className="mt-2"
                onClick={onHardDelete}
            >
                <i className="bi bi-trash3 me-2"></i>
                Eliminar Utilizador Permanente
            </Button>
        </div>
    );
};

export default AdminActions;