# Better alignment with CDI

In the initial Jakarta REST 3.1 proposal, an exciting feature is using CDI as default injection provider to replace the existing one in Jakarta REST, that means you can use `Inject` to replace Jakarta REST  `Context` to inject Jakarta REST specific resources. Unfortunately this feature is delayed to the next version, and not included in the final 3.1 version.

But Jersey itself provides an extra module to implement this feature.

Let's create a simple TODO Jakarta REST application to expose resources at the `/todos` endpoint.

Firstly create a JPA entity - `Todo`.

```java
@Entity
@Table(name = "todos")
public class Todo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    String title;
    boolean completed = false;

    // setters and getters, hashCode and equals
}
```

Create a simple EJB `@Stateless` bean to create a Todo and retrieve todos.

```java
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
```

Create a EJB `@Singleton` bean to initialize some sample data.

```java
@Singleton
@Startup
public class TodoSamples {
    private static final Logger LOGGER = Logger.getLogger(TodoSamples.class.getName());
    @Inject
    TodoService todoService;

    @PostConstruct
    public void init() {
        var todos = Stream.of("What's new in JPA 3.1?", "What's new in Jaxrs 3.1", "Learn new features in Faces 4.0")
                .map(Todo::new)
                .map(it -> todoService.create(it))
                .toList();
        LOGGER.log(Level.INFO, "initialized todo samples: {0}", todos);
    }
}
```

Now create a Jaxrs resource to expose `/todos` endpoint.

```java
@Path("todos")
@RequestScoped
public class TodoResources {

     @Inject
    //@Context
    ResourceContext resourceContext;

    @Inject
    // @Context
    UriInfo uriInfo;

    @Inject
    TodoService todoService;

    @GET
    public Response getAllTodos() {
        var todos = todoService.findAll();
        return Response.ok(todos).build();
    }

    @POST
    public Response createTodo(Todo todo) throws Exception {
        var saved = todoService.create(todo);
        return Response.created(uriInfo.getBaseUriBuilder().path("todos/{id}").build(saved.getId())).build();
    }

    @GET
    @Path("{id}")
    public TodoResource subResource() {
        return resourceContext.getResource(TodoResource.class);
    }
}


// TodoResource for single resource.
@RequestScoped
public class TodoResource {

    @Inject
    TodoService todoService;

    @PathParam("id")
    UUID id;

    @GET
    public Response getById() {
        var todos = todoService.findById(id);
        return Response.ok(todos).build();
    }

}
```

To make sure it works on GlassFish, copy `jersey-cdi-rs-inject` to the GlassFish *GlassFish_installdir/glassfish/modules* folder.

Simply add the following fragment to the `glassfish` profile, and use `maven-dependency-plugin` to download a copy of `jersey-cdi-rs-inject` to the cargo managed GlassFish instance.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>${maven-dependency-plugin.version}</version>
    <executions>
        <execution>
            <id>copy</id>
            <phase>process-classes</phase>
            <goals>
                <goal>copy</goal>
            </goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>org.glassfish.jersey.ext.cdi</groupId>
                        <artifactId>jersey-cdi-rs-inject</artifactId>
                        <version>${jersey.version}</version>
                        <type>jar</type>
                        <overWrite>false</overWrite>
                    </artifactItem>
                </artifactItems>
                <outputDirectory>${project.build.directory}/cargo/installs/glassfish-${glassfish.version}/glassfish7/glassfish/modules</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Build and run the application, and use `curl` to test our endpoint `/todos`.

```bash
> curl http://localhost:8080/rest-examples/api/todos
[
    {
        "Completed": false,
        "Id": "7db8cb74-6ec6-4b3b-8930-e6a29a9c363a",
        "Title": "Learn new features in Faces 4.0"
    },
    {
        "Completed": false,
        "Id": "0dba2afd-943f-42a2-b1bd-2cd9fea3a140",
        "Title": "What's new in JPA 3.1?"
    },
    {
        "Completed": false,
        "Id": "ff7ac837-fe68-4d47-b79d-f11fd87fd43a",
        "Title": "What's new in Jaxrs 3.1"
    }
]
```

Create a simple Arquillian test to verify the functionality.

