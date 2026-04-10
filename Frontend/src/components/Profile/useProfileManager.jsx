import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { userService } from "../../services/userService";
import { useModalManager } from "../../Modal/useModalManager";
import { useUserStore } from "../../stores/UserStore"; // <-- 1. IMPORTA A STORE

export const useProfileManager = (targetUserId, isOwnProfile) => {
    const navigate = useNavigate();
    const { modalConfig, openModal, closeModal } = useModalManager();

    // <-- 2. VAI BUSCAR A FUNÇÃO PARA ATUALIZAR A FOTO NO HEADER
    const setPhotoUrl = useUserStore((state) => state.setPhotoUrl);

    const [formData, setFormData] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const load = async () => {
            try {
                const data = isOwnProfile
                    ? await userService.getMe()
                    : await userService.getUserById(targetUserId);
                setFormData(data);
            } catch (err) {
                console.error("Erro ao carregar dados do perfil:", err);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [targetUserId, isOwnProfile]);

    // <-- 3. NOVA FUNÇÃO: Atualiza o estado quando o utilizador escreve no formulário
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    // <-- 4. NOVA FUNÇÃO: Guarda na Base de Dados e atualiza o Header
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            setLoading(true);
            // Chama a API para gravar os dados
            await userService.updateMyProfile(formData);

            // ✨ A FOTO MUDA NO HEADER AQUI ✨
            setPhotoUrl(formData.photoUrl);

            alert("Perfil atualizado com sucesso!");
        } catch (err) {
            alert("Erro ao guardar o perfil.");
        } finally {
            setLoading(false);
        }
    };

    const handleConfirmAction = async (data) => {
        try {
            const actionMap = {
                "USER_HARD_DELETE": async () => {
                    await userService.deleteUserPermanent(targetUserId);
                    closeModal();
                    navigate("/users");
                },
                "USER_TOGGLE_STATUS": async () => {
                    const action = data.softDelete ? "activate" : "deactivate";
                    await userService.toggleUserStatus(targetUserId, action);
                    closeModal();
                    window.location.reload();
                }
            };

            if (actionMap[modalConfig.type]) {
                await actionMap[modalConfig.type]();
            }
        } catch (err) {
            alert("Erro na operação de administração.");
        }
    };

    return {
        formData, loading, modalConfig, openModal, closeModal, handleConfirmAction,
        handleChange, handleSubmit // <-- 5. EXPORTA AS NOVAS FUNÇÕES
    };
};