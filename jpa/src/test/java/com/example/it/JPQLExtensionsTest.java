/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */
package com.example.it;

import com.example.Person;
import com.example.Person.Gender;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ArquillianExtension.class)
public class JPQLExtensionsTest {

    private final static Logger LOGGER = Logger.getLogger(JPQLExtensionsTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(Person.class, Gender.class)
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @PersistenceContext
    private EntityManager em;

    @Inject
    UserTransaction ux;

    @BeforeEach
    public void before() throws Exception {
        clearPersons();
        startTx();
    }

    private void clearPersons() throws Exception {
        startTx();
        var builder = em.getCriteriaBuilder();
        var deletePersonQuery = builder.createCriteriaDelete(Person.class);
        var deletedPersons = em.createQuery(deletePersonQuery).executeUpdate();
        LOGGER.log(Level.INFO, "Deleted {0} persons", deletedPersons);
        endTx();
    }

    private void startTx() throws Exception {
        ux.begin();
        em.joinTransaction();
    }

    @AfterEach
    public void after() throws Exception {
        endTx();
    }

    private void endTx() throws Exception {
        LOGGER.log(Level.INFO, "Transaction status: {0}", ux.getStatus());
        try {
            if (ux.getStatus() == Status.STATUS_ACTIVE) {
                ux.commit();
            }
        } catch (Exception e) {
            ux.rollback();
        }
    }

    @Test
    public void testNumericFunctions() throws Exception {
        var person = new Person("John", 30);
        em.persist(person);
        assertNotNull(person.getId());
        endTx();

        startTx();
        var query = """
                SELECT p.name,
                CEILING(p.salary) as ceiling, 
                FLOOR(p.salary) as floor,
                ROUND(p.salary, 2) as round,
                EXP(p.yearsWorked) as exp,
                LN(p.yearsWorked) as ln,
                POWER(p.yearsWorked,2) as power,
                SIGN(p.yearsWorked) as sign
                FROM Person p
                """;
        var resultList = em.createQuery(query).getResultList();
        LOGGER.log(Level.INFO, "resultList: {0}", resultList);
    }

    @Test
    public void testDateTimeFunctions() throws Exception {
        var person = new Person("John", 30);
        em.persist(person);
        assertNotNull(person.getId());
        endTx();

        startTx();
        var query = """
                SELECT p.name,
                LOCAL TIME as localTime,
                LOCAL DATETIME as localDateTime,
                LOCAL DATE as localDate
                FROM Person p
                """;
        var resultList = em.createQuery(query).getResultList();
        LOGGER.log(Level.INFO, "resultList: {0}", resultList);
    }

    @Test
    public void testExtractFunctions() throws Exception {
        var person = new Person("John", 30);
        em.persist(person);
        assertNotNull(person.getId());
        endTx();

        startTx();
        var query = """
                SELECT p.name,
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
        var resultList = em.createQuery(query).getResultList();
        LOGGER.log(Level.INFO, "resultList: {0}", resultList);
    }
}
