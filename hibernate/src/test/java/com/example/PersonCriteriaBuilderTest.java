package com.example;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class PersonCriteriaBuilderTest {
    private static final Logger log = LoggerFactory.getLogger(PersonCriteriaBuilderTest.class);

    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("defaultPU");
        var entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        var builder = entityManager.getCriteriaBuilder();
        var deletePersonQuery = builder.createCriteriaDelete(Person.class);
        var deletedPersons = entityManager.createQuery(deletePersonQuery).executeUpdate();
        log.debug("Deleted {} persons", deletedPersons);
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
            // see: https://hibernate.zulipchat.com/#narrow/stream/132096-hibernate-user/topic/New.20functions.20in.20JPA.203.2E1/near/289429903
            var cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();
            var query = cb.createTupleQuery();
            var root = query.from(Person.class);

            query.multiselect(root.get("name"),
                    cb.ceiling(root.get("salary")),
                    cb.floor(root.get("salary")),
                    cb.round(root.get("salary"), 1),
                    cb.exp(root.get("yearsWorked")),
                    cb.ln(root.get("yearsWorked")),
                    // see: https://hibernate.atlassian.net/browse/HHH-15395
                    cb.power(root.get("yearsWorked"), 2),
                    cb.sign(root.get("yearsWorked"))
            );
            query.where(cb.equal(root.get("id"), id));
            var resultList = entityManager.createQuery(query).getResultList();
            log.debug("Result list: {}", resultList);

            resultList.forEach(result ->
                    log.debug(
                            "result: ({},{},{},{},{},{},{},{})",
                            result.get(0, String.class),
                            result.get(1, BigDecimal.class),
                            result.get(2, BigDecimal.class),
                            result.get(3, BigDecimal.class),
                            result.get(4, Double.class),
                            result.get(5, Double.class),
                            result.get(6, Double.class),
                            result.get(7, Integer.class)
                    )
            );
        } catch (Exception ex) {
            fail(ex);
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
            var cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();
            var query = cb.createTupleQuery();
            var root = query.from(Person.class);

            query.multiselect(root.get("name"),
                    cb.localTime(),
                    cb.localDateTime(),
                    cb.localDate()
            );
            query.where(cb.equal(root.get("id"), id));

            var resultList = entityManager.createQuery(query).getResultList();
            log.debug("Result list: {}", resultList);
            resultList.forEach(result ->
                    log.debug(
                            "result: ({},{},{},{})",
                            result.get(0, String.class),
                            result.get(1, LocalTime.class),
                            result.get(2, LocalDateTime.class),
                            result.get(3, LocalDate.class)
                    )
            );
        } catch (Exception ex) {
            fail(ex);
        }
    }

    //see: https://github.com/eclipse-ee4j/jpa-api/pull/356
//    @Test
//    @DisplayName(">>> test `EXTRACT` functions")
//    public void testExtractFunctions() throws Exception {
//        var person = new Person("John", 30);
//        var entityManager = entityManagerFactory.createEntityManager();
//        entityManager.getTransaction().begin();
//        entityManager.persist(person);
//        entityManager.getTransaction().commit();
//        var id = person.getId();
//        assertNotNull(id);
//
//        try {
//            var queryString = """
//                    SELECT p.name as name,
//                    EXTRACT(YEAR FROM p.birthDate) as year,
//                    EXTRACT(QUARTER FROM p.birthDate) as quarter,
//                    EXTRACT(MONTH FROM p.birthDate) as month,
//                    EXTRACT(WEEK FROM p.birthDate) as week,
//                    EXTRACT(DAY FROM p.birthDate) as day,
//                    EXTRACT(HOUR FROM p.birthDate) as hour,
//                    EXTRACT(MINUTE FROM p.birthDate) as minute,
//                    EXTRACT(SECOND FROM p.birthDate) as second
//                    FROM Person p
//                    """;
//            var query = entityManager.createQuery(queryString);
//
//            var resultList = query.getResultList();
//            log.debug("Result list: {}", resultList);
//            resultList.forEach(result -> log.debug("result: {}", result));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    @AfterEach
    void tearDown() {
        entityManagerFactory.close();
    }
}