package com.example;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.*;

@ContextServiceDefinition(name = "java:app/concurrent/MyContextService",
        propagated = APPLICATION,
        cleared = {TRANSACTION, SECURITY},
        unchanged = ALL_REMAINING
)
@ManagedExecutorDefinition(name = "java:module/concurrent/MyExecutor",
        context = "java:app/concurrent/MyContextService",
        maxAsync = 5
)
@ApplicationScoped
public class TodoService {

    @Resource(lookup = "java:module/concurrent/MyExecutor")
    ManagedExecutorService executorService;

    @PersistenceContext
    EntityManager entityManager;

    CompletionStage<java.util.List<Todo>> getAllTodosAsync() {
        return CompletableFuture
                .supplyAsync(
                        () -> entityManager.createQuery("select t from Todo t", Todo.class).getResultList(),
                        executorService
                );
    }

    java.util.List<Todo> getAllTodos() {
        return entityManager.createQuery("select t from Todo t", Todo.class).getResultList();
    }

    @Transactional
    Todo create(Todo todo) {
        entityManager.persist(todo);
        return todo;
    }
}
