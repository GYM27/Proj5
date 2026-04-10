// import React, { useState } from "react";
// import { Form, Button, Alert, Spinner } from "react-bootstrap";
// import { useLeadStore } from "../../stores/LeadsStore";
// import { useUserStore } from "../../stores/UserStore";
//
// const NewLeadForm = ({
//   targetUserId,
//   onSuccess,
//   onCancel,
//   initialState = 1,
// }) => {
//   const [leadData, setLeadData] = useState({
//     title: "",
//     description: "",
//     state: initialState, // Usa o estado da coluna onde clicaste
//   });
//
//   const [error, setError] = useState(null);
//   const { addLead, loading } = useLeadStore();
//   const userRole = useUserStore((state) => state.userRole);
//
//   const handleChange = (e) => {
//     const { name, value } = e.target;
//     setLeadData((prev) => ({
//       ...prev,
//       [name]: name === "state" ? parseInt(value) : value,
//     }));
//   };
//
//   const handleSubmit = async (e) => {
//     e.preventDefault();
//     setError(null);
//
//     // Se o admin estiver a ver um user específico, targetUserId já vem preenchido
//     const success = await addLead(leadData, userRole, targetUserId);
//
//     if (success) {
//       onSuccess(); // Fecha o modal e atualiza a lista
//     } else {
//       setError("Erro ao guardar os dados da lead.");
//     }
//   };
//
//   return (
//     <Form onSubmit={handleSubmit}>
//       {error && <Alert variant="danger">{error}</Alert>}
//
//       <Form.Group className="mb-3">
//         <Form.Label>Título da Oportunidade</Form.Label>
//         <Form.Control
//           type="text"
//           name="title"
//           placeholder="Ex: Software de Gestão"
//           value={leadData.title}
//           onChange={handleChange}
//           required
//         />
//       </Form.Group>
//
//       <Form.Group className="mb-3">
//         <Form.Label>Descrição Detalhada</Form.Label>
//         <Form.Control
//           as="textarea"
//           rows={3}
//           name="description"
//           placeholder="Notas sobre o contacto..."
//           value={leadData.description}
//           onChange={handleChange}
//         />
//       </Form.Group>
//
//       <Form.Group className="mb-4">
//         <Form.Label>Estado Inicial</Form.Label>
//         <Form.Select
//           name="state"
//           value={leadData.state}
//           onChange={handleChange}
//         >
//           <option value={1}>Novo</option>
//           <option value={2}>Em Análise</option>
//           <option value={3}>Proposta</option>
//           <option value={4}>Ganho</option>
//           <option value={5}>Perdido</option>
//         </Form.Select>
//       </Form.Group>
//
//       <div className="d-flex gap-2 justify-content-end">
//         <Button variant="outline-secondary" onClick={onCancel}>
//           Cancelar
//         </Button>
//         <Button variant="primary" type="submit" disabled={loading}>
//           {loading ? <Spinner size="sm" /> : "Gravar Lead"}
//         </Button>
//       </div>
//     </Form>
//   );
// };
//
// export default NewLeadForm;
