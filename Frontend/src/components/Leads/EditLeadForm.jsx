import React, { useState } from "react";
import { Form, Button, Spinner } from "react-bootstrap";
import { useLeadStore } from "../../stores/LeadsStore";
import { useUserStore } from "../../stores/UserStore";

/**
 * COMPONENTE: EditLeadForm
 * -----------------------
 * DESCRIÇÃO: Formulário para edição de uma Lead existente dentro de um Modal.
 * @param {Object} initialData - Dados da lead recebidos do pai (renomeado para clareza).
 * @param {Function} onSuccess - Callback para fechar o modal e atualizar a lista após sucesso.
 * @param {Function} onCancel - Callback para fechar o modal sem alterações.
 */
const EditLeadForm = ({ leadData: initialData, onSuccess, onCancel }) => {
  // Acedemos às Stores para persistência de dados e verificação de permissões (userRole)
  const { updateLead, loading } = useLeadStore();
  const userRole = useUserStore((state) => state.userRole);

  /**
   * ESTADO LOCAL:
   * Inicializado diretamente com as props (initialData).
   * Nota: O useEffect é dispensável aqui pois o componente é montado
   * já com os dados disponíveis, otimizando a performance.
   */
  const [leadData, setLeadData] = useState({
    title: initialData.title || "",
    description: initialData.description || "",
    state: initialData.state || 1, // Valor por defeito: Estado 1 (Novo)
  });

  /**
   * MANIPULAÇÃO DE INPUTS:
   * Garante a tipagem correta dos dados. O campo 'state' é convertido
   * para Inteiro para coincidir com os Enums do Backend Java.
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setLeadData((prev) => ({
      ...prev,
      [name]: name === "state" ? parseInt(value, 10) : value,
    }));
  };

  /**
   * SUBMISSÃO DO FORMULÁRIO:
   * Invoca a store para atualizar a lead na base de dados PostgreSQL.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Enviamos o ID original, os novos dados e a role do utilizador para validação no backend
    const success = await updateLead(initialData.id, leadData, userRole);

    if (success) {
      // Se a API retornar sucesso, executamos o callback para fechar a interface de edição
      onSuccess();
    }
  };

  return (
      <Form onSubmit={handleSubmit}>
        {/* Título da Lead: Campo obrigatório para identificação rápida no Kanban */}
        <Form.Group className="mb-3">
          <Form.Label>Título</Form.Label>
          <Form.Control
              name="title"
              value={leadData.title}
              onChange={handleChange}
              required
          />
        </Form.Group>

        {/* Descrição: Campo de texto longo para detalhes da oportunidade de negócio */}
        <Form.Group className="mb-3">
          <Form.Label>Descrição</Form.Label>
          <Form.Control
              as="textarea"
              rows={3}
              name="description"
              value={leadData.description}
              onChange={handleChange}
          />
        </Form.Group>

        {/* ESTADO DA LEAD (MAGIC CONSTANTS):
          Estes valores (1 a 5) mapeiam diretamente para os estados no Servidor.
          Manteve-se a estrutura para garantir compatibilidade com o Kanban.
      */}
        <Form.Group className="mb-4">
          <Form.Label>Estado</Form.Label>
          <Form.Select
              name="state"
              value={leadData.state}
              onChange={handleChange}
          >
            <option value={1}>Novo</option>
            <option value={2}>Em Análise</option>
            <option value={3}>Proposta</option>
            <option value={4}>Ganho</option>
            <option value={5}>Perdido</option>
          </Form.Select>
        </Form.Group>

        {/* BOTÕES DE ACÇÃO:
          O botão principal desativa-se durante o 'loading' para prevenir
          múltiplas submissões da mesma lead (Idempotência no Frontend).
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

export default EditLeadForm;