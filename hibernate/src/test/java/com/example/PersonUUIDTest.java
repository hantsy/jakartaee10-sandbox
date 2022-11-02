package com.example;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PersonUUIDTest {
    private static final Logger log = LoggerFactory.getLogger(PersonUUIDTest.class);

    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("defaultPU");
        var entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        var deleteFromPerson = entityManager.createQuery("DELETE FROM Person").executeUpdate();
        log.debug("Deleted {} persons", deleteFromPerson);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test
    @DisplayName("insert person and verify person")
    public void testInsertAndFindPerson() throws Exception {
        var person = new Person("John", 30);
        var entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();
        var id = person.getId();
        assertNotNull(id);

        try {
            var foundPerson = entityManager.find(Person.class, id);
            assertThat(foundPerson.getId()).isNotNull();
            assertThat(foundPerson.getName()).isEqualTo("John");
            assertThat(foundPerson.getAge()).isEqualTo(30);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        entityManagerFactory.close();
    }
}
