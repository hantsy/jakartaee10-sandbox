package com.example;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class PersonRepository {

    @PersistenceContext
    EntityManager entityManager;

    public List<Person> getAllPersons() {
        return entityManager.createQuery("select p from Person p", Person.class)
                .getResultList();
    }
}
