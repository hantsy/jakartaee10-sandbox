package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@RequestScoped
public class TodoResource {

    @PersistenceContext
    EntityManager entityManager;

    @PathParam("id")
    UUID id;

    @GET
    public Response getTodo() {
        return Response.ok(entityManager.find(Todo.class, id)).build();
    }


}
