import React from "react";
import ActionButton from "./ActionButton";
import {BUTTON_TYPES} from "./buttonConfigs";

/**
 * COMPONENTE: ActionGroup
 * ----------------------
 * DESCRIÇÃO: Agrupador dinâmico de botões de ação.
 * FUNCIONALIDADE: Centraliza a lógica de exibição de botões (Editar, Ver, Apagar, Restaurar)
 * dependendo se estamos a visualizar dados ativos ou a Lixeira.
 * * @param {Object} actions - Dicionário de funções de callback (onEdit, onDelete, etc).
 * @param {Object} item - O recurso individual (Lead/Cliente) sobre o qual a ação atua.
 * @param {boolean} isTrashMode - Alterna entre o conjunto de botões normais e de restauro.
 * @param {boolean} isAdmin - Filtro de segurança para ações críticas (Eliminação Permanente).
 * @param {boolean} isBulk - Define se a ação é individual ou para todos os itens (Header).
 */
const ActionGroup = ({actions = {}, item, isTrashMode, isAdmin, isBulk = false}) => {

    /**
     * FUNÇÃO AUXILIAR: renderButton
     * -----------------------------
     * OBJETIVO: Garante que um botão só é renderizado se a função de ação
     * correspondente tiver sido passada pelas props. Evita botões que não fazem nada.
     */
    const renderButton = (type, actionFn, tooltip) => {
        if (!actionFn) return null;
        return (
            <ActionButton
                {...type} // Propaga as configurações de ícone e cor do buttonConfigs
                onClick={() => isBulk ? actionFn() : actionFn(item)}
                tooltip={tooltip || type.tooltip}
            />
        );
    };

    return (
        <div className="d-flex gap-2">
            {!isTrashMode ? (
                /* --- MODO NORMAL (GESTÃO DO DIA-A-DIA) --- */
                <>
                    {!isBulk ? (
                        /* Ações Individuais (nos Cards) */
                        <>
                            {renderButton(BUTTON_TYPES.VIEW, actions.onView)}
                            {renderButton(BUTTON_TYPES.EDIT, actions.onEdit)}
                            {renderButton(BUTTON_TYPES.DELETE, actions.onDelete)}
                        </>
                    ) : (
                        /* ACÇÕES EM MASSA (No Header):
                           Regra de Segurança: Apenas Administradores podem mover tudo para a lixeira.
                        */
                        isAdmin && renderButton(BUTTON_TYPES.DELETE, actions.onBulkDelete, "Mover Tudo para Lixeira")
                    )}
                </>
            ) : (
                /* --- MODO LIXEIRA (RECUPERAÇÃO DE DADOS) --- */
                <>
                    {/* Botão de Restauro: Disponível para todos os utilizadores ou em bulk */}
                    {renderButton(BUTTON_TYPES.RESTORE, isBulk ? actions.onRestoreAll : actions.onRestore)}

                    {/* ELIMINAÇÃO PERMANENTE (Regra A14):
                        Apenas Administradores têm permissão para limpar a lixeira permanentemente.
                    */}
                    {isAdmin && renderButton(
                        BUTTON_TYPES.HARD_DELETE,
                        isBulk ? actions.onEmptyTrash : actions.onHardDelete,
                        isBulk ? "Esvaziar Lixeira" : null
                    )}
                </>
            )}
        </div>
    );
};

export default ActionGroup;