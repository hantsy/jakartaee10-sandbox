package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("todos")
@RequestScoped
public class TodoResources {

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    ResourceContext resourceContext;

    @Inject
    UriInfo uriInfo;

    @Inject
    UserTransaction tx;

    @GET
    public Response getAllTodos() {
        var todos = entityManager.createQuery("select t from Todo t", Todo.class).getResultList();
        return Response.ok(todos).build();
    }

    @POST
    public Response createTodo(Todo todo) throws Exception {
        tx.begin();
        entityManager.persist(todo);
        tx.commit();
        return Response.created(uriInfo.getBaseUriBuilder().path("todos/{id}").build(todo.getId())).build();
    }

    @GET
    @Path("{id}")
    public TodoResource subResource() {
        return resourceContext.getResource(TodoResource.class);
    }
}
