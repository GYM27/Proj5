import React, { useState } from "react";
import { Form, Button, Spinner, Alert } from "react-bootstrap";
import { useClientStore } from "../../stores/ClientsStore";

/**
 * COMPONENTE: EditClientForm
 * -------------------------
 * DESCRIÇÃO: Formulário especializado para a edição de clientes existentes.
 * * @param {Object} clientData - Dados atuais do cliente para pré-preenchimento.
 * @param {Function} onSuccess - Callback executado após o sucesso da atualização (ex: fechar modal).
 * @param {Function} onCancel - Callback para fechar o formulário sem guardar.
 */
const EditClientForm = ({ clientData, onSuccess, onCancel }) => {
  // Acedemos à Store global de Clientes para executar a atualização e monitorizar o estado de loading
  const { updateClient, loading } = useClientStore();
  const [error, setError] = useState(null);

  /**
   * ESTADO LOCAL DO FORMULÁRIO:
   * Inicializado com os dados atuais (clientData) para permitir uma edição imediata.
   * Usamos o operador '|| ""' para garantir que os campos nunca são 'undefined' (Controlled Components).
   */
  const [formData, setFormData] = useState({
    name: clientData.name || "",
    email: clientData.email || "",
    phone: clientData.phone || "",
    organization: clientData.organization || "",
  });

  /**
   * GESTOR DE ALTERAÇÕES:
   * Função genérica que atualiza o estado local conforme o utilizador escreve nos inputs.
   */
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  /**
   * SUBMISSÃO DO FORMULÁRIO:
   * Bloqueia o comportamento padrão do browser e invoca a lógica da Store.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // Chamada assíncrona à API através da Store
    const success = await updateClient(clientData.id, formData);

    if (success) {
      // Notifica o componente pai do sucesso para refrescar a interface
      onSuccess();
    } else {
      // Feedback visual em caso de falha na comunicação com o Backend (Critério: Tratamento de Erros)
      setError("Erro ao atualizar o cliente.");
    }
  };

  return (
      <Form onSubmit={handleSubmit}>
        {/* ALERTA DE ERRO: Exibido apenas se a operação falhar */}
        {error && <Alert variant="danger">{error}</Alert>}

        <Form.Group className="mb-3">
          <Form.Label>Nome Completo</Form.Label>
          <Form.Control
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              placeholder="Ex: João Silva"
          />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Email</Form.Label>
          <Form.Control
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              placeholder="exemplo@email.com"
          />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Telemóvel</Form.Label>
          <Form.Control
              name="phone"
              value={formData.phone}
              onChange={handleChange}
              required
          />
        </Form.Group>

        <Form.Group className="mb-4">
          <Form.Label>Organização</Form.Label>
          <Form.Control
              name="organization"
              value={formData.organization}
              onChange={handleChange}
              placeholder="Nome da empresa (opcional)"
          />
        </Form.Group>

        {/* BOTÕES DE ACÇÃO:
          O botão de submissão exibe um Spinner e fica desativado durante o loading
          para evitar cliques duplos e submissões repetidas.
      */}
        <div className="d-flex gap-2 justify-content-end border-top pt-3">
          <Button variant="outline-secondary" onClick={onCancel}>
            Cancelar
          </Button>
          <Button variant="primary" type="submit" disabled={loading}>
            {loading ? (
                <>
                  <Spinner size="sm" className="me-2" />
                  A guardar...
                </>
            ) : (
                "Guardar Alterações"
            )}
          </Button>
        </div>
      </Form>
  );
};

export default EditClientForm;