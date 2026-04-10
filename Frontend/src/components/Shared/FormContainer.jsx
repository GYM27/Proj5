import React from "react";
import { Container, Row, Col, Card, Button, Spinner } from "react-bootstrap";

/**
 * COMPONENTE: FormContainer (HOC - High Order Component Pattern)
 * -------------------------------------------------------------
 * DESCRIÇÃO: Atua como um "Template" para todos os formulários da aplicação.
 * BENEFÍCIO: Centraliza o layout (Card, Sombras, Responsividade) e a lógica
 * visual de submissão (Botões, Spinnner) num único local.
 * * @param {string} title - O título que aparece no topo do formulário.
 * @param {string} icon - Classe do Bootstrap Icon (ex: 'bi-person').
 * @param {boolean} loading - Estado que controla a animação do botão de gravar.
 * @param {Function} onSubmit - Função de submissão do formulário pai.
 * @param {Function} onCancel - Função para retroceder ou cancelar a ação.
 * @param {ReactNode} children - Os campos específicos de cada formulário (Inputs).
 */
const FormContainer = ({ title, icon, loading, onSubmit, onCancel, children }) => (
    <Container className="mt-4">
        {/* Centralização do formulário no ecrã para melhor foco do utilizador */}
        <Row className="justify-content-center">
            <Col md={8} lg={6}>
                <Card className="shadow-sm border-0">
                    <Card.Body className="p-4">

                        {/* CABEÇALHO DO FORMULÁRIO:
                            Destaque visual com a cor primária e ícone descritivo.
                        */}
                        <h3 className="mb-4 fw-bold text-primary">
                            <i className={`bi ${icon} me-2`}></i>{title}
                        </h3>

                        <form onSubmit={onSubmit}>
                            {/* RENDERIZAÇÃO DINÂMICA (PROPS CHILDREN):
                                Aqui o React injeta os inputs específicos de cada página
                                (ex: NewLead ou NewClient), mantendo o container genérico.
                            */}
                            {children}

                            {/* RODAPÉ DE AÇÕES:
                                - Botão Cancelar: Permite abortar a operação.
                                - Botão Gravar: Gerencia o estado de 'loading' para evitar
                                  múltiplas submissões enquanto a API processa o pedido.
                            */}
                            <div className="d-flex gap-2 justify-content-end border-top pt-3 mt-4">
                                <Button variant="outline-secondary" onClick={onCancel}>
                                    Cancelar
                                </Button>

                                <Button variant="primary" type="submit" disabled={loading}>
                                    {/* Feedback visual de processamento (UX - 3%) */}
                                    {loading ? <Spinner size="sm" className="me-2" /> : null}
                                    {loading ? "A guardar..." : "Gravar"}
                                </Button>
                            </div>
                        </form>
                    </Card.Body>
                </Card>
            </Col>
        </Row>
    </Container>
);

export default FormContainer;