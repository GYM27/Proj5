import { userService } from "../../services/userService.js";

/**
 * HOOK: useUserActions
 * --------------------
 * DESCRIÇÃO: Isola a lógica de negócio e as chamadas à API da gestão de utilizadores.
 * @param {Function} onSuccess - Callback executado quando a ação tem sucesso (ex: loadUsers).
 * @param {Function} onComplete - Callback executado no fim da ação (ex: closeModal).
 */
export const useUserActions = (onSuccess, onComplete) => {

    const executeUserAction = async (actionType, userData) => {
        try {
            // O mapeamento de ações fica isolado aqui
            const actionMap = {
                "USER_HARD_DELETE": async () => {
                    await userService.deleteUserPermanent(userData.id);
                },
                "USER_TOGGLE_STATUS": async () => {
                    const action = userData.softDelete ? "activate" : "deactivate";
                    await userService.toggleUserStatus(userData.id, action);
                }
            };

            const actionToExecute = actionMap[actionType];

            if (actionToExecute) {
                await actionToExecute();

                // Se correu bem, fecha o modal e atualiza a lista
                if (onComplete) onComplete();
                if (onSuccess) onSuccess();
            }
        } catch (err) {
            console.error("Erro na ação de utilizador:", err);
            alert("Erro ao processar a ação. Verifique se o utilizador possui dependências ativas (Leads ou Clientes).");
        }
    };

    return { executeUserAction };
};