package aor.paj.projecto5.bean;

import aor.paj.projecto5.dto.LoginDTO;
import aor.paj.projecto5.dto.LoginResponseDTO;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import aor.paj.projecto5.dao.UserDao;
import aor.paj.projecto5.dto.UserBaseDTO;
import aor.paj.projecto5.dto.UserDTO;
import aor.paj.projecto5.entity.UserEntity;
import jakarta.ws.rs.WebApplicationException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise JavaBean (EJB) Stateless responsável pela lógica de negócio
 * e gestão de Utilizadores (Users).
 * Contém métodos para registo, edição, autenticação e listagem de utilizadores.
 */
@Stateless
public class UsersBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Data Access Object (DAO) para operações na base de dados relacionadas com a entidade UserEntity.
     */
    @Inject
    UserDao userDao;

    /**
     * Bean responsável pela criação, validação e gestão de tokens de autenticação.
     */
    @Inject
    TokenBean tokenBean;

    /**
     * Método auxiliar para mapear dados de um Data Transfer Object (DTO) para uma Entidade.
     * Atualiza apenas os campos gerais de perfil (nome, email, contacto, foto).
     * O username nunca é alterado por este método; o Role e SoftDelete são tratados separadamente.
     *
     * @param dto O DTO contendo os novos dados do utilizador.
     * @param entity A entidade a ser atualizada.
     */
    private void mapDtoToEntity(UserBaseDTO dto, UserEntity entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setContact(dto.getCellphone());
        entity.setPhoto(dto.getPhotoUrl());

    }

    /**
     * Converte uma entidade UserEntity para o seu formato UserBaseDTO.
     * Este DTO é seguro para listagens e perfis públicos, pois omite dados sensíveis como a password.
     *
     * @param entity A entidade {@link UserEntity} a converter.
     * @return O {@link UserBaseDTO} correspondente, ou null se a entidade for null.
     */
    public UserBaseDTO convertToUserBaseDTO(UserEntity entity) {
        if (entity == null) return null;
        UserBaseDTO dto = new UserBaseDTO();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setCellphone(entity.getContact());
        dto.setPhotoUrl(entity.getPhoto());
        dto.setSoftDelete(entity.isSoftDelete());
        if (entity.getUserRole() != null) {
            dto.setRole(entity.getUserRole().name());
        }
        return dto;
    }

    /**
     * Converte uma entidade UserEntity para o seu formato completo UserDTO.
     * Este DTO inclui campos privados (username e password), sendo adequado para
     * operações no próprio perfil ou fluxos internos de autenticação.
     *
     * @param entity A entidade {@link UserEntity} a converter.
     * @return O {@link UserDTO} correspondente, ou null se a entidade for null.
     */
    public UserDTO convertToUserDTO(UserEntity entity) {
        if (entity == null) return null;
        UserDTO dto = new UserDTO();
        // Preenche campos base
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setCellphone(entity.getContact());
        dto.setPhotoUrl(entity.getPhoto());
        dto.setSoftDelete(entity.isSoftDelete());
        dto.setRole(entity.getUserRole() != null ? entity.getUserRole().name() : null);
        // Campos privados
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        return dto;
    }

    // --- MÉTODOS DE CONSULTA ---

    /**
     * Obtém os dados completos (UserDTO) do utilizador atualmente autenticado através do seu token.
     *
     * @param token O token de autenticação fornecido no cabeçalho da requisição.
     * @return O {@link UserDTO} correspondente ao utilizador autenticado.
     */
    public UserDTO getUserDTOByToken(String token) {
        UserEntity user = tokenBean.getUserEntityByToken(token);
        return convertToUserDTO(user);
    }

    /**
     * Obtém os dados básicos (UserBaseDTO) de um utilizador através do seu ID.
     *
     * @param id O identificador único do utilizador.
     * @return O {@link UserBaseDTO} correspondente.
     */
    public UserBaseDTO getUserBaseDTOById(Long id) {
        return convertToUserBaseDTO(userDao.find(id));
    }

    /**
     * Procura um utilizador pelo seu username.
     * Necessário para carregar a página de perfil quando o utilizador acede através do URL partilhável.
     *
     * @param username O nome de utilizador a pesquisar.
     * @return O DTO do utilizador encontrado.
     * @throws WebApplicationException Se o utilizador não existir (Erro 404).
     */
    public UserBaseDTO getUserBaseDTOByUsername(String username) {
        UserEntity entity = userDao.findUserByUsername(username);
        if (entity == null) {
            throw new WebApplicationException("Utilizador não encontrado", 404);
        }
        return convertToUserBaseDTO(entity);
    }

    // --- GESTÃO DE UTILIZADORES ---

    /**
     * Regista um novo utilizador no sistema.
     * Valida a unicidade do username e do email antes de persistir a entidade.
     *
     * @param userDTO O DTO contendo os dados do novo utilizador.
     * @throws WebApplicationException Se o username ou email já estiverem em uso (HTTP 409).
     */
    public void registerUser(UserDTO userDTO) {
        if (userDao.findUserByUsername(userDTO.getUsername()) != null)
            throw new WebApplicationException("Username já existe.", 409);

        if (userDao.findUserByEmail(userDTO.getEmail()) != null)
            throw new WebApplicationException("Email já registado.", 409);

        UserEntity newUser = new UserEntity();
        mapDtoToEntity(userDTO, newUser);
        newUser.setUsername(userDTO.getUsername());
        newUser.setPassword(userDTO.getPassword());
        userDao.persist(newUser);
    }

    /**
     * Permite que um utilizador autenticado edite o seu próprio perfil.
     * Valida se o novo email inserido não pertence já a outra conta existente.
     *
     * @param token O token do utilizador que faz o pedido.
     * @param userDTO Os novos dados a aplicar ao perfil.
     * @throws WebApplicationException Se o utilizador não for encontrado (404) ou o email já estiver em uso por outro (409).
     */
    public void putEditOwnUser(String token, UserDTO userDTO) {
        UserEntity user = tokenBean.getUserEntityByToken(token);
        if (user == null) throw new WebApplicationException("User não encontrado", 404);

        // Valida se o novo email já pertence a outro ID
        UserEntity other = userDao.findUserByEmail(userDTO.getEmail());
        if (other != null && !other.getId().equals(user.getId()))
            throw new WebApplicationException("Email em uso.", 409);

        mapDtoToEntity(userDTO, user);
        user.setPassword(userDTO.getPassword());
    }

    /**
     * Permite que um Administrador edite o perfil de qualquer utilizador no sistema.
     * Permite a alteração do nível de acesso (Role) do utilizador.
     *
     * @param id O identificador do utilizador a editar.
     * @param dto Os novos dados do utilizador (incluindo potencialmente um novo role).
     * @throws WebApplicationException Se o utilizador não for encontrado (404) ou o email já estiver em uso (409).
     */
    public void putEditUser(Long id, UserBaseDTO dto) {
        UserEntity user = userDao.find(id);
        if (user == null) throw new WebApplicationException("Utilizador não encontrado", 404);

        // Valida email duplicado (excluindo o próprio user)
        UserEntity other = userDao.findUserByEmail(dto.getEmail());
        if (other != null && !other.getId().equals(id))
            throw new WebApplicationException("Email já associado a outra conta.", 409);

        mapDtoToEntity(dto, user);
        // O Admin também pode alterar o Role se necessário
        if (dto.getRole() != null) {
            user.setUserRole(aor.paj.projecto5.utils.UserRoles.valueOf(dto.getRole()));
        }
    }

    /**
     * Desativa um utilizador (Soft Delete), impedindo o seu login sem remover o seu histórico de dados.
     *
     * @param id O ID do utilizador a desativar.
     * @throws WebApplicationException Se o utilizador não for encontrado (HTTP 404).
     */
    public void softDeleteUser(Long id) {
        UserEntity user = userDao.find(id);
        if (user == null) throw new WebApplicationException("Não encontrado", 404);
        user.setSoftDelete(true);
    }

    /**
     * Reativa um utilizador previamente desativado (Remove o Soft Delete).
     *
     * @param id O ID do utilizador a reativar.
     * @throws WebApplicationException Se o utilizador não for encontrado (HTTP 404).
     */
    public void softUnDeleteUser(Long id) {
        UserEntity user = userDao.find(id);
        if (user == null) throw new WebApplicationException("Não encontrado", 404);
        user.setSoftDelete(false);
    }

    /**
     * Remove permanentemente um utilizador do sistema (Hard Delete).
     * Para manter a integridade referencial, todos os registos associados a este utilizador
     * são transferidos para um utilizador de sistema estático denominado "deleted_user".
     *
     * @param id O ID do utilizador a ser removido fisicamente.
     * @throws WebApplicationException Se o utilizador alvo não for encontrado ou se o utilizador de sistema falhar.
     */
    public void deleteUser(Long id) {
        UserEntity userToDelete = userDao.find(id);
        if (userToDelete == null) throw new WebApplicationException("Não encontrado", 404);

        UserEntity systemUser = userDao.findUserByUsername("deleted_user");
        if (systemUser == null) throw new WebApplicationException("Erro: deleted_user não existe.", 500);

        userDao.transferOwnership(userToDelete, systemUser);
        userDao.hardDelete(id);
    }

    /**
     * Obtém uma lista de todos os utilizadores registados no sistema.
     * Oculta o utilizador técnico "deleted_user" para não aparecer em listagens e dashboards.
     *
     * @return Uma lista de objetos {@link UserBaseDTO} correspondentes aos utilizadores.
     */
    public List<UserBaseDTO> getAllUsers() {
        List<UserEntity> entities = userDao.findAll();
        List<UserBaseDTO> result = new ArrayList<>();

        for (UserEntity u : entities) {
            // Filtra o utilizador de sistema
            if (!u.getUsername().equals("deleted_user")) {
                result.add(convertToUserBaseDTO(u));
            }
        }
        return result;
    }

    /**
     * Autentica um utilizador verificando as credenciais e o estado da conta.
     * Garante que o utilizador existe, a password coincide e que a conta não se encontra desativada.
     * Se a autenticação tiver sucesso, gera e devolve um novo token.
     *
     * @param loginDTO Dados vindos do formulário de login (username e password).
     * @return Um {@link LoginResponseDTO} contendo os dados base do utilizador e o token gerado,
     * ou null caso as credenciais sejam inválidas ou a conta inativa.
     */
    public LoginResponseDTO authenticateUser(LoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getUsername() == null) {
            return null;
        }

        // 1. Procura o utilizador pelo username
        UserEntity userEntity = userDao.findUserByUsername(loginDTO.getUsername());

        // 2. Validação de Segurança:
        // - O utilizador tem de existir
        // - A password tem de coincidir (estás a usar plain text por agora)
        // - O utilizador NÃO pode estar em softDelete (inativo)
        if (userEntity != null &&
                userEntity.getPassword().equals(loginDTO.getPassword()) &&
                !userEntity.isSoftDelete()) {

            // 3. Se tudo estiver OK, geramos o Token
            String token = tokenBean.generateNewToken(userEntity);

            // 4. Montamos a resposta para o React
            // CORREÇÃO: Passamos também a foto da entidade para o DTO
            return new LoginResponseDTO(
                    userEntity.getId(),
                    userEntity.getFirstName(),
                    userEntity.getUserRole(),
                    token,
                    userEntity.getPhoto() //
            );
        }

        // Se falhar qualquer condição, retornamos null (o LoginBean tratará do erro 401)
        return null;
    }
}