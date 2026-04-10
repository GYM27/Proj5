import React from "react";
import { Row, Col } from "react-bootstrap";
import UserCard from "./UserCard";

/**
 * COMPONENTE PRESENTATIONAL: UserGrid
 * -----------------------------------
 * DESCRIÇÃO: Responsável apenas por renderizar a grelha de utilizadores.
 * Recebe os dados e as funções de clique através de Props.
 */
const UserGrid = ({ users, onViewProfile, onToggleStatus, onHardDelete }) => {
    // ESTADO VAZIO: Se não houver dados, mostra a mensagem visual
    if (users.length === 0) {
        return (
            <Row>
                <Col className="text-center p-5 bg-light rounded shadow-sm">
                    <i className="bi bi-people display-4 text-muted"></i>
                    <p className="mt-3 text-muted fw-bold">Nenhum utilizador encontrado no sistema.</p>
                </Col>
            </Row>
        );
    }

    // GRELHA COM DADOS: Mapeia o array e desenha os UserCards
    return (
        <Row className="g-3">
            {users.map((user) => (
                <Col key={user.id} xs={12} sm={6} md={4} lg={3}>
                    <UserCard
                        user={user}
                        onViewProfile={onViewProfile}
                        onToggleStatus={onToggleStatus}
                        onHardDelete={onHardDelete}
                    />
                </Col>
            ))}
        </Row>
    );
};

export default UserGrid;