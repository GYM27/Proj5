import React from "react";
import {Card} from "react-bootstrap";
import ActionGroup from "../Shared/ActionGroup";

/**
 * COMPONENTE: ClientCard
 * ---------------------
 * DESCRIÇÃO: Representa visualmente um cliente na lista/grelha.
 * NOTA TÉCNICA: Este é um "Presentational Component". Ele não sabe como apagar ou editar;
 * apenas recebe as instruções (cardActions) e os dados (client) e exibe-os.
 */
const ClientCard = ({client, isTrashMode, isAdmin, cardActions}) => {

    // --- CONFIGURAÇÃO DE ESTILOS (CRITÉRIO: EVITAR MAGIC CONSTANTS) ---
    // Extraímos os valores fixos para constantes para facilitar a leitura e manutenção.
    const STYLE_CONTACTS = {fontSize: "0.95em"};
    const STYLE_OWNER_LABEL = {fontSize: "0.75rem", color: "#444"};
    const STYLE_ICON_SMALL = {fontSize: "0.8rem"};

    return (
        <Card className="h-100 shadow-sm border-start border-2 border-primary">
            <Card.Body className="d-flex flex-column">

                {/* TÍTULO: Nome do Cliente ou Empresa */}
                <Card.Title className="fw-bold text-truncate mb-1">
                    {client.name}
                </Card.Title>

                {/* BLOCO DE CONTACTOS (Regra: Exibir dados vindos da API) */}
                <div className="border-top pt-2 mt-2" style={STYLE_CONTACTS}>
                    <Card.Text className="text-primary small fw-bold mb-2">
                        <i className="bi bi-building me-2"></i>
                        {client.organization}
                    </Card.Text>

                    <div className="text-truncate mb-1">
                        <i className="bi bi-envelope me-2 text-muted"></i>
                        {client.email}
                    </div>

                    <div className="text-muted">
                        <i className="bi bi-telephone me-2"></i>
                        {client.phone}
                    </div>
                </div>

                {/* RODAPÉ DO CARTÃO: Informação do Proprietário e Ações Disponíveis */}
                <div className="d-flex justify-content-between align-items-center mt-auto pt-3">

                    {/* ACTION GROUP:
                        Componente que centraliza os botões (Editar, Apagar, Ver).
                        Passamos 'cardActions' que contém a lógica de cliques definida no pai.
                    */}
                    <ActionGroup
                        actions={cardActions}
                        item={client}
                        isTrashMode={isTrashMode}
                        isAdmin={isAdmin}
                    />
                </div>
            </Card.Body>
        </Card>
    );
};

export default ClientCard;