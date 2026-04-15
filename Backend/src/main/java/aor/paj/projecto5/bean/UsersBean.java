package aor.paj.projecto5.bean;

import aor.paj.projecto5.dao.UserDao;
import aor.paj.projecto5.dto.LoginDTO;
import aor.paj.projecto5.dto.LoginResponseDTO;
import aor.paj.projecto5.dto.UserBaseDTO;
import aor.paj.projecto5.dto.UserDTO;
import aor.paj.projecto5.entity.ConfirmationTokenEntity;
import aor.paj.projecto5.entity.UserEntity;
import aor.paj.projecto5.utils.UserRoles;
import aor.paj.projecto5.utils.UserState;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise JavaBean (EJB) Stateless responsável pela lógica de negócio
 * e gestão de Utilizadores no sistema.
 * * Implementa um fluxo de pré-registo baseado em tokens de email e
 * utiliza métodos de mapeamento centralizados para garantir a integridade dos dados.
 */
@Stateless
public class UsersBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Inject
    private UserDao userDao;

    @Inject
    private TokenBean tokenBean;

    @Inject
    private ConfirmationTokenBean confirmationTokenBean;

    // =================================================================================
    // MÉTODOS DE MAPEAMENTO (DRY - DON'T REPEAT YOURSELF)
    // =================================================================================

    /**
     * SENTIDO: DB -> FRONTEND
     * Preenche os campos comuns a qualquer DTO (Base ou Completo) a partir de uma entidade.
     * Centraliza a conversão de nomes de campos (ex: contact -> cellphone).
     *
     * @param entity A entidade de origem vinda da base de dados.
     * @param dto O DTO de destino (pode ser UserBaseDTO ou UserDTO devido à herança).
     */
    private void fillBaseData(UserEntity entity, UserBaseDTO dto) {
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setCellphone(entity.getContact());
        dto.setPhotoUrl(entity.getPhoto());

        if (entity.getUserRole() != null) {
            dto.setRole(entity.getUserRole().name());
        }
        if (entity.getState() != null) {
            dto.setState(entity.getState().name());
        }
    }

    /**
     * SENTIDO: FRONTEND -> DB
     * Mapeia os dados recebidos de um DTO para uma entidade UserEntity.
     * Centraliza a atribuição de campos obrigatórios para evitar falhas de persistência.
     *
     * @param dto O DTO contendo os dados submetidos pelo utilizador.
     * @param entity A entidade alvo a ser preenchida/atualizada.
     */
    private void mapDtoToEntity(UserBaseDTO dto, UserEntity entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setContact(dto.getCellphone());
        entity.setPhoto(dto.getPhotoUrl());
        entity.setUsername(dto.getUsername());
    }

    // =================================================================================
    // CONVERSORES PÚBLICOS
    // =================================================================================

    /**
     * Converte uma UserEntity para UserBaseDTO (perfil público/listagens).
     *
     * @param entity A entidade a converter.
     * @return O DTO preenchido.
     */
    public UserBaseDTO convertToUserBaseDTO(UserEntity entity) {
        if (entity == null) return null;
        UserBaseDTO dto = new UserBaseDTO();
        fillBaseData(entity, dto);
        return dto;
    }

    /**
     * Converte uma UserEntity para UserDTO (inclui dados sensíveis como password).
     *
     * @param entity A entidade a converter.
     * @return O DTO completo preenchido.
     */
    public UserDTO convertToUserDTO(UserEntity entity) {
        if (entity == null) return null;
        UserDTO dto = new UserDTO();
        fillBaseData(entity, dto);
        dto.setPassword(entity.getPassword());
        return dto;
    }

    // =================================================================================
    // FLUXO DE REGISTO EM DOIS PASSOS (PRÉ-REGISTO)
    // =================================================================================

    /**
     * PASSO 1: Inicia o processo de registo validando o email e gerando um token.
     * Não cria o utilizador na base de dados nesta fase.
     *
     * @param email O email que o utilizador deseja registar.
     * @throws WebApplicationException 409 se o email já estiver associado a uma conta ativa.
     */
    public void requestRegistration(String email) {
        if (userDao.findUserByEmail(email) != null) {
            throw new WebApplicationException("Este email já se encontra registado.", 409);
        }

        // Cria token associado apenas ao email
        String token = confirmationTokenBean.createTokenForEmail(email);
    }

    /**
     * PASSO 2: Conclui o registo após a validação do token de email.
     * Cria a entidade UserEntity já com estado ACTIVE.
     *
     * @param tokenString O token recebido por email.
     * @param userDTO Os dados completos do formulário de registo.
     * @throws WebApplicationException Se o token for inválido, o email não coincidir ou o username já existir.
     */
    public void completeRegistration(String tokenString, UserDTO userDTO) {
        ConfirmationTokenEntity tokenEntity = confirmationTokenBean.validateToken(tokenString);

        if (tokenEntity == null) {
            throw new WebApplicationException("Token de confirmação inválido ou expirado.", 401);
        }

        if (!tokenEntity.getEmail().equalsIgnoreCase(userDTO.getEmail())) {
            throw new WebApplicationException("O email do formulário não corresponde ao email do convite.", 403);
        }

        if (userDao.findUserByUsername(userDTO.getUsername()) != null) {
            throw new WebApplicationException("O nome de utilizador já está em uso.", 409);
        }

        UserEntity newUser = new UserEntity();
        mapDtoToEntity(userDTO, newUser);
        newUser.setPassword(userDTO.getPassword());

        // Como o email foi validado pelo token, a conta nasce ACTIVE
        newUser.setState(UserState.ACTIVE);
        newUser.setUserRole(UserRoles.NORMAL);

        userDao.persist(newUser);

        // Remove o token para não ser reutilizado
        confirmationTokenBean.deleteToken(tokenEntity);
    }

    // =================================================================================
    // GESTÃO E CONSULTA DE PERFIS
    // =================================================================================

    // =================================================================================
    // MÉTODOS DE CONSULTA (LEITURA)
    // =================================================================================

    /**
     * Obtém os dados base (perfil público/admin) de um utilizador pelo seu ID.
     * Utilizado para listagens e visualização de perfis por administradores.
     *
     * @param id O identificador único do utilizador.
     * @return O DTO correspondente, ou null se a entidade não existir na base de dados.
     */
    public UserBaseDTO getUserBaseDTOById(Long id) {
        return convertToUserBaseDTO(userDao.find(id));
    }

    /**
     * Obtém os dados base de um utilizador pesquisando pelo seu username.
     *
     * @param username O nome de utilizador a procurar.
     * @return O DTO correspondente.
     * @throws WebApplicationException com status 404 se o utilizador não for encontrado.
     */
    public UserBaseDTO getUserBaseDTOByUsername(String username) {
        UserEntity entity = userDao.findUserByUsername(username);
        if (entity == null) {
            throw new WebApplicationException("Utilizador não encontrado", 404);
        }
        return convertToUserBaseDTO(entity);
    }

    /**
     * Obtém os dados completos (incluindo campos privados) do utilizador autenticado.
     * Este método é acedido através do token de sessão atual.
     *
     * @param token O token de sessão enviado no cabeçalho (header) do pedido.
     * @return O UserDTO completo pertencente ao dono do token.
     */
    public UserDTO getUserDTOByToken(String token) {
        UserEntity user = tokenBean.getUserEntityByToken(token);
        return convertToUserDTO(user);
    }

    /**
     * Edita os dados do próprio utilizador autenticado.
     *
     * @param token Token de sessão ativa.
     * @param userDTO Novos dados a aplicar.
     */
    public void putEditOwnUser(String token, UserDTO userDTO) {
        UserEntity user = tokenBean.getUserEntityByToken(token);
        if (user == null) throw new WebApplicationException("Sessão inválida", 401);

        UserEntity other = userDao.findUserByEmail(userDTO.getEmail());
        if (other != null && !other.getId().equals(user.getId())) {
            throw new WebApplicationException("Email já em uso por outro utilizador.", 409);
        }

        mapDtoToEntity(userDTO, user);
        user.setPassword(userDTO.getPassword());
    }

    /**
     * Edição administrativa de qualquer utilizador.
     * Permite alteração de Role e de State diretamente.
     *
     * @param id ID do utilizador alvo.
     * @param dto Novos dados e metadados.
     */
    public void putEditUser(Long id, UserBaseDTO dto) {
        UserEntity user = userDao.find(id);
        if (user == null) throw new WebApplicationException("Utilizador não encontrado", 404);

        UserEntity other = userDao.findUserByEmail(dto.getEmail());
        if (other != null && !other.getId().equals(id)) {
            throw new WebApplicationException("Email já associado a outra conta.", 409);
        }

        mapDtoToEntity(dto, user);

        if (dto.getRole() != null) {
            user.setUserRole(UserRoles.valueOf(dto.getRole()));
        }
        if (dto.getState() != null) {
            user.setState(UserState.valueOf(dto.getState()));
        }
    }

    /**
     * Desativa logicamente um utilizador (Estado DISABLED).
     *
     * @param id ID do utilizador.
     */
    public void softDeleteUser(Long id) {
        UserEntity user = userDao.find(id);
        if (user == null) throw new WebApplicationException("Não encontrado", 404);
        user.setState(UserState.DISABLED);
    }

    /**
     * Reativa um utilizador (Estado ACTIVE).
     *
     * @param id ID do utilizador.
     */
    public void softUnDeleteUser(Long id) {
        UserEntity user = userDao.find(id);
        if (user == null) throw new WebApplicationException("Não encontrado", 404);
        user.setState(UserState.ACTIVE);
    }

    /**
     * Remove permanentemente o utilizador e transfere os seus dados para o utilizador de sistema.
     *
     * @param id ID do utilizador a remover fisicamente.
     */
    public void deleteUser(Long id) {
        UserEntity userToDelete = userDao.find(id);
        if (userToDelete == null) throw new WebApplicationException("Não encontrado", 404);

        UserEntity systemUser = userDao.findUserByUsername("deleted_user");
        userDao.transferOwnership(userToDelete, systemUser);
        userDao.hardDelete(id);
    }

    /**
     * Autentica o utilizador verificando credenciais e se o estado é ACTIVE.
     *
     * @param loginDTO Credenciais (username e password).
     * @return LoginResponseDTO com token se sucesso; null caso contrário.
     */
    public LoginResponseDTO authenticateUser(LoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getUsername() == null) return null;

        UserEntity userEntity = userDao.findUserByUsername(loginDTO.getUsername());

        if (userEntity != null &&
                userEntity.getPassword().equals(loginDTO.getPassword()) &&
                userEntity.getState() == UserState.ACTIVE) {

            String token = tokenBean.generateNewToken(userEntity);

            return new LoginResponseDTO(
                    userEntity.getId(),
                    userEntity.getFirstName(),
                    userEntity.getUserRole(),
                    token,
                    userEntity.getPhoto()
            );
        }
        return null;
    }

    /**
     * Obtém a lista completa de utilizadores ativos e inativos, exceto o utilizador de sistema.
     *
     * @return Lista de UserBaseDTO.
     */
    public List<UserBaseDTO> getAllUsers() {
        List<UserEntity> entities = userDao.findAll();
        List<UserBaseDTO> result = new ArrayList<>();

        for (UserEntity u : entities) {
            if (!u.getUsername().equals("deleted_user")) {
                result.add(convertToUserBaseDTO(u));
            }
        }
        return result;
    }

    // =================================================================================
    // RECUPERAÇÃO DE PASSWORD
    // =================================================================================

    /**
     * PASSO 1: Inicia o processo de recuperação de password.
     * Gera um token de segurança associado ao email do utilizador.
     *
     * @param email O email da conta a recuperar.
     */
    public void requestPasswordReset(String email) {
        UserEntity user = userDao.findUserByEmail(email);

        // Segurança: Se o email não existir, não dizemos "Erro".
        // Dizemos apenas que o email foi enviado para evitar que hackers saibam quem tem conta.
        if (user != null && user.getState() == UserState.ACTIVE) {
            String token = confirmationTokenBean.createTokenForEmail(email);
            System.out.println("DEBUG: Link de Recuperação para " + email + " -> http://localhost:3000/reset-password?token=" + token);
        }
    }

    /**
     * PASSO 2: Define uma nova password usando o token de validação.
     *
     * @param tokenString O token recebido por email.
     * @param newPassword A nova password escolhida pelo utilizador.
     * @throws WebApplicationException 401 se o token for inválido/expirado.
     */
    public void resetPassword(String tokenString, String newPassword) {
        // 1. Valida o token
        ConfirmationTokenEntity tokenEntity = confirmationTokenBean.validateToken(tokenString);
        if (tokenEntity == null) {
            throw new WebApplicationException("O link de recuperação é inválido ou já expirou.", 401);
        }

        // 2. Encontra o utilizador pelo email guardado no token
        UserEntity user = userDao.findUserByEmail(tokenEntity.getEmail());
        if (user == null) {
            throw new WebApplicationException("Utilizador não encontrado.", 404);
        }

        // 3. Atualiza a password
        user.setPassword(newPassword);
        userDao.merge(user);

        // 4. Limpa o token para não ser usado duas vezes
        confirmationTokenBean.deleteToken(tokenEntity);
    }
}