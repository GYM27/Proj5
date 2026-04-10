import React, {useEffect, useState} from "react";
import {Container, Spinner, Row, Col} from "react-bootstrap";
import {useClientStore} from "../stores/ClientsStore";
import {useUserStore} from "../stores/UserStore";
import {userService} from "../services/userService";

// COMPONENTES SHARED (Arquitetura Modular - 5%)
import {useModalManager} from "../Modal/useModalManager.jsx";
import {useResourceActions} from "../components/Shared/useResourceActions";
import ConfirmActionContent from "../Modal/ConfirmActionContent.jsx";

// COMPONENTES DE CLIENTES
import ClientsHeader from "../components/Clients/ClientsHeader";
import ClientCard from "../components/Clients/ClientCard";
import DynamicModal from "../Modal/DynamicModal.jsx";
import EditClientForm from "../components/Clients/EditClientForm";
import "../App.css";

/**
 * COMPONENTE: Clients (Página Principal de Clientes)
 * ------------------------------------------------
 * DESCRIÇÃO: Gere a visualização, filtragem e ciclo de vida dos clientes.
 * FUNCIONALIDADES: Alternância entre vista ativa/lixeira, filtragem por utilizador (Admin),
 * e execução de ações individuais ou em massa (Bulk).
 */
const Clients = () => {
    // 1. ESTADOS E STORES (CRITÉRIO: GESTÃO DE ESTADO - 5%):
    const clientStore = useClientStore();
    const {userRole} = useUserStore();
    const isAdmin = userRole === "ADMIN";

    const [isTrashMode, setIsTrashMode] = useState(false);
    const [filters, setFilters] = useState({userId: ""});
    const [users, setUsers] = useState([]);

    // 2. GESTÃO DE MODAIS E ACÇÕES VIA SHARED HOOKS:
    // Centralizamos a lógica de interface para evitar repetição de código (DRY).
    const {modalConfig, openModal, closeModal} = useModalManager();

    // Injetamos as dependências para obter as ações prontas para serem distribuídas pelos Cards e Header.
    const {clients: actions} = useResourceActions(openModal, filters, {clientStore, userRole});

    // 3. CARREGAMENTO DE DADOS (CRITÉRIO: FILTRAGEM NO SERVIDOR - 3%):
    // Carrega a lista de utilizadores apenas se for Admin (para o filtro de responsável).
    useEffect(() => {
        if (isAdmin) userService.getAllUsers().then(setUsers).catch(console.error);
    }, [isAdmin]);

    /**
     * EFEITO DE SINCRONIZAÇÃO:
     * Dispara um novo fetch sempre que o filtro de utilizador ou o modo lixeira muda.
     * Isto garante que o processamento de dados é feito no Backend (Java/PostgreSQL).
     */
    useEffect(() => {
        clientStore.fetchClients(userRole, {
            userId: filters.userId || null,
            showTrash: isTrashMode,
        });
    }, [userRole, filters.userId, isTrashMode, clientStore.fetchClients]);

    // FEEDBACK VISUAL DE CARREGAMENTO (UX - 3%)
    if (clientStore.loading && clientStore.clients.length === 0) {
        return (
            <Container className="mt-4 text-center">
                <Spinner animation="border" variant="primary"/>
                <p>A carregar clientes...</p>
            </Container>
        );
    }

    return (
        <Container className="mt-4">
            {/* 1. CABEÇALHO: Controla filtros e ações globais */}
            <ClientsHeader
                isTrashMode={isTrashMode}
                setIsTrashMode={setIsTrashMode}
                isAdmin={isAdmin}
                filters={filters}
                setFilters={setFilters}
                users={users}
                hasClients={clientStore.clients.length > 0}
                actions={actions}
            />

            {/* 2. LISTA DE CLIENTES (GRID RESPONSIVA - 4%):
                Usa o sistema de grelha do Bootstrap para adaptar o número de colunas ao ecrã.
            */}
            <div>
                {clientStore.clients.length === 0 ? (
                    /* EMPTY STATE: Feedback visual quando não há resultados */
                    <div className="text-center p-5 bg-light rounded border">
                        <i className="bi bi-folder2-open display-4 text-muted"></i>
                        <p className="mt-3 text-muted">
                            {isTrashMode
                                ? "Lixeira vazia ou utilizador não selecionado."
                                : "Nenhum cliente encontrado."}
                        </p>
                    </div>
                ) : (
                    <Row className="g-3">
                        {clientStore.clients.map((client) => (
                            <Col key={client.id} xs={12} sm={6} md={4} lg={3}>
                                <ClientCard
                                    client={client}
                                    isTrashMode={isTrashMode}
                                    isAdmin={isAdmin}
                                    cardActions={actions.cardActions}
                                />
                            </Col>
                        ))}
                    </Row>
                )}
            </div>

            {/* 3. MODAL DINÂMICO ÚNICO (Lógica Centralizada):
                Este modal único serve para Edição e para todas as Confirmações.
            */}
            <DynamicModal
                show={modalConfig.show}
                onHide={closeModal}
                title={modalConfig.title}
            >
                {modalConfig.type === "EDIT_CLIENT" ? (
                    <EditClientForm
                        clientData={modalConfig.data}
                        onSuccess={() => {
                            clientStore.fetchClients(userRole, {
                                userId: filters.userId || null,
                                showTrash: isTrashMode,
                            });
                            closeModal();
                        }}
                        onCancel={closeModal}
                    />
                ) : (
                    /* ACTION MAP (PADRÃO DE DESIGN):
                       Mapeia o tipo de modal para a função correspondente na Store.
                    */
                    <ConfirmActionContent
                        type={modalConfig.type}
                        data={modalConfig.data}
                        onCancel={closeModal}
                        onConfirm={async (data) => {
                            const currentFilters = {userId: filters.userId || null, showTrash: isTrashMode};

                            const actionMap = {
                                "SOFT_DELETE": () => clientStore.deleteClient(data.id, false), // Regra A9
                                "HARD_DELETE": () => clientStore.deleteClient(data.id, true),  // Regra A14
                                "BULK_SOFT_DELETE": () => clientStore.handleBulkAction(data.userId, "DEACTIVATE_ALL", userRole, currentFilters),
                                "BULK_HARD_DELETE": () => clientStore.handleBulkAction(data.userId, "EMPTY_TRASH", userRole, currentFilters),
                                "RESTORE_CLIENT": () => clientStore.restoreClient(data.id, data, userRole),
                                "RESTORE_ALL": () => clientStore.handleBulkAction(data.userId, "RESTORE_ALL", userRole, {
                                    userId: filters.userId,
                                    softDeleted: isTrashMode
                                })
                            };

                            const actionToExecute = actionMap[modalConfig.type];

                            if (actionToExecute) {
                                await actionToExecute();
                                closeModal();
                            }
                        }}
                    />
                )}
            </DynamicModal>
        </Container>
    );
};

export default Clients;