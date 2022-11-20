package com.example;

import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@RequestScoped
public class TodoResource {

    @Inject
    TodoService todoService;

    @PathParam("id")
    UUID id;

    @GET
    public Response getById() {
        var todos = todoService.findById(id);
        return Response.ok(todos).build();
    }

}
