import React, { useState, useEffect } from "react";
import { Form } from "react-bootstrap";
import { userService } from "../../services/userService";

/**
 * COMPONENTE: AdminAssignmentField
 * -------------------------------
 * DESCRIÇÃO: Campo de seleção para atribuição de responsabilidade sobre um recurso.
 * REQUISITO: Cumpre a regra de gestão de leads/clientes por parte do Administrador.
 * @param {boolean} isAdmin - Flag de segurança que decide se o campo deve existir.
 * @param {string|number} value - ID do utilizador selecionado.
 * @param {Function} onChange - Função que atualiza o estado no componente pai (NewLead/NewClient).
 * @param {string} label - Texto descritivo do campo (opcional).
 */
const AdminAssignmentField = ({ isAdmin, value, onChange, label = "Atribuir Responsável" }) => {
    // Estado local para armazenar a lista de colaboradores vinda da base de dados
    const [users, setUsers] = useState([]);

    /**
     * EFEITO DE CARREGAMENTO (SIDE EFFECT):
     * O pedido à API só é efetuado se o utilizador for Administrador.
     * Isto poupa recursos de rede e evita erros de permissão (403 Forbidden)
     * para utilizadores comuns que não têm acesso à lista completa de staff.
     */
    useEffect(() => {
        if (isAdmin) {
            userService.getAllUsers()
                .then(setUsers)
                .catch((err) => console.error("Erro ao carregar utilizadores para atribuição:", err));
        }
    }, [isAdmin]);

    /** * GUARDA DE SEGURANÇA (RENDERIZAÇÃO CONDICIONAL):
     * Se o utilizador não for ADMIN, o componente não renderiza nada (null),
     * garantindo que a funcionalidade de delegação fica totalmente oculta.
     */
    if (!isAdmin) return null;

    return (
        <Form.Group className="mb-4 bg-light p-3 rounded border">
            {/* Rótulo com ícone significativo para facilitar a leitura visual */}
            <Form.Label className="fw-bold text-secondary">
                <i className="bi bi-person-badge me-2"></i>{label}
            </Form.Label>

            {/* SELECT DINÂMICO:
                - A opção por defeito (vazia) assume o utilizador atual no Backend.
                - Mapeia o array 'users' para gerar as opções com nome completo.
            */}
            <Form.Select
                value={value}
                onChange={(e) => onChange(e.target.value)}
            >
                <option value="">Atribuir a mim mesmo</option>
                {users.map((u) => (
                    <option key={u.id} value={u.id}>
                        {u.firstName} {u.lastName}
                    </option>
                ))}
            </Form.Select>
        </Form.Group>
    );
};

export default AdminAssignmentField;