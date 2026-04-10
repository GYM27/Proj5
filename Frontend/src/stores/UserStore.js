import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";

/**
 * STORE: useUserStore (Zustand com Persistência)
 * --------------------------------------------
 * DESCRIÇÃO: Gere a identidade e as permissões do utilizador logado.
 * FUNCIONALIDADE: Centraliza o Perfil e o Role (Cargo), servindo de base para
 * o Controlo de Acesso em toda a interface (Sidebar, Header, Botões).
 */
export const useUserStore = create(
    persist(
        (set) => ({
            // 1. ESTADO INICIAL (ESTRUTURA DE DADOS - 5%):
            // Mapeia os campos essenciais do LoginResponseDTO vindo do Java.
            username: "",
            firstName: "",
            lastName: "",
            email: "",
            userRole: "",   // CRUCIAL: Define a visibilidade de funções ADMIN (Regras A13/A14).
            photoUrl: "",
            isAuthenticated: false,

            /** * ACÇÃO: setUser
             * DESCRIÇÃO: Popula a store com os dados validados pelo Backend.
             * @param {Object} userData - Dados vindos do loginService.
             */
            setUser: (userData) => set({
                username: userData.username,
                firstName: userData.firstName,
                lastName: userData.lastName,
                email: userData.email,
                userRole: userData.userRole,
                photoUrl: userData.photoUrl || "",
                isAuthenticated: true
            }),

            // ACÇÃO: setPhotoUrl (UPDATE PARCIAL):
            // Permite atualizar a imagem de perfil sem necessidade de um novo login.
            setPhotoUrl: (url) => set({ photoUrl: url }),

            /** * ACÇÃO: clearUser (CLEANUP):
             * Utilizada no fluxo de Logout para garantir que nenhum dado sensível
             * permanece na memória da aplicação.
             */
            clearUser: () => set({
                username: "",
                firstName: "",
                lastName: "",
                email: "",
                userRole: "",
                photoUrl: "",
                isAuthenticated: false
            }),
        }),
        /** * CONFIGURAÇÃO DE PERSISTÊNCIA (SEGURANÇA - 2%):
         * 'name': Chave única no armazenamento do browser.
         * 'storage': Utilizamos sessionStorage para que os dados expirem
         * automaticamente quando o utilizador fecha o separador/browser.
         */
        {
            name: "user-storage",
            storage: createJSONStorage(() => sessionStorage)
        }
    )
);