import React from "react";
import { Form } from "react-bootstrap";
import ActionGroup from "../Shared/ActionGroup";
import ActionButton from "../Shared/ActionButton";
import { BUTTON_TYPES } from "../Shared/buttonConfigs";

/**
 * COMPONENTE: ClientsHeader
 * -------------------------
 * DESCRIÇÃO: Cabeçalho dinâmico da página de Clientes.
 * Gerencia filtros de utilizadores (para Admins), alterna entre visualização normal e lixeira,
 * e exibe botões de ação global (Criar novo, Ações em massa).
 */
const ClientsHeader = ({
                           isTrashMode,
                           setIsTrashMode,
                           isAdmin,
                           filters,
                           setFilters,
                           users,
                           hasClients,
                           clientsCount,
                           actions,
                       }) => {

    // LÓGICA DE APRESENTAÇÃO: Identifica o nome do utilizador selecionado no filtro para o título
    const selectedUser = users.find((u) => String(u.id) === String(filters.userId));
    const displayName = selectedUser ? `${selectedUser.firstName} ${selectedUser.lastName}` : "Todos";

    return (
        <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3 p-3 bg-white rounded shadow-sm">

            {/* SECÇÃO 1: TÍTULO DINÂMICO E ESTATÍSTICAS RÁPIDAS */}
            <div>
                {/* O título muda de cor e texto dependendo se estamos a ver a Lixeira */}
                <h2 className={`fw-bold m-0 ${isTrashMode ? "text-danger" : "text-secondary"}`} style={{ fontSize: '1.5rem' }}>
                    {isTrashMode ? "LIXEIRA CLIENTES :" : "CLIENTES :"}{" "}
                    <span className="text-dark opacity-75">{displayName}</span>
                </h2>
                <p className="text-muted small m-0">Total: {clientsCount} registos</p>
            </div>

            {/* SECÇÃO 2: CONTROLOS E FILTROS */}
            <div className="d-flex gap-2 align-items-center flex-wrap">

                {/* FILTRO DE RESPONSÁVEL (REDE DE SEGURANÇA):
              Apenas visível para ADMIN, permitindo filtrar clientes por colaborador (Regra A5/A11)
          */}
                {isAdmin && (
                    <div className="d-flex align-items-center gap-2 me-2 border-end pe-3">
                        <span className="fw-bold small text-secondary">Responsável:</span>
                        <Form.Select
                            size="sm"
                            style={{ width: "180px" }}
                            value={filters.userId}
                            onChange={(e) => setFilters({ ...filters, userId: e.target.value })}
                        >
                            <option value="">Todos</option>
                            {users.map((u) => (
                                <option key={u.id} value={u.id}>
                                    {u.firstName} {u.lastName}
                                </option>
                            ))}
                        </Form.Select>
                    </div>
                )}

                {/* BOTÃO ALTERNAR LIXEIRA:
              Utiliza o componente reutilizável ActionButton com configurações pré-definidas.
          */}
                <ActionButton
                    {...(isTrashMode ? BUTTON_TYPES.TRASH_CLOSE : BUTTON_TYPES.TRASH_OPEN)}
                    onClick={() => setIsTrashMode(!isTrashMode)}
                />

                {/* GRUPO DE ACÇÕES EM MASSA:
              Permite executar ações (como restaurar ou eliminar permanentemente)
              em todos os itens filtrados de uma vez.
          */}
                {isAdmin && filters.userId && hasClients && (
                    <ActionGroup
                        actions={actions}
                        isTrashMode={isTrashMode}
                        isAdmin={isAdmin}
                        isBulk={true}
                    />
                )}

                {/* BOTÃO NOVO CLIENTE:
              Só é exibido fora do modo lixeira para evitar criação de dados em estado 'eliminado'.
          */}
                {!isTrashMode && (
                    <ActionButton
                        {...BUTTON_TYPES.ADD}
                        tooltip="Novo Cliente"
                        onClick={() => actions.openCreate()}
                    />
                )}
            </div>
        </div>
    );
};

export default ClientsHeader;