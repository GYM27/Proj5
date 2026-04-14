package aor.paj.projecto5.service;

import java.util.List;

import aor.paj.projecto5.bean.ClientsBean;
import aor.paj.projecto5.bean.UserVerificationBean;
import aor.paj.projecto5.dto.ClientsDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * SERVIÇO REST: ClientsService
 * ----------------------------
 * DESCRIÇÃO: Controlador JAX-RS responsável pela gestão de clientes.
 * ARQUITETURA: Atua como a camada de apresentação da API, recebendo os pedidos HTTP,
 * delegando a segurança ao 'verifier' e a lógica de negócio ao 'ClientsBean'.
 */
@Path("/clients")
public class ClientsService {

    @Inject
    ClientsBean clientsBean;

    @Inject
    UserVerificationBean verifier;

    // =========================================================================
    // --- 1. OPERAÇÕES DE UTILIZADOR E DONO ---
    // =========================================================================

    /**
     * CRIAÇÃO DE CLIENTE (RESTful POST)
     * O uso de @Valid garante que a validação do DTO ocorra antes de atingir o Bean.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addClient(@HeaderParam("token") String token, @Valid ClientsDTO clientDto) {
        // SEGURANÇA: Garante que apenas utilizadores com sessão ativa podem criar.
        verifier.verifyUser(token);

        ClientsDTO createdClient = clientsBean.addClient(token, clientDto);

        // BOAS PRÁTICAS REST: Retorna 201 Created quando um novo recurso é gerado.
        return Response.status(201).entity(createdClient).build();
    }

    /**
     * LISTAGEM DINÂMICA (POLIMORFISMO DE DADOS)
     * O comentário indica que o Bean decide internamente o que devolver com base no token.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listClients(
            @HeaderParam("token") String token,
            @QueryParam("userId") Long userId // Permite filtragem opcional via URL
    ) {
        verifier.verifyUser(token);
        // LÓGICA DE NEGÓCIO: O Bean trata a lógica Admin vs User internamente
        List<ClientsDTO> clients = clientsBean.listClients(token, userId);
        return Response.ok(clients).build();
    }

    /**
     * ATUALIZAÇÃO DE CLIENTE
     */
    @PUT
    @Path("/{id:[0-9]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateClient(@PathParam("id") Long clientId,
                                 @HeaderParam("token") String token,
                                 @Valid ClientsDTO clientDto) {
        // SEGURANÇA (RBAC): Valida simultaneamente se o token é válido E se o utilizador
        // tem permissão para editar este recurso específico (é o Dono ou é Admin).
        verifier.verifyOwnershipOrAdmin(token, clientId);

        ClientsDTO updatedClient = clientsBean.editClient(clientId, clientDto);
        return Response.ok(updatedClient).build();
    }

    /**
     * SOFT DELETE DE CLIENTE (Lixeira)
     * -------------------------------------------------------------------------
     * CORREÇÃO DE ROTEAMENTO: A adição de `:[0-9]+` garante que o servidor JAX-RS
     * este endpoint só aceita números, evitando colisões com rotas
     * estáticas como "/me-trash" ou "/trash".
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    public Response softDelete(@HeaderParam("token") String token, @PathParam("id") Long id) {
        // SEGURANÇA (RBAC): Valida se o utilizador é o dono do registo ou um Administrador
        verifier.verifyOwnershipOrAdmin(token, id);

        clientsBean.softDeleteClient(id);

        // BOAS PRÁTICAS REST: 204 No Content indica sucesso numa operação de eliminação
        return Response.noContent().build();
    }

    /**
     * RESTAURO DE CLIENTE
     * -------------------------------------------------------------------------
     * Utiliza PATCH porque estamos a realizar uma atualização parcial (apenas a flag softDelete).
     * Regex `:[0-9]+` aplicada ao PathParam.
     */
    @PATCH
    @Path("/{id:[0-9]+}/restore")
    @Produces(MediaType.APPLICATION_JSON) // Garante que o cabeçalho HTTP devolve o formato correto
    public Response restoreClient(@HeaderParam("token") String token, @PathParam("id") Long id) {
        // SEGURANÇA (RBAC): Verificação de posse ou privilégios globais
        verifier.verifyOwnershipOrAdmin(token, id);

        // Guarda o DTO retornado pelo Bean após a alteração de estado
        ClientsDTO restoredClient = clientsBean.restoreClient(id);

        // Retorna o objeto completo para que o Frontend possa atualizar a UI sem fazer novos pedidos
        return Response.ok(restoredClient).build();
    }

