package com.example;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Stateless
public class EjbTodoService {
    @PersistenceContext(unitName = "defaultPU")
    EntityManager entityManager;

    @Resource
    ManagedExecutorService executorService;

    @jakarta.ejb.Asynchronous
    public Future<List<Todo>> getAllTodosEjbAsync() {
        Callable<List<Todo>> callable = () -> entityManager.createQuery("select t from Todo t", Todo.class).getResultList();
        return executorService.submit(callable);
    }

}
