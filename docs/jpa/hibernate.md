# Jakarta Persistence 3.1

Jakarta Persistence(aka JPA) 3.1 brings a collection of improvements.

* The `UUID` class now is treated as Basic Java Type. To support UUID type `ID` in `Entity` class, JPA introduces a new UUID generator.
* Several numeric functions and date/time specific functions are added in JPQL and type-safe Criteria API.

More details please read [What's New in Jakarta Persistence 3.1](https://newsroom.eclipse.org/eclipse-newsletter/2022/march/what%E2%80%99s-new-jakarta-persistence-31).

Next let's explore these features by writing some real example codes.

## Hibernate 6.1

Generate a simple **Java application** project via [Maven Quickstart archetype](https://maven.apache.org/archetypes/maven-archetype-quickstart/).

```bash
mvn archetype:generate
    -DarchetypeGroupId=org.apache.maven.archetypes
    -DarchetypeArtifactId=maven-archetype-quickstart
    -DarchetypeVersion=1.4
```

There are some interactive steps to guide you setup the project info, such as groupId, artifact, version etc. In this example project, we use `com.example` as groupId, and `demo` as artifactId. Then confirm and begin to generate the project source codes.

After it is done, open the project in a Java IDE such as IntelliJ IDEA(Community Edition is free), or Eclipse Java/Java EE bundle, or NetBeans IDE, or a simple text editor, eg. VS Code.

Modify the *pom.xml* in the project root folder, add Hibernate 6.1, and JUnit etc. into project dependencies, and setup Maven compiler plugin to use Java 17 to compile the source codes.

The final *pom.xml* looks like the following.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>hibernate6</artifactId>
    <version>1.0-SNAPSHOT</version>
    <parent>
        <groupId>com.example</groupId>
        <artifactId>jakartaee10-sandbox-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>..</relativePath>
    </parent>

    <name>hibernate6</name>
    <description>Jakarta EE 10 Sandbox: Hibernate 6/JPA 3.1 example</description>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>

        <!-- requires 6.1.2.Final or higher -->
        <hibernate.version>6.1.4.Final</hibernate.version>
        <h2.version>2.1.214</h2.version>

        <!-- test deps -->
        <junit-jupiter.version>5.9.1</junit-jupiter.version>
        <assertj-core.version>3.23.1</assertj-core.version>

        <slf4j.version>2.0.3</slf4j.version>
        <logback.version>1.4.4</logback.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>
        <dependency>
            <groupId>jakarta.persistence</groupId>
            <artifactId>jakarta.persistence-api</artifactId>
            <version>3.1.0</version>
        </dependency>

        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>${h2.version}</version>
        </dependency>

        <!-- logging with logback -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>jcl-over-slf4j</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-core</artifactId>
            <version>${logback.version}</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>${logback.version}</version>
        </dependency>

        <!-- test dependencies -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit-jupiter.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj-core.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-testing</artifactId>
            <version>${hibernate.version}</version>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M7</version>
            </plugin>
        </plugins>
    </build>
</project>
```

NOTE: To share common resources for all feature-based projects, create a parent POM to centralize the common configurations in one place, check [the parent pom.xml file](https://github.com/hantsy/jakartaee10-sandbox/blob/master/pom.xml).

In this example project, we use H2 embedded database for test purpose. Hibernate 6.1 implements the features of Jakarta Persistence 3.1, but it includes a Jakarta Persistence 3.0 API in the transitive dependency tree.

To use Jakarta Persistence 3.1 API, we have to add `jakarta.persistence:jakarta.persistence-api` 3.1 explicitly.

In the *src/main/resources/META-INF*, add a new file named *persistence.xml*.

```xml
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_1.xsd"
             version="3.1">

    <persistence-unit name="defaultPU" transaction-type="RESOURCE_LOCAL">

        <description>Hibernate test case template Persistence Unit</description>
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <exclude-unlisted-classes>false</exclude-unlisted-classes>

        <properties>
            <property name="hibernate.archive.autodetection" value="class, hbm"/>

            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
            <property name="hibernate.connection.driver_class" value="org.h2.Driver"/>
            <property name="hibernate.connection.url" value="jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1"/>
            <property name="hibernate.connection.username" value="sa"/>

            <property name="hibernate.connection.pool_size" value="5"/>

            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            <property name="hibernate.hbm2ddl.auto" value="create-drop"/>

            <property name="hibernate.max_fetch_depth" value="5"/>

            <property name="hibernate.cache.region_prefix" value="hibernate.test"/>
            <property name="hibernate.cache.region.factory_class"
                      value="org.hibernate.testing.cache.CachingRegionFactory"/>

            <!--NOTE: hibernate.jdbc.batch_versioned_data should be set to false when testing with Oracle-->
            <property name="hibernate.jdbc.batch_versioned_data" value="true"/>

            <property name="jakarta.persistence.validation.mode" value="NONE"/>
            <property name="hibernate.service.allow_crawling" value="false"/>
            <property name="hibernate.session.events.log" value="true"/>
        </properties>

    </persistence-unit>
</persistence>
```

We use logback as the logging framework in this project. In the *src/main/resources*, add a *logback.xml* to configure logback.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <property name="LOGS" value="./logs"/>

    <appender name="Console"
              class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <Pattern>
                %green(%d{ISO8601}) %highlight(%-5level) [%blue(%t)] %yellow(%C{1.}): %msg%n%throwable
            </Pattern>
        </layout>
    </appender>

    <appender name="RollingFile"
              class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOGS}/app.log</file>
        <encoder
                class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <Pattern>%d %p %C{1.} [%t] %m%n</Pattern>
        </encoder>

        <rollingPolicy
                class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- rollover daily and when the file reaches 10 MegaBytes -->
            <fileNamePattern>${LOGS}/archived/app-%d{yyyy-MM-dd}.%i.log
            </fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
    </appender>

    <!-- LOG everything at INFO level -->
    <root level="info">
        <appender-ref ref="RollingFile"/>
        <appender-ref ref="Console"/>
    </root>

    <!-- Debug hibernate SQL,  see: https://thorben-janssen.com/hibernate-logging-guide/ -->
    <logger name="org.hibernate.SQL" level="DEBUG"/>
    <logger name="org.hibernate.type.descriptor.sql" level="trace"/>

    <!-- Custom debug level for the application code -->
    <logger name="com.example" level="debug" additivity="false">
        <appender-ref ref="RollingFile"/>
        <appender-ref ref="Console"/>
    </logger>
</configuration>
```

We set `org.hibernate.SQL` logging level to `DEBUG` and `org.hibernate.type.descriptor.sql` to `trace`, it will help you to dig into the Hibernate generated sql at runtime.

### UUID Basic Type Support

JPA 3.1 allows to use UUID as basic Java type, especially it add a UUID ID generator.

Create a simple `Entity`.

```java
@Entity
public class Person {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private int age = 30;

    public Person() {
    }

    public Person(String name, int age) {
        assert age > 0;
        this.name = name;
        this.age = age;
        this.birthDate = LocalDateTime.now().minusYears(this.age);
    }

    // getters and setters
    // override equals and hashCode
}
```

An entity class is annotated with an `@Entity`, optionally you can specify the entity name and add table definition with an extra `@Table` annotation.

Here we defined a UUID type ID, and use a UUID generation strategy.

JPA requires an Entity should includes a no-arguments constructor, if you declare another constructor with a few arguments, you should declare this no-arguments constructor explicitly.

Create a simple JUnit test to verify if the UUID type working as expected.

```java
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
```

In the `@BeforeEach` method, we will create an `EntityManagerFactory` instance. And in the `@AfterEach` we call the `EntityManagerFactory.close` to release the resource.

In the `@BeforeEach` we try to clean up the Person data.

Now in the `testInsertAndFindPerson` test, we insert a new person, then utilize `entityManager.find` to find the inserted person.

The person id is annotated with `@ID` and `@GeneratedValue`, when inserting a person into table, hibernate will generate an ID automatically. After it is persisted, the returned instance is filled with the generated id, it should not be a null.

### Numeric Functions

JPA 3.1 adds a collection of new numeric functions in literal JPQL query and type-safe Criteria Builder API.

Add some extra properties in the above `Person` class.

```java
public class Person{
    private Integer yearsWorked = 2;
    private LocalDateTime birthDate = LocalDateTime.now().minusYears(30);
    private BigDecimal salary = new BigDecimal("12345.678");
    private BigDecimal hourlyRate = new BigDecimal("34.56");

    // setters and getters
}
```

Create a new test to verify the new numeric functions: `ceiling`, `floor`, `round`, `exp`, `ln`, `power`, `sign`.

```java
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
```

Next, let's have a look at how to use them in the Criteria APIs.

```java
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
```

Note, when using Hibernate 6.1, we have to cast `CriteriaBuilder` to `HibernateCriteriaBuilder` to experience the new numeric functions. Hibernate 6.2 will align to JPA 3.1 and fix the issue.

### DateTime Functions

JPA 3.1 add a series of datetime functions and ease the usage of Java 8 DateTime APIs.

```java
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
```

The `LOCAL TIME`, `LOCAL DATETIME`, `LOCAL DATE` query result will be treated as Java 8 `LocalTime`, `LocalDateTime`, `LocalDate` directly.

Let's have a look at the usage in the CriteriaBuilder APIs.

```java
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
```

### `EXTRACT` function

JPA 3.1 introduces a `extract` function to decode fragments from a datetime value.

```java
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
```

Use the new `extract` function, we can read the `year`, `quarter`, `month`, `week`, `day`, `hour`, `minute`, `second` values from a Java 8 DateTime type property in the JPQL query.

Note, there is no mapped extract function in the CriteriaBuilder APIs, for more details, check issue: <https://github.com/eclipse-ee4j/jpa-api/pull/356>

## JakartaEE Runtime

Next let's go to the Jakarta EE 10 compatible products to experience the new features of JPA 3.1.

Firstly we will prepare a Jakarta EE 10 web application.

Simply generate a web application skeleton via [Maven Webapp Archetype](https://maven.apache.org/archetypes/maven-archetype-webapp/).

```bash
mvn archetype:generate
    -DarchetypeGroupId=org.apache.maven.archetypes
    -DarchetypeArtifactId=maven-archetype-webapp
    -DarchetypeVersion=1.4
```

Then add Jakarta EE 10 dependency into the project pom.xml. Let's have a look at the modified pom.xml.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>jpa-examples</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>
    <parent>
        <groupId>com.example</groupId>
        <artifactId>jakartaee10-sandbox-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>..</relativePath>
    </parent>

    <name>jpa-examples</name>
    <description>Jakarta EE 10 Sandbox: Persistence 3.1 Examples</description>
    <properties>
    </properties>

    <dependencies>
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.eclipse.persistence</groupId>
            <artifactId>org.eclipse.persistence.jpa.modelgen.processor</artifactId>
            <version>4.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.arquillian.junit5</groupId>
            <artifactId>arquillian-junit5-container</artifactId>
            <scope>test</scope>
        </dependency>
        <!-- see: https://github.com/arquillian/arquillian-core/issues/248 -->
        <!-- and https://github.com/arquillian/arquillian-core/pull/246/files -->
        <dependency>
            <groupId>org.jboss.arquillian.protocol</groupId>
            <artifactId>arquillian-protocol-servlet-jakarta</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

In the above pom.xml, we also add JUnit 5 and [Arquillian](https://arquillian.org) related dependencies in test scope. Through the container specific Aquillian adapter, we can run the tests in Jakarta EE application servers.

In this project, we reuse the the `Person` entity we have introduced in the Hibernate section.

Now let's move to persistence configuration. Create a *persistence.xml* in the *src/main/resources/META-INFO* folder.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0" xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">
    <persistence-unit name="defaultPU" transaction-type="JTA">
        <jta-data-source>java:comp/DefaultDataSource</jta-data-source>
        <exclude-unlisted-classes>false</exclude-unlisted-classes>
        <properties>
            <property name="jakarta.persistence.schema-generation.database.action" value="drop-and-create"/>

            <!-- for  Glassfish/Payara/EclipseLink -->
            <property name="eclipselink.logging.level.sql" value="FINE"/>
            <property name="eclipselink.logging.level" value="FINE"/>
            <property name="eclipselink.logging.parameters" value="true"/>

            <!-- for WildFly/Hibernate -->
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

The configuration is a little different from the one we introduced in the Hibernate section.

* In a container environment, we would like choose `JTA` as transaction-type.
* We do not setup database connection info, instead we configure a built-in DataSource. The `java:comp/DefaultDataSource` is the default DataSource for all Jakarta EE compatible products.

### Creating Jakarta EE Sample Application

To interact with our backend database, we will create a simple complete JAXRS application, including:

* A EJB `@Stateless` bean to read data from database
* And expose data via a simple JAXRS resource

OK, let's create class `PersonRepository` which is annotated with `@Stateless`. In this class, inject a `EntityManager` bean with an annotation `@PersistenceContext`, and add a new method `getAllResource` to execute a JPQL query to retrieve all persons.

```java
@Stateless
public class PersonRepository {

    @PersistenceContext
    EntityManager entityManager;

    public List<Person> getAllPersons() {
        return entityManager.createQuery("select p from Person p", Person.class)
                .getResultList();
    }
}
```

Next, create a `PersonResource` to expose persons to client.

```java
@RequestScoped
@Path("/persons")
public class PersonResource {

    @Inject
    PersonRepository personRepository;

    @Path("")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response allPersons() {
        var data = personRepository.getAllPersons();
        return Response.ok(data).build();
    }
}
```

The `PersonResource` is annotated with `RequestScoped`, it is a CDI bean, the `@Path` on the class define the root path of all subresources in this class. The `allPersons` will produces all persons to client in JSON format when HTTP Client request matches HTTP GET method, and URI is `/persons` and HTTP Header Accept is compatible with `application/json`.

To activate JAXRS feature, create a class to extend the JAXRS `Application`, add `@ApplicationPath` to specify the root context path of all JAXRS resources.

```java
@ApplicationPath("/rest")
public class RestActivator extends Application {
}
```

Let's create a bean to add some sample data at the application startup.

```java
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
```

### Deploying to Jakarta EE Containers

Build and package the application into a war archive. Open a terminal, switch to the project root folder, and execute the following command.

```bash
mvn clean package -DskipTests -D"maven.test.skip=true"
```

When it is done, there is war package is ready in the path *target/jpa-examples.war*.

#### GlassFish 7.0

1. Download the [latest GlassFish 7.0](https://github.com/eclipse-ee4j/glassfish/releases), extract files to a location, eg. D:\glassfish7, mark as *GlassFish_install*.
2. To start GlassFish and Derby, open a terminal, enter *GlassFish_install/bin*, run `asadmin start-database` and `asadmin start-domain domain1`.
3. Copy the above war package to *Glassfish_install/glassfish/domains/domain1/autodeploy* folder.
4. Open *GlassFish_install/glassfish/domains/domain1/logs/server.log*, and wait the deployment is completed.
5. Open another terminal window, execute `curl http://localhost:8080/jpa-examples/rest/persons`. You will the following response in the console.

    ```bash
    [{"age":18,"birthDate":"2004-11-06T14:54:05.4504678","gender":"MALE","hourlyRate":34.56,"id":"d8552d71-ff7f-4650-b5a0-ce1c5fb3fe0b","name":"Rose","salary":12345.678,"yearsWorked":2},{"age":20,"birthDate":"2002-11-06T14:54:05.4504678","gender":"MALE","hourlyRate":34.56,"id":"cdf94cdc-21b3-492c-b1b5-06bc8cae9947","name":"Jack","salary":12345.678,"yearsWorked":2}]
    ```

6. To stop GlassFish and Derby, run `asadmin stop-database` and `asadmin stop-domain domain1`

#### WildFly Preview 27

1. Download the latest [WildFly Preview](https://wildfly.org), extract files to a location, eg. D:\wildfly-preview-27.0.0.Beta1, mark as *WildFly_install*.
2. Open a terminal, enter *WildFly_install/bin*, run `standalone` to start WildFly with the default standalone profile configuration.
3. Copy the built war to *WildFly_install/standalone/deployments*.
4. Wait the deployment progress is done, you can use the curl in GlassFish section to verify the application.
5. Send a `CTLR+C` keys combination in the original WildFly startup console to stop WildFly.

### Deploying Application via Maven Plugin

#### Deploying to GlassFish via Cargo Plugin

The GlassFish project does not include an official Maven plugin to manage GlassFish server.
There is a Maven plugin named `cargo-maven3-plugin` which can be used to manage all popular Jakarta EE application servers and web servers.

Add the following `profile` section to use cargo plugin to manage the lifecycle of GlassFish server.

```xml
<profile>
    <id>glassfish</id>
    <activation>
        <activeByDefault>true</activeByDefault>
    </activation>
    <properties>
        <cargo.zipUrlInstaller.downloadDir>${project.basedir}/../installs</cargo.zipUrlInstaller.downloadDir>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.cargo</groupId>
                <artifactId>cargo-maven3-plugin</artifactId>
                <configuration>
                    <container>
                        <containerId>glassfish7x</containerId>
                        <!-- <artifactInstaller>
                            <groupId>org.glassfish.main.distributions</groupId>
                            <artifactId>glassfish</artifactId>
                            <version>${glassfish.version}</version>
                        </artifactInstaller> -->
                        <zipUrlInstaller>
                            <url>https://github.com/eclipse-ee4j/glassfish/releases/download/${glassfish.version}/glassfish-${glassfish.version}.zip</url>
                            <downloadDir>${cargo.zipUrlInstaller.downloadDir}</downloadDir>
                        </zipUrlInstaller>
                    </container>
                    <configuration>
                        <!-- the configuration used to deploy -->
                        <home>${project.build.directory}/glassfish7x-home</home>
                        <properties>
                            <cargo.remote.password></cargo.remote.password>
                            <cargo.glassfish.removeDefaultDatasource>true</cargo.glassfish.removeDefaultDatasource>
                        </properties>
                        <datasources>
                            <datasource>
                                <driverClass>org.apache.derby.jdbc.EmbeddedDriver</driverClass>
                                <url>jdbc:derby:derbyDB;create=true</url>
                                <jndiName>jdbc/__default</jndiName>
                                <username>APP</username>
                                <password>nonemptypassword</password>
                            </datasource>
                        </datasources>
                    </configuration>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

Unlike the approach in NetBeans IDE or Eclipse IDE with GlassFish Pack, where starting GlassFish it will start the built-in Derby at the same time. Cargo does not start the built-in Derby as expected, to use the default DataSource in our project, clear the default DataSource and add a new default DataSource based on the embedded Derby.

Run the following command. It will compile the project source codes and package the application into a war archive, then start the managed GlassFish server(with a new `cargo-domain`), and then deploy the package into this running server.

```bash
mvn clean package cargo:run -DskipTests -Dmaven.test.skip=true
```

Note, when you run this command at the first time, it will spend some time to download a copy of  the GlassFish redistribution, and extract the files into the build folder.

In another terminal window, execute `curl http://localhost:8080/jpa-examples/rest/persons` to verify the endpoint.

To stop the server, just send a `CTRL+C` in the original GlassFish running console.

#### Deploying to WildFly via WildFly Plugin

The WildFly project itself provides an official WildFly Maven plugin, we will configure it in a new Maven profile.

> Cargo maven plugin also supports WildFly, check [Cargo WildFly docs](https://codehaus-cargo.github.io/cargo/WildFly+27.x.html).

```xml
<profile>
    <id>wildfly</id>
    <properties>
        <!-- Wildfly server -->
        <wildfly.artifactId>wildfly-preview-dist</wildfly.artifactId>
        <jboss-as.home>${project.build.directory}/wildfly-preview-${wildfly.version}</jboss-as.home>
    </properties>
    <build>
        <plugins>

            <!-- unpack a copy of WildFly-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>${maven-dependency-plugin.version}</version>
                <executions>
                    <execution>
                        <id>unpack</id>
                        <phase>process-classes</phase>
                        <goals>
                            <goal>unpack</goal>
                        </goals>
                        <configuration>
                            <artifactItems>
                                <artifactItem>
                                    <groupId>org.wildfly</groupId>
                                    <artifactId>${wildfly.artifactId}</artifactId>
                                    <version>${wildfly.version}</version>
                                    <type>zip</type>
                                    <overWrite>false</overWrite>
                                    <outputDirectory>${project.build.directory}</outputDirectory>
                                </artifactItem>
                            </artifactItems>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- The WildFly plugin deploys your war to a local running WildFly container -->
            <!-- To use, run: mvn package wildfly:deploy -->
            <!-- For Jakarta EE 9, use `wildfly-preview-dist` as artifactId instead to start and deploy applications-->
            <!-- Run: mvn clean wildfly:run -PWildfly -Dwildfly.artifactId=wildfly-preview-dist -Dwildfly.version=22.0.0.Alpha1 -->
            <!-- or set the `jboss-as.home` to run: mvn clean wildfly:run -PWildfly -Djboss-as.home=D:\appsvr\wildfly-preview-22.0.0.Alpha1-->
            <plugin>
                <groupId>org.wildfly.plugins</groupId>
                <artifactId>wildfly-maven-plugin</artifactId>
                <version>${wildfly-maven-plugin.version}</version>
            </plugin>
        </plugins>
    </build>
    <repositories>
        <repository>
            <id>opensaml</id>
            <url>https://build.shibboleth.net/nexus/content/repositories/releases/</url>
        </repository>
    </repositories>
</profile>
```

With the WildFly plugin, we can deploy applications into an embedded WildFly, a managed WildFly server or a remote running WildFly server.

```bash
mvn clean wildfly:run -Pwildfly -DskipTests -Dmaven.test.skip=true
```

By default, if we do not setup a `jboss-as.home` or remote host connection info, it will bootstrap an embedded WildFly and run the application with the embedded server.

Here we configure Maven dependency plugin to download a copy of WildFly, extract the files to the project build directory, and setup a `jboss-as.home` property, the value is the WildFly location. The WildFly plugin will manage the whole WildFly lifecycle - start the WildFly server, deploy applications into the running server, (use `CTRL+C` hotkey) stop the server.

### Testing JPA Features

Here I assume you are familiar with [JUnit](https://www.junit.org) and [Arquillian](https://arquillian.org) before.

> For the developers new to Arqullian framework, please read the official [Arquillian Guides](https://arquillian.org/guides) to start your first step. Note, these tutorials are available in several languages, including Simplified Chinese.

> Go to my [Jakarta EE 8 starter boilerplate project](https://github.com/hantsy/jakartaee8-starter-boilerplate) and [Jakarta EE 9 starter boilerplate project](https://github.com/hantsy/jakartaee9-starter-boilerplate) to update your Arquilian knowledge.

Since Jakarta EE 9, it uses the new `jakarta` namespace, Arquillian 1.7.0.x starts to support these changes.

In the next steps, we will configure a managed GlassFish Arquillian Adapter to run the testing codes.

```xml
<profile>
    <id>arq-glassfish-managed</id>
    <properties>
        <skip.unit.tests>true</skip.unit.tests>
        <skip.integration.tests>false</skip.integration.tests>
    </properties>
    <dependencies>
        <!-- Jersey -->
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-sse</artifactId>
            <version>${jersey.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-binding</artifactId>
            <version>${jersey.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
            <version>${jersey.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-client</artifactId>
            <version>${jersey.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.github.hantsy.arquillian-container-glassfish-jakarta</groupId>
            <artifactId>arquillian-glassfish-managed-jakarta</artifactId>
            <version>${arquillian-glassfish-jakarta.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <testResources>
            <testResource>
                <directory>src/test/resources</directory>
            </testResource>
            <testResource>
                <directory>src/test/arq-glassfish-managed</directory>
            </testResource>
        </testResources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>${maven-dependency-plugin.version}</version>
                <executions>
                    <execution>
                        <id>unpack</id>
                        <phase>pre-integration-test</phase>
                        <goals>
                            <goal>unpack</goal>
                        </goals>
                        <configuration>
                            <artifactItems>
                                <artifactItem>
                                    <groupId>org.glassfish.main.distributions</groupId>
                                    <artifactId>glassfish</artifactId>
                                    <version>${glassfish.version}</version>
                                    <type>zip</type>
                                    <overWrite>false</overWrite>
                                    <outputDirectory>${project.build.directory}</outputDirectory>
                                </artifactItem>
                            </artifactItems>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>${maven-failsafe-plugin.version}</version>
                <configuration>
                    <environmentVariables>
                        <GLASSFISH_HOME>${project.build.directory}/glassfish7</GLASSFISH_HOME>
                    </environmentVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

In the above configuration, we add `com.github.hantsy.arquillian-container-glassfish-jakarta:arquillian-glassfish-managed-jakarta`, which is my [fork](https://github.com/hantsy/arquillian-container-glassfish-jakarta) of the official [Arquillian Container GlassFish project](https://github.com/arquillian/arquillian-container-glassfish6).

We preapre a copy of the latest GlassFish 7.0 in the `pre-integration-test` phase. The Arquillian tests will be exectued in the `integretion-test` phase.

Let's create a simple Arquillian tests to verify the UUID basic type feature in JPA 3.1.

```java
@ExtendWith(ArquillianExtension.class)
public class UUIDStrategyTest {

    private final static Logger LOGGER = Logger.getLogger(UUIDStrategyTest.class.getName());

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
        startTx();
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
        try {
            if( ux.getStatus() == Status.STATUS_ACTIVE ) {
                ux.commit();
            }
        } catch (Exception e) {
            ux.rollback();
        }
    }

    @Test
    public void testPersistingPersons() throws Exception {
        final Person person = new Person();
        person.setName("Hantsy Bai");
        em.persist(person);
        endTx();

        startTx();
        final Person foundPerson = em.find(Person.class, person.getId());
        assertNotNull(foundPerson.getId());
        LOGGER.log(Level.INFO, "Found person: {0}", foundPerson);
    }
}
```

The `@ExtendWith(ArquillianExtension.class)` annotation on a test class to support Arquillian test lifecycle.

The `@Deployment` annotated static method defines the resources that will be packaged into the test archive and deployed into the manged GlassFish server. It is easy to use shrinkwrap to create a fine-grined deploymen unit.

You can inject `EntityManager` and `UserTransaction` beans in an Arquillian test like what you do in a simple CDI bean.

In this test class, we setup `@BeforeEach` and `@AfterEach` hooks to start a transacation and end the transaction.

The test method `testPersistingPersons` looks no difference from a plain JUnit test. Firstly we persist a person entity, and commit the transaction to ensure it will be flushed into the database as expected. Then exectuing a simple JPA query to verify the persisted data.

Execute the following command to run the tests.

```bash
mvn clean verify -Parq-glassfish-managed
```

Similiarly, create a test to verify the new numeric functions and datetime functions in Jakarta rumtimes.

```java
@ExtendWith(ArquillianExtension.class)
public class JPQLFunctionsTest {

    private final static Logger LOGGER = Logger.getLogger(JPQLFunctionsTest.class.getName());

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
    @DisplayName(">>> test numeric functions")
    public void testNumericFunctions() throws Exception {
        var person = new Person("John", 30);
        em.persist(person);
        var id = person.getId();
        assertNotNull(id);
        endTx();

        startTx();
        try {
            var queryString = """
                    SELECT p.name,
                    CEILING(p.salary),
                    FLOOR(p.salary),
                    ROUND(p.salary, 1),
                    EXP(p.yearsWorked),
                    LN(p.yearsWorked),
                    POWER(p.yearsWorked,2),
                    SIGN(p.yearsWorked)
                    FROM Person p
                    WHERE p.id=:id
                    """;
            var query = em.createQuery(queryString);

            query.setParameter("id", id);
            // for EclipseLinks
            query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);
            List<Map<String, Object>> resultList = query.getResultList();
            LOGGER.log(Level.INFO, "result size:{0}", resultList.size());
            resultList.forEach(data -> {
                data.forEach((k, v) -> LOGGER.log(Level.INFO, "field:{0}, value: {1}", new Object[]{k, v}));
            });
        } catch (Exception ex) {
            fail(ex);
        }
    }

    @Test
    @DisplayName(">>> test nen datetime functions")
    public void testDateTimeFunctions() throws Exception {
        var person = new Person("John", 30);
        em.persist(person);
        assertNotNull(person.getId());
        endTx();

        startTx();
        try {
            var queryString = """
                    SELECT p.name as name,
                    LOCAL TIME as localTime,
                    LOCAL DATETIME as localDateTime,
                    LOCAL DATE as localDate
                    FROM Person p
                    """;
            // for EclipseLinks
            var query = em.createQuery(queryString);
            // for EclipseLinks
            query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);
            List<Map<String, Object>> resultList = query.getResultList();
            LOGGER.log(Level.INFO, "result size:{0}", resultList.size());
            resultList.forEach(data -> {
                data.forEach((k, v) -> LOGGER.log(Level.INFO, "field:{0}, value: {1}", new Object[]{k, v}));
            });
        } catch (Exception ex) {
            fail(ex);
        }
    }

    @Test
    @DisplayName(">>> test `EXTRACT` functions")
    public void testExtractFunctions() throws Exception {
        var person = new Person("John", 30);
        em.persist(person);
        assertNotNull(person.getId());
        endTx();

        startTx();
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
            var query = em.createQuery(queryString);
            // for EclipseLinks
            query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);
            List<Map<String, Object>> resultList = query.getResultList();
            LOGGER.log(Level.INFO, "result size:{0}", resultList.size());
            resultList.forEach(data -> {
                data.forEach((k, v) -> LOGGER.log(Level.INFO, "field:{0}, value: {1}", new Object[]{k, v}));
            });
        } catch (Exception ex) {
            fail(ex);
        }
    }
}
```

Alternatively, create a test to verify the Criteria APIs.

```java
@ExtendWith(ArquillianExtension.class)
public class JPQLCriteriaBuilderTest {

    private final static Logger LOGGER = Logger.getLogger(JPQLCriteriaBuilderTest.class.getName());

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
    @DisplayName(">>> test numeric functions")
    public void testNumericFunctions() throws Exception {
        var person = new Person("John", 30);
        em.persist(person);
        var id = person.getId();
        assertNotNull(id);
        endTx();

        startTx();
        try {
            var cb = em.getCriteriaBuilder();
            var query = cb.createTupleQuery();
            var root = query.from(Person.class);

            query.multiselect(root.get("name"),
                    cb.ceiling(root.get("salary")),
                    cb.floor(root.get("salary")),
                    cb.round(root.get("salary"), 1),
                    cb.exp(root.get("yearsWorked")),
                    cb.ln(root.get("yearsWorked")),
                    cb.power(root.get("yearsWorked"), 2),
                    cb.sign(root.get("yearsWorked"))
            );
            query.where(cb.equal(root.get("id"), id));

            var resultList = em.createQuery(query).getResultList();
            LOGGER.log(Level.INFO, "result size:{0}", resultList.size());
            resultList.forEach(result ->
                    LOGGER.log(
                            Level.INFO,
                            // see: https://github.com/eclipse-ee4j/eclipselink/issues/1593
                            // John,12,345,12,345,12,345,7.389,0.693,4,1
                            "tuple data :{0},{1},{2},{3},{4},{5},{6},{7}",
                            new Object[]{
                                    result.get(0, String.class),
                                    result.get(1, BigDecimal.class), // it should return BigDecimal
                                    result.get(2, BigDecimal.class),
                                    result.get(3, BigDecimal.class),
                                    result.get(4, Double.class),
                                    result.get(5, Double.class),
                                    result.get(6, Double.class),
                                    result.get(7, Integer.class)
                            }
                    )
            );
        } catch (Exception ex) {
            fail(ex);
        }
    }

    @Test
    @DisplayName(">>> test nen datetime functions")
    public void testDateTimeFunctions() throws Exception {
        var person = new Person("John", 30);
        em.persist(person);
        var id = person.getId();
        assertNotNull(id);
        endTx();

        startTx();
        try {
            var cb = em.getCriteriaBuilder();
            var query = cb.createTupleQuery();
            var root = query.from(Person.class);

            query.multiselect(root.get("name"),
                    cb.localTime(),
                    cb.localDateTime(),
                    cb.localDate()
            );
            query.where(cb.equal(root.get("id"), id));

            var resultList = em.createQuery(query).getResultList();
            LOGGER.log(Level.INFO, "result size:{0}", resultList.size());
            resultList.forEach(data ->
                    LOGGER.log(
                            Level.INFO,
                            "tuple data :{0},{1},{2},{3}",
                            new Object[]{
                                    data.get(0, String.class),
                                    data.get(1, java.time.LocalTime.class),
                                    data.get(2, java.time.LocalDateTime.class),
                                    data.get(3, java.time.LocalDate.class)
                            }
                    )
            );
        } catch (Exception ex) {
            fail(ex);
        }
    }
}
```

But unfortunately, there is a bug in the GlassFish 7.0.0-M9 will fail the test `JPQLFunctionsTest`, more details please check Github issues [GlassFish #24120](https://github.com/eclipse-ee4j/glassfish/issues/24120).

Check the sample codes of [Hibernate](https://github.com/hantsy/jakartaee10-sandbox/tree/master/hibernate) and [Jakarta Persistence](https://github.com/hantsy/jakartaee10-sandbox/tree/master/jpa) from my github.
