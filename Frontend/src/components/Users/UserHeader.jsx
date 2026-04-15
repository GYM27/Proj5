import React from 'react';
import { Container, Row, Col, Button } from 'react-bootstrap';

const UsersHeader = ({ onInviteClick }) => {
    return (
        <Container fluid className="bg-white border-bottom py-3 mb-4">
            <Row className="align-items-center">
                <Col>
                    <h2 className="fw-bold m-0 text-secondary">GESTÃO DE USERS (ADMIN)</h2>
                    <p className="text-muted small m-0">Gerencie as contas e estados dos colaboradores.</p>
                </Col>
                <Col xs="auto">
                    <Button
                        variant="primary"
                        className="px-4 fw-semibold"
                        onClick={onInviteClick}
                    >
                        <i className="bi bi-envelope-plus me-0"></i>

                    </Button>
                </Col>
            </Row>
        </Container>
    );
};

export default UsersHeader;