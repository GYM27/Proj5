import React from "react";
import { Form } from "react-bootstrap";
import ActionGroup from "../Shared/ActionGroup";
import ActionButton from "../Shared/ActionButton";
import { BUTTON_TYPES } from "../Shared/buttonConfigs";

/**
 * COMPONENTE: KanbanHeader
 * -----------------------
 * DESCRIÇÃO: Cabeçalho especializado para o quadro Kanban de Leads.
 * FUNCIONALIDADES: Exibe estatísticas, filtra por utilizador (Admin),
 * alterna o estado da lixeira e permite ações em massa.
 */
const KanbanHeader = ({
                          displayName,
                          leadsCount,
                          isTrashMode,
                          setIsTrashMode,
                          isAdmin,
                          filters,
                          setFilters,
                          users,
                          actions,
                      }) => {
    return (
        <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3 p-3 bg-white rounded shadow-sm">

            {/* SECÇÃO 1: TÍTULO E CONTAGEM (UX FEEDBACK)
            O título adapta-se visualmente (cores e texto) para indicar se o utilizador
            está no modo de edição normal ou na lixeira.
        */}
            <div>
                <h2 className={`fw-bold m-0 ${isTrashMode ? "text-danger" : "text-secondary"}`} style={{ fontSize: '1.5rem' }}>
                    {isTrashMode ? "LIXEIRA :" : "LEADS :"}{" "}
                    <span className="text-dark opacity-75">{displayName}</span>
                </h2>
                <p className="text-muted small m-0">Total: {leadsCount} registos</p>
            </div>

            {/* SECÇÃO 2: CONTROLOS DE FILTRAGEM E ACÇÕES */}
            <div className="d-flex gap-2 align-items-center flex-wrap">

                {/* FILTRO DE RESPONSÁVEL (CRITÉRIO: FILTRAGEM NO SERVIDOR)
              Apenas visível para administradores. Alterar este select despoleta um novo
              pedido à API para obter apenas as leads do utilizador selecionado (Regra A11).
          */}
                {isAdmin && (
                    <div className="d-flex align-items-center gap-2 me-2 border-end pe-3">
                        <span className="fw-bold small text-secondary">Responsável:</span>
                        <Form.Select
                            size="sm"
                            style={{ width: "180px" }} // Constante de estilo para largura fixa
                            value={filters.userId}
                            onChange={(e) => setFilters({ ...filters, userId: e.target.value })}
                        >
                            <option value="">Todos</option>
                            {/* Mapeamento dinâmico da lista de colaboradores vinda da Store */}
                            {users.map((u) => (
                                <option key={u.id} value={u.id}>
                                    {u.firstName} {u.lastName}
                                </option>
                            ))}
                        </Form.Select>
                    </div>
                )}

                {/* BOTÃO ALTERNAR LIXEIRA:
              Inverte o estado 'isTrashMode' para carregar leads ativas ou eliminadas.
          */}
                <ActionButton
                    {...(isTrashMode ? BUTTON_TYPES.TRASH_CLOSE : BUTTON_TYPES.TRASH_OPEN)}
                    onClick={() => setIsTrashMode(!isTrashMode)}
                />

                {/* GRUPO DE ACÇÕES EM MASSA:
              Permite realizar limpezas ou restauros de todas as leads exibidas.
              Só é ativado se houver um filtro de utilizador aplicado para evitar erros globais.
          */}
                {isAdmin && filters.userId && leadsCount > 0 && (
                    <ActionGroup
                        actions={actions}
                        isTrashMode={isTrashMode}
                        isAdmin={isAdmin}
                        isBulk={true}
                    />
                )}

                {/* BOTÃO NOVA LEAD:
              Renderização condicional: Criar novas leads só é permitido fora da lixeira.
              Por defeito, abre o formulário no estado '1' (Novo).
          */}
                {!isTrashMode && (
                    <ActionButton
                        {...BUTTON_TYPES.ADD}
                        tooltip="Nova Lead"
                        onClick={() => actions.openCreate(1)}
                    />
                )}
            </div>
        </div>
    );
};

export default KanbanHeader;