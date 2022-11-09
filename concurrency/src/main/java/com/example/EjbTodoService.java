package com.example;

import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Stateless
public class EjbTodoService {
    @PersistenceContext
    EntityManager entityManager;

    @Asynchronous
    public Future<List<Todo>> getAllTodosEjbAsync() {
        return CompletableFuture.supplyAsync(() -> entityManager.createQuery("select t from Todo t", Todo.class).getResultList());
    }

}
