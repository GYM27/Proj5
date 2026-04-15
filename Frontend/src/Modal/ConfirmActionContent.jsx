import React from "react";
import { Button } from "react-bootstrap";

/**
 * COMPONENTE: ConfirmActionContent
 * -------------------------------
 * DESCRIÇÃO: Componente polimórfico para diálogos de confirmação.
 * OBJETIVO: Centralizar todas as mensagens de aviso, erro e sucesso do sistema.
 * BENEFÍCIO: Garante que o utilizador recebe sempre o mesmo padrão visual
 * em ações críticas (Eliminar, Restaurar, Desativar).
 * * @param {string} type - Chave que define a configuração (ex: 'SOFT_DELETE').
 * @param {Object} data - Objeto alvo da ação (Lead, Cliente ou Utilizador).
 * @param {Function} onCancel - Fecha o modal sem executar a ação.
 * @param {Function} onConfirm - Executa a lógica de negócio após confirmação.
 */
const ConfirmActionContent = ({ type, data, onCancel, onConfirm }) => {

    /**
     * DICIONÁRIO DE CONFIGURAÇÃO (PATTERN: STRATEGY / FACTORY):
     * Em vez de múltiplos IFs espalhados, usamos um Switch para mapear
     * cada 'type' a um conjunto de ícones, cores e mensagens.
     */
    const getModalInfo = () => {
        switch (type) {
            // REGRA A9: Eliminação lógica (Soft Delete)
            case "SOFT_DELETE":
                return {
                    icon: "bi-trash text-warning",
                    message: `Tem a certeza que deseja mover "${data?.title || data?.name}" para a lixeira?`,
                    confirmText: "Mover para Lixeira",
                    variant: "warning",
                };

            // REGRA A14: Eliminação física (Hard Delete)
            case "HARD_DELETE":
                return {
                    icon: "bi-exclamation-triangle text-danger",
                    message: `Deseja eliminar permanentemente "${data?.title || data?.name}"? Esta ação não pode ser desfeita.`,
                    confirmText: "Eliminar Permanente",
                    variant: "danger",
                };

            // ACÇÕES EM MASSA (BULK):
            // Implementa limpeza rápida para Administradores.
            case "BULK_SOFT_DELETE":
                return {
                    icon: "bi-trash-fill text-warning",
                    message: "Tem a certeza que deseja mover todos os itens selecionados para a lixeira?",
                    confirmText: "Mover Tudo",
                    variant: "warning",
                };

            case "BULK_HARD_DELETE":
                return {
                    icon: "bi-exclamation-octagon text-danger",
                    message: "Deseja esvaziar a lixeira definitivamente? Todos os itens serão perdidos para sempre.",
                    confirmText: "Esvaziar Lixeira",
                    variant: "danger",
                };

            // GESTÃO DE UTILIZADORES:
            // Mensagens específicas para o contexto de segurança e acesso.
            case "USER_HARD_DELETE":
                return {
                    icon: "bi-person-x text-danger",
                    message: `Está prestes a apagar permanentemente o utilizador "${data?.name}". O acesso será revogado e os seus dados serão reatribuídos ao sistema.`,
                    confirmText: "Apagar Utilizador",
                    variant: "danger",
                };

            // ACÇÕES DE RESTAURO (UX - FEEDBACK POSITIVO):
            // Note-se o uso da variante 'success' (verde) para ações construtivas.
            case "RESTORE_LEAD":
                return {
                    icon: "bi-arrow-counterclockwise text-success",
                    message: `Tem a certeza que deseja restaurar a lead "${data?.title || data?.name}"? Ela voltará a estar ativa no quadro principal.`,
                    confirmText: "Sim, Restaurar",
                    variant: "success",
                };

            case "RESTORE_ALL":
                return {
                    icon: "bi-arrow-counterclockwise text-success",
                    message: "Tem a certeza que deseja restaurar TODOS os itens visíveis na lixeira? Eles voltarão a estar ativos.",
                    confirmText: "Sim, Restaurar Tudo",
                    variant: "success",
                };

            // LÓGICA DINÂMICA DE UTILIZADOR :
            // Alterna a mensagem consoante o utilizador esteja Ativo ou Inativo.
            case "USER_TOGGLE_STATUS": {
                const isInactive = data?.softDelete;
                return {
                    icon: isInactive ? "bi-person-check-fill text-success" : "bi-person-dash-fill text-warning",
                    message: isInactive
                        ? `Deseja reativar a conta de "${data?.firstName}"? O utilizador voltará a ter acesso.`
                        : `Deseja desativar a conta de "${data?.firstName}"? O utilizador deixará de conseguir fazer login.`,
                    confirmText: isInactive ? "Sim, Reativar" : "Sim, Desativar",
                    variant: isInactive ? "success" : "warning",
                };
            };

            default:
                return {
                    icon: "bi-question-circle",
                    message: "Tem a certeza que deseja realizar esta ação?",
                    confirmText: "Confirmar",
                    variant: "primary",
                };
        }
    };

    const info = getModalInfo();

    return (
        <div className="text-center p-3">
            {/* Ícone dinâmico: display-4 garante visibilidade de aviso (UX - 3%) */}
            <i className={`${info.icon} display-4 mb-3 d-block`}></i>

            {/* Mensagem: Uso de 'lead' do Bootstrap para maior destaque legível */}
            <p className="mb-4 lead" style={{ fontSize: "1.1rem" }}>
                {info.message}
            </p>

            <div className="d-flex justify-content-center gap-2 mt-4">
                <Button variant="outline-secondary" onClick={onCancel}>
                    Cancelar
                </Button>
                <Button
                    variant={info.variant}
                    onClick={() => onConfirm(data)} // Devolve os dados originais para o processamento final
                >
                    {info.confirmText}
                </Button>
            </div>
        </div>
    );
};

export default ConfirmActionContent;