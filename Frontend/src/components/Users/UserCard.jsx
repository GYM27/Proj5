import React from "react";
import {Card, Button, Badge} from "react-bootstrap";

/**
 * COMPONENTE: UserCard
 * -------------------
 * DESCRIÇÃO: Representa um utilizador do sistema na lista de gestão de staff.
 * @param {Object} user - Dados do utilizador (nome, email, cargo, estado).
 * @param {Function} onToggleStatus - Callback para ativar/desativar (Soft Delete - Regra A9).
 * @param {Function} onHardDelete - Callback para eliminação permanente (Regra A14).
 * @param {Function} onViewProfile - Callback para navegar para o perfil detalhado.
 */
const UserCard = ({user, onToggleStatus, onHardDelete, onViewProfile}) => {
    // LÓGICA DE ESTADO (REGRA A9):
    // Identifica se o utilizador está desativado no sistema.
    const isInactive = user.softDelete;

    return (
        <Card
            /**
             * FEEDBACK VISUAL DINÂMICO :
             * - Se inativo, aplicamos 'opacity-75' e 'bg-light' para "apagar" visualmente o cartão.
             * - A borda superior muda de cor: Amarelo (Aviso) para Inativo, Verde para Ativo.
             */
            className={`shadow-sm border-0 h-100 ${isInactive ? "opacity-75 bg-light" : ""}`}
            style={{borderTop: `4px solid ${isInactive ? "#ffc107" : "#28a745"}`}}
        >
            <Card.Body className="p-3 d-flex flex-column">

                {/* CABEÇALHO: Nome e Badge de Estado */}
                <div className="d-flex justify-content-between align-items-start mb-2">
                    <h6 className="fw-bold mb-1" style={{fontSize: "1.1rem", color: "#2c3e50"}}>
                        {user.firstName} {user.lastName}
                    </h6>
                    <Badge bg={isInactive ? "warning" : "success"}>
                        {isInactive ? "Inativo" : "Ativo"}
                    </Badge>
                </div>

                {/* CARGO / ROLE:
                    Identificação clara se o utilizador é ADMIN ou USER comum.
                */}
                <div className="text-muted mb-3 pb-2 border-bottom" style={{fontSize: "0.85rem"}}>
                    <i className="bi bi-person-badge me-1 text-primary"></i>
                    <span className="fw-bold">{user.role || "Utilizador"}</span>
                </div>

                {/* DETALHES DE CONTACTO:
                    - 'flexGrow: 1' garante que todos os cartões na mesma linha tenham
                      a mesma altura, alinhando os botões no fundo (Design Pattern).
                */}
                <div className="mb-3" style={{fontSize: "0.85rem", flexGrow: 1}}>
                    <div className="mb-1">
                        <i className="bi bi-envelope-fill text-muted me-2"></i>
                        {user.email}
                    </div>
                    <div>
                        <i className="bi bi-telephone-fill text-muted me-2"></i>
                        {user.cellphone || "N/A"}
                    </div>
                </div>

                {/* BOTÕES DE ACÇÃO (CONTROLO DE ADMIN):
                    Centralização de ícones com dimensões fixas (28x28) para consistência visual.
                */}
                <div className="d-flex justify-content-end gap-2 pt-2 mt-auto border-top">

                    {/* Botão Ver Perfil: Atalho para edição completa */}
                    <Button variant="outline-primary" size="sm"
                            className="p-1 d-flex align-items-center justify-content-center"
                            style={{width: "28px", height: "28px"}} title="Ver Perfil"
                            onClick={() => onViewProfile(user)}>
                        <i className="bi bi-person" style={{fontSize: "0.9rem"}}></i>
                    </Button>

                    {/* Botão Alternar Estado (Soft Delete):
                        Muda de ícone (bi-ban / bi-arrow-counterclockwise) e de cor
                        consoante o estado atual do utilizador.
                    */}
                    <Button variant={isInactive ? "outline-success" : "outline-warning"} size="sm"
                            className="p-1 d-flex align-items-center justify-content-center"
                            style={{width: "28px", height: "28px"}} title={isInactive ? "Reativar" : "Desativar"}
                            onClick={() => onToggleStatus(user)}>
                        <i className={`bi ${isInactive ? "bi-arrow-counterclockwise" : "bi-ban"}`}
                           style={{fontSize: "0.9rem"}}></i>
                    </Button>

                    {/* Botão Eliminar Permanente (Hard Delete):
                        Ação crítica (Regra A14) com variante 'danger'.
                    */}
                    <Button variant="outline-danger" size="sm"
                            className="p-1 d-flex align-items-center justify-content-center"
                            style={{width: "28px", height: "28px"}} title="Eliminar Permanente"
                            onClick={() => onHardDelete(user)}>
                        <i className="bi bi-trash3" style={{fontSize: "0.9rem"}}></i>
                    </Button>
                </div>

            </Card.Body>
        </Card>
    );
};

export default UserCard;