    /**
     * Endpoint: Lixeira exclusiva do próprio utilizador (User Normal)
     * Rota React: api("/clients/me-trash", "GET")
     */
    @GET
    @Path("/me-trash")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyTrash(@HeaderParam("token") String token) {
        // 1. Apenas exige que seja um utilizador válido
        verifier.verifyUser(token);

        // 2. LÓGICA INTELIGENTE: Como ele não é Admin,
        // o teu Bean vai ignorar o 'null' e usar o ID do próprio utilizador.
        List<ClientsDTO> trash = clientsBean.listDeletedClientsDTO(token, null);

        return Response.ok(trash).build();
    }


    // =========================================================================
    // --- 2. OPERAÇÕES EXCLUSIVAS DE ADMINISTRADOR ---
    // =========================================================================

    /**
     * HARD DELETE (Remoção Física)
     * -------------------------------------------------------------------------
     * Exclusivo para operações de limpeza permanente da base de dados.
     * Regex `:[0-9]+` isola o parâmetro dinâmico das rotas de sistema.
     */
    @DELETE
    @Path("/{id:[0-9]+}/permanent")
    public Response permanentDelete(@HeaderParam("token") String token, @PathParam("id") Long id) {
        // 1. SEGURANÇA ESTRITA: Apenas o perfil Administrador tem acesso a eliminações físicas
        verifier.verifyAdmin(token);

        // 2. Chama o método de execução destrutiva no Bean
        clientsBean.permanentDeleteClient(id);

        // 3. Retorna 204 No Content
        return Response.noContent().build();
    }

    /**
     * AÇÃO EM LOTE: Desativar todos os clientes de um utilizador.
     */
    @PATCH
    @Path("/user/{userId}/status/deactivate-all")
    public Response softDeleteAllUserClients(
            @HeaderParam("token") String token,
            @PathParam("userId") Long userId) {
        verifier.verifyAdmin(token);
        // LÓGICA DE PERFORMANCE: Bulk Update em vez de iterar cliente a cliente.
        clientsBean.softDeleteAllClientsByUser(userId);
        return Response.ok().build();
    }

    /**
     * LIXEIRA POR UTILIZADOR (Visão de Admin)
     */
    @GET
    @Path("/user/{userId}/trash")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeletedClientsByUserId(
            @HeaderParam("token") String token,
            @PathParam("userId") Long userId) {
        verifier.verifyAdmin(token);
        // Atualizado para usar o novo método dinâmico com filtro
        List<ClientsDTO> clients = clientsBean.listDeletedClientsDTO(token, userId);
        return Response.ok(clients).build();
    }

    /**
     * AÇÃO EM LOTE: Restaurar todos os clientes de um utilizador.
     */
    @PATCH
    @Path("/user/{userId}/status/activate-all")
    public Response restoreAllUserClients(
            @HeaderParam("token") String token,
            @PathParam("userId") Long userId) {
        verifier.verifyAdmin(token);
        clientsBean.unSoftDeleteAllClientsByUser(userId); // Agora usa Bulk Update
        return Response.ok().build();
    }

    /**
     * AÇÃO DE LIMPEZA GERAL (Hard Delete em Lote)
     */
    @DELETE
    @Path("/user/{userId}/trash")
    public Response emptyTrash(
            @PathParam("userId") Long userId,
            @HeaderParam("token") String token) {
        verifier.verifyAdmin(token);
        clientsBean.emptyTrash(userId);
        return Response.noContent().build();
    }

    /**
     * CRIAÇÃO DE CLIENTE POR PROCURAÇÃO
     * O Admin cria o cliente, mas atribui-o a outro utilizador.
     */
    @POST
    @Path("/user/{userId}")
    public Response createClientForUser(
            @PathParam("userId") Long userId,
            @Valid ClientsDTO dto,
            @HeaderParam("token") String token) {
        verifier.verifyAdmin(token);
        ClientsDTO created = clientsBean.createClientForUser(userId, dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * LIXEIRA GLOBAL (Visão de Admin de todos os apagados)
     */
    @GET
    @Path("/trash")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDeletedClients(@HeaderParam("token") String token) {
        verifier.verifyAdmin(token);
        // Chama a lixeira global sem filtro de utilizador
        List<ClientsDTO> clients = clientsBean.listAllDeletedClients();
        return Response.ok(clients).build();
    }
}