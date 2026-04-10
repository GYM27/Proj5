import React, { useEffect, useState } from "react";
import { Container, Spinner } from "react-bootstrap";
import { useLeadStore } from "../stores/LeadsStore";
import { useUserStore } from "../stores/UserStore";
import { userService } from "../services/userService";

// Componentes da Pasta Shared (Arquitetura Modular - 5%)
import { useModalManager } from "../Modal/useModalManager.jsx";
import { useResourceActions } from "../components/Shared/useResourceActions.jsx";
import ConfirmActionContent from "../Modal/ConfirmActionContent.jsx";

// Componentes de Leads
import KanbanHeader from "../components/Leads/KanbanHeader.jsx";
import KanbanColumn from "../components/Leads/KanbanColumn";
import DynamicModal from "../Modal/DynamicModal.jsx";
import EditLeadForm from "../components/Leads/EditLeadForm";

/**
 * CONFIGURAÇÃO DE COLUNAS (MAGIC CONSTANTS - 2%):
 * Centralizamos os estados do funil de vendas.
 * Estes IDs (1 a 5) mapeiam diretamente os Enums ou constantes do Backend Java.
 */
const COLUMNS_DEF = [
    { id: 1, title: "Novo", color: "#007bff" },
    { id: 2, title: "Em Análise", color: "#ffc107" },
    { id: 3, title: "Proposta", color: "#17a2b8" },
    { id: 4, title: "Ganho", color: "#28a745" },
    { id: 5, title: "Perdido", color: "#dc3545" },
];

/**
 * COMPONENTE: LeadsKanban
 * ----------------------
 * DESCRIÇÃO: Vista principal de gestão de Leads em formato Kanban.
 * FUNCIONALIDADES: Filtragem Admin, alternância de Lixeira, gestão de modais
 * e distribuição de leads por colunas de estado.
 */
const LeadsKanban = () => {
    // 1. ESTADOS E STORES (CRITÉRIO: GESTÃO DE ESTADO - 5%):
    const leadStore = useLeadStore();
    const { userRole, firstName: currentUserName } = useUserStore();
    const isAdmin = userRole === "ADMIN";

    const [users, setUsers] = useState([]);
    const [filters, setFilters] = useState({ userId: "", state: "" });
    const [isTrashMode, setIsTrashMode] = useState(false);

    // 2. GESTÃO DE MODAIS: Utiliza o hook partilhado para centralizar o estado da UI.
    const { modalConfig, openModal, closeModal } = useModalManager();

    // 3. AÇÕES CENTRALIZADAS (SHARED LOGIC):
    // Injeta a lógica de navegação e abertura de modais específica para Leads.
    const { leads: actions } = useResourceActions(openModal, filters, { leadStore, userRole });

    // 4. CARREGAMENTO DE DADOS (CRITÉRIO: FILTRAGEM NO SERVIDOR - 3%):
    useEffect(() => {
        if (isAdmin) {
            userService.getAllUsers().then(setUsers).catch(console.error);
        }
    }, [isAdmin]);

    /**
     * EFEITO DE SINCRONIZAÇÃO:
     * Dispara um novo pedido à API sempre que o utilizador selecionado
     * ou o modo lixeira (softDeleted) é alterado.
     */
    useEffect(() => {
        leadStore.fetchMyLeads(userRole, {
            userId: filters.userId,
            softDeleted: isTrashMode
        });
    }, [userRole, filters.userId, isTrashMode, leadStore.fetchMyLeads]);

    // FEEDBACK DE LOADING (UX - 3%)
    if (leadStore.loading && leadStore.leads.length === 0) {
        return (
            <div className="text-center mt-5">
                <Spinner animation="border" variant="primary" />
                <p>A carregar painel...</p>
            </div>
        );
    }

    // Lógica de exibição de nome para personalização do cabeçalho
    const selectedUser = users.find(u => String(u.id) === String(filters.userId));
    const displayName = isAdmin
        ? (selectedUser ? `${selectedUser.firstName} ${selectedUser.lastName}` : "GERAL ADMIN")
        : currentUserName;

    return (
        <Container fluid className="mt-4 px-4">
            {/* CABEÇALHO: Componente especializado que recebe as ações do hook shared */}
            <KanbanHeader
                displayName={displayName}
                leadsCount={leadStore.leads.length}
                isTrashMode={isTrashMode}
                setIsTrashMode={setIsTrashMode}
                isAdmin={isAdmin}
                filters={filters}
                setFilters={setFilters}
                users={users}
                actions={actions}
            />

            {/* ZONA KANBAN:
            Layout horizontal com scroll automático. Cada coluna filtra as leads
            que pertencem ao seu ID de estado correspondente.
        */}
            <div className="d-flex gap-3 pb-4" style={{ overflowX: "auto", minHeight: "80vh", alignItems: "flex-start" }}>
                {COLUMNS_DEF.map((col) => (
                    <KanbanColumn
                        key={col.id}
                        col={col}
                        leads={leadStore.leads.filter((l) => l.state === col.id)}
                        isTrashMode={isTrashMode}
                        isAdmin={isAdmin}
                        onAddClick={actions.openCreate}
                        cardActions={actions.cardActions}
                    />
                ))}
            </div>

            {/* MODAL ÚNICO DINÂMICO:
            Centraliza a renderização de formulários ou avisos de confirmação
            baseado no modalConfig.type.
        */}
            <DynamicModal show={modalConfig.show} onHide={closeModal} title={modalConfig.title}>
                {modalConfig.type === "EDIT_LEAD" ? (
                    <EditLeadForm
                        leadData={modalConfig.data}
                        onSuccess={() => {
                            // Refresh dos dados após edição bem-sucedida
                            leadStore.fetchMyLeads(userRole, { userId: filters.userId, softDeleted: isTrashMode });
                            closeModal();
                        }}
                        onCancel={closeModal}
                    />
                ) : (
                    /* CONTEÚDO DE CONFIRMAÇÃO (SOFT/HARD DELETE/BULK/RESTORE):
                       Usa um actionMap para mapear o tipo de modal à função da store.
                    */
                    <ConfirmActionContent
                        type={modalConfig.type}
                        data={modalConfig.data}
                        onCancel={closeModal}
                        onConfirm={async (data) => {
                            const actionMap = {
                                "SOFT_DELETE": () => leadStore.deleteLead(data.id, userRole, false), // Regra A9
                                "HARD_DELETE": () => leadStore.deleteLead(data.id, userRole, true),  // Regra A14
                                "BULK_SOFT_DELETE": () => leadStore.handleBulkAction(data.userId, "SOFT_DELETE_ALL", userRole, { userId: filters.userId, softDeleted: isTrashMode }),
                                "BULK_HARD_DELETE": () => leadStore.handleBulkAction(data.userId, "EMPTY_TRASH", userRole, { userId: filters.userId, softDeleted: isTrashMode }),
                                "RESTORE_LEAD": () => leadStore.restoreLead(data.id, data, userRole),
                                "RESTORE_ALL": () => leadStore.handleBulkAction(data.userId, "RESTORE_ALL", userRole, { userId: filters.userId, softDeleted: isTrashMode })
                            };

                            const actionToExecute = actionMap[modalConfig.type];

                            if (actionToExecute) {
                                const success = await actionToExecute();
                                if (success) closeModal();
                            }
                        }}
                    />
                )}
            </DynamicModal>
        </Container>
    );
};

export default LeadsKanban;