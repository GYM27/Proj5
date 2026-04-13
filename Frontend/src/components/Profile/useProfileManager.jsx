import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { userService } from "../../services/userService";
import { useModalManager } from "../../Modal/useModalManager";
import { useUserStore } from "../../stores/UserStore"; // <-- 1. IMPORTA A STORE

/**
 * Hook customizado responsável por gerir a lógica de negócio da página de Perfil.
 * Centraliza o carregamento de dados, a edição do formulário, a atualização global
 * de estado (ex: fotografia no Header) e as ações de administração (ativar/desativar/eliminar).
 *
 * @param {string} targetUsername - O username do perfil a ser visualizado (undefined se for o próprio perfil).
 * @param {boolean} isOwnProfile - Flag que indica se o utilizador está a visualizar o seu próprio perfil.
 * @returns {Object} Um objeto contendo os estados e funções necessários para renderizar e interagir com o perfil.
 */
export const useProfileManager = (targetUsername, isOwnProfile) => {
    const navigate = useNavigate();

    // Gestor centralizado de modais de confirmação
    const { modalConfig, openModal, closeModal } = useModalManager();

    // <-- 2. VAI BUSCAR A FUNÇÃO PARA ATUALIZAR A FOTO NO HEADER
    const setPhotoUrl = useUserStore((state) => state.setPhotoUrl);

    /** @type {[Object, Function]} Estado que guarda os dados do perfil atualmente em visualização/edição. */
    const [formData, setFormData] = useState({});

    /** @type {[boolean, Function]} Estado que indica se os dados do perfil estão a ser carregados. */
    const [loading, setLoading] = useState(true);

    /**
     * Efeito responsável por carregar os dados do perfil assim que o componente é montado
     * ou quando a rota/utilizador alvo muda.
     */
    useEffect(() => {
        const load = async () => {
            try {
                // Carrega os dados do próprio utilizador ou do utilizador alvo consoante a rota
                const data = isOwnProfile
                    ? await userService.getMe()
                    : await userService.getUserByUsername(targetUsername);
                setFormData(data);
            } catch (err) {
                console.error("Erro ao carregar dados do perfil:", err);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [targetUsername, isOwnProfile]);

    /**
     * <-- 3. NOVA FUNÇÃO: Atualiza o estado quando o utilizador escreve no formulário
     * Captura os eventos de mudança nos inputs e atualiza o estado local `formData` dinamicamente.
     * * @param {React.ChangeEvent<HTMLInputElement>} e - O evento de alteração do input.
     */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    /**
     * <-- 4. NOVA FUNÇÃO: Guarda na Base de Dados e atualiza o Header
     * Submete as alterações do perfil para a API e atualiza a fotografia de forma global.
     * * @param {React.FormEvent<HTMLFormElement>} e - O evento de submissão do formulário.
     */
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

    /**
     * Executa as ações críticas de administração após confirmação no modal.
     * Mapeia o tipo de ação guardado no `modalConfig` para a respetiva chamada à API.
     * * @param {Object} data - Os dados adicionais passados para a ação (ex: estado de softDelete do utilizador).
     */
    const handleConfirmAction = async (data) => {
        try {
            const actionMap = {
                // Ação de eliminação permanente (Hard Delete)
                "USER_HARD_DELETE": async () => {
                    await userService.deleteUserPermanent(formData.id);
                    closeModal();
                    navigate("/users"); // Redireciona para a listagem após eliminação
                },
                // Ação para ativar/desativar a conta (Soft Delete)
                "USER_TOGGLE_STATUS": async () => {
                    const action = data.softDelete ? "activate" : "deactivate";
                    await userService.toggleUserStatus(formData.id, action);
                    closeModal();
                    window.location.reload(); // Recarrega para obter os dados frescos
                }
            };

            // Executa a função correspondente ao tipo de modal ativo
            if (actionMap[modalConfig.type]) {
                await actionMap[modalConfig.type]();
            }
        } catch (err) {
            alert("Erro na operação de administração.");
        }
    };

    return {
        formData,
        loading,
        modalConfig,
        openModal,
        closeModal,
        handleConfirmAction,
        handleChange,
        handleSubmit // <-- 5. EXPORTA AS NOVAS FUNÇÕES
    };
};