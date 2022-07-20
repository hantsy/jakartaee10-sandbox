package com.example;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("todos")
@RequestScoped
public class TodoResources {

    private static final Logger LOGGER = Logger.getLogger(TodoResources.class.getName());

    //@Inject
    @Context
    ResourceContext resourceContext;

    //@Inject
    @Context
    UriInfo uriInfo;

    @Inject
    TodoService todoService;

    @GET
    public CompletionStage<Response> getAllTodos() {
        return todoService.getAllTodosAsync().thenApply(todos -> Response.ok(todos).build());
    }

    @POST
    public Response createTodo(Todo todo) throws Exception {
        var saved = todoService.create(todo);
        return Response.created(uriInfo.getBaseUriBuilder().path("todos/{id}").build(saved.getId())).build();
    }

    @Path("{id}")
    public TodoResource todoResource() {
        return resourceContext.getResource(TodoResource.class);
    }
}
