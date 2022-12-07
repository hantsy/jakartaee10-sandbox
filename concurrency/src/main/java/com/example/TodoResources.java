package com.example;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
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

    @Inject
    EjbTodoService ejbTodoService;

    @Resource(lookup = "java:module/concurrent/MyExecutor")
    ManagedExecutorService executorService;

    @GET
    @Path("getAllTodosEjbFuture")
    public Future<List<Todo>> getAllTodosEjbFuture() {
        return ejbTodoService.getAllTodosEjbAsync();
    }

    @GET
    @Path("getAllTodosAsync")
    public CompletableFuture<Response> getAllTodosAsync() {
        return todoService.getAllTodosAsync().thenApply(todos -> Response.ok(todos).build());
    }

    @GET
    @Path("getAllTodosAndAsync")
    public CompletableFuture<Response> getAllTodosAndAsync() {
        var todos = todoService.getAllTodos();
        return CompletableFuture.supplyAsync(() -> todos).thenApply(data -> Response.ok(data).build());
    }

    @GET
    @Path("getAllTodosAndConcurrencyAsync")
    @Asynchronous
    public CompletableFuture<Response> getAllTodosAndConcurrencyAsync() {
        var todos = todoService.getAllTodos();
        return executorService.supplyAsync(() -> todos).thenApply(data -> Response.ok(data).build());
    }

    @GET
    @Path("")
    public Response getAllTodos() {
        var todos = todoService.getAllTodos();
        return Response.ok(todos).build();
    }

    // create todos
    @POST
    @Path("async")
    public CompletionStage<Response> createTodoAsync(Todo todo) throws Exception {
        var uriBuilder = uriInfo.getBaseUriBuilder();
        return todoService.createAsync(todo)
                .thenApply(saved -> Response.created(uriBuilder.path("todos/{id}").build(saved.getId())).build());
    }

    @POST
    @Path("")
    public Response createTodo(Todo todo) throws Exception {
        var uriBuilder = uriInfo.getBaseUriBuilder();
        var saved = todoService.create(todo);
        return Response.created(uriBuilder.path("todos/{id}").build(saved.getId())).build();
    }

    @Path("{id}")
    public TodoResource todoResource() {
        return resourceContext.getResource(TodoResource.class);
    }
}
