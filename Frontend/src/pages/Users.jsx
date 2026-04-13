import React, { useEffect, useState, useCallback } from "react";
import { Container, Spinner, Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useUserStore } from "../stores/UserStore";
import { userService } from "../services/userService";

// Componentes extraídos
import UserGrid from "../components/Users/UserGrid";
import { useModalManager } from "../Modal/useModalManager.jsx";
import DynamicModal from "../Modal/DynamicModal.jsx";
import ConfirmActionContent from "../Modal/ConfirmActionContent.jsx";
import { useUserActions } from "../components/Users/useUserActions.jsx";

/**
 * Componente responsável pela listagem e gestão de utilizadores.
 * Página de acesso restrito a Administradores.
 * Permite visualizar os perfis através de URLs únicos, alterar o estado (Ativar/Desativar)
 * e eliminar permanentemente contas de utilizadores.
 * * @returns {JSX.Element} A interface de listagem e gestão de utilizadores.
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
     * Ordena a lista colocando os utilizadores ativos primeiro e os desativados (softDelete) no fim.
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

    /**
     * Efeito que verifica o nível de acesso do utilizador atual.
     * Se não for administrador, redireciona para o dashboard. Caso contrário, carrega a lista.
     */
    useEffect(() => {
        if (!isAdmin) {
            navigate("/dashboard");
            return;
        }
        loadUsers();
    }, [isAdmin, navigate, loadUsers]);

    // INJEÇÃO DA LÓGICA DE NEGÓCIO:
    // Passamos o loadUsers e o closeModal para o hook saber o que fazer após a API responder.
    const { executeUserAction } = useUserActions(loadUsers, closeModal);

    /**
     * Delega a ação confirmada no modal para o hook de ações de utilizador.
     * * @param {Object} data - Os dados do utilizador sobre o qual a ação será executada.
     */
    const handleConfirmAction = async (data) => {
        await executeUserAction(modalConfig.type, data);
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
        <Container className="mt-4">
            <div className="mb-4">
                <h2 className="fw-bold m-0 text-secondary">GESTÃO DE USERS (ADMIN)</h2>
                <p className="text-muted small">Gerencie as contas e estados dos colaboradores.</p>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            <UserGrid
                users={users}
                /* CORREÇÃO: Utilizar a variável correta "u" definida no parâmetro do callback */
                onViewProfile={(u) => navigate(`/users/${u.username}`)}
                onToggleStatus={(u) => openModal("USER_TOGGLE_STATUS", u.softDelete ? "Reativar" : "Desativar", u)}
                onHardDelete={(u) => openModal("USER_HARD_DELETE", "Ação Irreversível", u)}
            />

            <DynamicModal show={modalConfig.show} onHide={closeModal} title={modalConfig.title}>
                <ConfirmActionContent
                    type={modalConfig.type}
                    data={modalConfig.data}
                    onCancel={closeModal}
                    onConfirm={handleConfirmAction} // Passa a chamada limpa
                />
            </DynamicModal>
        </Container>
    );
};

export default Users;