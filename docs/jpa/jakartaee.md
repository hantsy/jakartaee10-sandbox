# Jakarta EE Runtime Environment

Next let's go to the Jakarta EE 10 compatible products to experience the new features of JPA 3.1.

Firstly we will prepare a Jakarta EE 10 web project.

## Creating a Jakarta EE Web Project

Simply generate a web project skeleton via [Maven Webapp Archetype](https://maven.apache.org/archetypes/maven-archetype-webapp/).

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

In the above pom.xml, we also add JUnit 5 and [Arquillian](https://arquillian.org) related dependencies in test scope. Through the Jakarta EE container specific Aquillian adapter, we can run the tests in a Jakarta EE containers.

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

## Creating Jaxrs Resource

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

## Deploying to Jakarta EE Containers

Build and package the application into a war archive. Open a terminal, switch to the project root folder, and execute the following command.

```bash
mvn clean package -DskipTests -D"maven.test.skip=true"
```

When it is done, there is war package is ready in the path *target/jpa-examples.war*.

### GlassFish 7.0

1. Download the [latest GlassFish 7.0](https://github.com/eclipse-ee4j/glassfish/releases), extract files to a location, eg. D:\glassfish7, mark as *GlassFish_install*.
2. To start GlassFish and Derby, open a terminal, enter *GlassFish_install/bin*, run `asadmin start-database` and `asadmin start-domain domain1`.
3. Copy the above war package to *Glassfish_install/glassfish/domains/domain1/autodeploy* folder.
4. Open *GlassFish_install/glassfish/domains/domain1/logs/server.log*, and wait the deployment is completed.
5. Open another terminal window, execute `curl http://localhost:8080/jpa-examples/rest/persons`. You will see the following result in the console.

    ```json
    [{"age":18,"birthDate":"2004-11-06T14:54:05.4504678","gender":"MALE","hourlyRate":34.56,"id":"d8552d71-ff7f-4650-b5a0-ce1c5fb3fe0b","name":"Rose","salary":12345.678,"yearsWorked":2},{"age":20,"birthDate":"2002-11-06T14:54:05.4504678","gender":"MALE","hourlyRate":34.56,"id":"cdf94cdc-21b3-492c-b1b5-06bc8cae9947","name":"Jack","salary":12345.678,"yearsWorked":2}]
    ```

6. To stop GlassFish and Derby, run `asadmin stop-database` and `asadmin stop-domain domain1`
respectively.

### WildFly Preview 27

1. Download the latest [WildFly Preview](https://wildfly.org), extract files to a location, eg. D:\wildfly-preview-27.0.0.Beta1, mark as *WildFly_install*.
2. Open a terminal, enter *WildFly_install/bin*, run `standalone` to start WildFly with the default standalone profile configuration.
3. Copy the built war to *WildFly_install/standalone/deployments*.
4. Wait the deployment progress is done, you can use the curl in GlassFish section to verify the application.
5. Send a `CTLR+C` keys combination in the original WildFly startup console to stop WildFly.

## Deploying Application via Maven Plugin

### GlassFish 7.0

The GlassFish project does not include an official Maven plugin to manage GlassFish server.
But there is a community-based `cargo-maven3-plugin` which can be used to manage almost all popular Jakarta EE application servers and web servers.

Add the following `profile` section to use cargo Maven plugin to manage the lifecycle of GlassFish server.

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

Unlike the approach in NetBeans IDE or Eclipse IDE with GlassFish Pack, where starting GlassFish it will start the built-in Derby at the same time. Cargo maven plugin does not start the built-in Derby as expected, to use the default DataSource in our project, we have to clear the default DataSource and add a new default DataSource using the embedded Derby which is shipped by GlassFish distributions.

Run the following command.

```bash
mvn clean package cargo:run -DskipTests -Dmaven.test.skip=true
```

It will compile the project source codes and package the compiled resources into a war archive, then start the managed GlassFish server(with a new `cargo-domain`), and then deploy the package into the running server.

>Note, when you run this command at the first time, it will spend some time to download a copy of the GlassFish distribution, and extract the files into the project build folder.

In another terminal window, execute `curl http://localhost:8080/jpa-examples/rest/persons` to verify the endpoint.

To stop the server, just send a `CTRL+C` in the original GlassFish running console.

### WildFly Preview 27

The WildFly project itself provides an official WildFly Maven plugin, we will configure it in a new Maven profile.

> Cargo maven plugin also supports WildFly, check [Cargo WildFly support docs](https://codehaus-cargo.github.io/cargo/WildFly+27.x.html).

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

Utilize this WildFly plugin, we can deploy applications into an embedded WildFly, a managed WildFly server or a remote running WildFly server.

```bash
mvn clean wildfly:run -Pwildfly -DskipTests -Dmaven.test.skip=true
```

By default, if we do not setup a `jboss-as.home` or remote host connection info, it will bootstrap an embedded WildFly and run the application on the embedded server.

Here we configure Maven dependency plugin to download a copy of WildFly, extract the files to the project build directory, and setup a `jboss-as.home` property, the value is the WildFly location. The WildFly plugin will manage the whole WildFly lifecycle - start the WildFly server, deploy applications into the running server, (use `CTRL+C` hotkey) stop the server.

## Testing JPA Components

Here I assume you are familiar with [JUnit](https://www.junit.org) and [Arquillian](https://arquillian.org).

> For the developers those are new to Arqullian framework, please read the official [Arquillian Guides](https://arquillian.org/guides) to start your first step. Note, these tutorials are available in several languages, including Simplified Chinese.

> If you have some basic knowledge of Arquillian, go to my [Jakarta EE 8 starter boilerplate project](https://github.com/hantsy/jakartaee8-starter-boilerplate) and [Jakarta EE 9 starter boilerplate project](https://github.com/hantsy/jakartaee9-starter-boilerplate) to update yourself.

Since Jakarta EE 9, it begins to use the new `jakarta` namespace in all specifications. Arquillian 1.7.0.x starts to support these changes.

### Configuring GlassFish Managed Adapter

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

Then we prepare a copy of the latest GlassFish 7.0 in the `pre-integration-test` phase. The Arquillian tests will be executed in the `integretion-test` phase.

### Creating Arquillian Tests

Let's create a simple Arquillian test to verify the UUID basic type feature in JPA 3.1.

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

The `@Deployment` annotated static method defines the resources that will be packaged into the test archive and deployed into the manged GlassFish server. It is easy to use shrinkwrap to create a fine-grained deployment unit.

You can inject `EntityManager` and `UserTransaction` beans in an Arquillian test like what you do in a simple CDI bean.

In this test class, we setup `@BeforeEach` and `@AfterEach` hooks to start a transaction and end the transaction.

The test method `testPersistingPersons` looks no difference from a plain JUnit test. Firstly we persist a person entity, and commit the transaction to ensure it will be flushed into the database as expected. Then executing a simple JPA query to verify the persisted data.

Execute the following command to run the tests.

```bash
mvn clean verify -Parq-glassfish-managed
```

Similarly, create a test to verify the new numeric functions and datetime functions in Jakarta EE containers.

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

Alternatively, create another test to verify the new functionalities using the Criteria APIs.

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

Get a copy of [sample codes](https://github.com/hantsy/jakartaee10-sandbox/tree/master/jpa) from my github and experience yourself.
