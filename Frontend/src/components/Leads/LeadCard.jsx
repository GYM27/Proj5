import React from "react";
import { Card } from "react-bootstrap";
import ActionGroup from "../Shared/ActionGroup";

/**
 * COMPONENTE: LeadCard
 * -------------------
 * DESCRIÇÃO: Responsável por renderizar a informação individual de cada Lead no quadro Kanban.
 * @param {Object} lead - Objeto com os dados da lead (título, autor, data, etc).
 * @param {boolean} isTrashMode - Indica se o cartão está na lixeira (muda o comportamento das ações).
 * @param {boolean} isAdmin - Define se o utilizador tem permissões de administração.
 * @param {Array} cardActions - Conjunto de funções de ação (Editar, Apagar, etc.) passadas pelo componente pai.
 */
const LeadCard = ({ lead, isTrashMode, isAdmin, cardActions }) => {
  return (
      <Card className="shadow-sm border-0 kanban-card mb-3">
        <Card.Body className="p-3">

          {/* TÍTULO DA LEAD: Identificação do registo na coluna */}
          <h6 className="fw-bold mb-1">{lead.title}</h6>

          <div className="d-flex justify-content-between align-items-end border-top pt-2 mt-2">
            <div>
              {/* NOME DO UTILIZADOR: Exibe quem está associado à lead */}
              <div className="fw-bold" style={{ fontSize: "0.75rem" }}>
                {lead.firstName} {lead.lastName}
              </div>

              {/* DATA FORMATADA: Proveniente do Backend para garantir consistência visual */}
              <div className="text-muted" style={{ fontSize: "0.7rem" }}>
                {lead.formattedDate}
              </div>
            </div>

            {/* ACTION GROUP:
                Componente que gere os botões de ação (Ver, Editar, Apagar).
                Recebe as 'cardActions' configuradas dinamicamente no hook useResourceActions.
            */}
            <ActionGroup
                actions={cardActions}
                item={lead}
                isTrashMode={isTrashMode}
                isAdmin={isAdmin}
            />
          </div>
        </Card.Body>
      </Card>
  );
};

export default LeadCard;