package com.example;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Startup
@Singleton
public class DataInitializer {

    @PersistenceContext
    EntityManager entityManager;

    @PostConstruct
    public void init() {
        List
                .of(
                        new Person("Jack", 20),
                        new Person("Rose", 18)
                )
                .forEach(entityManager::persist);
    }
}
