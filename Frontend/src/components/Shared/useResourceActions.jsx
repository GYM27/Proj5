import { useNavigate } from "react-router-dom";

/**
 * HOOK PERSONALIZADO: useResourceActions
 * --------------------------------------
 * DESCRIÇÃO: Centraliza e padroniza as ações para Leads e Clientes.
 * OBJETIVO: Implementar o princípio DRY (Don't Repeat Yourself), evitando
 * duplicar lógica de navegação e abertura de modais em múltiplos componentes.
 * * @param {Function} openModal - Função vinda do useModalManager para disparar a UI.
 * @param {Object} filters - Filtros atuais (ex: userId selecionado no Header).
 * @param {Object} stores - Acesso ao estado global (Zustand).
 */
export const useResourceActions = (openModal, filters, stores) => {
    const navigate = useNavigate();
    const { leadStore, clientStore, userRole } = stores;

    // CONTROLO DE ACESSO (RBAC - 2%):
    // Variável crucial que dita se funções críticas (Restauro/Hard Delete) serão criadas ou não.
    const isAdmin = userRole === "ADMIN";

    // --- LÓGICA DE LEADS (GESTÃO DE OPORTUNIDADES) ---
    const leadActions = {
        // Ações Base: Disponíveis para todos os colaboradores.
        onView: (lead) => navigate(`/leads/${lead.id}`),
        onEdit: (lead) => openModal("EDIT_LEAD", "Editar Lead", lead),
        onDelete: (lead) => openModal("SOFT_DELETE", "Mover para Lixeira", lead),

        /** * openCreate (NAVEGAÇÃO COM ESTADO - 6%):
         * Passa o 'initialState' (coluna do Kanban) e o 'targetId' (atribuição de Admin)
         * através do state do react-router, otimizando o preenchimento do formulário.
         */
        openCreate: (stateId = 1) => navigate("/leads/new", {
            state: { targetId: filters?.userId, initialState: stateId }
        }),

        // Ações Bulk (Em Massa):
        onBulkDelete: () => openModal("BULK_SOFT_DELETE", "Mover Tudo para Lixeira", { userId: filters?.userId }),

        /** * AÇÕES EXCLUSIVAS DE ADMIN (REGRA A14):
         * Se o utilizador não for ADMIN, estas funções ficam 'undefined'.
         * O ActionGroup ignorará estas ações automaticamente, garantindo segurança no Frontend.
         */
        onRestore: isAdmin ? (lead) => openModal("RESTORE_LEAD", "Restaurar Lead", lead) : undefined,
        onRestoreAll: isAdmin ? () => openModal("RESTORE_ALL", "Restaurar Todos os Itens", { userId: filters?.userId }) : undefined,
        onEmptyTrash: isAdmin ? () => openModal("BULK_HARD_DELETE", "Esvaziar Lixeira Definitivamente", { userId: filters?.userId }) : undefined,
    };

    // --- LÓGICA DE CLIENTES (GESTÃO DE ORGANIZAÇÕES) ---
    const clientActions = {
        onView: (client) => navigate(`/clients/${client.id}`),
        onEdit: (client) => openModal("EDIT_CLIENT", "Editar Cliente", client),
        onDelete: (client) => openModal("SOFT_DELETE", "Remover Cliente", client),
        openCreate: () => navigate("/clients/new", { state: { targetId: filters?.userId } }),

        onBulkDelete: () => openModal("BULK_SOFT_DELETE", "Mover Tudo para Lixeira", { userId: filters?.userId }),

        // Espelhamento das permissões de Admin para Clientes
        onRestore: isAdmin ? (client) => openModal("RESTORE_CLIENT", "Restaurar Cliente", client) : undefined,
        onRestoreAll: isAdmin ? () => openModal("RESTORE_ALL", "Restaurar Todos os Itens", { userId: filters?.userId }) : undefined,
        onEmptyTrash: isAdmin ? () => openModal("BULK_HARD_DELETE", "Esvaziar Lixeira Definitivamente", { userId: filters?.userId }) : undefined,
    };

    /**
     * RETORNO ESTRUTURADO:
     * Devolvemos objetos separados para Leads e Clientes, incluindo o 'cardActions',
     * que é um subconjunto formatado especificamente para ser consumido pelos
     * componentes LeadCard e ClientCard.
     */
    return {
        leads: {
            ...leadActions,
            cardActions: {
                onView: leadActions.onView,
                onEdit: leadActions.onEdit,
                onDelete: leadActions.onDelete,
                onRestore: leadActions.onRestore,
                onHardDelete: leadActions.onHardDelete,
            }
        },
        clients: {
            ...clientActions,
            cardActions: {
                // onView: clientActions.onView,
                onEdit: clientActions.onEdit,
                onDelete: clientActions.onDelete,
                onRestore: clientActions.onRestore,
                onHardDelete: clientActions.onHardDelete,
            }
        }
    };
};