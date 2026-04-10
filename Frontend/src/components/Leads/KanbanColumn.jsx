import React from "react";
import { Badge, Button } from "react-bootstrap";
import LeadCard from "./LeadCard";

/**
 * COMPONENTE: KanbanColumn
 * -----------------------
 * DESCRIÇÃO: Representa uma coluna individual no quadro Kanban (ex: Novo, Em Análise).
 * @param {Object} col - Configuração da coluna (id, título, cor).
 * @param {Array} leads - Lista de leads filtradas para esta coluna específica.
 * @param {boolean} isTrashMode - Define se a coluna deve exibir estilos de lixeira (vermelho).
 * @param {boolean} isAdmin - Permissões para exibição de botões de gestão.
 * @param {Function} onAddClick - Função para abrir o formulário de nova lead nesta coluna.
 * @param {Array} cardActions - Configurações de botões injetadas para os LeadCards.
 */
const KanbanColumn = ({
                        col,
                        leads,
                        isTrashMode,
                        isAdmin,
                        onAddClick,
                        cardActions
                      }) => {

  // --- CONFIGURAÇÃO DE ESTILOS (CRITÉRIO: EVITAR MAGIC CONSTANTS) ---
  const COLUMN_STYLE = {
    minWidth: "280px",
    borderTop: `5px solid ${isTrashMode ? "#dc3545" : col.color}`
  };
  const TITLE_STYLE = { fontSize: "0.85rem", color: "#555" };
  const BADGE_STYLE = { fontSize: "0.7rem" };

  return (
      <div
          className="kanban-column bg-light rounded p-2 shadow-sm"
          style={COLUMN_STYLE}
      >
        {/* CABEÇALHO DA COLUNA: Título, Botão de Adição e Contador */}
        <div className="d-flex justify-content-between align-items-center mb-3 px-2 pt-1">
          <h6 className="fw-bold m-0 text-uppercase" style={TITLE_STYLE}>
            {col.title}
          </h6>

          <div className="d-flex align-items-center gap-2">
            {/* RENDERIZAÇÃO CONDICIONAL:
              O botão de adicionar só aparece fora da lixeira para evitar
              a criação de leads em estado 'eliminado'.
          */}
            {!isTrashMode && (
                <Button
                    variant="link"
                    className="p-0 text-secondary"
                    onClick={() => onAddClick(col.id)}
                    title="Adicionar Lead neste estado"
                >
                  <i className="bi bi-plus-circle-fill"></i>
                </Button>
            )}

            {/* Contador de Leads: Feedback visual imediato da carga de trabalho por coluna */}
            <Badge
                pill
                bg={isTrashMode ? "danger" : "secondary"}
                style={BADGE_STYLE}
            >
              {leads.length}
            </Badge>
          </div>
        </div>

        {/* LISTA DE CARTÕES (DATA MAPPING):
          Iteramos sobre o array de leads para renderizar os cartões individuais.
          Utilizamos o ID da lead como 'key' para otimização do Virtual DOM (React Best Practice).
      */}
        <div className="d-flex flex-column gap-2">
          {leads.map((lead) => (
              <LeadCard
                  key={lead.id}
                  lead={lead}
                  isTrashMode={isTrashMode}
                  isAdmin={isAdmin}
                  cardActions={cardActions}
              />
          ))}

          {/* Empty State: Feedback visual caso a coluna esteja vazia */}
          {leads.length === 0 && (
              <div className="text-center py-4 text-muted small opacity-50 border-dashed rounded">
                Sem leads
              </div>
          )}
        </div>
      </div>
  );
};

export default KanbanColumn;