package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@Path("todos")
@RequestScoped
public class TodoResources {

    private static final Logger LOGGER = Logger.getLogger(TodoResources.class.getName());

    @Context
    ResourceContext resourceContext;

    @Context
    UriInfo uriInfo;

    @Inject
    TodoService todoService;

    @GET
    public CompletableFuture<Response> getAllTodos() {
        return todoService.getAllTodosAsync().thenApply(todos -> Response.ok(todos).build());
    }

    @GET
    @Path("async1")
    public CompletableFuture<Response> getAllTodosAndAsync() {
        var todos = todoService.getAllTodos();
        return CompletableFuture.supplyAsync(() -> todos).thenApply(data -> Response.ok(data).build());
    }

    @POST
    public CompletionStage<Response> createTodo(Todo todo) throws Exception {
        var uriBuilder = uriInfo.getBaseUriBuilder();
        return todoService.createAsync(todo)
                .thenApply(saved -> Response.created(uriBuilder.path("todos/{id}").build(saved.getId())).build());
    }

    @Path("{id}")
    public TodoResource todoResource() {
        return resourceContext.getResource(TodoResource.class);
    }
}