```java
@ExtendWith(ArquillianExtension.class)
public class TodoResourceTest {

    private final static Logger LOGGER = Logger.getLogger(TodoResourceTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .asFile();
        var war = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(extraJars)
                .addClasses(
                        TodoResource.class,
                        TodoResources.class,
                        TodoService.class,
                        Todo.class,
                        TodoSamples.class,
                        RestConfig.class
                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "war deployment: {0}", new Object[]{war.toString(true)});
        return war;
    }

    @ArquillianResource
    private URL baseUrl;

    Client client;

    @BeforeEach
    public void before() throws Exception {
        LOGGER.log(Level.INFO, "baseURL: {0}", new Object[]{baseUrl.toExternalForm()});
        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void after() throws Exception {
        client.close();
    }

    @Test
    @RunAsClient
    public void testGetTodos() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/todos"));
        Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        LOGGER.log(Level.INFO, "Get /todos response status: {0}", r.getStatus());
        assertEquals(200, r.getStatus());
        String jsonString = r.readEntity(String.class);
        LOGGER.log(Level.INFO, "Get /todos result string: {0}", jsonString);
    }
}
```

To make the test be run successfully on GlassFish, similarly copy `jersey-cdi-rs-inject` to the target GlassFish server.

In the `arq-glassfish-managed` Maven profile, find the `dependency-maven-plugin` config, add the following content at the end of `configuration` section.

```xml
<execution>
    <id>copy</id>
    <phase>pre-integration-test</phase>
    <goals>
        <goal>copy</goal>
    </goals>
    <configuration>
        <artifactItems>
            <artifactItem>
                <groupId>org.glassfish.jersey.ext.cdi</groupId>
                <artifactId>jersey-cdi-rs-inject</artifactId>
                <version>${jersey.version}</version>
                <type>jar</type>
                <overWrite>false</overWrite>
            </artifactItem>
        </artifactItems>
        <outputDirectory>${project.build.directory}/glassfish7/glassfish/modules</outputDirectory>
    </configuration>
</execution>
```

Then run `TodoResourceTest` test.

```bash
> mvn clean verify -Parq-glassfish-managed -D"it.test=TodoResourceTest"
...
[INFO] --- maven-failsafe-plugin:3.0.0-M7:integration-test (integration-test) @ rest-examples ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.it.TodoResourceTest
Starting database using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, start-database, -t]
Starting database in the background.
Log redirected to D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\databases\derby.log.
Starting container using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, start-domain, -t]
Attempting to start domain1.... Please look at the server log for more details.....
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Dec 04, 2022 5:09:02 PM com.example.it.TodoResourceTest createDeployment
INFO: war deployment: 4a77aebc-2b83-44e6-9ab0-f93e656e1b2c.war:
/WEB-INF/
/WEB-INF/lib/
/WEB-INF/lib/assertj-core-3.23.1.jar
/WEB-INF/lib/byte-buddy-1.12.10.jar
/WEB-INF/classes/
/WEB-INF/classes/com/
/WEB-INF/classes/com/example/
/WEB-INF/classes/com/example/TodoResource.class
/WEB-INF/classes/com/example/TodoResources.class
/WEB-INF/classes/com/example/TodoService.class
/WEB-INF/classes/com/example/Todo.class
/WEB-INF/classes/com/example/TodoSamples.class
/WEB-INF/classes/com/example/RestConfig.class
/WEB-INF/classes/META-INF/
/WEB-INF/classes/META-INF/persistence.xml
/WEB-INF/beans.xml
Dec 04, 2022 5:09:15 PM com.example.it.TodoResourceTest before
INFO: baseURL: http://localhost:8080/4a77aebc-2b83-44e6-9ab0-f93e656e1b2c/
Dec 04, 2022 5:09:16 PM com.example.it.TodoResourceTest testGetTodos
INFO: Get /todos response status: 200
Dec 04, 2022 5:09:16 PM com.example.it.TodoResourceTest testGetTodos
INFO: Get /todos result string: [{"completed":false,"id":"6686c811-71cb-40aa-a38a-24d775c679ba","title":"Learn new features in Faces 4.0"},{"completed":false,"id":"8efb7123-0c43-46aa-aabc-0777494be620","title":"What's new in JPA 3.1?"},{"completed":false,"id":"0478a20e-b8c1-4577-91e4-cc0362ab14d5","title":"What's new in Jaxrs 3.1"}]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 78.438 s - in com.example.it.TodoResourceTest
Stopping container using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, stop-domain, --kill, -t]
Stopping database using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, stop-database, -t]
Sun Dec 04 17:09:20 CST 2022 : Connection obtained for host: 0.0.0.0, port number 1527.
Sun Dec 04 17:09:20 CST 2022 : Apache Derby Network Server - 10.15.2.0 - (1873585) shutdown
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- maven-failsafe-plugin:3.0.0-M7:verify (integration-test) @ rest-examples ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:45 min
[INFO] Finished at: 2022-12-04T17:09:21+08:00
[INFO] ------------------------------------------------------------------------
```

Get the [sample codes](https://github.com/hantsy/jakartaee10-sandbox/blob/master/rest/) from my Github account.
