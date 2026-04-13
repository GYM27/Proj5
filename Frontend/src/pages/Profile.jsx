import React from "react";
import { useParams } from "react-router-dom";
import { Container, Card, Spinner, Row, Col } from "react-bootstrap";
import { useUserStore } from "../stores/UserStore";

import { useProfileManager } from "../components/Profile/useProfileManager.jsx";
import DynamicModal from "../Modal/DynamicModal.jsx";
import ConfirmActionContent from "../Modal/ConfirmActionContent.jsx";
import ProfilePhoto from "../components/Profile/ProfilePhoto";
import ProfileForm from "../components/Profile/ProfileForm";
import AdminActions from "../components/Profile/AdminActions";

/**
 * Componente responsável por renderizar a página de Perfil.
 * Suporta dois modos de visualização baseados no URL:
 * 1. O Meu Perfil: Quando não há parâmetro no URL (ex: rota /profile).
 * 2. Perfil de Terceiros: Quando o URL inclui um username (ex: rota /users/:username).
 * * Permite a visualização e edição dos dados (através do ProfileForm) e, caso o
 * utilizador autenticado seja um Administrador a visualizar o perfil de outra pessoa,
 * apresenta opções de gestão da conta (AdminActions).
 * * @returns {JSX.Element} A estrutura JSX que compõe a interface gráfica do perfil.
 */
const Profile = () => {
  /**
   * Lê o parâmetro dinâmico da rota atual.
   * Exemplo: Se o URL for '/users/maria', o valor de username será 'maria'.
   */
  const { username } = useParams();

  /**
   * Determina a lógica de visualização. Se existir um 'username' na rota,
   * o utilizador está a visitar o perfil de outra pessoa. Caso contrário,
   * está a aceder ao seu próprio perfil.
   */
  const targetUsername = username;
  const isOwnProfile = !targetUsername;

  /**
   * Extrai o nível de permissões do utilizador atualmente autenticado
   * a partir da store global para gerir a visibilidade do bloco de administração.
   */
  const { userRole } = useUserStore();
  const isAdmin = userRole === "ADMIN";

  /**
   * Hook customizado que concentra toda a lógica de negócio do perfil,
   * desde chamadas à API (fetching de dados) até à gestão dos estados do formulário e modais.
   */
  const {
    formData,
    loading,
    modalConfig,
    openModal,
    closeModal,
    handleConfirmAction,
    handleChange,
    handleSubmit
  } = useProfileManager(targetUsername, isOwnProfile);

  // Renderiza um ecrã de carregamento com Spinner enquanto os dados da API não são resolvidos
  if (loading) {
    return (
        <Container className="d-flex flex-column justify-content-center align-items-center" style={{ minHeight: "60vh" }}>
          <Spinner animation="grow" variant="primary" className="mb-3" />
          <h5 className="text-muted fw-light">A preparar o perfil...</h5>
        </Container>
    );
  }

  return (
      <Container className="py-5">
        <Row className="justify-content-center">
          <Col md={10} lg={8} xl={7}>
            <Card className="shadow border-0 rounded-4 overflow-hidden">

              <div className="bg-light pt-5 pb-4 px-4 text-center border-bottom">
                <ProfilePhoto photoUrl={formData?.photoUrl} firstName={formData?.firstName} lastName={formData?.lastName} />
                <h2 className="fw-bolder mt-3 text-dark mb-1">
                  {isOwnProfile ? "O Meu Perfil" : `Perfil de ${formData?.firstName}`}
                </h2>
                <p className="text-muted mb-0">{formData?.email}</p>

                {/* Feedback visual de segurança exibido se a conta estiver sob um "Soft Delete" */}
                {formData?.softDelete && (
                    <span className="badge bg-danger bg-gradient rounded-pill px-4 py-2 mt-3 shadow-sm">
                         <i className="bi bi-exclamation-triangle-fill me-2"></i> CONTA DESATIVADA
                      </span>
                )}
              </div>

              <Card.Body className="p-4 p-md-5">

                {/* Formulário genérico que apresenta os inputs com os dados do utilizador */}
                <ProfileForm
                    formData={formData}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    isOwnProfile={isOwnProfile}
                    loading={loading}
                />

                {/* Renderização Condicional: Bloco de Administração.
                  Visível apenas se o visualizador for ADMIN e NÃO estiver na sua própria página.
                */}
                {!isOwnProfile && isAdmin && (
                    <div className="mt-5 p-4 bg-light border rounded-4">
                      <h5 className="text-danger fw-bold mb-3 d-flex align-items-center">
                        <i className="bi bi-shield-lock-fill me-2"></i> Área de Administração
                      </h5>
                      <p className="text-muted small mb-4">
                        Tem cuidado. As ações abaixo afetam as permissões de acesso deste utilizador no sistema.
                      </p>
                      <AdminActions
                          isDeleted={formData?.softDelete}
                          onToggleStatus={() => openModal("USER_TOGGLE_STATUS", "Alterar Estado da Conta", formData)}
                          onHardDelete={() => openModal("USER_HARD_DELETE", "Eliminação Permanente de Utilizador", formData)}
                      />
                    </div>
                )}

              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Modal global da página utilizado para pedir confirmações críticas (ex: eliminar conta) */}
        <DynamicModal show={modalConfig.show} onHide={closeModal} title={modalConfig.title}>
          <ConfirmActionContent
              type={modalConfig.type}
              data={modalConfig.data}
              onCancel={closeModal}
              onConfirm={handleConfirmAction}
          />
        </DynamicModal>
      </Container>
  );
};

export default Profile;