package com.example;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@Stateless
public class TodoService {

    @PersistenceContext
    EntityManager entityManager;

   @Transactional
    public Todo create(Todo data) {
        entityManager.persist(data);
        return data;
    }

    public Todo findById(UUID id) {
        return entityManager.find(Todo.class, id);
    }

    public List<Todo> findAll() {
        return entityManager.createQuery("select t from Todo t", Todo.class).getResultList();
    }
}
