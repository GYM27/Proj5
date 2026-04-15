import React, { useEffect, useState, useCallback } from "react";
import { Container, Spinner, Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useUserStore } from "../stores/UserStore";
import { userService } from "../services/userService";

// Componentes extraídos
import UserGrid from "../components/Users/UserGrid";
import UsersHeader from "../components/Users/UserHeader";
import { useModalManager } from "../Modal/useModalManager.jsx";
import DynamicModal from "../Modal/DynamicModal.jsx";
import ConfirmActionContent from "../Modal/ConfirmActionContent.jsx";
import { useUserActions } from "../components/Users/useUserActions.jsx";

/**
 * Componente responsável pela listagem e gestão de utilizadores.
 * Página de acesso restrito a Administradores.
 */
const Users = () => {
    const { userRole } = useUserStore();
    const isAdmin = userRole === "ADMIN";
    const navigate = useNavigate();

    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const { modalConfig, openModal, closeModal } = useModalManager();

    /**
     * Carrega a lista completa de utilizadores a partir da API.
     */
    const loadUsers = useCallback(async () => {
        try {
            setLoading(true);
            const data = await userService.getAllUsers();
            const sortedUsers = data.sort((a, b) => (a.softDelete === b.softDelete ? 0 : a.softDelete ? 1 : -1));
            setUsers(sortedUsers);
        } catch (err) {
            setError("Erro ao carregar a lista de utilizadores.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (!isAdmin) {
            navigate("/dashboard");
            return;
        }
        loadUsers();
    }, [isAdmin, navigate, loadUsers]);

    const { executeUserAction } = useUserActions(loadUsers, closeModal);

    const handleConfirmAction = async (data) => {
        await executeUserAction(modalConfig.type, data);
    };

    /**
     * Lógica para o botão "Enviar Convite" no Header.
     * Abre o modal específico para introduzir o e-mail do novo colaborador.
     */
    const handleInvite = () => {
        // "USER_INVITE" deve ser tratado no seu ConfirmActionContent/useUserActions
       openModal("USER_INVITE", "Convidar Novo Colaborador", {});
    };

    if (loading && users.length === 0) {
        return (
            <Container className="mt-5 text-center">
                <Spinner animation="border" variant="primary" />
                <p className="mt-3 text-muted">A carregar equipa...</p>
            </Container>
        );
    }

    return (
        <div className="users-page">
            {/* O Header fica fora do Container para ocupar toda a largura e mostrar a borda inferior */}
            <UsersHeader onInviteClick={handleInvite} />

            <Container>
                {error && <Alert variant="danger">{error}</Alert>}

                <UserGrid
                    users={users}
                    onViewProfile={(u) => navigate(`/users/${u.username}`)}
                    onToggleStatus={(u) => openModal("USER_TOGGLE_STATUS", u.softDelete ? "Reativar" : "Desativar", u)}
                    onHardDelete={(u) => openModal("USER_HARD_DELETE", "Ação Irreversível", u)}
                />

                <DynamicModal show={modalConfig.show} onHide={closeModal} title={modalConfig.title}>
                    <ConfirmActionContent
                        type={modalConfig.type}
                        data={modalConfig.data}
                        onCancel={closeModal}
                        onConfirm={handleConfirmAction}
                    />
                </DynamicModal>
            </Container>
        </div>
    );
};

export default Users;