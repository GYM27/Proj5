import React from "react";
import { Form, Row, Col, Button, Spinner } from "react-bootstrap";

/**
 * COMPONENTE: ProfileForm
 * ----------------------
 * DESCRIÇÃO: Formulário de visualização e edição de perfil de utilizador.
 * @param {Object} formData - Estado contendo os dados do utilizador (firstName, email, etc).
 * @param {Function} handleChange - Gestor de eventos para atualizar o estado ao digitar.
 * @param {Function} handleSubmit - Função que processa a submissão dos dados para a API.
 * @param {boolean} isOwnProfile - Define se o formulário é editável (próprio perfil) ou apenas leitura.
 * @param {boolean} loading - Estado de carregamento para feedback visual no botão.
 */
const ProfileForm = ({ formData, handleChange, handleSubmit, isOwnProfile, loading }) => (
    <Form onSubmit={handleSubmit}>
        <Row>
            {/* CAMPOS DE IDENTIFICAÇÃO:
          - Usamos o atributo 'disabled={!isOwnProfile}' para garantir que um Administrador
            não altere dados pessoais de outro utilizador por acidente (Regra de Segurança).
          - O operador '|| ""' previne erros de 'Controlled Components' caso os dados demorem a carregar.
      */}
            <Col md={6}>
                <Form.Group className="mb-3">
                    <Form.Label>Nome</Form.Label>
                    <Form.Control
                        name="firstName"
                        value={formData.firstName || ""}
                        onChange={handleChange}
                        disabled={!isOwnProfile}
                        required
                    />
                </Form.Group>
            </Col>

            <Col md={6}>
                <Form.Group className="mb-3">
                    <Form.Label>Apelido</Form.Label>
                    <Form.Control
                        name="lastName"
                        value={formData.lastName || ""}
                        onChange={handleChange}
                        disabled={!isOwnProfile}
                        required
                    />
                </Form.Group>
            </Col>

            {/* CREDENCIAIS DE ACESSO: Username e Password */}
            <Col md={6}>
                <Form.Group className="mb-3">
                    <Form.Label>Username</Form.Label>
                    <Form.Control
                        name="username"
                        value={formData.username || ""}
                        onChange={handleChange}
                        disabled={!isOwnProfile}
                        required
                    />
                </Form.Group>
            </Col>

            <Col md={6}>
                <Form.Group className="mb-3">
                    <Form.Label>Password</Form.Label>
                    <Form.Control
                        type="password"
                        name="password"
                        value={formData.password || ""}
                        onChange={handleChange}
                        disabled={!isOwnProfile}
                        required
                    />
                </Form.Group>
            </Col>
        </Row>

        {/* CONTACTOS E IDENTIDADE VISUAL */}
        <Form.Group className="mb-3">
            <Form.Label>Email</Form.Label>
            <Form.Control
                type="email"
                name="email"
                value={formData.email || ""}
                onChange={handleChange}
                disabled={!isOwnProfile}
                required
            />
        </Form.Group>

        <Form.Group className="mb-3">
            <Form.Label>Telemóvel</Form.Label>
            <Form.Control
                name="cellphone"
                value={formData.cellphone || ""}
                onChange={handleChange}
                disabled={!isOwnProfile}
                required
            />
        </Form.Group>

        <Form.Group className="mb-4">
            <Form.Label>URL da Foto</Form.Label>
            <Form.Control
                name="photoUrl"
                value={formData.photoUrl || ""}
                onChange={handleChange}
                disabled={!isOwnProfile}
                placeholder="https://exemplo.com/foto.jpg"
            />
        </Form.Group>

        {/* RENDERIZAÇÃO CONDICIONAL DO BOTÃO:
        - O botão de guardar só aparece se o utilizador estiver a ver o seu próprio perfil.
        - Implementa feedback visual (Spinner) durante a comunicação assíncrona com o servidor.
    */}
        {isOwnProfile && (
            <Button variant="primary" type="submit" className="w-100 fw-bold" disabled={loading}>
                {loading ? (
                    <>
                        <Spinner size="sm" className="me-2" />
                        A guardar alterações...
                    </>
                ) : (
                    "Guardar Alterações"
                )}
            </Button>
        )}
    </Form>
);

export default ProfileForm;