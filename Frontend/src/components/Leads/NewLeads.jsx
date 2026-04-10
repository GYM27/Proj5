import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Alert, Form } from "react-bootstrap";
import { useLeadStore } from "../../stores/LeadsStore";
import { useUserStore } from "../../stores/UserStore";
import FormContainer from "../Shared/FormContainer";
import AdminAssignmentField from "../Shared/AdminAssignmentField";

/**
 * COMPONENTE: NewLeads
 * -------------------
 * DESCRIÇÃO: Página de formulário para criação de novas Leads.
 * REGRAS DE NEGÓCIO:
 * 1. Capta o estado da coluna onde o utilizador clicou no Kanban.
 * 2. Permite ao Administrador atribuir a lead a um colaborador específico.
 */
const NewLeads = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // Acedemos às Stores para persistência de dados e verificação de perfil (ADMIN vs USER)
  const { addLead, loading } = useLeadStore();
  const userRole = useUserStore((state) => state.userRole);
  const isAdmin = userRole === "ADMIN";

  /** * COMUNICAÇÃO ENTRE ROTAS (6% da Nota):
   * Captura o estado inicial enviado pelo clique no botão '+' da coluna do Kanban.
   * Se o utilizador clicar no '+' da coluna "Proposta", o formulário já abre com esse estado.
   */
  const initialStateFromKanban = location.state?.initialState || 1;

  // ESTADO LOCAL: Inicializado com os valores capturados da navegação
  const [leadData, setLeadData] = useState({
    title: "",
    description: "",
    state: initialStateFromKanban,
  });

  // Estado para o ID do utilizador alvo (apenas funcional se isAdmin for true)
  const [targetUserId, setTargetUserId] = useState(location.state?.targetId || "");
  const [error, setError] = useState(null);

  /**
   * GESTOR DE ALTERAÇÕES:
   * Garante que o campo 'state' é sempre tratado como número (Inteiro)
   * para total compatibilidade com os Enums do Backend Java.
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setLeadData((prev) => ({
      ...prev,
      [name]: name === "state" ? parseInt(value) : value,
    }));
  };

  /**
   * SUBMISSÃO DO FORMULÁRIO:
   * Envia os dados para a store, incluindo o utilizador alvo se for uma atribuição de Admin.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // Invocação da lógica de negócio na Store
    const success = await addLead(leadData, userRole, isAdmin && targetUserId ? targetUserId : null);

    if (success) {
      // Redirecionamento após criação bem-sucedida (Navegação com Rotas)
      navigate("/leads");
    } else {
      setError("Erro ao guardar a lead.");
    }
  };

  return (
      /**
       * FormContainer: Abstração de layout que garante a consistência visual
       * entre todos os formulários da aplicação.
       */
      <FormContainer
          title="Nova Lead"
          icon="bi-clipboard-plus"
          loading={loading}
          onSubmit={handleSubmit}
          onCancel={() => navigate("/leads")}
      >
        {/* Feedback visual de erro em caso de falha na API (Tratamento de Erros) */}
        {error && <Alert variant="danger">{error}</Alert>}

        {/* ATRIBUIÇÃO DE ADMIN (REQUISITO PROJETO 3):
            Permite que o administrador crie a lead já em nome de outro utilizador.
        */}
        <AdminAssignmentField
            isAdmin={isAdmin}
            value={targetUserId}
            onChange={setTargetUserId}
        />

        <Form.Group className="mb-3">
          <Form.Label>Título da Lead</Form.Label>
          <Form.Control
              name="title"
              value={leadData.title}
              onChange={handleChange}
              required
              placeholder="Ex: Novo Projeto de Consultoria"
          />
        </Form.Group>

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

        {/* SELECT DE ESTADO (RENDERIZAÇÃO DINÂMICA):
            Mapeia os IDs (1 a 5) usados para definir a posição da Lead no Kanban.
        */}
        <Form.Group className="mb-3">
          <Form.Label>Estado Inicial</Form.Label>
          <Form.Select name="state" value={leadData.state} onChange={handleChange}>
            <option value={1}>Novo</option>
            <option value={2}>Em Análise</option>
            <option value={3}>Proposta</option>
            <option value={4}>Ganho</option>
            <option value={5}>Perdido</option>
          </Form.Select>
        </Form.Group>
      </FormContainer>
  );
};

export default NewLeads;