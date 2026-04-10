import React, {useState} from "react";
import {useParams, useNavigate} from "react-router-dom";
import {Container, Card, Button, Row, Col} from "react-bootstrap";
import {useLeadStore} from "../../stores/LeadsStore";
import {useUserStore} from "../../stores/UserStore";

// Importa os componentes do novo sistema de Modais
import DynamicModal from "../../Modal/DynamicModal.jsx";
import EditLeadForm from "./EditLeadForm";

/**
 * COMPONENTE: LeadDetails
 * -----------------------
 * DESCRIÇÃO: Página de detalhes de uma Lead específica.
 * FUNCIONALIDADES: Exibe informações completas e permite gerir o ciclo de vida
 * da lead (Editar/Apagar) através de um sistema de modais dinâmicos.
 */
const LeadDetails = () => {
    // Recuperamos o ID da lead diretamente do URL (Navegação com Rotas - 6%)
    const {id} = useParams();
    const navigate = useNavigate();

    // Integração com as Stores globais para persistência e verificação de permissões
    const {leads, deleteLead, fetchMyLeads} = useLeadStore();
    const userRole = useUserStore((state) => state.userRole);

    /**
     * ESTADO UNIFICADO PARA MODAIS:
     * Segue o padrão implementado no LeadsKanban.jsx para garantir consistência visual.
     * Centraliza o título, o tipo de ação e os dados no mesmo objeto de estado.
     */
    const [modalConfig, setModalConfig] = useState({
        show: false,
        title: "",
        type: null,
        data: null,
    });

    // Localizamos a lead específica dentro do array global da Store
    const lead = leads.find((l) => String(l.id) === String(id));

    // Fecha o modal e limpa a configuração mantendo a estrutura do objeto
    const closeModal = () => setModalConfig({...modalConfig, show: false});

    /**
     * ABERTURA DE MODAIS:
     * Preparam o estado para exibir o formulário de edição ou o aviso de eliminação.
     */
    const openEdit = () => {
        setModalConfig({
            show: true,
            title: "Editar Lead",
            type: "EDIT",
            data: lead,
        });
    };

    const openDelete = () => {
        setModalConfig({
            show: true,
            title: "Confirmar Eliminação",
            type: "DELETE",
            data: lead,
        });
    };

    /**
     * LÓGICA DE ELIMINAÇÃO:
     * Invoca a store para remover o registo na BD e redireciona o utilizador
     * para a listagem principal após o sucesso.
     */
    const handleConfirmDelete = async () => {
        const success = await deleteLead(id, userRole);
        if (success) {
            closeModal();
            navigate("/leads");
        }
    };

    // Fallback de segurança caso o ID no URL não corresponda a nenhuma lead carregada
    if (!lead)
        return (
            <Container className="mt-4">
                <p>Lead não encontrada.</p>
            </Container>
        );

    return (
        <Container className="mt-4">
            {/* Botão de retorno com ícone significativo (UX) */}
            <Button
                variant="link"
                onClick={() => navigate("/leads")}
                className="mb-3 p-0"
            >
                <i className="bi bi-arrow-left"></i> Voltar para a lista
            </Button>

            <Card className="shadow-sm border-0">
                <Card.Body className="p-4">
                    <h2 className="fw-bold mb-4">{lead.title}</h2>

                    <Row className="mb-4">
                        <Col md={12}>
                            <h5>Descrição</h5>
                            {/* Bloco de descrição com estilo diferenciado para leitura facilitada */}
                            <p className="bg-light p-3 rounded">
                                {lead.description || "Sem descrição."}
                            </p>
                            <p className="text-muted small">
                                Criada em: {lead.formattedDate}
                            </p>
                        </Col>
                    </Row>

                    {/* Botões de Ação de Gestão */}
                    <div className="d-flex gap-2 justify-content-end">
                        <Button variant="outline-primary" onClick={openEdit}>
                            <i className="bi bi-pencil"></i> Editar
                        </Button>
                        <Button variant="danger" onClick={openDelete}>
                            <i className="bi bi-trash"></i> Apagar
                        </Button>
                    </div>
                </Card.Body>
            </Card>

            {/* MODAL DINÂMICO REUTILIZADO:
                Garante que não duplicamos código de modais e mantemos a lógica
                de 'Content Mapping' baseada no modalConfig.type.
            */}
            <DynamicModal
                show={modalConfig.show}
                onHide={closeModal}
                title={modalConfig.title}
            >
                {/* Cenário de Edição: Injeta o formulário especializado */}
                {modalConfig.type === "EDIT" && (
                    <EditLeadForm
                        leadData={modalConfig.data}
                        onSuccess={() => {
                            // Sincronização: Forçamos o refresh da store para atualizar
                            // a vista de detalhes com os novos dados gravados.
                            fetchMyLeads(userRole);
                            closeModal();
                        }}
                        onCancel={closeModal}
                    />
                )}

                {/* Cenário de Eliminação: Diálogo de confirmação simples */}
                {modalConfig.type === "DELETE" && (
                    <div>
                        <p>
                            Tem a certeza que deseja apagar a lead{" "}
                            <strong>{lead.title}</strong>?
                        </p>
                        <div className="d-flex justify-content-end gap-2 mt-3">
                            <Button variant="secondary" onClick={closeModal}>
                                Cancelar
                            </Button>
                            <Button variant="danger" onClick={handleConfirmDelete}>
                                Confirmar
                            </Button>
                        </div>
                    </div>
                )}
            </DynamicModal>
        </Container>
    );
};

export default LeadDetails;