import React, {useEffect, useState, useCallback} from "react";
import {Container, Spinner, Alert} from "react-bootstrap";
import {useNavigate} from "react-router-dom";
import {useUserStore} from "../stores/UserStore";
import {userService} from "../services/userService";

// Componentes extraídos
import UserGrid from "../components/Users/UserGrid";
import {useModalManager} from "../Modal/useModalManager.jsx";
import DynamicModal from "../Modal/DynamicModal.jsx";
import ConfirmActionContent from "../Modal/ConfirmActionContent.jsx";
import {useUserActions} from "../components/Users/useUserActions.jsx";

const Users = () => {
    const {userRole} = useUserStore();
    const isAdmin = userRole === "ADMIN";
    const navigate = useNavigate();

    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const {modalConfig, openModal, closeModal} = useModalManager();

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

    // INJEÇÃO DA LÓGICA DE NEGÓCIO:
    // Passamos o loadUsers e o closeModal para o hook saber o que fazer após a API responder.
    const {executeUserAction} = useUserActions(loadUsers, closeModal);

    // O HANDLE FICA REDUZIDO A UM DELEGADOR:
    const handleConfirmAction = async (data) => {
        await executeUserAction(modalConfig.type, data);
    };

    if (loading && users.length === 0) {
        return (
            <Container className="mt-5 text-center">
                <Spinner animation="border" variant="primary"/>
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
                onViewProfile={(u) => navigate(`/profile?userId=${u.id}`)}
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