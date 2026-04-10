import React from "react";
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap";

/**
 * COMPONENTE: ActionButton
 * -----------------------
 * DESCRIÇÃO: Componente atómico e reutilizável para ações rápidas (ícones).
 * FUNCIONALIDADE: Padroniza o visual de todos os botões de ação do sistema,
 * incluindo suporte nativo para Tooltips (acessibilidade) e estados desativados.
 * * @param {string} icon - Nome da classe Bootstrap Icon (ex: 'bi-pencil').
 * @param {string} variant - Variante de cor do Bootstrap (ex: 'danger', 'primary').
 * @param {Function} onClick - Função a executar ao clicar.
 * @param {string} tooltip - Texto explicativo que aparece ao passar o rato.
 * @param {string} size - Tamanho do botão (padrão 'sm').
 * @param {boolean} disabled - Estado de interação do botão.
 */
const ActionButton = ({ icon, variant, onClick, tooltip, size = "sm", disabled = false }) => {

    // RENDERIZAÇÃO DO BOTÃO BASE:
    // Utilizamos 'outline-' para um visual mais limpo e moderno (Design Pattern).
    // Dimensões fixas garantem que todos os botões de ação tenham o mesmo alinhamento.
    const button = (
        <Button
            variant={`outline-${variant}`}
            size={size}
            onClick={onClick}
            disabled={disabled}
            className="p-1 d-flex align-items-center justify-content-center"
            style={{ width: "30px", height: "30px" }}
        >
            {/* Ícone centralizado com tamanho controlado para consistência visual */}
            <i className={`bi ${icon}`} style={{ fontSize: "0.9rem" }}></i>
        </Button>
    );

    // Se não for fornecido um tooltip, retorna apenas o botão (Otimização de DOM)
    if (!tooltip) return button;

    /**
     * ACESSIBILIDADE E UX:
     * O 'OverlayTrigger' envolve o botão para mostrar o 'Tooltip' no topo.
     * Melhora a experiência do utilizador ao explicar a função de ícones abstratos.
     */
    return (
        <OverlayTrigger
            placement="top"
            overlay={<Tooltip id={`tooltip-${icon}`}>{tooltip}</Tooltip>}
        >
            {button}
        </OverlayTrigger>
    );
};

export default ActionButton;