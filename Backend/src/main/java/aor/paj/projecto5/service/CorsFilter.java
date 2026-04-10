package aor.paj.projecto5.service;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        // Permite o teu porto do Vite
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
//        responseContext.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:5174");

        // Permite os métodos que usas
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");

        // IMPORTANTE: Permite o header 'token' que criaste no api.js
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, token");

        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");

        // Se for um pedido OPTIONS (preflight), respondemos OK imediatamente
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            responseContext.setStatus(200);
        }
    }
}