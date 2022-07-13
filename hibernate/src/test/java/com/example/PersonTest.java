package com.example;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PersonTest {
    private static final Logger log = LoggerFactory.getLogger(PersonTest.class);

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
    @DisplayName(">>> test numeric functions")
    public void testNumericFunctions() throws Exception {
        var person = new Person("John", 30);
        var entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();
        var id = person.getId();
        assertNotNull(id);

        try {
            var queryString = """
                    SELECT p.name as name,
                    CEILING(p.salary) as ceiling,
                    FLOOR(p.salary) as floor,
                    ROUND(p.salary, 1) as round,
                    EXP(p.yearsWorked) as exp,
                    LN(p.yearsWorked) as ln,
                    POWER(p.yearsWorked,2) as power,
                    SIGN(p.yearsWorked) as sign
                    FROM Person p
                    WHERE p.id=:id
                    """;
            var query = entityManager.createQuery(queryString);
            query.setParameter("id", id);
            var resultList = query.getResultList();
            log.debug("Result list: {}", resultList);
            resultList.forEach(result -> log.debug("result: {}", result));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    @DisplayName(">>> test datetime functions")
    public void testDateTimeFunctions() throws Exception {
        var person = new Person("John", 30);
        var entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();
        var id = person.getId();
        assertNotNull(id);

        try {
            var queryString = """
                    SELECT p.name as name,
                    LOCAL TIME as localTime,
                    LOCAL DATETIME as localDateTime,
                    LOCAL DATE as localDate
                    FROM Person p
                    """;

            var query = entityManager.createQuery(queryString);
            var resultList = query.getResultList();
            log.debug("Result list: {}", resultList);
            resultList.forEach(result -> log.debug("result: {}", result));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    @DisplayName(">>> test `EXTRACT` functions")
    public void testExtractFunctions() throws Exception {
        var person = new Person("John", 30);
        var entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();
        var id = person.getId();
        assertNotNull(id);

        try {
            var queryString = """
                    SELECT p.name as name,
                    EXTRACT(YEAR FROM p.birthDate) as year,
                    EXTRACT(QUARTER FROM p.birthDate) as quarter,
                    EXTRACT(MONTH FROM p.birthDate) as month,
                    EXTRACT(WEEK FROM p.birthDate) as week,
                    EXTRACT(DAY FROM p.birthDate) as day,
                    EXTRACT(HOUR FROM p.birthDate) as hour,
                    EXTRACT(MINUTE FROM p.birthDate) as minute,
                    EXTRACT(SECOND FROM p.birthDate) as second
                    FROM Person p
                    """;
            var query = entityManager.createQuery(queryString);

            var resultList = query.getResultList();
            log.debug("Result list: {}", resultList);
            resultList.forEach(result -> log.debug("result: {}", result));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        entityManagerFactory.close();
    }
}