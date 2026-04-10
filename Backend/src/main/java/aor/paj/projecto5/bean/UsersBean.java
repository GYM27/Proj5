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

@Stateless
public class UsersBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Inject
    UserDao userDao;
    @Inject
    TokenBean tokenBean;

    /**
     * Helper: Mapeia dados do DTO para a Entidade (Campos Comuns).
     */
    private void mapDtoToEntity(UserBaseDTO dto, UserEntity entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setContact(dto.getCellphone());
        entity.setPhoto(dto.getPhotoUrl());
        // Username nunca é alterado; Role e SoftDelete são tratados em métodos específicos
    }

    /**
     * Converte Entidade para UserBaseDTO (Seguro para Listagens).
     */
    public UserBaseDTO convertToUserBaseDTO(UserEntity entity) {
        if (entity == null) return null;
        UserBaseDTO dto = new UserBaseDTO();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
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
     * Converte Entidade para UserDTO (Completo para o Perfil "Me").
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

    public UserDTO getUserDTOByToken(String token) {
        UserEntity user = tokenBean.getUserEntityByToken(token);
        return convertToUserDTO(user);
    }

    public UserBaseDTO getUserBaseDTOById(Long id) {
        return convertToUserBaseDTO(userDao.find(id));
    }

    // --- GESTÃO DE UTILIZADORES ---

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
     * Edição pelo próprio utilizador.
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
     * Edição feita pelo ADMIN sobre qualquer utilizador.
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

    public void softDeleteUser(Long id) {
        UserEntity user = userDao.find(id);
        if (user == null) throw new WebApplicationException("Não encontrado", 404);
        user.setSoftDelete(true);
    }

    public void softUnDeleteUser(Long id) {
        UserEntity user = userDao.find(id);
        if (user == null) throw new WebApplicationException("Não encontrado", 404);
        user.setSoftDelete(false);
    }

    public void deleteUser(Long id) {
        UserEntity userToDelete = userDao.find(id);
        if (userToDelete == null) throw new WebApplicationException("Não encontrado", 404);

        UserEntity systemUser = userDao.findUserByUsername("deleted_user");
        if (systemUser == null) throw new WebApplicationException("Erro: deleted_user não existe.", 500);

        userDao.transferOwnership(userToDelete, systemUser);
        userDao.hardDelete(id);
    }

    /**
     * Lista para o Admin (filtra o deleted_user e protege o próprio Admin).
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
     * Autentica um utilizador verificando username, password e estado da conta.
     * @param loginDTO Dados vindos do formulário de login.
     * @return LoginResponseDTO se bem-sucedido, null se as credenciais falharem ou conta inativa.
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