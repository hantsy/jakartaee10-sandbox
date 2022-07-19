package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@RequestScoped
public class TodoResource {
    @Inject
    TodoService todoService;

    @PathParam("id") UUID id;

    @GET
    public Response getById() {
        var todos = todoService.findById(id);
        return Response.ok(todos).build();
    }

 }
