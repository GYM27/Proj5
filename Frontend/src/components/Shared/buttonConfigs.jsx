/**
 * CONFIGURAÇÃO GLOBAL DE BOTÕES (DESIGN SYSTEM)
 * -------------------------------------------
 * DESCRIÇÃO: Este ficheiro centraliza a identidade visual de todas as ações da interface.
 * OBJETIVO: Seguir o princípio DRY (Don't Repeat Yourself) e evitar "Magic Constants".
 * * Se for necessário alterar o ícone de "Eliminar" em todo o projeto,
 * basta mudar a propriedade 'icon' aqui uma única vez.
 */
export const BUTTON_TYPES = {
    // AÇÕES DE CONSULTA E EDIÇÃO
    VIEW: { icon: "bi-eye", variant: "info", tooltip: "Ver Detalhes" },
    EDIT: { icon: "bi-pencil", variant: "primary", tooltip: "Editar" },

    // GESTÃO DE CICLO DE VIDA (SOFT DELETE - REGRA A9)
    DELETE: { icon: "bi-trash3", variant: "danger", tooltip: "Mover para Lixeira" },
    RESTORE: { icon: "bi-arrow-counterclockwise", variant: "success", tooltip: "Restaurar" },

    // GESTÃO CRÍTICA (HARD DELETE - REGRA A14)
    // Reservado para Administradores: Eliminação definitiva da Base de Dados.
    HARD_DELETE: { icon: "bi-x-circle", variant: "danger", tooltip: "Eliminar Permanente" },

    // AÇÕES DE CRIAÇÃO E NAVEGAÇÃO
    ADD: { icon: "bi-plus-lg", variant: "primary", tooltip: "Adicionar Novo" },

    // CONTROLO DE VISTA (LIXEIRA)
    // Permite alternar dinamicamente o estado da interface entre Dados Ativos e Lixeira.
    TRASH_OPEN: { icon: "bi-trash", variant: "primary", tooltip: "Ver Lixeira" },
    TRASH_CLOSE: { icon: "bi-arrow-left", variant: "primary", tooltip: "Sair da Lixeira" },
};