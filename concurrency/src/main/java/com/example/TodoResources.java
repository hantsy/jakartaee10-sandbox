package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.concurrent.CompletionStage;

@Path("todos")
@RequestScoped
public class TodoResources {

   @Inject
    ResourceContext resourceContext;

    @Inject
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

    @GET
    @Path("{id}")
    public TodoResource subResource() {
        return resourceContext.getResource(TodoResource.class);
    }
}
