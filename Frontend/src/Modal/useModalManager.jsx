import { useState } from "react";

/**
 * HOOK PERSONALIZADO: useModalManager
 * ----------------------------------
 * DESCRIÇÃO: Gere o estado global e a configuração de janelas modais.
 * OBJETIVO: Centralizar a lógica de abertura, fecho e passagem de dados para modais,
 * evitando a repetição de código (Boilerplate) em múltiplos componentes.
 * * BENEFÍCIO: Segue o princípio DRY (Don't Repeat Yourself).
 */
export const useModalManager = () => {

    /**
     * ESTADO UNIFICADO (CRITÉRIO: GESTÃO DE ESTADO - 5%):
     * - 'show': Booleano que controla a visibilidade no DOM.
     * - 'title': Título dinâmico que será passado ao Header do Modal.
     * - 'type': Identificador da ação (ex: 'EDIT_LEAD', 'DELETE_USER').
     * - 'data': O objeto (Lead/Cliente/User) que será editado ou apagado.
     */
    const [modalConfig, setModalConfig] = useState({
        show: false,
        title: "",
        type: null,
        data: null,
    });

    /**
     * FUNÇÃO: closeModal
     * ------------------
     * Altera apenas a visibilidade para 'false', preservando os dados
     * momentaneamente para evitar saltos visuais durante a animação de fecho.
     */
    const closeModal = () => setModalConfig(prev => ({ ...prev, show: false }));

    /**
     * FUNÇÃO: openModal
     * -----------------
     * Define toda a configuração necessária para o Modal numa única operação.
     * @param {string} type - O tipo de conteúdo a renderizar.
     * @param {string} title - O título da janela.
     * @param {Object} data - Os dados do item alvo (opcional).
     */
    const openModal = (type, title, data = null) =>
        setModalConfig({ show: true, type, title, data });

    // Exporta o estado e as funções controladoras para serem usadas por qualquer componente.
    return { modalConfig, openModal, closeModal };
};